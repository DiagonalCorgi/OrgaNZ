package com.humanharvest.organz.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;

import com.humanharvest.organz.HistoryItem;
import com.humanharvest.organz.controller.components.FormattedLocalDateTimeCell;
import com.humanharvest.organz.state.Session;
import com.humanharvest.organz.state.Session.UserType;
import com.humanharvest.organz.state.State;

/**
 * Controller for the history page.
 */
public class HistoryController extends SubController {

    private static final Logger LOGGER = Logger.getLogger(HistoryController.class.getName());
    private static final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss");

    private final ObservableList<HistoryItem> historyItems = FXCollections.observableArrayList();
    private final SortedList<HistoryItem> sortedHistoryItems = new SortedList<>(historyItems);

    private Session session;

    @FXML
    private TableColumn<HistoryItem, LocalDateTime> timestampCol;
    @FXML
    private TableColumn<HistoryItem, String> typeCol, detailsCol;
    @FXML
    private TableView<HistoryItem> historyTable;
    @FXML
    private Pane menuBarPane;

    public HistoryController() {
        this.session = State.getSession();
    }

    /**
     * Initializes the UI for this page.
     * - Loads the sidebar.
     * - Sets up cell factories to generate the values for the history table.
     * - Loads history data from file and populates the table with it.
     */
    @FXML
    private void initialize() {
        timestampCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        timestampCol.setCellFactory(cell -> new FormattedLocalDateTimeCell<>(dateTimeFormat));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        detailsCol.setCellValueFactory(new PropertyValueFactory<>("details"));
    }

    @Override
    public void setup(MainController mainController) {
        super.setup(mainController);

        if (session.getLoggedInUserType() == UserType.CLIENT || windowContext.isClinViewClientWindow()) {
            mainController.setTitle("Client History");
        } else {
            mainController.setTitle("System History");
        }

        sortedHistoryItems.setComparator(Comparator.comparing(HistoryItem::getTimestamp).reversed());
        historyTable.setItems(sortedHistoryItems);

        mainController.loadNavigation(menuBarPane);
        refresh();
    }

    /**
     * Updates the history items to the latest information
     */
    @Override
    public void refresh() {
        if (session.getLoggedInUserType() == UserType.CLIENT) {
            historyItems.setAll(State.getClientResolver().getHistory(session.getLoggedInClient()));
        } else if (windowContext.isClinViewClientWindow()) {
            historyItems.setAll(State.getClientResolver().getHistory(windowContext.getViewClient()));
        } else if (session.getLoggedInUserType() == UserType.CLINICIAN) {
            historyItems.setAll(State.getClinicianResolver().getHistory(session.getLoggedInClinician()));
        } else {

            historyItems.setAll(State.getAdministratorResolver().getHistory());
        }
    }
}
