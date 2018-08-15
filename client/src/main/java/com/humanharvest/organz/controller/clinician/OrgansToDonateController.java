package com.humanharvest.organz.controller.clinician;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.humanharvest.organz.Client;
import com.humanharvest.organz.DonatedOrgan;
import com.humanharvest.organz.controller.MainController;
import com.humanharvest.organz.controller.SubController;
import com.humanharvest.organz.controller.components.DurationUntilExpiryCell;
import com.humanharvest.organz.state.ClientManager;
import com.humanharvest.organz.state.Session;
import com.humanharvest.organz.state.Session.UserType;
import com.humanharvest.organz.state.State;
import com.humanharvest.organz.utilities.enums.Organ;
import com.humanharvest.organz.utilities.exceptions.IfMatchFailedException;
import com.humanharvest.organz.utilities.exceptions.NotFoundException;
import com.humanharvest.organz.utilities.exceptions.ServerRestException;
import com.humanharvest.organz.utilities.view.Page;
import com.humanharvest.organz.utilities.view.PageNavigator;
import com.humanharvest.organz.utilities.view.WindowContext.WindowContextBuilder;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import com.humanharvest.organz.Client;
import com.humanharvest.organz.DonatedOrgan;
import com.humanharvest.organz.controller.MainController;
import com.humanharvest.organz.controller.SidebarController;
import com.humanharvest.organz.controller.SubController;
import com.humanharvest.organz.controller.components.DurationUntilExpiryCell;
import com.humanharvest.organz.state.ClientManager;
import com.humanharvest.organz.state.Session;
import com.humanharvest.organz.state.Session.UserType;
import com.humanharvest.organz.state.State;
import com.humanharvest.organz.utilities.enums.Organ;
import com.humanharvest.organz.utilities.exceptions.IfMatchFailedException;
import com.humanharvest.organz.utilities.exceptions.NotFoundException;
import com.humanharvest.organz.utilities.exceptions.ServerRestException;
import com.humanharvest.organz.utilities.view.Page;
import com.humanharvest.organz.utilities.view.PageNavigator;
import com.humanharvest.organz.utilities.view.WindowContext.WindowContextBuilder;
import org.controlsfx.control.Notifications;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;

public class OrgansToDonateController extends SubController {

    private static final int ROWS_PER_PAGE = 30;
    private static final Logger LOGGER = Logger.getLogger(SidebarController.class.getName());
    private static final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("d MMM yyyy hh:mm a");

    @FXML
    private HBox menuBarPane;

    @FXML
    private TableView<DonatedOrgan> tableView;

    @FXML
    private TableColumn<DonatedOrgan, String> clientCol;

    @FXML
    private TableColumn<DonatedOrgan, Organ> organCol;

    @FXML
    private TableColumn<DonatedOrgan, LocalDateTime> timeOfDeathCol;

    @FXML
    private TableColumn<DonatedOrgan, Duration> timeUntilExpiryCol;

    @FXML
    private ListView<Client> potentialRecipients;

    @FXML
    private Pagination pagination;

    @FXML
    private Text displayingXToYOfZText;

    private Session session;
    private ClientManager manager;
    private ObservableList<DonatedOrgan> observableOrgansToDonate = FXCollections.observableArrayList();
    private FilteredList<DonatedOrgan> filteredOrgansToDonate = new FilteredList<>(observableOrgansToDonate);
    private SortedList<DonatedOrgan> sortedOrgansToDonate = new SortedList<>(filteredOrgansToDonate);
    private DonatedOrgan selectedOrgan;

    private Label placeholder;

    /**
     * Gets the client manager from the global state.
     */
    public OrgansToDonateController() {
        session = State.getSession();
        manager = State.getClientManager();
    }

    // ---------------- Setup methods ----------------

    /**
     * Sets up the page, setting its title, loading the menu bar and doing the first refresh of the data.
     * @param mainController The main controller that defines which window this subcontroller belongs to.
     */
    @Override
    public void setup(MainController mainController) {
        super.setup(mainController);
        mainController.setTitle("Organs to donate");
        mainController.loadMenuBar(menuBarPane);
        refresh();
    }

    /**
     * Initializes the page and the table/pagination properties.
     */
    @FXML
    private void initialize() {
        setupOrgansTable();
        placeholder = new Label("Select an available organ to show potential recipients");
        placeholder.setWrapText(true);
        placeholder.setPadding(new Insets(0, 0, 0, 8));
        placeholder.setTextFill(Color.GREY);
        potentialRecipients.setPlaceholder(placeholder);

        //On pagination update call createPage
        // todo include pagination?
        //pagination.setPageFactory(this::createPage);
    }

    /**
     * Sets up the table columns with their respective value factories and representation factories. Also registers a
     * mouse event handler for double-clicking on a record in the table to open up the appropriate client profile.
     */
    private void setupOrgansTable() {
        clientCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDonor().getFullName()));
        organCol.setCellValueFactory(new PropertyValueFactory<>("organType"));
        timeOfDeathCol.setCellValueFactory(new PropertyValueFactory<>("dateTimeOfDonation"));
        timeUntilExpiryCol.setCellValueFactory(new PropertyValueFactory<>("durationUntilExpiry"));

        // Format all the datetime cells
        timeOfDeathCol.setCellFactory(cell -> formatDateTimeCell());
        timeUntilExpiryCol.setCellFactory(DurationUntilExpiryCell::new);

        potentialRecipients.setCellFactory(listView -> new ListCell<Client>() {
            @Override
            public void updateItem(Client item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(item.getFullName());
                }
            }
        });

        // Open the client profile when double-clicked
        potentialRecipients.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2) {
                Client client = potentialRecipients.getSelectionModel().getSelectedItem();
                if (client != null) {
                    MainController newMain = PageNavigator.openNewWindow();
                    if (newMain != null) {
                        newMain.setWindowContext(new WindowContextBuilder()
                                .setAsClinicianViewClientWindow()
                                .viewClient(client)
                                .build());
                        PageNavigator.loadPage(Page.VIEW_CLIENT, newMain);
                    }
                }
            }
        });

        // Register the mouse event for double-clicking on a record to open the client profile.
        tableView.setOnMouseClicked(mouseEvent -> {

            // Showing potential matches for the donated organ
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 1) {
                DonatedOrgan donatedOrgan = tableView.getSelectionModel().getSelectedItem();
                if (donatedOrgan != null) {
                    potentialRecipients.getItems().clear();
                    displayMatches(donatedOrgan);
                }
            }

            // Double clicking brings up the profile of the client who has donated the organ
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2) {
                DonatedOrgan organToDonate = tableView.getSelectionModel().getSelectedItem();
                if (organToDonate != null) {
                    Client client = organToDonate.getDonor();
                    MainController newMain = PageNavigator.openNewWindow();
                    if (newMain != null) {
                        newMain.setWindowContext(new WindowContextBuilder()
                                .setAsClinicianViewClientWindow()
                                .viewClient(client)
                                .build());
                        PageNavigator.loadPage(Page.VIEW_CLIENT, newMain);
                    }
                }

            // Right click to allow manual override of organ expiry
            } else if (mouseEvent.getButton().equals(MouseButton.SECONDARY)) {
                MenuItem manualExpireItem = new MenuItem();
                manualExpireItem.textProperty().setValue("Manually Override");
                selectedOrgan = tableView.getSelectionModel().getSelectedItem();
                manualExpireItem.setOnAction(event -> openManuallyExpireDialog());
                ContextMenu contextMenu = new ContextMenu(manualExpireItem);
                tableView.setContextMenu(contextMenu);
            }
        });

        // Attach timer to update table each second (for time until expiration)
        final Timeline clock = new Timeline(new KeyFrame(
                javafx.util.Duration.millis(1000),
                event -> {
                    tableView.refresh();
                    observableOrgansToDonate.removeIf(donatedOrgan ->
                            donatedOrgan.getOverrideReason() != null ||
                            donatedOrgan.getDurationUntilExpiry() != null &&
                            donatedOrgan.getDurationUntilExpiry().minusSeconds(1).isNegative());
                }));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        // Sets the comparator for sorting by organ column.
        organCol.setComparator(new Comparator<Organ>() {
            /**
             * Alphabetical order of the organ name.
             */
            @Override
            public int compare(Organ o1, Organ o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });

        // Sets the comparator for sorting by duration column.
        timeUntilExpiryCol.setComparator((o1, o2) -> {
            if (o1 == o2) {
                return 0;
            } else if (o1 == null) {
                return 1; // o1 is "biggest"
            } else if (o2 == null) {
                return -1; //o2 is "biggest"
            } else {
                return o1.compareTo(o2);
            }
        });

        filteredOrgansToDonate.setPredicate(donatedOrgan -> donatedOrgan.getOverrideReason() == null &&
                (donatedOrgan.getDurationUntilExpiry() == null || !donatedOrgan.getDurationUntilExpiry().isZero()));
        sortedOrgansToDonate.comparatorProperty().bind(tableView.comparatorProperty());
    }

    /**
     * Upon pagination, update the table to show the correct items
     * @param pageIndex The page we're now on (starts at 0)
     * @return An empty pane as pagination requires a non null return. Not used.
     */
    private Node createPage(int pageIndex) {
        updateOrgansToDonateList();
        return new Pane();
    }

    private void updateOrgansToDonateList() {
        Collection<DonatedOrgan> newOrgansToDonate = manager.getAllOrgansToDonate();

        observableOrgansToDonate.setAll(newOrgansToDonate);
        tableView.getSortOrder().setAll(timeUntilExpiryCol);

        /* TODO decide whether we need to paginate or not
        int newPageCount = Math.max(1, (newOrgansToDonate.getTotalResults() + ROWS_PER_PAGE - 1) / ROWS_PER_PAGE);
        if (pagination.getPageCount() != newPageCount) {
            pagination.setPageCount(newPageCount);
        }

        setupDisplayingXToYOfZText(newOrgansToDonate.getTotalResults());
        */
    }

    private void displayMatches(DonatedOrgan selectedOrgan) {
        try {
            List<Client> matches = State.getClientManager().getOrganMatches(selectedOrgan);
            potentialRecipients.setItems(FXCollections.observableArrayList(matches));

            if (matches.size() == 0) {
                placeholder.setText("No potential recipients for this organ");
            }

        }  catch (NotFoundException e) {
            LOGGER.log(Level.WARNING, "Organ not found");
            Notifications.create()
                    .title("Organ not found")
                    .text("The organ could not be found on the server, it may have been deleted")
                    .showWarning();
        } catch (ServerRestException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            Notifications.create()
                    .title("Server error")
                    .text("Could not access the server, please try again later")
                    .showError();
        }
    }

    /**
     * Refreshes the data in the transplants waiting list table. Should be called whenever any page calls a global
     * refresh.
     */
    @Override
    public void refresh() {
        observableOrgansToDonate.setAll(manager.getAllOrgansToDonate());
        tableView.setItems(sortedOrgansToDonate);
    }

    private void openManuallyExpireDialog() {
        // Create a popup with a text field to enter the reason
        TextInputDialog popup = new TextInputDialog();
        popup.setTitle("Manually Override Organ");
        popup.setHeaderText("Enter the reason for overriding this organ:");
        popup.setContentText("Reason:");
        popup.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
        popup.getEditor().textProperty().addListener((observable, oldValue, newValue) ->
                popup.getDialogPane().lookupButton(ButtonType.OK).setDisable(newValue.isEmpty()));

        // If user clicks the OK button
        String response = popup.showAndWait().orElse("");
        if (!response.isEmpty()) {
            try {
                StringBuilder overrideReason = new StringBuilder(response);
                overrideReason.append("\n").append(LocalDateTime.now().format(dateTimeFormat));
                if (session.getLoggedInUserType() == UserType.CLINICIAN) {
                    overrideReason.append(String.format("\nOverriden by clinician %d (%s)",
                            session.getLoggedInClinician().getStaffId(), session.getLoggedInClinician().getFullName()));
                } else if (session.getLoggedInUserType() == UserType.ADMINISTRATOR) {
                    overrideReason.append(String.format("\nOverriden by admin '%s'.",
                            session.getLoggedInAdministrator().getUsername()));
                }
                State.getClientResolver().manuallyOverrideOrgan(selectedOrgan, overrideReason.toString());
                PageNavigator.refreshAllWindows();
            } catch (IfMatchFailedException exc) {
                // TODO deal with outdated error
            } catch (NotFoundException exc) {
                Notifications.create()
                        .title("Client/Organ Not Found")
                        .text("The client/donated organ could not be found on the server; it may have been deleted.")
                        .showWarning();
            } catch (ServerRestException exc) {
                Notifications.create()
                        .title("Server Error")
                        .text("A server error occurred when overriding this donated organ; please try again later.")
                        .showError();
            }
        }
    }

    // ---------------- Format methods ----------------
    /**
     * Formats a table cell that holds a {@link LocalDateTime} value to display that value in the date time format.
     * @return The cell with the date time formatter set.
     */
    private static TableCell<DonatedOrgan, LocalDateTime> formatDateTimeCell() {
        return new TableCell<DonatedOrgan, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(item.format(dateTimeFormat));
                }
            }
        };
    }

    /* TODO this is for pagination
     * Set the text that advises the currently viewed and pending amount of results
     * @param totalCount The total amount of current results matching filter options
     *
    private void setupDisplayingXToYOfZText(int totalCount) {
        int fromIndex = pagination.getCurrentPageIndex() * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, totalCount);
        if (totalCount < 2 || fromIndex + 1 == toIndex) {
            // 0 or 1 items OR the last item, on its own page
            displayingXToYOfZText.setText(String.format("Displaying %d of %d",
                    totalCount,
                    totalCount));
        } else {
            displayingXToYOfZText.setText(String.format("Displaying %d-%d of %d",
                    fromIndex + 1, toIndex,
                    totalCount));
        }
    }*/
}
