package com.humanharvest.organz.controller.spiderweb;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.LinearGradient;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Screen;

import com.humanharvest.organz.Client;
import com.humanharvest.organz.DonatedOrgan;
import com.humanharvest.organz.controller.MainController;
import com.humanharvest.organz.controller.SubController;
import com.humanharvest.organz.controller.components.ExpiryBarUtils;
import com.humanharvest.organz.controller.components.PotentialRecipientCell;
import com.humanharvest.organz.state.State;
import com.humanharvest.organz.touch.FocusArea;
import com.humanharvest.organz.touch.MultitouchHandler;
import com.humanharvest.organz.utilities.view.Page;
import com.humanharvest.organz.utilities.view.PageNavigator;

/**
 * The Spider web controller which handles everything to do with displaying panes in the spider web stage.
 */
public class SpiderWebController extends SubController {

    private static final Logger LOGGER = Logger.getLogger(SpiderWebController.class.getName());

    private final Client client;

    private final Pane canvas;
    private Pane deceasedDonorPane;
    private final List<Pane> organNodes = new ArrayList<>();

    public SpiderWebController(Client client) {
        this.client = client;

        canvas = MultitouchHandler.getCanvas();
        MultitouchHandler.setPhysicsHandler(new SpiderPhysicsHandler(MultitouchHandler.getRootPane()));
        for (MainController mainController : State.getMainControllers()) {
            mainController.closeWindow();
        }
        displayDonatingClient();
        displayOrgans();
    }

    /**
     * Sets a node's position using an {@link Affine} transform. The node must have an {@link FocusArea} for its
     * {@link Node#getUserData()}.
     *
     * @param node The node to apply the transform to. Must have a focusArea
     * @param x The x translation
     * @param y The y translation
     * @param angle The angle to rotate (degrees)
     */
    private static void setPositionUsingTransform(Node node, double x, double y, double angle) {
        FocusArea focusArea = (FocusArea) node.getUserData();

        Affine transform = new Affine();
        transform.prepend(new Translate(x, y));
        transform.prepend(new Rotate(angle, x, y));
        focusArea.setTransform(transform);
    }

    /**
     * Create a new stage to display all of the pane in.
     */
//    private void setupNewStage() {
//        Stage stage = new Stage();
//        stage.setTitle("Organ Spider Web");
//        canvas = new TuioFXCanvas();
//        Scene scene = new Scene(canvas);
//
//        FXMLLoader loader = new FXMLLoader();
//
//        try {
//            Pane backPane = loader.load(PageNavigatorTouch.class.getResourceAsStream(Page.BACKDROP.getPath()));
//
//            canvas.getChildren().add(backPane);
//            MultitouchHandler.initialise(canvas);
//
//        } catch (IOException e) {
//            LOGGER.log(Level.SEVERE, "Exception when setting up stage", e);
//        }
//
//        stage.setScene(scene);
//        stage.setFullScreen(true);
//        stage.setOnCloseRequest(event -> {
//            MultitouchHandler.stageClosing();
//        });
//        stage.show();
//    }

    /**
     * Loads a window for each non expired organ.
     */
    private void displayOrgans() {
        Collection<DonatedOrgan> donatedOrgans = State.getClientResolver().getDonatedOrgans(client);
        for (DonatedOrgan organ : donatedOrgans) {
            if (!organ.hasExpired()) {
                State.setOrganToDisplay(organ);
                MainController newMain = PageNavigator.openNewWindow(80, 80);
                PageNavigator.loadPage(Page.ORGAN_IMAGE, newMain);
                Pane organPane = newMain.getPane();
                organNodes.add(organPane);
                //TODO: Fix so organ stays expired once web is closed.
                organPane.setOnMouseClicked(click -> {
                    if (click.getClickCount() == 3) {

                        State.getClientResolver()
                                .manuallyOverrideOrgan(organ, "Manually Overridden by Doctor using WebView");
                        organ.manuallyOverride("Manually Overridden by Doctor using WebView");

                    }
                });
                // Create the line
                Line connector = new Line();
                connector.setStrokeWidth(4);
                Text durationText = new Text(ExpiryBarUtils.getDurationString(organ));

                // Redraws lines when organs or donor pane is moved
                deceasedDonorPane.localToParentTransformProperty().addListener((observable, oldValue, newValue) -> {
                    Bounds bounds = deceasedDonorPane.getBoundsInParent();
                    connector.setStartX(bounds.getMinX() + bounds.getWidth() / 2);
                    connector.setStartY(bounds.getMinY() + bounds.getHeight() / 2);
                    updateConnector(organ, connector, durationText);
                });
                organPane.localToParentTransformProperty().addListener((observable, oldValue, newValue) -> {
                    Bounds bounds = organPane.getBoundsInParent();
                    connector.setEndX(bounds.getMinX() + bounds.getWidth() / 2);
                    connector.setEndY(bounds.getMinY() + bounds.getHeight() / 2);
                    updateConnector(organ, connector, durationText);
                });

                // Setup the ListView
                ObservableList<Client> potentialMatches = FXCollections.observableArrayList(
                        State.getClientManager().getOrganMatches(organ));
                final ListView<Client> matchesList = new ListView<>();
                matchesList.setCellFactory(param -> new PotentialRecipientCell());
                matchesList.setItems(potentialMatches);
                matchesList.setOrientation(Orientation.VERTICAL);



                canvas.getChildren().add(0, connector);
                canvas.getChildren().add(0, durationText);
                canvas.getChildren().add(matchesList);

                Bounds bounds = deceasedDonorPane.getBoundsInParent();
                connector.setStartX(bounds.getMinX() + bounds.getWidth() / 2);
                connector.setStartY(bounds.getMinY() + bounds.getHeight() / 2);
                updateConnector(organ, connector, durationText);

                // Attach timer to update connector each second (for time until expiration)
                final Timeline clock = new Timeline(new KeyFrame(
                        javafx.util.Duration.seconds(1),
                        event -> updateConnector(organ, connector, durationText)));
                clock.setCycleCount(Animation.INDEFINITE);
                clock.play();

            }
        }
        layoutOrganNodes(300);
    }

    private void updateConnector(DonatedOrgan donatedOrgan, Line line, Text durationText) {
        Duration duration = donatedOrgan.getDurationUntilExpiry();
        if (ExpiryBarUtils.isDurationZero(duration)) {
            line.setStroke(ExpiryBarUtils.greyColour);
        } else {
            // Progress as a decimal. starts at 0 (at time of death) and goes to 1.
            double progressDecimal = donatedOrgan.getProgressDecimal();
            double fullMarker = donatedOrgan.getFullMarker();

            LinearGradient linearGradient = ExpiryBarUtils.getLinearGradient(progressDecimal, fullMarker,
                    line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY());

            line.setStroke(linearGradient);
        }

        durationText.setText(ExpiryBarUtils.getDurationString(donatedOrgan));

        double xWidth = line.getStartX() - line.getEndX();
        double yWidth = line.getStartY() - line.getEndY();
        double x = line.getEndX() + xWidth * .1;
        double y = line.getEndY() + yWidth * .1;
        double angle = (getAngle(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY()));
        durationText.getTransforms().removeIf(transform -> transform instanceof Affine);
        Affine trans = new Affine();
        trans.prepend(new Translate(0, -5));
        trans.prepend(new Rotate(angle));
        durationText.getTransforms().add(trans);
        durationText.setTranslateX(x);
        durationText.setTranslateY(y);
//        durationText.setRotate(angle);
    }

    private double getAngle(double x1, double y1, double x2, double y2) {
        double angle = Math.toDegrees(Math.atan2(y1 - y2, x1 - x2));
        if (angle < 0) {
            angle += 360;
        }
        return angle;
    }

    private void addOrganNode(DonatedOrgan organ) {

    }

    private void displayDonatingClient() {
        MainController newMain = PageNavigator.openNewWindow(200, 400);
        PageNavigator.loadPage(Page.RECEIVER_OVERVIEW, newMain);
        deceasedDonorPane = newMain.getPane();
        ((FocusArea) deceasedDonorPane.getUserData()).setTranslatable(false);

        int centerX = (int) Screen.getPrimary().getVisualBounds().getWidth() / 2;
        int centerY = (int) Screen.getPrimary().getVisualBounds().getHeight() / 2;
        setPositionUsingTransform(deceasedDonorPane, centerX, centerY, 0);
    }

    private void layoutOrganNodes(double radius) {
        final int numNodes = organNodes.size();
        final double angleSize = (Math.PI * 2) / numNodes;

        for (int i = 0; i < numNodes; i++) {
            setPositionUsingTransform(organNodes.get(i),
                    deceasedDonorPane.getLocalToParentTransform().getTx() + radius * Math.sin(angleSize * i),
                    deceasedDonorPane.getLocalToParentTransform().getTy() + radius * Math.cos(angleSize * i),
                    360 - Math.toDegrees(angleSize * i));
        }
    }
}
