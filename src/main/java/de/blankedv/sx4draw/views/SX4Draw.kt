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

package de.blankedv.sx4draw.views

import de.blankedv.sx4draw.*
import de.blankedv.sx4draw.config.*
import de.blankedv.sx4draw.util.*

import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Application
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Orientation
import javafx.scene.Cursor
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Line
import javafx.scene.shape.StrokeLineCap
import javafx.stage.FileChooser
import javafx.stage.FileChooser.ExtensionFilter
import javafx.stage.Stage
import javafx.stage.WindowEvent
import javafx.util.Duration
import javafx.util.Pair

import java.io.File
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Collections
import java.util.Date
import java.util.prefs.Preferences

import de.blankedv.sx4draw.Constants.INVALID_INT
import de.blankedv.sx4draw.Constants.DEBUG
import de.blankedv.sx4draw.Constants.RASTER
import de.blankedv.sx4draw.Constants.RECT_X
import de.blankedv.sx4draw.Constants.RECT_Y
import de.blankedv.sx4draw.Constants.PEState

import de.blankedv.sx4draw.PanelElement.Companion.SENSOR_WIDTH
import de.blankedv.sx4draw.PanelElement.Companion.TRACK_WIDTH
import de.blankedv.sx4draw.model.*
import de.blankedv.sx4draw.util.Calc
import java.lang.NullPointerException

class SX4Draw : Application() {

    private var lineGroup: Group? = null
    private val draggedGroup = Group()
    private var raster: Group? = null

    private var canvas: Canvas? = null // The canvas on which the image is drawn.
    private var gc: GraphicsContext? = null  // The graphics context for the canvas.
    private var vb: VBox? = null

    private var routingTable: RoutesTable? = null
    private var currentRoute: Route? = null

    private var line: Line? = null

    internal val dispAddresses = CheckMenuItem("Adressen anzeigen")
    internal val rasterOn = CheckMenuItem("Raster")
    internal val showMousePos = CheckMenuItem("Mauspos. anzeigen")
    internal val mousePositionToolTip = Tooltip("")
    internal val anchorPane = AnchorPane()
    // internal val status = Label("status")


    private var currentGUIState = GUIState.SELECT
    private val moveStart = IntPoint()
    private var currentFileName = ""

    enum class GUIState {
        ADD_TRACK, ADD_SENSOR, ADD_SENSOR_US, ADD_SIGNAL, ADD_ROUTEBTN, ADD_ROUTE, SELECT, MOVE
    }

    override fun start(primaryStage: Stage) {
        primaryStage.title = "Panel XML File Creator (Name: ???)"
        primaryStage.icons.add(Image("sx4_draw_ico64.png"))
        //primaryStage.getIcons().add(new Image("file:sx4_ico256.png"));
        //primaryStage.getIcons().add(new Image("file:sx4_iconx.png"));

        val prefs = Preferences.userNodeForPackage(this.javaClass)


        // Build the VBox container for the lineBox, canvas, and toolBox

        vb = VBox(3.0)
        val scene = Scene(vb!!, 1300.0, 660.0)
        canvas = Canvas(RECT_X.toDouble(), RECT_Y.toDouble())
        gc = canvas!!.graphicsContext2D
        mousePositionToolTip.opacity = 0.7
        mousePositionToolTip.isAutoHide = true

        // A group to hold all the drawn shapes
        lineGroup = Group()
        lineGroup!!.onMousePressed = EventHandler { me ->
            val poi = IntPoint(me.x, me.y)
            println("linegroup mouse pressed x=" + poi.x + " y=" + poi.y + " currentGUIState=" + currentGUIState.name + " src=" + me.source)
            if (me.button == MouseButton.PRIMARY) {
                println("prim btn")
                when (currentGUIState) {
                    SX4Draw.GUIState.ADD_TRACK, SX4Draw.GUIState.ADD_SIGNAL, SX4Draw.GUIState.ADD_SENSOR, SX4Draw.GUIState.ADD_SENSOR_US, SX4Draw.GUIState.ADD_ROUTEBTN ->
                        // this does not work ....
                        //MyPoint start = IntPoint.toRaster(poi);
                        //line = startNewLine(start);
                        //
                        println("should start new line ...")
                    SX4Draw.GUIState.SELECT ->
                        // if (me.isControlDown()) {
                        //    toggleSelectionPENotTrack(poi);
                        //} else if (me.isShiftDown()) {
                        //    toggleSelectionPENotTrackNotSensor(poi);
                        //} else {
                        toggleSelectionPE(poi)
                    SX4Draw.GUIState.MOVE -> {
                        moveStart.x = me.x.toInt()
                        moveStart.y = me.y.toInt()
                        println("moveStart")
                        addToDraggedGroup()
                    }
                }//}
            } else if (me.button == MouseButton.SECONDARY) {
                println("sec btn")
                editPanelElement(poi, primaryStage)
            }
        }

        raster = Group()

        val buttons = createButtonBar()

        val menuBar = MenuBar()
        createMenu(prefs, menuBar, primaryStage)

        anchorPane.addEventHandler(KeyEvent.KEY_PRESSED) { t ->
            if (t.code == KeyCode.ESCAPE) {
                println("click on escape")
                if (currentGUIState == GUIState.ADD_ROUTE) {
                    // abbruch
                    for (sel in panelElements) {
                        sel.createShapeAndSetState(Constants.PEState.DEFAULT)
                    }
                    //redrawPanelElements();
                    currentRoute = null // reset
                }
            }
        }

        // Build the canvas
        //System.out.println("size rect: x=" + (scene.getWidth()) + " y=" + (scene.getHeight() - 230));
        canvas!!.cursor = Cursor.DEFAULT
        canvas!!.addEventHandler(MouseEvent.MOUSE_PRESSED) { me ->
            val poi = IntPoint(me.x, me.y)
            val end: IntPoint
            start = IntPoint.toRaster(poi, getRaster())
            println("canvas mouse pressed x=" + poi.x + " y=" + poi.y + " currentGUIState=" + currentGUIState.name)
            if (me.button == MouseButton.PRIMARY) {
                println("primary button")
                val poiRast = IntPoint.toRaster(poi, getRaster())
                when (currentGUIState) {
                    SX4Draw.GUIState.ADD_TRACK -> line = startNewLine(start, TRACK_WIDTH)
                    SX4Draw.GUIState.ADD_SIGNAL -> {
                        val sig = Signal(poiRast)
                        if (!isPETypeOn(poiRast)) {
                            val pe = PanelElement(sig)
                            lineGroup!!.children.add(pe.shape)
                            panelElements.add(pe)
                            lastPE = pe
                            btnUndo.isDisable = false
                        }
                    }
                    SX4Draw.GUIState.ADD_ROUTEBTN -> {
                        val rt = RouteButton(poiRast)
                        if (!isPETypeOn(poiRast)) {
                            val pe = PanelElement(rt)
                            lineGroup!!.children.add(pe.shape)
                            panelElements.add(pe)
                            lastPE = pe
                            btnUndo.isDisable = false
                        }
                    }
                    SX4Draw.GUIState.ADD_SENSOR_US -> {
                        val se = Sensor(poiRast)
                        if (!isPETypeOn(poiRast)) {
                            val pe = PanelElement(se)
                            lineGroup!!.children.add(pe.shape)
                            panelElements.add(pe)
                            lastPE = pe
                            btnUndo.isDisable = false

                        }
                    }
                    SX4Draw.GUIState.ADD_ROUTE -> createRoute(poi)
                    SX4Draw.GUIState.ADD_SENSOR -> {
                        line = startNewLine(start, SENSOR_WIDTH)
                        line!!.strokeDashArray.addAll(15.0, 10.0)
                        line!!.stroke = Color.YELLOW
                    }
                    SX4Draw.GUIState.SELECT ->
                        // TODO make selection where ROUTEB has higher prio than SENSOR has higher prio than TRACK
                        // avoiding use of extra keys for selection !!!
                        //if (me.isControlDown()) {
                        //    toggleSelectionPENotTrack(poi);
                        //} else if (me.isShiftDown()) {
                        //    toggleSelectionPENotTrackNotSensor(poi);
                        //} else {
                        toggleSelectionPE(poi)

                    SX4Draw.GUIState.MOVE -> {
                        val ms = IntPoint.toRaster(poi, getRaster())
                        moveStart.x = ms.x
                        moveStart.y = ms.y
                        println("moveStart at $moveStart")
                        addToDraggedGroup()
                    }
                }//}
            } else if (me.button == MouseButton.SECONDARY) {
                println("secondary button, poi=" + poi.x + "," + poi.y)
                editPanelElement(poi, primaryStage)
            }

        }

        canvas!!.onMouseMoved = EventHandler { event ->
            val msg = "(" + event.x.toInt() + ", " + event.y.toInt() + ")"
            /* + ")\n(sceneX: "
                    + event.getSceneX() + ", sceneY: " + event.getSceneY() + ")\n(screenX: "
                    + event.getScreenX() + ", screenY: " + event.getScreenY() + ")"; */
            if (showMousePos.isSelected) {
                mousePositionToolTip.text = msg
                val node = event.source as Node
                mousePositionToolTip.show(node, event.x + 30, event.y + 20)

            }
        }

        canvas!!.onMouseReleased = EventHandler { me ->
            // keep shapes within rectangle
            if (canvas!!.boundsInLocal.contains(me.x, me.y)) {
                val poi = IntPoint(me.x, me.y)
                println("end approx x=" + poi.x + " y=" + poi.y)
                val end = IntPoint.correctAngle(start, poi, getRaster())
                println("end x=" + end.x + " y=" + end.y)
                when (currentGUIState) {
                    SX4Draw.GUIState.ADD_TRACK,
                    SX4Draw.GUIState.ADD_SENSOR -> {
                        line!!.endX = end.x.toDouble()
                        line!!.endY = end.y.toDouble()
                        lineGroup!!.children.remove(line)  // will be re-added from within PE
                        if (Math.abs(line!!.endX - line!!.startX) > 5 || Math.abs(line!!.endY - line!!.startY) > 5) {
                            if (currentGUIState == GUIState.ADD_TRACK) {
                                val pe = PanelElement(Track(line!!))
                                lastPE = pe
                                panelElements.add(pe)
                            } else {
                                val pe = PanelElement(Sensor(line!!))
                                lastPE = pe
                                panelElements.add(pe)
                            }

                            lineGroup!!.children.add(lastPE!!.shape)
                            btnUndo.isDisable = false
                            redrawPanelElements()
                        }
                    }

                    SX4Draw.GUIState.ADD_SENSOR_US, SX4Draw.GUIState.ADD_SIGNAL, SX4Draw.GUIState.ADD_ROUTEBTN, SX4Draw.GUIState.ADD_ROUTE -> {
                    }
                    SX4Draw.GUIState.MOVE    // MOVE ends
                    -> {
                        val d = IntPoint.delta(moveStart, poi, getRaster())
                        println("move END: final delta =" + d.x + "," + d.y)
                        PanelElement.moveSelected(d)
                        draggedGroup.children.clear()
                        resetPEStates()
                        redrawPanelElements()
                    }
                }
            }
        }

        canvas!!.onMouseDragged = EventHandler { me ->
            if (canvas!!.boundsInLocal.contains(me.x, me.y)) {
                val poi = IntPoint(me.x, me.y)
                when (currentGUIState) {
                    SX4Draw.GUIState.ADD_TRACK, SX4Draw.GUIState.ADD_SENSOR -> {
                        lineGroup!!.children.remove(line)
                        val mp = IntPoint.correctAngle(start, poi, getRaster())
                        line!!.endX = mp.x.toDouble()
                        line!!.endY = mp.y.toDouble()
                        //System.out.println("x=" + (int)me.getX() + " y=" + (int)me.getY());
                        if (!lineGroup!!.children.contains(line)) {
                            lineGroup!!.children.add(line)
                        }
                    }

                    SX4Draw.GUIState.ADD_SIGNAL -> {
                    }
                    SX4Draw.GUIState.MOVE  // MOVE dragging
                    -> {
                        val d = IntPoint.delta(moveStart, poi, getRaster())
                        //System.out.println("delta =" + d.x + "," + d.y);
                        draggedGroup.translateX = d.x.toDouble()
                        draggedGroup.translateY = d.y.toDouble()
                    }
                }// do nothing
            }

            if (showMousePos.isSelected) {
                val msg = "(" + me.x.toInt() + ", " + me.y.toInt() + ")"
                mousePositionToolTip.text = msg
                val node = me.source as Node
                mousePositionToolTip.show(node, (me.x.toInt() + 30).toDouble(), (me.y.toInt() + 20).toDouble())
            }
        }

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


        val scPane = ScrollPane()
        vb!!.children.addAll(menuBar, buttons, scPane)    //, status);
        //scPane.setFitToHeight(true);
        //scPane.setFitToWidth(true);
        scPane.maxWidth = RECT_X.toDouble()
        scPane.maxHeight = RECT_Y.toDouble()
        vb!!.maxWidth = RECT_X.toDouble()
        vb!!.maxHeight = (RECT_Y + 40).toDouble()
        scPane.setPrefSize(RECT_X.toDouble(), RECT_Y.toDouble())
        scPane.isFitToWidth = true
        //BorderPane.setAlignment(scPane, Pos.CENTER);
        VBox.setVgrow(scPane, Priority.ALWAYS)
        HBox.setHgrow(scPane, Priority.ALWAYS)

        //anchorPane.setPrefSize(1100,540);
        //scPane.setContent(stackPane);
        scPane.content = anchorPane
        //bp.setCenter(scPane);
        //bp.setTop(vb);

        var i = 0
        while (i <= RECT_X + 100) {
            var j = 0
            while (j <= RECT_Y + 160) {
                val cir = Circle(i.toDouble(), j.toDouble(), 0.5)
                cir.fill = Color.BLUE
                if (canvas!!.boundsInParent.contains(i.toDouble(), j.toDouble())) {
                    raster!!.children.add(cir)
                }
                j = j + RASTER * 2
            }
            i = i + RASTER * 2
        }

        anchorPane.children.addAll(lineGroup, raster, canvas, draggedGroup)

        primaryStage.scene = scene

        primaryStage.show()

        primaryStage.setOnCloseRequest { _: WindowEvent ->
            saveOnExit(prefs, primaryStage)
            System.exit(0)
        }

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

    private fun createButtonBar(): HBox {
        val selIcon = ImageView(Image("select.png"))
        val plusIcon = ImageView(Image("plus.png"))
        val plusIcon1 = ImageView(Image("plus.png"))
        val plusIcon2 = ImageView(Image("plus.png"))
        val plusIcon3 = ImageView(Image("plus.png"))
        val plusIcon4 = ImageView(Image("plus.png"))
        val plusIcon5 = ImageView(Image("plus.png"))
        val delIcon = ImageView(Image("delete.png"))
        val moveIcon = ImageView(Image("move.png"))
        val undoIcon = ImageView(Image("undo.png"))

        val sep1 = Separator(Orientation.VERTICAL)
        val sep2 = Separator(Orientation.VERTICAL)

        val btns = HBox(3.0)
        btns.children.addAll(btnSelect, btnUnSelect, sep1, btnAddTrack, btnAddSensor, btnAddSensorUS, btnAddSignal, btnRouteBtn, btnAddRoute, sep2, btnUndo, btnDelete, btnMove)

        btnSelect.toggleGroup = toggleGroup
        btnSelect.isSelected = true
        btnMove.isSelected = false
        btnSelect.text = "Select"
        btnSelect.graphic = selIcon
        btnSelect.setOnAction { event: ActionEvent -> enterSelectState() }

        btnUnSelect.text = "Unselect"
        //btnUnSelect.setGraphic(selIcon);
        btnUnSelect.setOnAction {
            println("unselect")
            unselectAll()
            redrawPanelElements()
        }

        btnMove.toggleGroup = toggleGroup
        btnMove.isSelected = false
        btnMove.isDisable = true
        btnSelect.isSelected = false
        btnMove.text = "Move"
        btnMove.graphic = moveIcon
        btnMove.setOnAction {
            currentGUIState = GUIState.MOVE
            canvas!!.cursor = Cursor.CLOSED_HAND
        }

        btnAddTrack.toggleGroup = toggleGroup
        btnAddTrack.text = "Track"
        btnAddTrack.graphic = plusIcon1
        btnAddTrack.setOnAction {
            currentGUIState = GUIState.ADD_TRACK
            canvas!!.cursor = Cursor.CROSSHAIR
            //resetLines();
        }

        btnAddSensor.toggleGroup = toggleGroup
        btnAddSensor.text = "Sensor"
        btnAddSensor.graphic = plusIcon2
        btnAddSensor.setOnAction {
            currentGUIState = GUIState.ADD_SENSOR
            canvas!!.cursor = Cursor.CROSSHAIR
            //resetLines();
        }

        btnAddSensorUS.toggleGroup = toggleGroup
        btnAddSensorUS.text = "Sensor-US"
        btnAddSensorUS.graphic = plusIcon4
        btnAddSensorUS.setOnAction {
            currentGUIState = GUIState.ADD_SENSOR_US
            canvas!!.cursor = Cursor.CROSSHAIR
            //resetLines();
        }

        btnAddSignal.toggleGroup = toggleGroup
        btnAddSignal.text = "Signal"
        btnAddSignal.graphic = plusIcon
        btnAddSignal.setOnAction {
            currentGUIState = GUIState.ADD_SIGNAL
            canvas!!.cursor = Cursor.CROSSHAIR
            //resetLines();
        }

        btnRouteBtn.toggleGroup = toggleGroup
        btnRouteBtn.text = "RT-Button"
        btnRouteBtn.graphic = plusIcon3
        btnRouteBtn.setOnAction {
            currentGUIState = GUIState.ADD_ROUTEBTN
            canvas!!.cursor = Cursor.CROSSHAIR
            //resetLines();
        }

        btnAddRoute.toggleGroup = toggleGroup
        btnAddRoute.text = "Fahrstr."
        btnAddRoute.graphic = plusIcon5
        btnAddRoute.setOnAction {
            if (PanelElement.addressesAvail()) {
                currentGUIState = GUIState.ADD_ROUTE
                canvas!!.cursor = Cursor.CLOSED_HAND
            } else {

                val alert = Alert(AlertType.WARNING)
                alert.title = "Warnung"
                alert.headerText = "Es sind keine oder nur wenige Adressen eingegeben"
                alert.contentText = "Bitte Adressen definieren vor der Fahrstraßeneingabe!"
                alert.showAndWait()
                enterSelectState()
            }
        }

        btnUndo.text = "Undo"
        btnUndo.isDisable = true
        btnUndo.graphic = undoIcon
        btnUndo.setOnAction {
            val pe = lastPE
            if (pe != null) {
                lineGroup!!.children.remove(pe!!.shape)
                panelElements.remove(pe)
                line = null
                lastPE = null
                println("last line removed")
                btnUndo.isDisable = true
            }
        }

        btnDelete.text = "Delete"
        btnDelete.isDisable = true

        btnDelete.graphic = delIcon
        btnDelete.setOnAction {
            btnUndo.isDisable = true
            lastPE = null
            var toDelete = INVALID_INT
            for (i in panelElements.indices.reversed()) {
                val l = panelElements[i].shape
                if (l.stroke === Color.RED) {
                    // is selected
                    toDelete = i
                    println("toDelete i=$i")
                    break
                }
            }
            if (toDelete != INVALID_INT) {
                val pe = panelElements[toDelete]
                val l = pe.shape
                l.stroke = Color.BLACK
                lineGroup!!.children.remove(l)
                panelElements.removeAt(toDelete)
                println("removed  #$toDelete")
                checkSelection()
            }

        }

        return btns
    }

    private fun enterSelectState() {
        currentGUIState = GUIState.SELECT
        btnUndo.isDisable = true
        btnAddRoute.isSelected = false
        btnSelect.isSelected = true
        canvas!!.cursor = Cursor.DEFAULT
        checkSelection()
    }

    private fun unselectAll() {
        for (pe in panelElements) {
            pe.createShapeAndSetState(Constants.PEState.DEFAULT)
        }
    }

    private fun saveOnExit(prefs: Preferences, stage: Stage) {
        val result = Dialogs.confAlert("Abspeichern?", "Vor dem Beenden abspeichern?", "")
        if (result) {
            val path = prefs.get("directory", System.getProperty("user.home"))
            val lastPath = writeFile(stage, path)
            if (lastPath != null && !lastPath.isEmpty()) {
                prefs.put("directory", lastPath)
            }
        }
    }

    private fun createMenu(prefs: Preferences, menuBar: MenuBar, stage: Stage) {
        // final ImageView ivSettings = new ImageView(new Image("/de/blankedv/sx4monitorfx/res/settings.png"));
        val ivInfo = ImageView(Image("info.png"))
        val ivSX4generic = ImageView(Image("sx4_draw_ico32.png"))
        val menu1 = Menu("File")
        val menuOptions = Menu("Optionen")
        val setName = MenuItem("Set Panel-Name")

        val scale200 = MenuItem("Scale 200%")
        val scale50 = MenuItem("Scale 50%")
        val openRoutingTable = MenuItem("Fahrstr. anzeigen")

        val menuCalc = Menu("Berechnen")
        val cTurnouts = MenuItem("Weichen berechnen")
        val cNormPositions = MenuItem("Start Position normieren auf (20,20)")
        val menuExtra = Menu("Extras")
        val cSearch = MenuItem("Suche nach Adressen")
        val menuInfo = Menu("Hilfe")
        val saveItem = MenuItem("Panel abspeichern")
        val openItem = MenuItem("Panel öffnen")
        val exitItem = MenuItem("Programm-Ende/Exit")
        menu1.items.addAll(openItem, saveItem, exitItem)
        menuOptions.items.addAll(setName, dispAddresses, rasterOn, showMousePos, openRoutingTable)
        menuCalc.items.addAll(cTurnouts, cNormPositions, scale200, scale50)
        menuExtra.items.addAll(cSearch)
        rasterOn.isSelected = true
        showMousePos.isSelected = false

        exitItem.setOnAction { event ->
            saveOnExit(prefs, stage)
            System.exit(0)

        }

        saveItem.setOnAction { event ->
            //PanelElement.normPositions();
            redrawPanelElements()
            val path = prefs.get("directory", System.getProperty("user.home"))
            val lastPath = writeFile(stage, path)
            if (lastPath != null && !lastPath.isEmpty()) {
                prefs.put("directory", lastPath)
            }
            stage.title = "Create Panel Description (Name: $panelName) File: $currentFileName"
        }

        openItem.setOnAction { event ->
            val path = prefs.get("directory", System.getProperty("user.home"))
            val lastPath = openFile(stage, path)
            if (lastPath != null && !lastPath.isEmpty()) {
                prefs.put("directory", lastPath)
            }
            stage.title = "Create Panel Description (Name: $panelName) File: $currentFileName"
        }

        setName.setOnAction { event ->
            println("set panelName")
            val getName = TextInputDialog(panelName)
            getName.title = "Neuer Panel-Name"
            getName.headerText = null
            getName.contentText = "Panel-Name:"
            val result = getName.showAndWait()
            result.ifPresent { name ->
                panelName = name
                stage.title = "Create Panel Description (Name: $panelName)"
            }

        }

        rasterOn.setOnAction { event ->
            println("raster toggle")
            if (rasterOn.isSelected) {
                if (!anchorPane.children.contains(raster)) {
                    anchorPane.children.add(raster)
                }
            } else {
                anchorPane.children.remove(raster)
            }
        }

        showMousePos.setOnAction { event ->
            println("mousepos toggle")
            if (showMousePos.isSelected) {
                //mousePositionToolTip.setText(null);
            } else {
                //mousePositionToolTip.remo
            }
        }

        scale200.setOnAction { event ->
            println("scale 200%")
            PanelElement.scalePlus()
            redrawPanelElements()
        }

        scale50.setOnAction { event ->

            println("scale 50%")
            PanelElement.scaleMinus()
            redrawPanelElements()
        }

        dispAddresses.setOnAction { event ->
            println("display addresses = " + dispAddresses.isSelected)
            if (dispAddresses.isSelected) {
                drawAddresses(gc!!)
            } else {
                gc!!.clearRect(0.0, 0.0, RECT_X.toDouble(), RECT_Y.toDouble())
            }

        }

        cTurnouts.setOnAction { event ->
            println("calc. turnouts")
            Calc.turnouts()
            redrawPanelElements()
        }
        cNormPositions.setOnAction { event ->
            println("norm positions")

            PanelElement.normPositions()
            redrawPanelElements()
        }

        cSearch.setOnAction { event ->
            println("search")
            val dialog = TextInputDialog("Suche")
            dialog.title = "PanelElement suchen"
            dialog.headerText = "Eingabebeispiel '937' oder auch '93?'"
            dialog.contentText = "Adresse: "
            val result = dialog.showAndWait()
            result.ifPresent { _address ->
                //addr = addr.replaceAll("\\s+", "");
                var addr = _address.replace("[ \\t\\n\\x0b\\r\\f.]+".toRegex(), "")  // replace whitespace and '.'
                println("suche nach: $addr")
                if (addr.contains("?")) {
                    addr = addr.substring(0, addr.length - 1)
                    try {
                        val address = Integer.parseInt(addr)
                        for (pe in panelElements) {
                            if (pe.gpe.getAddr() / 10 == address || pe.gpe.getAddr2() / 10 == address) {
                                pe.createShapeAndSetState(Constants.PEState.SELECTED)
                            } else {
                                pe.createShapeAndSetState(Constants.PEState.DEFAULT)
                            }
                        }
                    } catch (e: NumberFormatException) {
                    }

                } else {
                    try {
                        val addr2 = Integer.parseInt(addr)
                        for (pe in panelElements) {
                            if (pe.gpe.getAddr() == addr2 || pe.gpe.getAddr2() == addr2) {
                                pe.createShapeAndSetState(Constants.PEState.SELECTED)
                            } else {
                                pe.createShapeAndSetState(Constants.PEState.DEFAULT)
                            }
                        }
                    } catch (e: NumberFormatException) {
                    }

                }
                redrawPanelElements()

            }

        }

        openRoutingTable.setOnAction { event ->
            println("openrouting table")
            if (routingTable == null) {
                routingTable = RoutesTable(stage, this)
            } else {
                routingTable!!.show()
            }
        }

        val infoItem = MenuItem("Info")
        val updateItem = MenuItem("Sind Updates verfügbar?")
        menuInfo.items.addAll(infoItem, updateItem)
        infoItem.graphic = ivInfo
        infoItem.setOnAction { event ->
            println("info clicked")
            Dialogs.buildInfoAlert("Info", "SX4Draw\nhttps://opensx.net/sx4 ", "Programm Version:$version", this)
        }

        updateItem.graphic = ivSX4generic
        updateItem.setOnAction { event ->
            println("checking version ...")
            // does not work as expected ProgressIndicator p1 = new ProgressIndicator();
            //anchorPane.getChildren().add(p1);
            // gets never diplayes:   status.setText("checking version ...");
            // TODO move to async as soon as using kotlin
            val newVersion = Utils.readLastVersionFromURL()
            // status.setText("");
            //anchorPane.getChildren().remove(p1);
            println("read from github: $newVersion")
            if (newVersion < 0.0) {
                Dialogs.buildErrorAlert("Error", "Konnte die aktuelle Version nicht von Github lesen!", "?")
            } else if (newVersion <= vNumber) {
                Dialogs.buildInformationAlert("keine neue Version vorhanden", "", "Version $version ist aktuell", this)
            } else {
                val title = "$vNumber ist nicht aktuell."
                val msg = "Download der aktuellen Version $newVersion von: https://opensx.net/sx4 möglich "
                Dialogs.buildInfoAlert(title, msg, "", this)
            }
        }

        menuBar.menus.addAll(menu1, menuOptions, menuCalc, menuExtra, menuInfo)
    }

    private fun toggleSelectionPE(p: IntPoint) {
        val pe = selectedPE(p.x.toDouble(), p.y.toDouble())
        if (pe != null) {
            pe.toggleShapeSelected()
            redrawPanelElements()
        } else {
            println("toggleSelPE - no PE found")
        }
        checkSelection()
    }

    private fun startNewLine(p: IntPoint, lineWidth: Double): Line {
        println("startNewLine")
        val l = Line()

        l.startX = p.x.toDouble()
        l.startY = p.y.toDouble()

        l.strokeWidth = lineWidth
        l.stroke = Color.BLACK
        l.strokeLineCap = StrokeLineCap.ROUND
        return l
    }

    private fun editTurnout(pe: PanelElement, primStage: Stage) {
        val tu = (pe.gpe as Turnout)
        println("IN: addr=" + tu.getAddr() + " inv=" + tu.inv )
        val initA = GenericAddress(tu.getAddr(), INVALID_INT, tu.inv)
        val result = AddressDialog.open(pe, primStage, initA)
        if (result.addr != -1) {
            println("OUT: addr=" + result.addr + " addr2=" + result.addr2 + " inv=" + result.inv + " orient=" + result.orient)
            val addressOK = Dialogs.checkAddress(initA, result)
            if (!addressOK) {
                println("addressOK=false")
                return  // do nothing
            }
            tu.adr = result.addr
            tu.inv = result.inv
            println("tu addr=" + tu.adr + " tu.inv=" + tu.inv)
            pe.recreateShape()
            redrawPanelElements()
        } else {
            println("no address selected")
        }
    }

    private fun editSignal(pe: PanelElement, primStage: Stage) {
        val si = (pe.gpe as Signal)
        val orientation = Utils.signalDX2ToOrient(IntPoint(
                (pe.gpe as Signal).x2 - pe.gpe.x, (pe.gpe as Signal).y2 - pe.gpe.y))
        val initA = GenericAddress(si.getAddr(), si.getAddr2(), 0, orient = orientation)
        val result = AddressDialog.open(pe, primStage, initA)
        if (result.addr != -1) {
            println("OUT: addr=" + result.addr + " addr2=" + result.addr2 + " inv=" + result.inv + " orient=" + result.orient)
            val addressOK = Dialogs.checkAddress(initA, result)
            if (!addressOK) {
                println("addressOK=false")
                return  // do nothing
            }
            if (result.addr2 != INVALID_INT) {
                si.adrStr = "" + result.addr + "," + (result.addr + 1)
            } else {
                si.adrStr = "" + result.addr
            }
            val d = Utils.signalOrientToDXY2(result.orient)
            println("or=" + result.orient + " dx=" + d.x + " dy=" + d.y)
            si.x2 = si.x + d.x
            si.y2 = si.y + d.y
            pe.recreateShape()  // orientation might have changed, create new
            lastPE = pe
            btnUndo.isDisable = false
            redrawPanelElements()

        } else {
            println("no address selected")
        }

    }

    private fun editSensor(pe: PanelElement, primStage: Stage) {
        val se = (pe.gpe as Sensor)
        val initA = GenericAddress(se.getAddr(), se.getAddr2())
        val result = AddressDialog.open(pe, primStage, initA)
        if (result.addr != -1) {
            println("OUT: addr=" + result.addr + " addr2=" + result.addr2 + " inv=" + result.inv + " orient=" + result.orient)
            val addressOK = Dialogs.checkAddress(initA, result)
            if (!addressOK) {
                println("addressOK=false")
                return  // do nothing
            }
            if (result.addr2 != INVALID_INT) {
                se.adrStr = "" + result.addr + "," + result.addr2
            } else {
                se.adrStr = "" + result.addr
            }

            redrawPanelElements()
        } else {
            println("no address selected")
        }

    }

    private fun editPanelElement(poi: IntPoint, primStage: Stage) {
        val pe = selectedPENotTrack(poi.x.toDouble(), poi.y.toDouble())
        // all panel elements except tracks have an address
        println("editing pe =" + pe.toString())
        if (pe != null) {
            when (pe.gpe) {
                is RouteButton -> {
                    println("no address editing for route button")
                    return
                }
                is Turnout -> editTurnout(pe, primStage)
                is Signal -> editSignal(pe, primStage)
                is Sensor -> editSensor(pe, primStage)
            }
        } else {
            println("no panel element found at " + poi.x + "/" + poi.y)
        }
    }

    fun drawRTButtons(btn1: Int, btn2: Int) {
        val bt1 = PanelElement.getPeByAddress(btn1)[0]
        val bt2 = PanelElement.getPeByAddress(btn2)[0]

        gc!!.strokeText("1", (bt1.gpe.x - 4).toDouble(), (bt1.gpe.y + 4).toDouble())
        gc!!.strokeText("2", (bt2.gpe.x - 4).toDouble(), (bt2.gpe.y + 4).toDouble())
    }

    fun redrawPanelElements() {
        if (DEBUG) {
            println("redrawPES() nPE=" + panelElements.size)
        }
        gc!!.clearRect(0.0, 0.0, RECT_X.toDouble(), RECT_Y.toDouble())
        if (dispAddresses.isSelected) {
            drawAddresses(gc!!)
        }

        lineGroup!!.children.clear()
        Collections.sort(panelElements)   // route buttons on top of sensors, sensors on top of track, etc ...
        for (pe in panelElements) {
            lineGroup!!.children.add(pe.shape)
            //System.out.println("drawing PE at " + pe.getX() + "," + pe.getY() + " type=" + pe.getType()
            //        + " state="+pe.getState().name() + " fillC=" + pe.getShape().getFill());
        }
    }

    private fun addToDraggedGroup() {
        for (sel in panelElements) {
            if (sel.state === Constants.PEState.SELECTED) {
                draggedGroup.children.add(sel.shape)
            }
        }
    }

    private fun resetPEStates() {
        currentGUIState = GUIState.SELECT
        btnSelect.isSelected = true
        canvas!!.cursor = Cursor.DEFAULT

        btnMove.isDisable = true
        btnUndo.isDisable = true
        btnDelete.isDisable = true
        btnMove.isSelected = false

        for (sel in panelElements) {
            sel.createShapeAndSetState(Constants.PEState.DEFAULT)
        }
    }

    private fun createRoute(poi: IntPoint) {
        val rtbtn = getRouteBtn(poi)
        if (currentRoute == null) {
            // initialize new route
            currentRoute = Route(Route.getAutoAddress())
            println("init route with adr=" + currentRoute!!.adr)
            if (rtbtn != null) {  // first button
                currentRoute!!.btn1 = rtbtn.gpe.getAddr()
                rtbtn.createShapeAndSetState(PEState.MARKED)
                redrawPanelElements()
                println("btn1 =" + currentRoute!!.btn1)
                //gc.strokeText("1", rtbtn.x - 4, rtbtn.y - YOFF + 4);
            } else {
                currentRoute = null // no routebtn at this point
            }
        } else {
            if (rtbtn == null) {
                // continue to add elements to route
                val peSt = selectedPENotTrackInclState(poi.x.toDouble(), poi.y.toDouble())
                if (peSt != null) {
                    // add to route and mark
                    currentRoute!!.addElement(peSt)
                    peSt.key.createShapeAndSetState(PEState.MARKED)
                    redrawPanelElements()
                }
            } else {
                // creation finished - a end route button has been selected
                currentRoute!!.btn2 = rtbtn.gpe.getAddr()
                rtbtn.createShapeAndSetState(PEState.MARKED)
                val crt = currentRoute
                showRoute(crt!!, 0)
                println("btn2 =" + currentRoute!!.btn2)
                routes.add(Route(currentRoute!!))  // add a new route from btn1 to btn2

                val alert = Alert(AlertType.INFORMATION)
                alert.title = "Fahrstraße " + currentRoute!!.adr + " abgeschlossen."
                alert.headerText = null
                alert.contentText = "Die Signalstellungen mssen noch manuell korrigiert werden!"
                alert.showAndWait()

                resetPEStates()
                redrawPanelElements()
                currentRoute = null // reset
            }
        }
    }

    private fun writeFile(stage: Stage, path: String): String? {
        var path = path


        //System.out.println("path=" + path);
        val fileChooser = FileChooser()
        val file = File(path)
        if (!file.isDirectory) {
            val p = file.parent
            println("p=$p")
            fileChooser.initialDirectory = File(p)
        } else {
            fileChooser.initialDirectory = file
        }
        if (currentFileName.isEmpty()) {
            fileChooser.initialFileName = "panel.xml"
        } else {
            fileChooser.initialFileName = currentFileName
        }
        fileChooser.title = "Panel XML File Speichern"
        fileChooser.extensionFilters.addAll(
                ExtensionFilter("Panel Files", "panel*.xml*"))
        val selectedFile = fileChooser.showSaveDialog(stage)
        if (selectedFile != null) {
            path = selectedFile.parent
            //System.out.println("path=" + path);
            var fn = selectedFile.toString()
            if (!fn.endsWith(".xml")) {
                fn = "$fn.xml"
            }

            prepareLCAndWrite(fn)
            //WriteConfig.toXML(fn, panelName);  // OLD
            currentFileName = selectedFile.name
            return selectedFile.parent
        } else {
            return ""
        }

    }

    private fun prepareLCAndWrite(fn: String) {

        val df = SimpleDateFormat("yyyyMMdd_HHmmss")
        val version = df.format(Date())
        System.out.println("creating Config, filename=$fn panelName=$panelName")
        val panelConfig = PanelConfig(panelName, panelElements, ArrayList(routes), ArrayList(compRoutes), trips, timetables)
        val shortFN = File(fn).name
        val layoutConfig = LayoutConfig(shortFN, panelConfig, version)

        WriteConfig.toXML(fn, layoutConfig!!)
    }


    private fun openFile(stage: Stage, path: String): String? {

        println("path=$path")
        val fileChooser = FileChooser()
        val file = File(path)
        if (!file.isDirectory) {
            val p = file.parent
            fileChooser.initialDirectory = File(p)
        } else {
            fileChooser.initialDirectory = file
        }
        fileChooser.title = "Panel XML File Öffnen"
        fileChooser.extensionFilters.addAll(
                ExtensionFilter("Panel Files", "panel*.xml*"),
                ExtensionFilter("Alle Files", "*"))
        val selectedFile = fileChooser.showOpenDialog(stage)
        if (selectedFile != null) {
            lastPE = null
            dispAddresses.isSelected = false
            rasterOn.isSelected = true
            gc!!.clearRect(0.0, 0.0, RECT_X.toDouble(), RECT_Y.toDouble())  // clear addresses labels, if any
            val layoutConfig = ReadConfig.fromXML(selectedFile.toString())
            if (layoutConfig != null) {
                currentFileName = selectedFile.name
                version = layoutConfig.version
                System.out.println("filename=${currentFileName} version=$version")
                locos = layoutConfig.getAllLocos()
                val panelConfig = layoutConfig.getPC0()   // first PanelConfig
                try {
                    panelElements = panelConfig!!.getAllPanelElements()
                    panelName = panelConfig!!.name
                    routes = FXCollections.observableArrayList(panelConfig!!.getAllRoutes())
                    compRoutes = FXCollections.observableArrayList(panelConfig!!.getAllCompRoutes())
                    trips = panelConfig!!.getAllTrips()
                    timetables = panelConfig!!.getAllTimetables()
                } catch (e: NullPointerException) {
                    System.out.println("ERROR in reading panelConfig")
                    System.out.println("ERROR: ${e.message}")
                }
                // from write...
                //val panelConfig = PanelConfig(panelName, panelElements, ArrayList(routes), ArrayList(compRoutes), trips, timetables)
                //val shortFN = File(fn).name
                //layoutConfig = LayoutConfig(shortFN, panelName, panelConfig, version)

                redrawPanelElements()
                return selectedFile.parent
            } else {
                val alert = Alert(AlertType.ERROR)
                alert.title = "Error"
                alert.headerText = "Konnte " + selectedFile.name + " nicht einlesen!"
                alert.contentText = ""
                alert.showAndWait()
                currentFileName = ""
                return ""
            }

        } else {
            currentFileName = ""
            return ""
        }
    }

    fun getRaster(): Int {
        return if (rasterOn.isSelected) {
            RASTER
        } else {
            1
        }
    }


    fun showRoute(rt: Route, seconds: Int) {
        rt.setRouteStates()
        redrawPanelElements()
        drawRTButtons(rt.btn1, rt.btn2)
        if (seconds != 0) {
            val timeline = Timeline(KeyFrame(Duration.seconds(seconds.toDouble()),
                    EventHandler<ActionEvent> {
                        rt.setMarked(false)
                        redrawPanelElements()
                    }))
            timeline.play()
        }
    }

    companion object {

        var vNumber = 0.42
        var vString = "21 Feb 2019"
        var version = "$vNumber - $vString"

        // TODO UNDO für ca. mehrere Elemente
        var locos = ArrayList<Loco>()
        var panelElements = ArrayList<PanelElement>()
        var routes = FXCollections.observableArrayList<Route>()
        var compRoutes = FXCollections.observableArrayList<CompRoute>()
        //public static final ObservableList<Trip> trips = FXCollections.observableArrayList();
        var trips = ArrayList<Trip>()

        var timetables = ArrayList<Timetable>()

        var lastPE: PanelElement? = null
        var panelName = ""

        var start = IntPoint(0, 0)
        private val btnUndo = Button()
        private val btnAddTrack = ToggleButton()
        private val btnAddSensor = ToggleButton()
        private val btnAddSensorUS = ToggleButton()
        private val btnAddSignal = ToggleButton()
        private val btnRouteBtn = ToggleButton()
        private val btnAddRoute = ToggleButton()
        private val btnSelect = ToggleButton()
        private val btnUnSelect = Button()
        private val btnMove = ToggleButton()
        private val btnDelete = Button()
        private val toggleGroup = ToggleGroup()


        /**
         * @param args the command line arguments
         */
        fun main(args: Array<String>) {
            Application.launch(*args)
        }

        private fun selectedPE(x: Double, y: Double): PanelElement? {
            // do the search from top element (SensorUS, RouteButton) to bottom (track)
            val peListRev = ArrayList(panelElements)
            Collections.sort(peListRev)
            Collections.sort(peListRev, Collections.reverseOrder())
            for (pe in peListRev) {
                val result = pe.gpe.isTouched(IntPoint(x, y))
                if (result.key) {
                    return pe
                }
            }
            return null
        }

        private fun selectedPENotTrack(x: Double, y: Double): PanelElement? {
            val peListRev = ArrayList(panelElements)
            Collections.sort(peListRev)
            Collections.sort(peListRev, Collections.reverseOrder())
            for (pe in peListRev) {
                if (pe.gpe !is Track) {
                    val result = pe.gpe.isTouched(IntPoint(x, y))
                    if (result.key) {
                        return pe
                    }
                }
            }
            return null
        }

        private fun selectedPENotTrackInclState(x: Double, y: Double): Pair<PanelElement, Int>? {
            val peListRev = ArrayList(panelElements)
            Collections.sort(peListRev)
            Collections.sort(peListRev, Collections.reverseOrder())
            for (pe in peListRev) {
                if (pe.gpe !is Track) {
                    val result = pe.gpe.isTouched(IntPoint(x, y))
                    if (result.key) {
                        return Pair(pe, result.value)
                    }
                }
            }
            return null
        }

        private fun getRouteBtn(p: IntPoint): PanelElement? {
            for (pe in panelElements) {
                if (pe.gpe is RouteButton) {
                    val result = pe.gpe.isTouched(p)
                    if (result.key) {
                        return pe
                    }

                }
            }
            return null
        }

        private fun isPETypeOn(poi: IntPoint): Boolean {
            var found = false
            for (pe in panelElements) {
                if (Math.abs(poi.x - pe.gpe.x) <= 1 && Math.abs(poi.y - pe.gpe.y) <= 1) {
                    found = true
                }
            }
            return found
        }

        private fun checkSelection() {
            if (PanelElement.atLeastOneSelected()) {
                btnDelete.isDisable = false
                btnMove.isDisable = false
            } else {
                btnDelete.isDisable = true
                btnMove.isDisable = true
            }
        }

        private fun drawAddresses(gc: GraphicsContext) {
            gc.clearRect(0.0, 0.0, RECT_X.toDouble(), RECT_Y.toDouble())
            gc.lineWidth = 1.0
            gc.stroke = Color.BLUE
            gc.fill = Color.LIGHTBLUE
            for (pe in panelElements) {
                pe.drawAddress(gc)
            }
        }
    }
}

