/*
SX4Draw
Copyright (C) 2019 Michael Blank

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.blankedv.sx4draw;

import de.blankedv.sx4draw.PanelElement.PEState;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import javafx.util.Pair;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.prefs.Preferences;

import static de.blankedv.sx4draw.Constants.*;
import static de.blankedv.sx4draw.PanelElement.SENSORWIDTH;
import static de.blankedv.sx4draw.PanelElement.TRACKWIDTH;
import static de.blankedv.sx4draw.ReadConfig.YOFF;

public class SX4Draw extends Application {

    public static String version = "0.37 - 06 Feb 2019";

    // FIXED: weichengleichheit nicht auf den Pixel genau
    // FIXED: UNDO funktioniert nicht nach Zeichnen eines Tracks
    // TODO UNDO für ca. mehrere Elemente
    // FIXED: Signale/ Weichen etc mit virtuellen Adressen (>1200)
    // FIXED: warnung bei doppelter Adresse (mit Anzeige)
    // FIXED: suchfunktion - Adresse eingeben -> elemente anzeigen
    // FIXED: Signale auch 45Grad, 135Grad, ....
    // FIXED: für weichen INV funktion einbauen (in Fahrstrassenanzeige)
    // TODO für "SX4" -> Mixbetrieb Fahrstrassen und Manueller Betrieb mit manuellem Fahrstrassenlöschen
    // FIXED ScrollPane einführen für Drawing bereich

    public static ArrayList<PanelElement> panelElements = new ArrayList<>();
    public static PanelElement lastPE = null;
    public static String panelName = "";

    private Group lineGroup;
    private Group draggedGroup = new Group();
    private Group raster;

    private Canvas canvas; // The canvas on which the image is drawn.
    private GraphicsContext gc;  // The graphics context for the canvas.
    private VBox vb;

    private RoutesTable routingTable;
    private Route currentRoute = null;

    public static IntPoint start;

    private Line line;
    private static final Button btnUndo = new Button();
    private static final ToggleButton btnAddTrack = new ToggleButton();
    private static final ToggleButton btnAddSensor = new ToggleButton();
    private static final ToggleButton btnAddSensorUS = new ToggleButton();
    private static final ToggleButton btnAddSignal = new ToggleButton();
    private static final ToggleButton btnRouteBtn = new ToggleButton();
    private static final ToggleButton btnAddRoute = new ToggleButton();
    private static final ToggleButton btnSelect = new ToggleButton();
    private static final Button btnUnSelect = new Button();
    private static final ToggleButton btnMove = new ToggleButton();
    private static final Button btnDelete = new Button();
    private static final ToggleGroup toggleGroup = new ToggleGroup();

    final CheckMenuItem dispAddresses = new CheckMenuItem("Adressen anzeigen");
    final CheckMenuItem rasterOn = new CheckMenuItem("Raster");
    final CheckMenuItem showMousePos = new CheckMenuItem("Mauspos. anzeigen");
    final Tooltip mousePositionToolTip = new Tooltip("");
    final AnchorPane anchorPane = new AnchorPane();
    final Label status = new Label("status");

    private final Group root = new Group();

    public enum GUIState {
        ADD_TRACK, ADD_SENSOR, ADD_SENSOR_US, ADD_SIGNAL, ADD_ROUTEBTN, ADD_ROUTE, SELECT, MOVE
    }

    public enum PEType {
        TRACK, SENSOR, SIGNAL, TURNOUT, ROUTEBUTTON;
    }

    //public static ArrayList<Route> routes = new ArrayList<>();
    public static final ObservableList<Route> routes = FXCollections.observableArrayList();
    public static final ObservableList<CompRoute> compRoutes = FXCollections.observableArrayList();
    public static final ObservableList<Trip> trips = FXCollections.observableArrayList();
    public static final ObservableList<Timetable> timetables = FXCollections.observableArrayList();

    public static enum RT {
        ROUTE, COMPROUTE, TRIP, TIMETABLE
    }

    private GUIState currentGUIState = GUIState.SELECT;
    private final IntPoint moveStart = new IntPoint();
    private String currentFileName = "";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Panel XML File Creator (Name: ???)");
        primaryStage.getIcons().add(new Image("sx4_draw_ico64.png"));
        //primaryStage.getIcons().add(new Image("file:sx4_ico256.png"));
        //primaryStage.getIcons().add(new Image("file:sx4_iconx.png"));

        final Preferences prefs = Preferences.userNodeForPackage(this.getClass());


        // Build the VBox container for the lineBox, canvas, and toolBox

        vb = new VBox(3);
        Scene scene = new Scene(vb, 1300, 660);
        canvas = new Canvas(RECT_X, RECT_Y);
        gc = canvas.getGraphicsContext2D();
        mousePositionToolTip.setOpacity(0.7);
        mousePositionToolTip.setAutoHide(true);

        // A group to hold all the drawn shapes
        lineGroup = new Group();
        lineGroup.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                IntPoint poi = new IntPoint(me.getX(), me.getY());
                System.out.println("linegroup mouse pressed x=" + poi.getX() + " y=" + poi.getY() + " currentGUIState=" + currentGUIState.name() + " src=" + me.getSource());
                if (me.getButton() == MouseButton.PRIMARY) {
                    System.out.println("prim btn");
                    switch (currentGUIState) {
                        case ADD_TRACK:
                        case ADD_SIGNAL:
                        case ADD_SENSOR:
                        case ADD_SENSOR_US:
                        case ADD_ROUTEBTN:
                            // this does not work ....
                            //MyPoint start = IntPoint.toRaster(poi);
                            //line = startNewLine(start);
                            //
                            System.out.println("should start new line ...");
                            break;
                        case SELECT:
                            // if (me.isControlDown()) {
                            //    toggleSelectionPENotTrack(poi);
                            //} else if (me.isShiftDown()) {
                            //    toggleSelectionPENotTrackNotSensor(poi);
                            //} else {
                            toggleSelectionPE(poi);
                            //}
                            break;
                        case MOVE:
                            moveStart.setX((int) me.getX());
                            moveStart.setY((int) me.getY());
                            System.out.println("moveStart");
                            addToDraggedGroup();
                            break;
                    }
                } else if (me.getButton() == MouseButton.SECONDARY) {
                    System.out.println("sec btn");
                    editPanelElement(poi, primaryStage);
                }
            }
        });

        raster = new Group();

        HBox buttons = createButtonBar();

        MenuBar menuBar = new MenuBar();
        createMenu(prefs, menuBar, primaryStage);

        root.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent t) {
                if (t.getCode() == KeyCode.ESCAPE) {
                    System.out.println("click on escape");
                    if (currentGUIState == GUIState.ADD_ROUTE) {
                        // abbruch
                        for (PanelElement sel : panelElements) {
                            sel.createShapeAndSetState(PEState.DEFAULT);
                        }
                        //redrawPanelElements();
                        currentRoute = null; // reset
                    }
                }
            }
        });

        // Build the canvas
        //System.out.println("size rect: x=" + (scene.getWidth()) + " y=" + (scene.getHeight() - 230));
        canvas.setCursor(Cursor.DEFAULT);
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, me -> {
            IntPoint poi = new IntPoint(me.getX(), me.getY());
            IntPoint end;
            start = IntPoint.Companion.toRaster(poi, getRaster());
            System.out.println("canvas mouse pressed x=" + poi.getX() + " y=" + poi.getY() + " currentGUIState=" + currentGUIState.name());
            if (me.getButton() == MouseButton.PRIMARY) {
                System.out.println("primary button");
                switch (currentGUIState) {
                    case ADD_TRACK:
                        line = startNewLine(start, TRACKWIDTH);
                        break;
                    case ADD_SIGNAL:
                        addElement(PEType.SIGNAL, poi, lineGroup);
                        break;
                    case ADD_ROUTEBTN:
                        addElement(PEType.ROUTEBUTTON, poi, lineGroup);
                        break;
                    case ADD_SENSOR_US:
                        addElement(PEType.SENSOR, poi, lineGroup);
                        break;
                    case ADD_ROUTE:
                        createRoute(poi);
                        break;
                    case ADD_SENSOR:
                        line = startNewLine(start, SENSORWIDTH);
                        line.getStrokeDashArray().addAll(15d, 10d);
                        line.setStroke(Color.YELLOW);
                        break;
                    case SELECT:
                        // TODO make selection where ROUTEB has higher prio than SENSOR has higher prio than TRACK
                        // avoiding use of extra keys for selection !!!
                        //if (me.isControlDown()) {
                        //    toggleSelectionPENotTrack(poi);
                        //} else if (me.isShiftDown()) {
                        //    toggleSelectionPENotTrackNotSensor(poi);
                        //} else {
                        toggleSelectionPE(poi);
                        //}
                        break;

                    case MOVE:
                        IntPoint ms = IntPoint.Companion.toRaster(poi, getRaster());
                        moveStart.setX(ms.getX());
                        moveStart.setY(ms.getY());
                        System.out.println("moveStart at " + moveStart.toString());
                        addToDraggedGroup();
                        break;

                }
            } else if (me.getButton() == MouseButton.SECONDARY) {
                System.out.println("secondary button");
                editPanelElement(poi, primaryStage);
            }

        });

        canvas.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                String msg = "(" + (int) event.getX() + ", " + ((int) event.getY() - YOFF) + ")";
                /* + ")\n(sceneX: "
                    + event.getSceneX() + ", sceneY: " + event.getSceneY() + ")\n(screenX: "
                    + event.getScreenX() + ", screenY: " + event.getScreenY() + ")"; */
                if (showMousePos.isSelected()) {
                    mousePositionToolTip.setText(msg);
                    Node node = (Node) event.getSource();
                    mousePositionToolTip.show(node, event.getX() + 30, event.getY() + 20);

                }
            }

        });

        canvas.setOnMouseReleased(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent me) {
                // keep shapes within rectangle
                if (canvas.getBoundsInLocal().contains(me.getX(), me.getY())) {
                    IntPoint poi = new IntPoint(me.getX(), me.getY());
                    System.out.println("end approx x=" + poi.getX() + " y=" + poi.getY());
                    IntPoint end = IntPoint.Companion.correctAngle(start, poi, getRaster());
                    System.out.println("end x=" + end.getX() + " y=" + end.getY());
                    switch (currentGUIState) {
                        case ADD_TRACK:
                            line.setEndX(end.getX());
                            line.setEndY(end.getY());
                            lineGroup.getChildren().remove(line);  // will be re-added from within PE
                            if ((Math.abs(line.getEndX() - line.getStartX()) > 5)
                                    || (Math.abs(line.getEndY() - line.getStartY()) > 5)) {
                                lastPE = new PanelElement(PEType.TRACK, line);
                                panelElements.add(lastPE);
                                lineGroup.getChildren().add(lastPE.getShape());
                                btnUndo.setDisable(false);
                            }
                            break;

                        case ADD_SENSOR:
                            line.setEndX(end.getX());
                            line.setEndY(end.getY());
                            lineGroup.getChildren().remove(line);  // will be re-added from within PE
                            if ((Math.abs(line.getEndX() - line.getStartX()) > 5)
                                    || (Math.abs(line.getEndY() - line.getStartY()) > 5)) {
                                lastPE = new PanelElement(PEType.SENSOR, line);
                                lineGroup.getChildren().add(lastPE.getShape());
                                panelElements.add(lastPE);
                                btnUndo.setDisable(false);
                            }
                            break;
                        case ADD_SENSOR_US:
                        case ADD_SIGNAL:
                        case ADD_ROUTEBTN:
                        case ADD_ROUTE:
                            break;
                        case MOVE:    // MOVE ends
                            IntPoint d = IntPoint.Companion.delta(moveStart, poi, getRasterDiv2());
                            System.out.println("move END: final delta =" + d.getX() + "," + d.getY());
                            PanelElement.Companion.translate(d);
                            draggedGroup.getChildren().clear();
                            resetPEStates();
                            redrawPanelElements();

                            break;
                    }
                }

            }
        });

        canvas.setOnMouseDragged(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent me) {
                if (canvas.getBoundsInLocal().contains(me.getX(), me.getY())) {
                    IntPoint poi = new IntPoint(me.getX(), me.getY());
                    switch (currentGUIState) {
                        case ADD_TRACK:
                        case ADD_SENSOR:
                            lineGroup.getChildren().remove(line);
                            IntPoint mp = IntPoint.Companion.correctAngle(start, poi, getRaster());
                            line.setEndX(mp.getX());
                            line.setEndY(mp.getY());
                            //System.out.println("x=" + (int)me.getX() + " y=" + (int)me.getY());
                            if (!lineGroup.getChildren().contains(line)) {
                                lineGroup.getChildren().add(line);
                            }
                            break;

                        case ADD_SIGNAL:
                            // do nothing
                            break;
                        case MOVE:  // MOVE dragging
                            IntPoint d = IntPoint.Companion.delta(moveStart, poi, getRasterDiv2());
                            //System.out.println("delta =" + d.x + "," + d.y);
                            draggedGroup.setTranslateX(d.getX());
                            draggedGroup.setTranslateY(d.getY());
                    }
                }

                if (showMousePos.isSelected()) {
                    String msg = "(" + (int) me.getX() + ", " + ((int) me.getY() - YOFF) + ")";
                    mousePositionToolTip.setText(msg);
                    Node node = (Node) me.getSource();
                    mousePositionToolTip.show(node, (int) me.getX() + 30, (int) me.getY() + 20);
                }

            }
        });

        //vb.setLayoutX(0);
        //vb.setLayoutY(0);
        //vb.getChildren().addAll(menuBar, buttons);

        /*BorderPane bp = new BorderPane();
        bp.setMaxSize(10000,6000);
        bp.minHeight(100);
        bp.minWidth(400);
        //bp.setPrefSize(2000,1200);
        bp.setPickOnBounds(true); */


        //StackPane stackPane = new StackPane();


        ScrollPane scPane = new ScrollPane();
        vb.getChildren().addAll(menuBar, buttons,scPane);    //, status);
        //scPane.setFitToHeight(true);
        //scPane.setFitToWidth(true);
        scPane.setMaxWidth(RECT_X);
        scPane.setMaxHeight(RECT_Y);
        vb.setMaxWidth(RECT_X);
        vb.setMaxHeight(RECT_Y + 40);
        scPane.setPrefSize(RECT_X,RECT_Y);
        scPane.setFitToWidth(true);
        //BorderPane.setAlignment(scPane, Pos.CENTER);
        VBox.setVgrow(scPane, Priority.ALWAYS);
        HBox.setHgrow(scPane, Priority.ALWAYS);

        //anchorPane.setPrefSize(1100,540);
        //scPane.setContent(stackPane);
        scPane.setContent(anchorPane);
        //bp.setCenter(scPane);
        //bp.setTop(vb);

        /* URSPRUNG Koordinatensystem x=0 y=60 */
        //raster
        for (int i = 0;
             i <= (RECT_X + 100);
             i = i + 20) {
            for (int j = 0; j <= (RECT_Y + 160); j = j + RASTER) {
                Circle cir = new Circle(i, j, 0.5);
                cir.setFill(Color.BLUE);
                if (canvas.getBoundsInParent().contains(i, j)) {
                    raster.getChildren().add(cir);
                }
            }
        }

        anchorPane.getChildren().addAll(lineGroup, raster,canvas,draggedGroup);

        primaryStage.setScene(scene);

        primaryStage.show();

        primaryStage.setOnCloseRequest((WindowEvent e) -> {
            Calc.turnouts();
            String path = prefs.get("directory", System.getProperty("user.home"));
            writeFile(primaryStage, path, false);
            System.exit(0);
            System.exit(0);
        });

        /*    Timeline oneSecond = new Timeline(new KeyFrame(Duration.seconds(1), (ActionEvent event) -> {
            // this is run on UI Thread (in contrast to "the good old java Timer")
            Platform.runLater(() -> {
                for (int i = 0; i < panelElements.size(); i++) {
                    PanelElement pe = panelElements.get(i);
                    if (pe.isMarked()) {
                        pe.shape.setStroke(Color.AQUA);
                    } else {
                        pe.setShapeDefaultColor();
                    }
                }
            });
            //}
        }));
        oneSecond.setCycleCount(Timeline.INDEFINITE);
        oneSecond.play(); */
    }

    private HBox createButtonBar() {
        final ImageView selIcon = new ImageView(new Image("select.png"));
        final ImageView plusIcon = new ImageView(new Image("plus.png"));
        final ImageView plusIcon1 = new ImageView(new Image("plus.png"));
        final ImageView plusIcon2 = new ImageView(new Image("plus.png"));
        final ImageView plusIcon3 = new ImageView(new Image("plus.png"));
        final ImageView plusIcon4 = new ImageView(new Image("plus.png"));
        final ImageView plusIcon5 = new ImageView(new Image("plus.png"));
        final ImageView delIcon = new ImageView(new Image("delete.png"));
        final ImageView moveIcon = new ImageView(new Image("move.png"));
        final ImageView undoIcon = new ImageView(new Image("undo.png"));

        Separator sep1 = new Separator(Orientation.VERTICAL);
        Separator sep2 = new Separator(Orientation.VERTICAL);

        HBox btns = new HBox(3);
        btns.getChildren().addAll(btnSelect, btnUnSelect, sep1, btnAddTrack, btnAddSensor, btnAddSensorUS, btnAddSignal, btnRouteBtn, btnAddRoute, sep2, btnUndo, btnDelete, btnMove);

        btnSelect.setToggleGroup(toggleGroup);
        btnSelect.setSelected(true);
        btnMove.setSelected(false);
        btnSelect.setText("Select");
        btnSelect.setGraphic(selIcon);
        btnSelect.setOnAction((ActionEvent event) -> {
            enterSelectState();
        });

        btnUnSelect.setText("Unselect");
        //btnUnSelect.setGraphic(selIcon);
        btnUnSelect.setOnAction((ActionEvent event) -> {
            System.out.println("unselect");
            unselectAll();
            redrawPanelElements();
        });

        btnMove.setToggleGroup(toggleGroup);
        btnMove.setSelected(false);
        btnMove.setDisable(true);
        btnSelect.setSelected(false);
        btnMove.setText("Move");
        btnMove.setGraphic(moveIcon);
        btnMove.setOnAction((ActionEvent event) -> {
            currentGUIState = GUIState.MOVE;
            canvas.setCursor(Cursor.CLOSED_HAND);
        });

        btnAddTrack.setToggleGroup(toggleGroup);
        btnAddTrack.setText("Track");
        btnAddTrack.setGraphic(plusIcon1);
        btnAddTrack.setOnAction((ActionEvent event) -> {
            currentGUIState = GUIState.ADD_TRACK;
            canvas.setCursor(Cursor.CROSSHAIR);
            //resetLines();
        });

        btnAddSensor.setToggleGroup(toggleGroup);
        btnAddSensor.setText("Sensor");
        btnAddSensor.setGraphic(plusIcon2);
        btnAddSensor.setOnAction((ActionEvent event) -> {
            currentGUIState = GUIState.ADD_SENSOR;
            canvas.setCursor(Cursor.CROSSHAIR);
            //resetLines();
        });

        btnAddSensorUS.setToggleGroup(toggleGroup);
        btnAddSensorUS.setText("Sensor-US");
        btnAddSensorUS.setGraphic(plusIcon4);
        btnAddSensorUS.setOnAction((ActionEvent event) -> {
            currentGUIState = GUIState.ADD_SENSOR_US;
            canvas.setCursor(Cursor.CROSSHAIR);
            //resetLines();
        });

        btnAddSignal.setToggleGroup(toggleGroup);
        btnAddSignal.setText("Signal");
        btnAddSignal.setGraphic(plusIcon);
        btnAddSignal.setOnAction((ActionEvent event) -> {
            currentGUIState = GUIState.ADD_SIGNAL;
            canvas.setCursor(Cursor.CROSSHAIR);
            //resetLines();
        });

        btnRouteBtn.setToggleGroup(toggleGroup);
        btnRouteBtn.setText("RT-Button");
        btnRouteBtn.setGraphic(plusIcon3);
        btnRouteBtn.setOnAction((ActionEvent event) -> {
            currentGUIState = GUIState.ADD_ROUTEBTN;
            canvas.setCursor(Cursor.CROSSHAIR);
            //resetLines();
        });

        btnAddRoute.setToggleGroup(toggleGroup);
        btnAddRoute.setText("Fahrstr.");
        btnAddRoute.setGraphic(plusIcon5);
        btnAddRoute.setOnAction((ActionEvent event) -> {
            if (PanelElement.Companion.addressCheck()) {
                currentGUIState = GUIState.ADD_ROUTE;
                canvas.setCursor(Cursor.CLOSED_HAND);
            } else {

                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("Warnung");
                alert.setHeaderText("Es sind keine oder nur wenige Adressen eingegeben");
                alert.setContentText("Bitte Adressen definieren vor der Fahrstraßeneingabe!");
                alert.showAndWait();
                enterSelectState();
            }
        });

        btnUndo.setText("Undo");
        btnUndo.setDisable(true);
        btnUndo.setGraphic(undoIcon);
        btnUndo.setOnAction((ActionEvent event) -> {
            //resetLines();
            if (lastPE != null) {
                lineGroup.getChildren().remove(lastPE.getShape());
                panelElements.remove(lastPE);
                line = null;
                lastPE = null;
                System.out.println("last line removed");
                btnUndo.setDisable(true);
            }
        });

        btnDelete.setText("Delete");
        btnDelete.setDisable(true);

        btnDelete.setGraphic(delIcon);
        btnDelete.setOnAction((ActionEvent event) -> {
            btnUndo.setDisable(true);
            lastPE = null;
            int toDelete = INVALID_INT;
            for (int i = panelElements.size() - 1; i >= 0; i--) {
                Shape l = panelElements.get(i).getShape();
                if (l.getStroke() == Color.RED) {
                    // is selected
                    toDelete = i;
                    System.out.println("toDelete i=" + i);
                    break;
                }
            }
            if (toDelete != INVALID_INT) {
                PanelElement pe = panelElements.get(toDelete);
                Shape l = pe.getShape();
                l.setStroke(Color.BLACK);
                lineGroup.getChildren().remove(l);
                panelElements.remove(toDelete);
                System.out.println("removed  #" + toDelete);
                checkSelection();
            }

        });

        return btns;
    }

    private void enterSelectState() {
        currentGUIState = GUIState.SELECT;
        btnUndo.setDisable(true);
        btnAddRoute.setSelected(false);
        btnSelect.setSelected(true);
        canvas.setCursor(Cursor.DEFAULT);
        checkSelection();
    }

    private void unselectAll() {
        for (PanelElement pe : panelElements) {
            pe.createShapeAndSetState(PEState.DEFAULT);
        }
    }

    private void createMenu(Preferences prefs, MenuBar menuBar, Stage stage) {
        // final ImageView ivSettings = new ImageView(new Image("/de/blankedv/sx4monitorfx/res/settings.png"));
        final ImageView ivInfo = new ImageView(new Image("info.png"));
        final ImageView ivSX4generic = new ImageView(new Image("sx4_draw_ico32.png"));
        final Menu menu1 = new Menu("File");
        final Menu menuOptions = new Menu("Optionen");
        final MenuItem setName = new MenuItem("Set Panel-Name");

        final MenuItem scale200 = new CheckMenuItem("Scale *2");
        final MenuItem openRoutingTable = new MenuItem("Fahrstr. anzeigen");

        final Menu menuCalc = new Menu("Berechnen");
        final MenuItem cTurnouts = new MenuItem("Weichen berechnen");
        final MenuItem cNormPositions = new MenuItem("Start Position normieren auf (20,20)");
        final Menu menuExtra = new Menu("Extras");
        final MenuItem cSearch = new MenuItem("Suche nach Adressen");
        final Menu menuInfo = new Menu("Hilfe");
        final MenuItem saveItem = new MenuItem("Panel abspeichern");
        final MenuItem openItem = new MenuItem("Panel öffnen");
        final MenuItem exitItem = new MenuItem("Programm-Ende/Exit");
        menu1.getItems().addAll(openItem, saveItem, exitItem);
        menuOptions.getItems().addAll(setName, dispAddresses, rasterOn, showMousePos, openRoutingTable);
        menuCalc.getItems().addAll(cTurnouts, cNormPositions, scale200); //, scale50);
        menuExtra.getItems().addAll(cSearch);
        rasterOn.setSelected(true);
        showMousePos.setSelected(false);

        exitItem.setOnAction((event) -> {
            //PanelElement.scaleAll();
            String path = prefs.get("directory", System.getProperty("user.home"));
            writeFile(stage, path, false);
            System.exit(0);
        });

        saveItem.setOnAction((event) -> {
            //PanelElement.scaleAll();
            redrawPanelElements();
            String path = prefs.get("directory", System.getProperty("user.home"));
            String lastPath = writeFile(stage, path, true);
            if ((lastPath != null) && (!lastPath.isEmpty())) {
                prefs.put("directory", lastPath);
            }
            stage.setTitle("Create Panel Description (Name: " + panelName + ") File: " + currentFileName);
        });

        openItem.setOnAction((event) -> {
            String path = prefs.get("directory", System.getProperty("user.home"));
            String lastPath = openFile(stage, path);
            if ((lastPath != null) && (!lastPath.isEmpty())) {
                prefs.put("directory", lastPath);
            }
            stage.setTitle("Create Panel Description (Name: " + panelName + ") File: " + currentFileName);
        });

        setName.setOnAction((event) -> {
            System.out.println("set panelName");
            TextInputDialog getName = new TextInputDialog(panelName);
            getName.setTitle("Neuer Panel-Name");
            getName.setHeaderText(null);
            getName.setContentText("Panel-Name:");
            Optional<String> result = getName.showAndWait();
            result.ifPresent(name -> {
                panelName = name;
                stage.setTitle("Create Panel Description (Name: " + panelName + ")");
            });

        });

        rasterOn.setOnAction((event) -> {
            System.out.println("raster toggle");
            if (rasterOn.isSelected()) {
                root.getChildren().add(raster);
            } else {
                root.getChildren().remove(raster);
            }
        });

        showMousePos.setOnAction((event) -> {
            System.out.println("mousepos toggle");
            if (showMousePos.isSelected()) {
                //mousePositionToolTip.setText(null);
            } else {
                //mousePositionToolTip.remo
            }
        });

        scale200.setOnAction((event) -> {
            if (((CheckMenuItem) scale200).isSelected()) {
                System.out.println("scale *2");
                PanelElement.Companion.scalePlus();
                redrawPanelElements();
                if (dispAddresses.isSelected()) {
                    drawAddresses(gc);
                } else {
                    gc.clearRect(0, 0, RECT_X, RECT_Y);
                }
            } else  {
                //scale back 50%
                System.out.println("scale * 0.5");
                PanelElement.Companion.scaleMinus();
                redrawPanelElements();
                if (dispAddresses.isSelected()) {
                    drawAddresses(gc);
                } else {
                    gc.clearRect(0, 0, RECT_X, RECT_Y);
                }
            }
        });

        dispAddresses.setOnAction((event) -> {
            System.out.println("display addresses = " + dispAddresses.isSelected());
            if (dispAddresses.isSelected()) {
                drawAddresses(gc);
            } else {
                gc.clearRect(0, 0, RECT_X, RECT_Y);
            }

        });

        cTurnouts.setOnAction((event) -> {
            System.out.println("calc. turnouts");
            Calc.turnouts();
            redrawPanelElements();
        });
        cNormPositions.setOnAction((event) -> {
            System.out.println("norm positions");
            PanelElement.Companion.scaleAll();
            redrawPanelElements();
        });

        cSearch.setOnAction((event) -> {
            System.out.println("search");
            TextInputDialog dialog = new TextInputDialog("Suche");
            dialog.setTitle("PanelElement suchen");
            dialog.setHeaderText("Eingabebeispiel '937' oder auch '93?'");
            dialog.setContentText("Adresse: ");
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(addr -> {
                //addr = addr.replaceAll("\\s+", "");
                addr = addr.replaceAll("[ \\t\\n\\x0b\\r\\f.]+","");  // replace whitespace and '.'
                System.out.println("suche nach: " + addr);
                if (addr.contains("?")) {
                    addr = addr.substring(0, addr.length() - 1);
                    try {
                        int address = Integer.parseInt(addr);
                        for (PanelElement pe : panelElements) {
                            if (((pe.getAdr() / 10) == address) || ((pe.getAdr2() / 10) == address)) {
                                pe.createShapeAndSetState(PEState.SELECTED);
                            } else {
                                pe.createShapeAndSetState(PEState.DEFAULT);
                            }
                        }
                    } catch (NumberFormatException e) {
                        ;
                    }
                } else {
                    try {
                        int address = Integer.parseInt(addr);
                        for (PanelElement pe : panelElements) {
                            if ((pe.getAdr() == address) || (pe.getAdr2() == address)) {
                                pe.createShapeAndSetState(PEState.SELECTED);
                            } else {
                                pe.createShapeAndSetState(PEState.DEFAULT);
                            }
                        }
                    } catch (NumberFormatException e) {
                        ;
                    }

                }
                redrawPanelElements();

            });

        });

        openRoutingTable.setOnAction((event) -> {
            System.out.println("openrouting table");
            if (routingTable == null) {
                routingTable = new RoutesTable(stage, this);
            } else {
                routingTable.show();
            }
        });

        final MenuItem infoItem = new MenuItem("Info");
        final MenuItem updateItem = new MenuItem("Sind Updates verfügbar?");
        menuInfo.getItems().addAll(infoItem, updateItem);
        infoItem.setGraphic(ivInfo);
        infoItem.setOnAction((event) -> {
            System.out.println("info clicked");
            Dialogs.INSTANCE.buildInfoAlert("Info", "SX4Draw\nhttps://opensx.net/sx4 ", "Programm Version:" + version, this);
        });

        updateItem.setGraphic(ivSX4generic);
        updateItem.setOnAction((event) -> {
            System.out.println("checking version ...");
            // does not work as expected ProgressIndicator p1 = new ProgressIndicator();
            //anchorPane.getChildren().add(p1);
            // gets never diplayes:   status.setText("checking version ...");
            // TODO move to async as soon as using kotlin
            String newVersion = Utils.readLastVersionFromURL();
            // status.setText("");
            //anchorPane.getChildren().remove(p1);
            System.out.println("read from github: " + newVersion);
            if (newVersion.contains("ERROR")) {
                Dialogs.INSTANCE.buildErrorAlert("Error","Konnte die aktuelle Version nicht von Github lesen!", newVersion);
            } else if (version.contains(newVersion)) {
                Dialogs.INSTANCE.buildInformationAlert("keine neue Version vorhanden" , "" , "Version "+version+" ist aktuell", this);
            } else {
                Dialogs.INSTANCE.buildInfoAlert("Download update", "von https://github.com/michael71/SX4Draw/sx4draw.zip ", "Programm Version:" + version, this);
            }
            });

        menuBar.getMenus().addAll(menu1, menuOptions, menuCalc, menuExtra, menuInfo);
    }

    private static PanelElement selectedPE(double x, double y) {
        // do the search from top element (SensorUS, RouteButton) to bottom (track)
        ArrayList<PanelElement> peListRev = new ArrayList<>(panelElements);
        Collections.sort(peListRev);
        Collections.sort(peListRev, Collections.reverseOrder());
        for (PanelElement pe : peListRev) {
            Pair<Boolean, Integer> result = pe.isTouched(new IntPoint(x, y));
            if (result.getKey()) {
                return pe;
            }
        }
        return null;
    }

    private static PanelElement selectedPENotTrack(double x, double y) {
        ArrayList<PanelElement> peListRev = new ArrayList<>(panelElements);
        Collections.sort(peListRev);
        Collections.sort(peListRev, Collections.reverseOrder());
        for (PanelElement pe : peListRev) {
            if (pe.getType() != PEType.TRACK) {
                Pair<Boolean, Integer> result = pe.isTouched(new IntPoint(x, y));
                if (result.getKey()) {
                    return pe;
                }
            }
        }
        return null;
    }

    private static Pair<PanelElement, Integer> selectedPENotTrackInclState(double x, double y) {
        ArrayList<PanelElement> peListRev = new ArrayList<>(panelElements);
        Collections.sort(peListRev);
        Collections.sort(peListRev, Collections.reverseOrder());
        for (PanelElement pe : peListRev) {
            if (pe.getType() != PEType.TRACK) {
                Pair<Boolean, Integer> result = pe.isTouched(new IntPoint(x, y));
                if (result.getKey()) {
                    return new Pair(pe, result.getValue());
                }
            }
        }
        return null;
    }

    /*
    private static PanelElement selectedPENotTrack(IntPoint p) {
        for (PanelElement pe : panelElements) {
            if ((pe.getType() != PEType.TRACK)
                    && (pe.isTouched(p))) {
                return pe;
            }
        }
        return null;
    }  */
    private static PanelElement getRouteBtn(IntPoint p) {
        for (PanelElement pe : panelElements) {
            if (pe.getType() == PEType.ROUTEBUTTON) {
                Pair<Boolean, Integer> result = pe.isTouched(p);
                if (result.getKey()) {
                    return pe;
                }

            }
        }
        return null;
    }

    private static boolean isPETypeOn(PEType type, IntPoint poi) {
        boolean found = false;
        for (PanelElement pe : panelElements) {
            if ((pe.getType() == type)
                    && (Math.abs(poi.getX() - pe.getX()) <= 1) && (Math.abs(poi.getY() - pe.getY()) <= 1)) {
                found = true;
            }
        }
        return found;
    }

    private void toggleSelectionPE(IntPoint p) {
        PanelElement pe = selectedPE(p.getX(), p.getY());
        if (pe != null) {
            pe.toggleShapeSelected();
            redrawPanelElements();
        } else {
            System.out.println("toggleSelPE - no PE found");
        }
        checkSelection();
    }

    private Line startNewLine(IntPoint p, double lineWidth) {
        System.out.println("startNewLine");
        Line l = new Line();

        l.setStartX(p.getX());
        l.setStartY(p.getY());

        l.setStrokeWidth(lineWidth);
        l.setStroke(Color.BLACK);
        l.setStrokeLineCap(StrokeLineCap.ROUND);
        return l;
    }

    private void editPanelElement(IntPoint poi, Stage primStage) {
        PanelElement pe = selectedPENotTrack(poi.getX(), poi.getY());
        // all panel elements except tracks have an address

        if (pe != null) {
            if (pe.getType() == PEType.ROUTEBUTTON) {
                System.out.println("no address editing for route button");
                return;
            }
            GenericAddress initA;
            if (pe.getType() == PEType.SIGNAL) {
                int orient = Utils.INSTANCE.signalDX2ToOrient(new IntPoint(pe.getX2() - pe.getX(), pe.getY2() - pe.getY()));
                initA = new GenericAddress(pe.getAdr(), pe.getInv(), orient);
            } else {
                initA = new GenericAddress(pe.getAdr(), pe.getInv(), INVALID_INT);
            }
            GenericAddress res = AddressDialog.INSTANCE.open(pe, primStage, initA);
            if (res.getAddr() != -1) {
                System.out.println("addr=" + res.getAddr() + " inv=" + res.getInv() + " orient=" + res.getOrient());
                boolean addressOK = Dialogs.INSTANCE.checkAddress(initA, res);
                if (!addressOK) {
                    System.out.println("addressOK=false");
                    return; // do nothing
                }
                pe.setAdr(res.getAddr());

                if (pe.getInv() != res.getInv()) {
                    // inv bit was changed, recreate shape
                    pe.setInv(res.getInv());
                    pe.recreateShape();
                }

                if (pe.getType() == PEType.SIGNAL) {
                    IntPoint d = Utils.INSTANCE.signalOrientToDXY2(res.getOrient());
                    System.out.println("or=" + res.getOrient() + " dx=" + d.getX() + " dy=" + d.getY());
                    pe.setX2(pe.getX() + d.getX());
                    pe.setY2(pe.getY() + d.getY());
                    pe.recreateShape();  // orientation might have changed, create new
                    lastPE = pe;
                    btnUndo.setDisable(false);
                    redrawPanelElements();  // including addresses
                } else {
                    // redraw address
                    if (dispAddresses.isSelected()) {
                        pe.drawAddress(gc);
                    }
                }
            } else {
                System.out.println("no address selected");
            }
        } else {
            System.out.println("no panel element found at " + poi.getX() + "/" + poi.getY());
        }
    }

    public void drawRTButtons(int btn1, int btn2) {
        PanelElement bt1 = PanelElement.Companion.getPeByAddress(btn1).get(0);
        PanelElement bt2 = PanelElement.Companion.getPeByAddress(btn2).get(0);

        gc.strokeText("1", bt1.getX() - 4, bt1.getY() - YOFF + 4);
        gc.strokeText("2", bt2.getX() - 4, bt2.getY() - YOFF + 4);
    }

    public void redrawPanelElements() {
        if (DEBUG) {
            System.out.println("redrawPES() nPE=" + panelElements.size());
        }
        gc.clearRect(0, 0, RECT_X, RECT_Y);
        if (dispAddresses.isSelected()) {
            drawAddresses(gc);
        }

        lineGroup.getChildren().clear();
        Collections.sort(panelElements);
        for (PanelElement pe : panelElements) {
            lineGroup.getChildren().add(pe.getShape());
            //System.out.println("drawing PE at " + pe.x + "," + pe.y + " type=" + pe.type + " state="+pe.getState().name());
        }
    }

    private void addToDraggedGroup() {
        for (PanelElement sel : panelElements) {
            if (sel.getState() == PEState.SELECTED) {
                draggedGroup.getChildren().add(sel.getShape());
            }
        }
    }

    private void resetPEStates() {
        currentGUIState = GUIState.SELECT;
        btnSelect.setSelected(true);
        canvas.setCursor(Cursor.DEFAULT);

        btnMove.setDisable(true);
        btnUndo.setDisable(true);
        btnDelete.setDisable(true);
        btnMove.setSelected(false);

        for (PanelElement sel : panelElements) {
            sel.createShapeAndSetState(PEState.DEFAULT);
        }
    }

    private static void checkSelection() {
        if (PanelElement.Companion.atLeastOneSelected()) {
            btnDelete.setDisable(false);
            btnMove.setDisable(false);
        } else {
            btnDelete.setDisable(true);
            btnMove.setDisable(true);
        }
    }

    private void addElement(PEType t, IntPoint p, Group lg) {
        IntPoint end = IntPoint.Companion.toRaster(p, getRasterDiv2());
        // avoid "double signal on same point
        if (!isPETypeOn(t, end)) {
            PanelElement pe = new PanelElement(t, end);
            lg.getChildren().add(pe.getShape());
            panelElements.add(pe);
            lastPE = pe;
            btnUndo.setDisable(false);
        }
    }

    private void createRoute(IntPoint poi) {
        PanelElement rtbtn = getRouteBtn(poi);
        if (currentRoute == null) {
            // initialize new route
            currentRoute = new Route(Route.Companion.getnewid());
            System.out.println("init route with id=" + currentRoute.getId());
            if (rtbtn != null) {  // first button
                currentRoute.setBtn1(rtbtn.getAdr());
                rtbtn.createShapeAndSetState(PEState.MARKED);
                redrawPanelElements();
                System.out.println("btn1 =" + currentRoute.getBtn1());
                //gc.strokeText("1", rtbtn.x - 4, rtbtn.y - YOFF + 4);
            } else {
                currentRoute = null; // no routebtn at this point
            }
        } else {
            if (rtbtn == null) {
                // continue to add elements to route
                Pair<PanelElement, Integer> peSt = selectedPENotTrackInclState(poi.getX(), poi.getY());
                if (peSt != null) {
                    // add to route and mark
                    currentRoute.addElement(peSt);
                    peSt.getKey().createShapeAndSetState(PEState.MARKED);
                    redrawPanelElements();
                }
            } else {
                // creation finished - a end route button has been selected
                currentRoute.setBtn2(rtbtn.getAdr());
                rtbtn.createShapeAndSetState(PEState.MARKED);
                showRoute(currentRoute,0);
                System.out.println("btn2 =" + currentRoute.getBtn2());
                routes.add(new Route(currentRoute));  // add a new route from btn1 to btn2

                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Fahrstraße "+currentRoute.getId()+ " abgeschlossen.");
                alert.setHeaderText(null);
                alert.setContentText("Die Signalstellungen mssen noch manuell korrigiert werden!");
                alert.showAndWait();

                resetPEStates();
                redrawPanelElements();
                currentRoute = null; // reset
            }
        }
    }


    private static void drawAddresses(GraphicsContext gc) {
        gc.clearRect(0, 0, RECT_X, RECT_Y);
        gc.setLineWidth(1.0);
        gc.setStroke(Color.BLUE);
        gc.setFill(Color.LIGHTBLUE);
        for (PanelElement pe : panelElements) {
            pe.drawAddress(gc);
        }
    }

    private String writeFile(Stage stage, String path, boolean chooseName) {
        if (chooseName) {
            System.out.println("path=" + path);
            FileChooser fileChooser = new FileChooser();
            File file = new File(path);
            if (!file.isDirectory()) {
                String p = file.getParent();
                System.out.println("p=" + p);
                fileChooser.setInitialDirectory(new File(p));
            } else {
                fileChooser.setInitialDirectory(file);
            }
            if (currentFileName.isEmpty()) {
                fileChooser.setInitialFileName("panel.xml");
            } else {
                fileChooser.setInitialFileName(currentFileName);
            }
            fileChooser.setTitle("Panel XML File Speichern");
            fileChooser.getExtensionFilters().addAll(
                    new ExtensionFilter("Panel Files", "panel*.xml*"));
            File selectedFile = fileChooser.showSaveDialog(stage);
            if (selectedFile != null) {
                path = selectedFile.getParent();
                System.out.println("path=" + path);
                String fn = selectedFile.toString();
                if (!fn.endsWith(".xml")) {
                    fn = fn + ".xml";
                }
                WriteConfig.writeToXML(fn, panelName);
                currentFileName = selectedFile.getName();
                return selectedFile.getParent();
            } else {
                return "";
            }
        } else {
            System.out.println("path=" + path);
            File file = new File(path);
            if (!file.isDirectory()) {
                String p = file.getParent();
                System.out.println("p=" + p);
                file = new File(p);
            }
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String version = df.format(new Date());
            String fName = file.getAbsolutePath() + File.separator + "panel.xml" + "." + version;
            WriteConfig.writeToXML(fName, panelName);
            return "";
        }
    }

    private String openFile(Stage stage, String path) {

        System.out.println("path=" + path);
        FileChooser fileChooser = new FileChooser();
        File file = new File(path);
        if (!file.isDirectory()) {
            String p = file.getParent();
            fileChooser.setInitialDirectory(new File(p));
        } else {
            fileChooser.setInitialDirectory(file);
        }
        fileChooser.setTitle("Panel XML File Öffnen");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Panel Files", "panel*.xml*"),
                new ExtensionFilter("Alle Files", "*"));
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            lastPE = null;
            dispAddresses.setSelected(false);
            rasterOn.setSelected(true);
            gc.clearRect(0, 0, RECT_X, INSTANCE.RECT_Y);  // clear addresses labels, if any
            String result = ReadConfig.readXML(selectedFile.toString());
            if (result.equals("OK")) {
                currentFileName = selectedFile.getName();
                redrawPanelElements();
                return selectedFile.getParent();
            } else {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Konnte " + selectedFile.getName() + " nicht einlesen!");
                alert.setContentText(result);
                alert.showAndWait();
                currentFileName = "";
                return "";
            }

        } else {
            currentFileName = "";
            return "";
        }
    }

    public int getRaster() {
        if (rasterOn.isSelected()) {
            return RASTER;
        } else {
            return 1;
        }
    }

    public int getRasterDiv2() {
        if (rasterOn.isSelected()) {
            return RASTER / 2;
        } else {
            return 1;
        }
    }

    public void showRoute(Route rt, int seconds) {
        rt.setRouteStates();
        redrawPanelElements();
        drawRTButtons(rt.getBtn1(), rt.getBtn2());
        if (seconds != 0) {
            Timeline timeline = new Timeline(new KeyFrame(
                    Duration.millis(seconds * 1000),
                    ae -> {
                        rt.setMarked(false);
                        redrawPanelElements();
                    }));

            timeline.play();
        }
    }
}

