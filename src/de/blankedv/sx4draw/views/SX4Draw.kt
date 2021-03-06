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

import de.blankedv.sx4draw.Constants
import de.blankedv.sx4draw.Constants.DEBUG
import de.blankedv.sx4draw.Constants.INVALID_INT
import de.blankedv.sx4draw.Constants.PEState
import de.blankedv.sx4draw.Constants.RASTER
import de.blankedv.sx4draw.Constants.RECT_X
import de.blankedv.sx4draw.Constants.RECT_Y
import de.blankedv.sx4draw.Constants.progVersion
import de.blankedv.sx4draw.GenericAddress
import de.blankedv.sx4draw.PanelElement
import de.blankedv.sx4draw.PanelElement.Companion.SENSOR_WIDTH
import de.blankedv.sx4draw.PanelElement.Companion.TRACK_WIDTH
import de.blankedv.sx4draw.config.LayoutConfig
import de.blankedv.sx4draw.config.PanelConfig
import de.blankedv.sx4draw.config.ReadConfig
import de.blankedv.sx4draw.config.WriteConfig
import de.blankedv.sx4draw.model.*
import de.blankedv.sx4draw.util.Calc
import de.blankedv.sx4draw.util.Utils
import de.blankedv.sx4draw.util.ZoomableScrollPane
import javafx.application.Application
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.geometry.Orientation
import javafx.scene.Cursor
import javafx.scene.Group
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
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import javafx.scene.shape.StrokeLineCap
import javafx.stage.FileChooser
import javafx.stage.FileChooser.ExtensionFilter
import javafx.stage.Stage
import javafx.util.Pair
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.prefs.Preferences
import kotlin.system.exitProcess


open class SX4Draw : Application() {

    // FIXED: String replace funktioniert nicht für Weichen-Adressenänderung (werte unter 11 = SXMIN_USED ausgeschlossen)

    // FIXED Fahrstraßenanzeige löschen, wenn neue Fahrst. erstellt wird

    // TODO UNDO für ca. mehrere Panel Elemente
    // TODO bug sensoren umbenennen  901-991 => ersetzt auch 1901 => 1991  ??
    // ';' bzw ',' voranstellen bei routes/sensors und dann ;333, ersetzen durch ;1234, und so weiter..
    // TODO  zusammengesetzte Fahrstrasse graphisch aufsetzen
    //      welche fahrstrassen gibt es? welche fehlen noch?

    // ?? Fahrplan zusammenklicken von "Bubble" zu "Bubble" (+Lok/V/Ri) ohne vorherige Fahrstrassendefinition ??


    private val lineGroup  = Group()
    private val draggedGroup = Group()
    //private var raster: Group? = null
    private val canvas =  Canvas(RECT_X.toDouble(), RECT_Y.toDouble()) // The canvas on which the image is drawn.
    private val gc = canvas.graphicsContext2D  // The graphics context for the canvas.
    private val vb = VBox(3.0)    // Build the VBox container for the lineBox, canvas, and toolBox

    private var routingTable: RoutesTable? = null
    private var currentRoute: Route? = null
    private var currentCompRoute: CompRoute? = null
    private var locosTable: LocosTable? = null
    private var compRoutesTable: CompRoutesTable? = null
    private var tripsTable: TripsTable? = null
    private var timetableTable: TimetableTable? = null
    private var currentTimeTable: Timetable? = null
    private var currentTrip: Trip? = null

    private var line: Line? = null

    private val dispAddresses = CheckMenuItem("Adressen anzeigen")
    private val rasterOn = CheckMenuItem("Raster")

    private val anchorPane = AnchorPane()
    private val scPane = ZoomableScrollPane(anchorPane)
    private val status = Label("status")


    private var currentGUIState = GUIState.SELECT
    private val moveStart = IntPoint()
    private var currentFileName = ""

    private val application = this

    private val btnUndo = Button()
    private val btnAddTrack = ToggleButton()
    private val btnAddSensor = ToggleButton()
    private val btnAddSensorUS = ToggleButton()
    private val btnAddSignal = ToggleButton()
    private val btnRouteBtn = ToggleButton()
    private val btnAddRoute = ToggleButton()
    private val btnAddCompRoute = ToggleButton()
    private val btnAddTimeTable = ToggleButton()
    private val btnSelect = ToggleButton()
    private val btnUnSelect = Button()
    private val btnMove = ToggleButton()
    private val btnDelete = Button()
    private val toggleGroup = ToggleGroup()

    private val resLoc = "/de/blankedv/sx4draw/resources/"

    private var dest = ArrayList<Int>()
    private var currentTripStartBtn = INVALID_INT

    enum class GUIState {
        ADD_TRACK, ADD_SENSOR, ADD_SENSOR_US, ADD_SIGNAL, ADD_ROUTEBTN, ADD_ROUTE, ADD_COMPROUTE, ADD_TIMETABLE, SELECT, MOVE
    }

    override fun start(primaryStage: Stage) {
        primaryStage.title = "Panel XML File Creator (Name: ???, Type: ???)"
        primaryStage.icons.add(Image(resLoc +"sx4_draw_ico64.png"))

        val prefs = Preferences.userNodeForPackage(this.javaClass)

        val scene = Scene(vb, 1300.0, 660.0)

        // A group to hold all the drawn shapes
        
        lineGroup.onMousePressed = EventHandler { me ->
            val poi = IntPoint(me.x, me.y)
            println("linegroup mouse pressed x=" + poi.x + " y=" + poi.y + " currentGUIState=" + currentGUIState.name + " src=" + me.source)
            if (me.button == MouseButton.PRIMARY) {
                println("prim btn")
                when (currentGUIState) {
                    GUIState.SELECT ->
                        toggleSelectionPE(poi)
                    GUIState.MOVE -> {
                        moveStart.x = me.x.toInt()
                        moveStart.y = me.y.toInt()
                        println("moveStart")
                        addToDraggedGroup()
                    }
                    else -> { /* do nothing */
                    }
                }
            } else if (me.button == MouseButton.SECONDARY) {
                println("sec btn")
                editPanelElement(poi, primaryStage)
            }
        }

        val buttons = createButtonBar()

        val menuBar = MenuBar()
        createMenu(prefs, menuBar, primaryStage)

        anchorPane.addEventHandler(KeyEvent.KEY_PRESSED) { t ->
            if (t.code == KeyCode.ESCAPE) {
                println("click on escape")
                if ((currentGUIState == GUIState.ADD_ROUTE) or (currentGUIState == GUIState.ADD_COMPROUTE)) {
                    // abbruch
                    for (sel in panelElements) {
                        sel.createShapeAndSetState(Constants.PEState.DEFAULT)
                    }
                    currentRoute = null // reset
                    currentCompRoute = null
                } else if (currentGUIState == GUIState.ADD_TIMETABLE) {
                    // abbruch
                    for (sel in panelElements) {
                        sel.createShapeAndSetState(Constants.PEState.DEFAULT)
                    }
                    currentTimeTable = null // reset
                    }
            }
        }

        // Build the canvas
        //System.out.println("size rect: x=" + (scene.getWidth()) + " y=" + (scene.getHeight() - 230));
        canvas.cursor = Cursor.DEFAULT
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED) { me ->
            val poi = IntPoint(me.x, me.y)
            start = IntPoint.toRaster(poi, getRasterValue())
            println("canvas mouse pressed x=" + poi.x + " y=" + poi.y + " currentGUIState=" + currentGUIState.name)
            if (me.button == MouseButton.PRIMARY) {
                println("primary button")
                val poiRast = IntPoint.toRaster(poi, getRasterValue())
                when (currentGUIState) {
                    GUIState.ADD_TRACK -> line = startNewLine(start, TRACK_WIDTH)
                    GUIState.ADD_SIGNAL -> {
                        if (!isPanelElementAlreadyOnPoint(poiRast)) {
                            val pe = PanelElement(Signal(poiRast))
                            addNewPanelElement(pe)
                        }
                    }
                    GUIState.ADD_ROUTEBTN -> {
                        if (!isPanelElementAlreadyOnPoint(poiRast)) {
                            val pe = PanelElement(RouteButton(poiRast))
                            addNewPanelElement(pe)
                        }
                    }
                    GUIState.ADD_SENSOR_US -> {
                        if (!isPanelElementAlreadyOnPoint(poiRast)) {
                            val pe = PanelElement(Sensor(poiRast))
                            addNewPanelElement(pe)
                        }
                    }
                    GUIState.ADD_ROUTE -> createRoute(poi)
                    GUIState.ADD_COMPROUTE -> createCompRoute(poi)
                    GUIState.ADD_TIMETABLE -> createTimeTable(poi)
                    GUIState.ADD_SENSOR -> {
                        line = startNewLine(start, SENSOR_WIDTH)
                        line!!.strokeDashArray.addAll(6.0, 12.0)
                        line!!.stroke = Color.YELLOW
                    }
                    GUIState.SELECT ->
                        // TODO make selection where ROUTEB has higher prio than SENSOR has higher prio than TRACK
                        // avoiding use of extra keys for selection !!!
                        //if (me.isControlDown()) {
                        //    toggleSelectionPENotTrack(poi);
                        //} else if (me.isShiftDown()) {
                        //    toggleSelectionPENotTrackNotSensor(poi);
                        //} else {
                        toggleSelectionPE(poi)

                    GUIState.MOVE -> {
                        val ms = IntPoint.toRaster(poi, getRasterValue())
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


        canvas.onMouseReleased = EventHandler { me ->
            // keep shapes within rectangle
            if (canvas.boundsInLocal.contains(me.x, me.y)) {
                val poi = IntPoint(me.x, me.y)
                println("end approx x=" + poi.x + " y=" + poi.y)
                val end = IntPoint.correctAngle(start, poi, getRasterValue())
                println("end x=" + end.x + " y=" + end.y)
                when (currentGUIState) {
                    SX4Draw.GUIState.ADD_TRACK,
                    SX4Draw.GUIState.ADD_SENSOR -> {
                        line!!.endX = end.x.toDouble()
                        line!!.endY = end.y.toDouble()
                        lineGroup.children.remove(line)  // will be re-added from within PE
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

                            lineGroup.children.add(lastPE!!.shape)
                            btnUndo.isDisable = false
                            redrawPanelElements()
                        }
                    }

                    SX4Draw.GUIState.ADD_SENSOR_US, SX4Draw.GUIState.ADD_SIGNAL,
                    SX4Draw.GUIState.ADD_ROUTEBTN, SX4Draw.GUIState.ADD_ROUTE -> {
                    }
                    SX4Draw.GUIState.MOVE    // MOVE ends
                    -> {
                        val d = IntPoint.delta(moveStart, poi, getRasterValue())
                        println("move END: final delta =" + d.x + "," + d.y)
                        PanelElement.moveSelected(d)
                        draggedGroup.children.clear()
                        resetPEStates()
                        redrawPanelElements()
                    }
                    else -> { /*do nothing */
                    }
                }
            }
        }

        canvas.onMouseDragged = EventHandler { me ->
            if (canvas.boundsInLocal.contains(me.x, me.y)) {
                val poi = IntPoint(me.x, me.y)
                when (currentGUIState) {
                    SX4Draw.GUIState.ADD_TRACK, SX4Draw.GUIState.ADD_SENSOR -> {
                        lineGroup.children.remove(line)
                        val mp = IntPoint.correctAngle(start, poi, getRasterValue())
                        line!!.endX = mp.x.toDouble()
                        line!!.endY = mp.y.toDouble()
                        //System.out.println("x=" + (int)me.getX() + " y=" + (int)me.getY());
                        if (!lineGroup.children.contains(line)) {
                            lineGroup.children.add(line)
                        }
                    }

                    SX4Draw.GUIState.ADD_SIGNAL -> {
                    }
                    SX4Draw.GUIState.MOVE  // MOVE dragging
                    -> {
                        val d = IntPoint.delta(moveStart, poi, getRasterValue())
                        //System.out.println("delta =" + d.x + "," + d.y);
                        draggedGroup.translateX = d.x.toDouble()
                        draggedGroup.translateY = d.y.toDouble()
                    }
                    else -> {
                        // do nothing
                    }
                }
            }
        }

        vb.children.addAll(menuBar, buttons, scPane, status);
        scPane.isFitToHeight = true
        scPane.isFitToWidth = true
        scPane.maxWidth = RECT_X.toDouble()
        scPane.maxHeight = RECT_Y.toDouble()
        vb.maxWidth = RECT_X.toDouble()
        vb.maxHeight = (RECT_Y + 40).toDouble()
        scPane.setPrefSize(RECT_X.toDouble(), RECT_Y.toDouble())

        VBox.setVgrow(scPane, Priority.ALWAYS)
        HBox.setHgrow(scPane, Priority.ALWAYS)

        anchorPane.children.addAll(lineGroup, /* raster, */ canvas, draggedGroup)
        
        drawRaster(gc)

        primaryStage.scene = scene

        primaryStage.show()

        primaryStage.setOnCloseRequest {
            saveOnExit(prefs, primaryStage)
            exitProcess(0)   // i.e. System.exit(0)
        }

        if (Utils.isOtherInstanceRunning()) {
            Dialogs.buildErrorAlert("ERROR",
                    "eine andere Instance von SX4Draw läuft bereits!","Programm wird jetzt beendet.")
            exitProcess(0)
        }

        status.text = getStatistics()

    }

    private fun addNewPanelElement(pe: PanelElement) {
        lineGroup.children.add(pe.shape)
        panelElements.add(pe)
        lastPE = pe
        btnUndo.isDisable = false
    }

    private fun createButtonBar(): HBox {

        val selIcon = ImageView(Image(resLoc + "select.png"))
        val plusIcon = ImageView(Image(resLoc + "plus.png"))
        val plusIcon1 = ImageView(Image(resLoc + "plus.png"))
        val plusIcon2 = ImageView(Image(resLoc + "plus.png"))
        val plusIcon3 = ImageView(Image(resLoc + "plus.png"))
        val plusIcon4 = ImageView(Image(resLoc + "plus.png"))
        val plusIcon5 = ImageView(Image(resLoc + "plus.png"))
        val plusIcon6 = ImageView(Image(resLoc + "plus.png"))
        val plusIcon7 = ImageView(Image(resLoc + "plus.png"))
        val delIcon = ImageView(Image(resLoc + "delete.png"))
        val moveIcon = ImageView(Image(resLoc + "move.png"))
        val undoIcon = ImageView(Image(resLoc + "undo.png"))

        val sep1 = Separator(Orientation.VERTICAL)
        val sep2 = Separator(Orientation.VERTICAL)

        val btnsBox = HBox(3.0)
        btnsBox.children.addAll(btnSelect, btnUnSelect, sep1, btnAddTrack, btnAddSensor, btnAddSensorUS,
                btnAddSignal, btnRouteBtn, btnAddRoute, /** btnAddCompRoute, **/ btnAddTimeTable, sep2, btnUndo, btnDelete, btnMove)

        btnSelect.toggleGroup = toggleGroup
        btnSelect.isSelected = true
        btnMove.isSelected = false
        btnSelect.text = "Select"
        btnSelect.graphic = selIcon
        btnSelect.setOnAction { enterSelectState() }

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
            canvas.cursor = Cursor.CLOSED_HAND
        }

        btnAddTrack.toggleGroup = toggleGroup
        btnAddTrack.text = "Track"
        btnAddTrack.graphic = plusIcon1
        btnAddTrack.setOnAction {
            currentGUIState = GUIState.ADD_TRACK
            canvas.cursor = Cursor.CROSSHAIR
            //resetLines();
        }

        btnAddSensor.toggleGroup = toggleGroup
        btnAddSensor.text = "Sensor"
        btnAddSensor.graphic = plusIcon2
        btnAddSensor.setOnAction {
            currentGUIState = GUIState.ADD_SENSOR
            canvas.cursor = Cursor.CROSSHAIR
            //resetLines();
        }

        btnAddSensorUS.toggleGroup = toggleGroup
        btnAddSensorUS.text = "Sensor-US"
        btnAddSensorUS.graphic = plusIcon4
        btnAddSensorUS.setOnAction {
            currentGUIState = GUIState.ADD_SENSOR_US
            canvas.cursor = Cursor.CROSSHAIR
            //resetLines();
        }

        btnAddSignal.toggleGroup = toggleGroup
        btnAddSignal.text = "Signal"
        btnAddSignal.graphic = plusIcon
        btnAddSignal.setOnAction {
            currentGUIState = GUIState.ADD_SIGNAL
            canvas.cursor = Cursor.CROSSHAIR
            //resetLines();
        }

        btnRouteBtn.toggleGroup = toggleGroup
        btnRouteBtn.text = "RT-Button"
        btnRouteBtn.graphic = plusIcon3
        btnRouteBtn.setOnAction {
            currentGUIState = GUIState.ADD_ROUTEBTN
            canvas.cursor = Cursor.CROSSHAIR
            //resetLines();
        }

        btnAddRoute.toggleGroup = toggleGroup
        btnAddRoute.text = "Fahrstr."
        btnAddRoute.graphic = plusIcon5
        btnAddRoute.setOnAction {
            if (PanelElement.addressesAvail()) {
                currentGUIState = GUIState.ADD_ROUTE
                canvas.cursor = Cursor.CLOSED_HAND
                currentRoute = null
                PanelElement.resetState()   // unselect all PE
                redrawPanelElements()
            } else {

                val alert = Alert(AlertType.WARNING)
                alert.title = "Warnung"
                alert.headerText = "Es sind keine oder nur wenige Adressen eingegeben"
                alert.contentText = "Bitte Adressen definieren vor der Fahrstraßeneingabe!"
                alert.showAndWait()
                enterSelectState()
            }
        }

        btnAddCompRoute.toggleGroup = toggleGroup
        btnAddCompRoute.text = "Zus. Fahrstr."
        btnAddCompRoute.graphic = plusIcon6
        btnAddCompRoute.setOnAction {
            if (PanelElement.addressesAvail()) {
                currentGUIState = GUIState.ADD_COMPROUTE
                canvas.cursor = Cursor.CLOSED_HAND
                currentCompRoute = null
                PanelElement.resetState()    // unselect all PE
                redrawPanelElements()
            } else {

                val alert = Alert(AlertType.WARNING)
                alert.title = "Warnung"
                alert.headerText = "Es sind keine oder nur wenige Adressen eingegeben"
                alert.contentText = "Bitte Adressen definieren vor der Fahrstraßeneingabe!"
                alert.showAndWait()
                enterSelectState()
            }
        }

        btnAddTimeTable.toggleGroup = toggleGroup
        btnAddTimeTable.text = "Fahrplan"
        btnAddTimeTable.graphic = plusIcon7
        btnAddTimeTable.setOnAction {
            if (PanelElement.addressesAvail()) {
                currentGUIState = GUIState.ADD_TIMETABLE
                canvas.cursor = Cursor.CLOSED_HAND
                currentTimeTable = null
                PanelElement.resetState()    // unselect all PE
                redrawPanelElements()
            } else {
                val alert = Alert(AlertType.WARNING)
                alert.title = "Warnung"
                alert.headerText = "Es sind keine oder nur wenige Fahrstraßen eingegeben"
                alert.contentText = "Bitte Fahrstraßen erzeugen vor Eingabe eines Fahrplans!"
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
                lineGroup.children.remove(pe.shape)
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
                lineGroup.children.remove(l)
                panelElements.removeAt(toDelete)
                println("removed  #$toDelete")
                checkSelection()
            }

        }

        return btnsBox
    }

    private fun enterSelectState() {
        currentGUIState = GUIState.SELECT
        btnUndo.isDisable = true
        btnAddRoute.isSelected = false
        btnSelect.isSelected = true
        canvas.cursor = Cursor.DEFAULT
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
        //val ivInfo = ImageView(Image("info.png"))
        //val ivSX4generic = ImageView(Image("sx4_draw_ico32.png"))
        val howtoTimetable = ImageView(Image(resLoc + "howto-timetable.png"))
        val menuFile = Menu("File")
        val menuWindows = Menu("Fenster")
        val openRoutingTable = MenuItem("Fahrstraßen anzeigen")
        val openLocoTable = MenuItem("Loks anzeigen")
        val openCompRoutesTable = MenuItem("Zusammenges. Fahrstraßen anzeigen")
        val openTripsTable = MenuItem("Fahrten anzeigen")
        val openTimetableTable = MenuItem("Fahrpläne anzeigen")
        val openAllTables = MenuItem("alle Tabellen anzeigen")

        val menuOptions = Menu("Optionen")
        val setName = MenuItem("Set Panel-Name")
        val setType = MenuItem("Set LayoutType (SX/DCC)")

        val choices = ArrayList<String>()
        choices.add("SX")
        choices.add("DCC")


        val scale200 = MenuItem("Scale 200%")
        val scale50 = MenuItem("Scale 50%")

        val zoomIn = MenuItem("Zoom IN")
        val zoomOut = MenuItem("Zoom OUT")

        val menuCalc = Menu("Berechnen")
        val cTurnouts = MenuItem("Weichen berechnen")
        val cNormPositions = MenuItem("Start Position normieren auf (20,20)")
        val menuExtra = Menu("Extras")
        val cSearch = MenuItem("Suche nach Adressen")

        val menuHelp = Menu("Hilfe")
        val howtoTimetableItem = MenuItem("Wie erstelle ich einen Fahrplan?")

        val saveItem = MenuItem("Panel abspeichern")
        val openItem = MenuItem("Panel öffnen")
        val exitItem = MenuItem("Programm-Ende/Exit")

        menuFile.items.addAll(openItem, saveItem, exitItem)
        menuWindows.items.addAll(openRoutingTable, openCompRoutesTable, openTripsTable,
                openTimetableTable, openLocoTable, SeparatorMenuItem(), openAllTables)
        menuOptions.items.addAll(setName, setType, dispAddresses, rasterOn, zoomIn, zoomOut) //, showMousePos) //, showScrollBars)
        menuCalc.items.addAll(cTurnouts, cNormPositions, scale200, scale50)
        menuExtra.items.addAll(cSearch)

        rasterOn.isSelected = true
        dispAddresses.isSelected = false

        //showMousePos.isSelected = false

        exitItem.setOnAction {
            saveOnExit(prefs, stage)
            System.exit(0)

        }

        saveItem.setOnAction {
            //PanelElement.normPositions();
            redrawPanelElements()
            val path = prefs.get("directory", System.getProperty("user.home"))
            val lastPath = writeFile(stage, path)
            if (lastPath != null && !lastPath.isEmpty()) {
                prefs.put("directory", lastPath)
            }
            updateTitle(stage)
        }

        openItem.setOnAction {
            val path = prefs.get("directory", System.getProperty("user.home"))
            val lastPath = openFile(stage, path)
            if (lastPath != null && !lastPath.isEmpty()) {
                prefs.put("directory", lastPath)
            }
            updateTitle(stage)
        }

        setName.setOnAction {
            println("set panelName")
            val getName = TextInputDialog(panelName)
            getName.title = "Neuer Panel-Name"
            getName.headerText = null
            getName.contentText = "Panel-Name:"
            val result = getName.showAndWait()
            result.ifPresent { name ->
                panelName = name
                if (currentFileName.isEmpty()) {
                    currentFileName = "panel-"+panelName+".xml"
                }
                updateTitle(stage)
            }

        }

        setType.setOnAction {
            println("set Type (SX vs. DCC")
            val dialog = ChoiceDialog("Layout Typ", choices)
            dialog.title = "Typ DCC oder SX?"
            dialog.headerText = null
            dialog.contentText = "Bitte auswählen:"

            val result = dialog.showAndWait()

            result.ifPresent { type ->
                layoutType = type
                updateTitle(stage)
            }

        }

        rasterOn.setOnAction {
            println("raster toggle")
            redrawPanelElements()
        }

        scale200.setOnAction {
            println("scale 200%")
            PanelElement.scalePlus()
            redrawPanelElements()
        }

        scale50.setOnAction {

            println("scale 50%")
            PanelElement.scaleMinus()
            redrawPanelElements()
        }


        zoomIn.setOnAction {
            println("Zoom In")
            scPane.zoomIn()
        }

        zoomOut.setOnAction {
            println("Zoom Out")
            scPane.zoomOut()
        }


        dispAddresses.setOnAction {
            println("display addresses = " + dispAddresses.isSelected)
            redrawPanelElements()
        }

        cTurnouts.setOnAction {
            println("calc. turnouts")
            Calc.turnouts()
            redrawPanelElements()
        }
        cNormPositions.setOnAction {
            println("norm positions")

            PanelElement.normPositions()
            redrawPanelElements()
        }

        cSearch.setOnAction {
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

        openRoutingTable.setOnAction {
            println("openrouting table")
            if (routingTable == null) {
                routingTable = RoutesTable(stage, this)
            } else {
                routingTable!!.show()
            }
        }

        openTripsTable.setOnAction {
            println("open trips table")
            if (tripsTable == null) {
                tripsTable = TripsTable(stage, this)
            } else {
                tripsTable!!.show()
            }
        }

        openTimetableTable.setOnAction {
            println("open timetable table")
            if (timetableTable == null) {
                timetableTable = TimetableTable(stage, this)
            } else {
                timetableTable!!.show()
            }
        }

        openLocoTable.setOnAction {
            println("open locos table")
            if (locosTable == null) {
                locosTable = LocosTable(stage, this)
            } else {
                locosTable!!.show()
            }
        }

        openCompRoutesTable.setOnAction {
            println("open compRoutes table")
            if (compRoutesTable == null) {
                compRoutesTable = CompRoutesTable(stage, this)
            } else {
                compRoutesTable!!.show()
            }
        }

        openAllTables.setOnAction {
            println("open all tables")
            if (routingTable == null) {
                routingTable = RoutesTable(stage, this)
            } else {
                routingTable!!.show()
            }
            if (compRoutesTable == null) {
                compRoutesTable = CompRoutesTable(stage, this)
            } else {
                compRoutesTable!!.show()
            }
            if (tripsTable == null) {
                tripsTable = TripsTable(stage, this)
            } else {
                tripsTable!!.show()
            }
            if (timetableTable == null) {
                timetableTable = TimetableTable(stage, this)
            } else {
                timetableTable!!.show()
            }
            if (locosTable == null) {
                locosTable = LocosTable(stage, this)
            } else {
                locosTable!!.show()
            }
        }

        openCompRoutesTable.setOnAction {
            println("open compRoutes table")
            if (compRoutesTable == null) {
                compRoutesTable = CompRoutesTable(stage, this)
            } else {
                compRoutesTable!!.show()
            }
        }

        howtoTimetableItem.setOnAction {
            println("howto Timetable clicked")
            Dialogs.buildInformationAlert("Wie erstelle ich einen Fahrplan?", "",
                    "", howtoTimetable)
        }
        val infoItem = MenuItem("Version Info")
        val updateItem = MenuItem("Sind Updates verfügbar?")

        infoItem.setOnAction {
            println("info clicked")
            Dialogs.buildInfoAlertOpenSX("Info", "SX4Draw\nhttps://opensx.net/sx4 ", "Programm Version:$progVersion", this)
        }

        updateItem.setOnAction {
            Utils.checkVersion(application)
        }

        val manualItem = MenuItem("Handbuch")
        manualItem.setOnAction {
            //val fileName = "index.html"
            //val classLoader = SX4Draw::class.java.getClassLoader()
           // val file = File(classLoader.getResource(fileName)!!.getFile())
            Dialogs.showManual(stage)
            println("open manual")
           /* try {
                hostServices.showDocument(DOCU_URL)
            } catch (e: Exception) {
                println(e.message)
            } */
        }

        menuHelp.items.addAll(howtoTimetableItem, manualItem, SeparatorMenuItem(),infoItem, updateItem)
        menuBar.menus.addAll(menuFile, menuOptions, menuCalc, menuExtra, menuWindows, menuHelp)
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
        var invert = false
        if (tu.inv == 1) invert = true
        println("IN: addr=" + tu.adr + " inv=" + invert)
        val addrIN = GenericAddress(tu.adr, INVALID_INT, inv = invert)
        val result = AddressDialog.open(pe, primStage, addrIN)
        if (result.addr != -1) {
            println("OUT: addr=" + result.addr + " addr2=" + result.addr2 + " inv=" + result.inv + " orient=" + result.orient)
            val addressOK = Dialogs.checkAddress(addrIN, result)
            if (!addressOK) {
                println("addressOK=false")
                return  // do nothing
            }
            tu.adr = result.addr
            if (!result.inv) {
                tu.inv = null
            } else {
                tu.inv = 1
            }
            if (result.reNumber) Route.addressInRouteChanged(addrIN.addr.toString(), tu.adr.toString())
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
        println("IN: orient=$orientation")
        val addrIN = GenericAddress(si.getAddr(), si.getAddr2(), orient = orientation)
        val result = AddressDialog.open(pe, primStage, addrIN)
        if (result.addr != -1) {
            println("OUT: addr=" + result.addr + " addr2=" + result.addr2 + " inv=" + result.inv + " orient=" + result.orient)
            val addressOK = Dialogs.checkAddress(addrIN, result)
            if (!addressOK) {
                println("addressOK=false")
                return  // do nothing
            }
            if (result.addr2 != INVALID_INT) {
                si.adrStr = "" + result.addr + "," + (result.addr + 1)
            } else {
                si.adrStr = "" + result.addr
            }
            if (result.reNumber) Route.addressInRouteChanged(addrIN.addr.toString(), result.addr.toString())
            val delta = Utils.signalOrientToDXY2(result.orient)
            println("or=" + result.orient + " dx=" + delta.x + " dy=" + delta.y)
            si.x2 = si.x + delta.x
            si.y2 = si.y + delta.y
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
        val addrIN = GenericAddress(se.getAddr(), se.getAddr2())
        val result = AddressDialog.open(pe, primStage, addrIN)
        if (result.addr != -1) {
            println("OUT: addr=" + result.addr + " addr2=" + result.addr2 + " inv=" + result.inv + " orient=" + result.orient)
            val addressOK = Dialogs.checkAddress(addrIN, result)
            if (!addressOK) {
                println("addressOK=false")
                return  // do nothing
            }
            if (result.addr2 != INVALID_INT) {
                se.adrStr = "" + result.addr + "," + result.addr2
            } else {
                se.adrStr = "" + result.addr
            }
            if (result.reNumber) Route.sensorAddressChanged(addrIN.addr.toString(), result.addr.toString())
            redrawPanelElements()
        } else {
            println("no address selected")
        }
    }

    private fun editPanelElement(poi: IntPoint, primStage: Stage) {
        val pe = selectedPENotTrack(poi.x.toDouble(), poi.y.toDouble())
        // all panel elements except tracks have an address

        if (pe != null) {
            println("editing pe =" + pe::class.simpleName)
            when (pe.gpe) {
                is RouteButton -> {
                    if (isDisplayRouteButtons) {
                        // clear route buttons
                        redrawPanelElements()
                        isDisplayRouteButtons = false
                    } else {
                        isDisplayRouteButtons = true
                        val rtb = pe.gpe as RouteButton
                        dispRouteEndButtons(rtb)
                    }
                }
                is Turnout -> editTurnout(pe, primStage)
                is Signal -> editSignal(pe, primStage)
                is Sensor -> editSensor(pe, primStage)
            }
        } else {
            println("no panel element found at " + poi.x + "/" + poi.y)
        }
    }

    private fun drawRTButtons(btn1: Int, btn2: Int) {
        val bt1 = PanelElement.getPeByAddress(btn1)[0]
        val bt2 = PanelElement.getPeByAddress(btn2)[0]

        gc.strokeText("1", (bt1.gpe.x - 4).toDouble(), (bt1.gpe.y + 4).toDouble())
        gc.strokeText("2", (bt2.gpe.x - 4).toDouble(), (bt2.gpe.y + 4).toDouble())
    }

    private fun redrawPanelElements() {
        if (DEBUG) {
            println("redrawPES() nPE=" + panelElements.size)
        }
        gc.clearRect(0.0, 0.0, RECT_X.toDouble(), RECT_Y.toDouble())

        if (rasterOn.isSelected) {
            drawRaster(gc)
        }
        
        if (dispAddresses.isSelected) {
            drawAddresses(gc)
        }

        lineGroup.children.clear()
        panelElements.sort()   // route buttons on top of sensors, sensors on top of track, etc ...
        for (pe in panelElements) {
            lineGroup.children.add(pe.shape)
            //System.out.println("drawing PE at " + pe.getX() + "," + pe.getY() + " type=" + pe.getType()
            //        + " state="+pe.getState().name() + " fillC=" + pe.getShape().getFill());
        }
        status.text = getStatistics()
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
        canvas.cursor = Cursor.DEFAULT

        btnMove.isDisable = true
        btnUndo.isDisable = true
        btnDelete.isDisable = true
        btnMove.isSelected = false

        for (sel in panelElements) {
            sel.createShapeAndSetState(Constants.PEState.DEFAULT)
        }
    }

    private fun createRoute(poi: IntPoint) {  // primaryStage: Stage) {
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
                    //PopupChoice.open(primaryStage)
                    // add to route and mark
                    // for signals: change desired state in route by clicking until state reached
                    if (peSt.key.gpe is Signal) {
                        if ((lastSignalAddr == 0) || (peSt.key.gpe.getAddr() != lastSignalAddr)) {
                            // hit for the first time, create new signal and set to 1=green
                            currentRoute!!.addElement(Pair(peSt.key, 1))
                            lastSignalAddr = peSt.key.gpe.getAddr()
                            lastSignalState = 1
                            peSt.key.createShapeAndSetState(PEState.STATE_1)
                        } else {
                            // editing existing signal
                            val newState = currentRoute!!.incrementSignalState(peSt.key)
                            peSt.key.state = newState
                            peSt.key.setColorFromState()
                        }
                    } else {
                        lastSignalAddr = 0
                        currentRoute!!.addElement(peSt)
                        peSt.key.createShapeAndSetState(PEState.MARKED)
                    }

                    redrawPanelElements()
                }
            } else {
                // creation finished - a end route button has been selected
                currentRoute!!.btn2 = rtbtn.gpe.getAddr()
                rtbtn.createShapeAndSetState(PEState.MARKED)
                var crt = currentRoute
                crt!!.uniqueAccessories()  // remove double turnout addresses
                showRoute(crt)
                println("btn2 =" + crt.btn2)
                routes.add(crt.copy())  // add a new route from btn1 to btn2
                routes.add(crt.reverseRoute())  // add reverse route also

                val alert = Alert(AlertType.INFORMATION)
                alert.title = "Fahrstraße " + currentRoute!!.adr
                alert.headerText = null
                alert.contentText = "Die Fahrstraße " + currentRoute!!.adr +" ist abgeschlossen."
                alert.showAndWait()

                resetPEStates()
                redrawPanelElements()
                currentRoute = null // reset
            }
        }
    }

    private fun createCompRoute(poi: IntPoint) {
        val rtbtn = getRouteBtn(poi)
        if (currentCompRoute == null) {
            // initialize new route
            currentCompRoute = CompRoute(CompRoute.getAutoAddress())
            println("init route with adr=" + currentCompRoute!!.adr)
            if (rtbtn != null) {  // first button
                currentCompRoute!!.btn1 = rtbtn.gpe.getAddr()
                rtbtn.createShapeAndSetState(PEState.MARKED)
                redrawPanelElements()
                println("btn1 =" + currentCompRoute!!.btn1)
                //gc.strokeText("1", rtbtn.x - 4, rtbtn.y - YOFF + 4);
            } else {
                currentCompRoute = null // no routebtn at this point
            }
        } else {
            if (rtbtn != null) {
                // creation finished - an end route button has been selected
                currentCompRoute!!.btn2 = rtbtn.gpe.getAddr()
                println("btn2 =" + currentCompRoute!!.btn2)
                // check if there is such a compound route
                val foundRoutes: String? = CompRoute.findCompRoute(currentCompRoute!!.btn1, currentCompRoute!!.btn2)
                if (foundRoutes == null) {
                    // no such compound Route
                    val alert = Alert(AlertType.INFORMATION)
                    alert.title = "Zusammenges. Fahrstraße " + currentCompRoute!!.adr + " ?"
                    alert.headerText = null
                    alert.contentText = "keine passende Fahrstraßenkombination vorhanden"
                    alert.showAndWait()
                } else {
                    currentCompRoute!!.routes = foundRoutes
                    rtbtn.createShapeAndSetState(PEState.MARKED)
                    val compRt = currentCompRoute
                    showCompRoute(compRt!!)

                    compRoutes.add(CompRoute(currentCompRoute!!))  // add a new route from btn1 to btn2
                    val revCompRoute = (currentCompRoute!!).reverseCompRoute()
                    compRoutes.add(revCompRoute)

                    val alert = Alert(AlertType.INFORMATION)
                    alert.title = "Zusammenges. Fahrstraße " + currentCompRoute!!.adr
                    alert.headerText = null
                    alert.contentText = " erstellt! - Routen: " + currentCompRoute!!.routes
                    alert.showAndWait()
                }
                resetPEStates()
                redrawPanelElements()
                currentCompRoute = null // reset
            }
        }
    }

    private fun createTimeTable(poi: IntPoint) {  // primaryStage: Stage) {
        val rtBtn = getRouteBtn(poi)
        if (currentTimeTable == null) {
            // initialize new route
            currentTimeTable = Timetable()
            println("init new timetable with adr=" + currentTimeTable!!.adr)
            if (rtBtn != null) {  // first button
                currentTrip = Trip(Trip.getUnusedAddress())
                val sensor = rtBtn.getSensor()    // TODO rework sensors check .... can it be done later ???
                if (sensor == null) {
                    println("no sensor at this point")
                    currentTrip = null
                    dest.clear()
                    currentTripStartBtn = INVALID_INT
                } else {
                    currentTripStartBtn = rtBtn.gpe.getAddr()
                    currentTrip!!.sens1 = sensor.getAddr()
                    rtBtn.createShapeAndSetState(PEState.MARKED)
                    redrawPanelElements()
                    val rtb = rtBtn.gpe as RouteButton
                    dest = dispRouteEndButtons(rtb)
                    println("btn1=" + rtBtn.gpe.getAddr()+"  sens1="+currentTrip!!.sens1)
                    //gc.strokeText("1", rtbtn.x - 4, rtbtn.y - YOFF + 4);
                }
            } else {
                currentTrip = null // no routebtn at this point
                dest.clear()
                currentTripStartBtn = INVALID_INT
            }
        }  else {
            // check if have hit one of the possible destinations
            var validEndpoint = false
            for (a in dest) {
                if (rtBtn!!.gpe.getAddr() == a) {
                    val endSensor = rtBtn.getSensor()
                    if (endSensor == null) {
                        println("no sensor at this point")
                        return
                    } else {
                        currentTrip!!.sens2 = endSensor.getAddr()
                        // find corresponding route
                        for (rt in routes.filter{ it -> it.btn1 == currentTripStartBtn}) {
                            if (rt.btn2 == a) {
                                println("found a route!")
                                showRoute(rt)
                                // TODO add Loco etc
                                // TODO add to timetables table
                                return
                            }
                        }
                    }
                }
            }
            /*
                currentRoute!!.btn2 = rtbtn.gpe.getAddr()
                rtbtn.createShapeAndSetState(PEState.MARKED)
                var crt = currentRoute
                crt!!.uniqueAccessories()  // remove double turnout addresses
                showRoute(crt)
                println("btn2 =" + crt.btn2)
                routes.add(crt.copy())  // add a new route from btn1 to btn2
                routes.add(crt.reverseRoute())  // add reverse route also

                val alert = Alert(AlertType.INFORMATION)
                alert.title = "Fahrstraße " + currentRoute!!.adr
                alert.headerText = null
                alert.contentText = "Die Fahrstraße " + currentRoute!!.adr +" ist abgeschlossen."
                alert.showAndWait()

                resetPEStates()
                redrawPanelElements()
                currentRoute = null // reset
            */
        }
    }

    private fun writeFile(stage: Stage, path: String): String? {

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
            fileChooser.initialFileName = "panel-"+panelName+".xml"
        } else {
            fileChooser.initialFileName = currentFileName
        }
        fileChooser.title = "Panel XML File Speichern"
        fileChooser.extensionFilters.addAll(
                ExtensionFilter("Panel Files", "panel*.xml*"))
        val selectedFile = fileChooser.showSaveDialog(stage)
        if (selectedFile != null) {
            var fn = selectedFile.toString()
            if (!fn.endsWith(".xml")) {
                fn = "$fn.xml"
            }
            prepareLayoutConfigAndWrite(fn)
            currentFileName = selectedFile.name
            return selectedFile.parent
        } else {
            return ""
        }

    }

    private fun prepareLayoutConfigAndWrite(fn: String) {

        val df = SimpleDateFormat("yyyyMMdd_HHmmss")
        val version = df.format(Date())
        System.out.println("creating Config, filename=$fn panelName=$panelName")

        val panelConfig = PanelConfig(panelName,
                ArrayList(locos),
                panelElements, ArrayList(routes),
                ArrayList(compRoutes),
                ArrayList(trips), ArrayList(timetables))
        val shortFN = File(fn).name
        val layoutConfig =  LayoutConfig(shortFN, version, layoutType, panelConfig)
        WriteConfig.toXML(fn, layoutConfig)
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
            gc.clearRect(0.0, 0.0, RECT_X.toDouble(), RECT_Y.toDouble())  // clear addresses labels, if any
            val layoutConfig = ReadConfig.fromXML(selectedFile.toString())
            if (layoutConfig != null) {
                currentFileName = selectedFile.name
                val version = layoutConfig.version
                layoutType = layoutConfig.type
                System.out.println("filename=$currentFileName version=$version")
                val panelConfig = layoutConfig.getPC0()   // first PanelConfig
                if (panelConfig != null) {
                    loadDataFromPanelConfig(panelConfig)
                } else {
                    println("ERROR: empty panelConfig in layoutConfig")
                }
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

    private fun loadDataFromPanelConfig(pc: PanelConfig) {
        // close open child windows
        routingTable?.close()
        routingTable = null
        locosTable?.close()
        locosTable = null
        compRoutesTable?.close()
        compRoutesTable = null
        tripsTable?.close()
        tripsTable = null
        timetableTable?.close()
        timetableTable = null
        // load data from panelconfig
        panelElements = pc.getAllPanelElements()
        panelName = pc.name
        locos = FXCollections.observableArrayList(pc.getAllLocos())
        //for (l in locos) println(l.toString())
        routes = FXCollections.observableArrayList(pc.getAllRoutes())
        compRoutes = FXCollections.observableArrayList(pc.getAllCompRoutes())
        trips = FXCollections.observableArrayList(pc.getAllTrips())
        //for (t in trips) println(t.toString())
        timetables = FXCollections.observableArrayList(pc.getAllTimetables())

    }

    private fun getRasterValue(): Int {
        return if (rasterOn.isSelected) {
            RASTER
        } else {
            1
        }
    }

    private fun drawRaster(gc2d: GraphicsContext) {
        gc2d.fill = Color.BLACK
        var i = 0
        while (i <= RECT_X + 100) {
            var j = 0
            while (j <= RECT_Y + 160) {
                gc2d.fillOval(i.toDouble()-0.25, j.toDouble()-0.25, 0.5, 0.5)
                j += RASTER * 2
            }
            i += RASTER * 2
        }
    }

    // mark end route button with a "2" and display route adr below
    // returns a list of addresses with the "2" possible end buttons
    private fun dispRouteEndButtons(rtBtn : RouteButton) : ArrayList<Int> {
        val destinations = ArrayList<Int>()
        gc.strokeText("1", (rtBtn.x - 4).toDouble(), (rtBtn.y + 4).toDouble())
        for (rt in routes.filter{ it -> it.btn1 == rtBtn.adr}) {
            val bt2 = PanelElement.getPeByAddress(rt.btn2)[0]
            destinations.add(bt2.gpe.getAddr())
            gc.strokeText("2", (bt2.gpe.x - 4).toDouble(), (bt2.gpe.y + 4).toDouble())
            //gc.fillRect((bt2.gpe.x - 12).toDouble(), (bt2.gpe.y + 16).toDouble(), (bt2.gpe.x + 12).toDouble(), (bt2.gpe.y + 16).toDouble())
            gc.strokeText("F"+rt.adr.toString(), (bt2.gpe.x - 18).toDouble(), (bt2.gpe.y + 18).toDouble())
        }
        for (crt in compRoutes.filter{ it -> it.btn1 == rtBtn.adr}) {
            val bt2 = PanelElement.getPeByAddress(crt.btn2)[0]
            gc.strokeText("2", (bt2.gpe.x - 4).toDouble(), (bt2.gpe.y + 4).toDouble())
            //gc.fillRect((bt2.gpe.x - 12).toDouble(), (bt2.gpe.y + 16).toDouble(), (bt2.gpe.x + 12).toDouble(), (bt2.gpe.y + 16).toDouble())
            gc.strokeText("F"+crt.adr.toString(), (bt2.gpe.x - 18).toDouble(), (bt2.gpe.y + 18).toDouble())
        }
        return destinations
    }

    fun showRoute(rt: Route?) {
        if (rt != null) {
            println("showRoute rt=${rt.adr}")
            rt.setRouteStates()
            redrawPanelElements()
            drawRTButtons(rt.btn1, rt.btn2)
        }
    }

    fun showCompRoute(compRt: CompRoute?) {
        val allRoutes = compRt!!.routes.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (allRoutes.isNotEmpty()) {
            for (i in 0..(allRoutes.size - 1)) {
                val rtStr = allRoutes[i]
                try {
                    val rtID = Integer.parseInt(rtStr)
                    val route = Route.getByAddress(rtID)
                    route?.setRouteStates()
                } catch (e: java.lang.NumberFormatException) {
                }
            }
        }
        redrawPanelElements()
        drawRTButtons(compRt.btn1, compRt.btn2)
    }

    fun hideAllRoutes() {
        PanelElement.resetState()
        redrawPanelElements()
    }

    private fun selectedPE(x: Double, y: Double): PanelElement? {
        // do the search from top element (SensorUS, RouteButton) to bottom (track)
        val peListRev = ArrayList(panelElements)
        peListRev.sortDescending()
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
        peListRev.sortDescending()
        // JAVA Collections.sort(peListRev, Collections.reverseOrder())
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
        peListRev.sortDescending()
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

    private fun isPanelElementAlreadyOnPoint(poi: IntPoint): Boolean {
        for (pe in panelElements) {
            if (Math.abs(poi.x - pe.gpe.x) <= 1 && Math.abs(poi.y - pe.gpe.y) <= 1) {
                return true
            }
        }
        return false
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
        gc.lineWidth = 1.0
        gc.stroke = Color.BLUE
        gc.fill = Color.LIGHTBLUE
        for (pe in panelElements) {
            pe.drawAddress(gc)
        }
    }

    private fun getStatistics() : String {
        var s = panelElements.size.toString() + " PEs  "
        s += routes.size.toString() + " Fahrstr.  "
        s += compRoutes.size.toString() + " zusFahrstr.  "
        s += trips.size.toString() + " Fahrten  "
        s += timetables.size.toString() + " Fahrpläne  "
        s += locos.size.toString() + " Loks/Züge"
        return s
    }

    private fun updateTitle(st : Stage) {
        st.title = "Create Panel (Name: $panelName Type: $layoutType) File: $currentFileName"
    }

    companion object {

        var locos: ObservableList<Loco> = FXCollections.observableArrayList<Loco>()
        var panelElements = ArrayList<PanelElement>()
        var routes: ObservableList<Route> = FXCollections.observableArrayList<Route>()
        var compRoutes: ObservableList<CompRoute> = FXCollections.observableArrayList<CompRoute>()
        var trips: ObservableList<Trip> = FXCollections.observableArrayList<Trip>()
        var timetables: ObservableList<Timetable> = FXCollections.observableArrayList<Timetable>()

        var lastPE: PanelElement? = null
        var panelName = ""
        var layoutType = "?"
        var lastSignalAddr : Int = 0
        var lastSignalState = 0

        var start = IntPoint(0, 0)

        var isDisplayRouteButtons = false

    }
}

