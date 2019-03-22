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

import de.blankedv.sx4draw.Constants.INVALID_INT
import de.blankedv.sx4draw.model.CompRoute
import de.blankedv.sx4draw.model.Route
import de.blankedv.sx4draw.model.Trip
import de.blankedv.sx4draw.views.SX4Draw.Companion.locos
import de.blankedv.sx4draw.views.SX4Draw.Companion.routes
import de.blankedv.sx4draw.views.SX4Draw.Companion.trips

import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import javafx.beans.binding.Bindings
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.util.StringConverter


/**
 * @author mblank
 */
class TripsTable internal constructor(primaryStage: Stage, private val app: SX4Draw) {

    internal var tripsTableScene: Scene
    // New window (Stage)
    internal var tripsWindow: Stage
    lateinit var pStage : Stage

    init {
        pStage = primaryStage
        val bp = BorderPane()
        /* HBox hb = new HBox(15);
        Button btnClose = new Button("close");
        hb.getChildren().addAll(btnClose);
        bp.setBottom(hb);
        hb.setAlignment(Pos.CENTER);
        BorderPane.setMargin(hb, new Insets(8, 8, 8, 8)); */
        val btnAddTrip = Button("+ NEUE Fahrt");
        val btnChangeLoco = Button("Zug ändern")
        tripsTableScene = Scene(bp, 720.0, 300.0)
        val vb = HBox(5.0)
        vb.children.addAll(btnAddTrip, btnChangeLoco)
        bp.top = vb
        bp.center = tableView

        // New window (Stage)
        tripsWindow = Stage()
        /* btnClose.setOnAction((e) -> {
            //sxAddress.addr = -1;
            locosWindow.close();
        }); */

        tripsWindow.title = "Fahrten (Trips) Tabelle"
        tripsWindow.scene = tripsTableScene

        // Specifies the modality for new window.
        //locosWindow.initModality(Modality.WINDOW_MODAL);
        // Specifies the owner Window (parent) for new window
        tripsWindow.initOwner(primaryStage)

        // Set position of second window, related to primary window.
        tripsWindow.x = primaryStage.x + 200
        tripsWindow.y = primaryStage.y + 150

        btnAddTrip.setOnAction {
            generateNewTrip()
        }

        btnChangeLoco.setOnAction {
            changeLocoInTrips(primaryStage)
        }

        show()

    }

    fun show() {
        createDataTables()
        tripsWindow.show()
    }

    fun close() {
        tripsWindow.close()
    }

    private fun generateNewTrip()  {

        if (routes.isEmpty()) {
            Dialogs.buildErrorAlert("keine Fahrstraßen vorhanden", "", "bitte erst Fahrstraßen erzeugen!")
            return
        } else if (locos.isEmpty()) {
            Dialogs.buildErrorAlert("keine Loks/Züge vorhanden", "", "bitte erst Loks/Züge definieren!")
            return
        } else {
            val newTrip = NewTripDialog.open(pStage)
            if (newTrip.adr != INVALID_INT) trips.add(newTrip)
        }
    }

    private fun changeLocoInTrips(stage: Stage) {
        val res = ChangeTrainDialog.open(stage)
        if (res.loco1 != INVALID_INT) {
            println("changing " + res.loco1 + ",*," + res.speed1 + " to " + res.loco2 + ",*," + res.speed2 + " invert Dir=" + res.invertDir)
            for (tr in trips.filter { it -> it.loco.substringBefore(',') == res.loco1.toString() }) {
                println("changing trip=" + tr.adr)
                if (res.invertDir) {
                    tr.loco = tr.loco.replace(res.loco1.toString() + ",1," + res.speed1, res.loco1.toString() + ",X," + res.speed1)   // temp
                    tr.loco = tr.loco.replace(res.loco1.toString() + ",0," + res.speed1, res.loco2.toString() + ",1," + res.speed2)
                    tr.loco = tr.loco.replace(res.loco1.toString() + ",X," + res.speed1, res.loco2.toString() + ",0," + res.speed2)
                } else {
                    tr.loco = tr.loco.replace(res.loco1.toString() + ",0," + res.speed1, res.loco2.toString() + ",0," + res.speed2)
                    tr.loco = tr.loco.replace(res.loco1.toString() + ",1," + res.speed1, res.loco2.toString() + ",1," + res.speed2)
                }
            }
            refresh()
        }
    }

    private fun createDataTables() {

        val adrCol = TableColumn<Trip, Int>("Adr (ID)")
        adrCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.10));
        val routeCol = TableColumn<Trip, Int>("Fahrstraße")
        routeCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.12));
        val sens1Col = TableColumn<Trip, Int>("Sensor 1")
        sens1Col.prefWidthProperty().bind(tableView.widthProperty().multiply(0.10));
        val sens2Col = TableColumn<Trip, Int>("Sensor 2")
        sens2Col.prefWidthProperty().bind(tableView.widthProperty().multiply(0.10));

        val startdelayCol = TableColumn<Trip, Int>("Startdelay[msec]")
        startdelayCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.17));
        val stopdelayCol = TableColumn<Trip, Int>("Stopdelay[msec]")
        stopdelayCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.16));
        val locoCol = TableColumn<Trip, String>("Zug (Adr,Dir,Geschw.)")
        locoCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.25));
        //   <trip adr="3100" route="2300" sens1="924" sens2="902" loco="29,1,126" stopdelay="1500" />


        /* final TextFormatter<String> formatter = new TextFormatter<String>(change -> {
            change.setText(change.getText().replaceAll("[^0-9.,]", ""));
            return change;

        }); */
        val myStringIntConverter = object : StringConverter<Int>() {
            override fun toString(`object`: Int?): String {
                return if (`object` == null) {
                    "-"
                } else "" + `object`
            }

            override fun fromString(string: String): Int? {
                try {
                    return Integer.parseInt(string)
                } catch (e: NumberFormatException) {
                    return null
                }

            }
        }

        tableView.columns.setAll(adrCol, routeCol, sens1Col, sens2Col, startdelayCol, stopdelayCol, locoCol)
        tableView.isEditable = true

        locoCol.cellFactory = TextFieldTableCell.forTableColumn()
        routeCol.cellFactory = TextFieldTableCell.forTableColumn(myStringIntConverter)
        startdelayCol.cellFactory = TextFieldTableCell.forTableColumn(myStringIntConverter)
        stopdelayCol.cellFactory = TextFieldTableCell.forTableColumn(myStringIntConverter)

        routeCol.setOnEditCommit { event: TableColumn.CellEditEvent<Trip, Int> ->
            val pos = event.tablePosition
            val newRouteAdr = event.newValue
            val row = pos.row
            val trip = event.tableView.items[row]

            // check if the number entered is the adr of an existing route
            val newRoute: Route? = Route.getByAddress(newRouteAdr)
            var newCompRoute: CompRoute? = CompRoute.getByAddress(newRouteAdr)
            if (newRoute != null) {
                trip.route = newRouteAdr
                val routeSensors = newRoute.sensors.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (routeSensors.size >= 2) {
                    trip.sens1 = routeSensors[0].toInt()
                    trip.sens2 = routeSensors[routeSensors.size - 1].toInt()
                } else {
                    trip.sens1 = INVALID_INT
                    trip.sens2 = INVALID_INT
                }
            } else if (newCompRoute != null) {
                trip.route = newRouteAdr  // new compRouteAddress
                trip.sens1 = newCompRoute.getStartSensor() ?: INVALID_INT
                trip.sens2 = newCompRoute.getEndSensor() ?: INVALID_INT
            } else {
                // NO CHANGE
                trip.sens1 = INVALID_INT
                trip.sens2 = INVALID_INT
                println("ERROR: start and end sensor not found for (comp)route=$newRouteAdr")
            }
            // this is a workaround for a bug in javafx, if not called, the row will not be repainted
            routeCol.isVisible = false
            routeCol.isVisible = true
        }

        locoCol.setOnEditCommit { event: TableColumn.CellEditEvent<Trip, String> ->
            val pos = event.tablePosition
            val newLocoVal = event.newValue
            val row = pos.row
            val trip = event.tableView.items[row]
            if (isValidLocoVal(newLocoVal)) {
                trip.loco = newLocoVal
            } else {
                locoCol.isVisible = false
                locoCol.isVisible = true
            }
        }

        startdelayCol.setOnEditCommit { event: TableColumn.CellEditEvent<Trip, Int> ->
            val pos = event.tablePosition
            val newStartdelay = event.newValue
            val row = pos.row
            val trip = event.tableView.items[row]
            if (newStartdelay != null) {
                if (newStartdelay in 0..60000) {
                    trip.startdelay = newStartdelay
                } else { // this is a workaround for a bug in javafx, value not repainted
                    startdelayCol.isVisible = false
                    startdelayCol.isVisible = true
                }
            } else {  // this is a workaround for a bug in javafx, value not repainted
                startdelayCol.isVisible = false
                startdelayCol.isVisible = true
            }
        }

        stopdelayCol.setOnEditCommit { event: TableColumn.CellEditEvent<Trip, Int> ->
            val pos = event.tablePosition
            val newStopdelay = event.newValue
            val row = pos.row
            val trip = event.tableView.items[row]
            if (newStopdelay != null) {
                if (newStopdelay in 0..60000) {
                    trip.stopdelay = newStopdelay
                } else { // this is a workaround for a bug in javafx, value not repainted
                    stopdelayCol.isVisible = false
                    stopdelayCol.isVisible = true
                }
            } else {  // this is a workaround for a bug in javafx, value not repainted
                stopdelayCol.isVisible = false
                stopdelayCol.isVisible = true
            }
        }

        //adrCol.setOnEditCommit( { ev -> (ev.tableView.items[ev.tablePosition.row] as Integer).adr = ev.newValue }

        /*massCol.setCellFactory(TextFieldTableCell.forTableColumn())
        massCol.setOnEditCommit { ev -> (ev.tableView.items[ev.tablePosition.row] as Integer).mass = ev.newValue }

        vmaxCol.setCellFactory(TextFieldTableCell.forTableColumn())
        vmaxCol.setOnEditCommit { ev -> (ev.tableView.items[ev.tablePosition.row] as Integer).vmax = ev.newValue } */


        tableView.isCenterShape = true
        tableView.setRowFactory { tv ->
            val row = TableRow<Trip>()
            val contextMenu = ContextMenu()
            val removeMenuItem = MenuItem("Fahrt löschen")
            removeMenuItem.onAction = EventHandler {
                tv.items.remove(row.item)
            }
            val newMenuItem = MenuItem("+ NEUE Fahrt")
            newMenuItem.onAction = EventHandler {
                generateNewTrip()
            }

            contextMenu.items.addAll(newMenuItem, removeMenuItem)

            // Set context menu on row, but use a binding to make it only show for non-empty rows:
            row.contextMenuProperty().bind(
                    Bindings.`when`(row.emptyProperty())
                            .then<ContextMenu>(null as ContextMenu?)
                            .otherwise(contextMenu)
            )
            row

        }

        adrCol.cellValueFactory = PropertyValueFactory("adr")
        routeCol.cellValueFactory = PropertyValueFactory("route")
        sens1Col.cellValueFactory = PropertyValueFactory("sens1")
        sens2Col.cellValueFactory = PropertyValueFactory("sens2")

        startdelayCol.cellValueFactory = PropertyValueFactory("startdelay")
        stopdelayCol.cellValueFactory = PropertyValueFactory("stopdelay")

        locoCol.cellValueFactory = PropertyValueFactory("loco")

        tableView.items = trips

        println("tripsTable " + tableView.items.size + " trips")

        // textField.setTextFormatter(formatter);
        // Utils.customResize(tableView)

    }

    private fun isValidLocoVal(s: String): Boolean {
        val spLoco = s.split(",")

        // check if we have 3 elements, 1. locoAddr, 2. dir, 3. speed
        if (spLoco.size != 3) return false

        // check if loco address is valid
        val locoAddr = spLoco[0].toInt()
        var found = false
        for (l in locos) {
            if (locoAddr == l.adr) found = true
        }
        if (found == false) {
            Dialogs.buildErrorAlert("loco string (Adr,Dir,V)", "", "Nicht erlaubt, die Lokadresse muss eine der Adressen aus der Loktabelle sein.")
            return false
        }

        // check if dir is valid
        if (!(spLoco[1].equals("0") or !spLoco[1].equals("1"))) {
            Dialogs.buildErrorAlert("loco string (Adr,Dir,V)", "", "Nicht erlaubt, die Fahrtrichtung muss 0 oder 1 sein.")
            return false
        }

        // check speed setting
        if ((spLoco[2].toInt() <= 0) or (spLoco[2].toInt() > 31)) {
            Dialogs.buildErrorAlert("loco string (Adr,Dir,V)", "", "Nicht erlaubt, die Geschwindigkeitsstufe muss zwischen 1 und 31 liegen.")

            return false
        }

        return true

    }

    companion object {

        private val tableView = TableView<Trip>()

        fun refresh() {
            tableView.refresh()
        }

    }

}
