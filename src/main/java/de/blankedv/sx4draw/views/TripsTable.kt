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
import de.blankedv.sx4draw.util.Utils
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
import javafx.util.StringConverter


/**
 * @author mblank
 */
class TripsTable internal constructor(primaryStage: Stage, private val app: SX4Draw) {

    internal var tripsTableScene: Scene
    // New window (Stage)
    internal var tripsWindow: Stage

    init {

        val bp = BorderPane()
        /* HBox hb = new HBox(15);
        Button btnClose = new Button("close");
        hb.getChildren().addAll(btnClose);
        bp.setBottom(hb);
        hb.setAlignment(Pos.CENTER);
        BorderPane.setMargin(hb, new Insets(8, 8, 8, 8)); */
        val btnAddTrip = Button("+ NEUE Fahrt");
        tripsTableScene = Scene(bp, 700.0, 300.0)
        bp.top = btnAddTrip
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

        show()

    }

    fun show() {

        createDataTables()
        tripsWindow.show()
    }

    fun close() {
        tripsWindow.close()
    }

    private fun generateNewTrip(): Trip? {
        val addr = Trip.getUnusedAddress()
        if (routes.isNotEmpty()) {
            val r0 = routes[0]
            var newTrip = Trip(addr, r0.adr, r0.findSens1(), r0.findSens2(), "29,1,30", 1500)
            trips.add(newTrip)
            return newTrip
        } else {
            Dialogs.buildErrorAlert("keine Fahrstraßen vorhanden", "", "bitte erst Fahrstraßen erzeugen!")
            return null
        }
    }

    private fun createDataTables() {

        val adrCol = TableColumn<Trip, Int>("Adr (ID)")
        val routeCol = TableColumn<Trip, Int>("Fahrstraße")
        val sens1Col = TableColumn<Trip, Int>("Sensor 1")
        val sens2Col = TableColumn<Trip, Int>("Sensor 2")
        val locoCol = TableColumn<Trip, String>("Loco (Adr,Dir,V)")
        locoCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.3));
        val stopdelayCol = TableColumn<Trip, Int>("Stopdelay[sec]")

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

        tableView.columns.setAll(adrCol, routeCol, sens1Col, sens2Col, locoCol, stopdelayCol)
        tableView.isEditable = true

        locoCol.cellFactory = TextFieldTableCell.forTableColumn();
        //adrCol.cellFactory = TextFieldTableCell.forTableColumn(myStringIntConverter);
        routeCol.cellFactory = TextFieldTableCell.forTableColumn(myStringIntConverter);
        stopdelayCol.cellFactory = TextFieldTableCell.forTableColumn(myStringIntConverter);

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

        /*nameCol.setOnEditCommit { event: TableColumn.CellEditEvent<Trip, String> ->
            val pos = event.tablePosition
            val newName = event.newValue
            val row = pos.row
            val loco = event.tableView.items[row]
            loco.name = newName
        }

        massCol.setOnEditCommit { event: TableColumn.CellEditEvent<Trip, Int> ->
            val pos = event.tablePosition
            val newMass = event.newValue
            val row = pos.row
            val loco = event.tableView.items[row]
            if ((newMass >= 1) and (newMass <= 5)) {
                loco.mass = newMass
            } else {  // this is a workaround for a bug in javafx, value not repainted
                massCol.setVisible(false);
                massCol.setVisible(true);
            }
        }

        vmaxCol.setOnEditCommit { event: TableColumn.CellEditEvent<Trip, Int> ->
            val pos = event.tablePosition
            val newVmax = event.newValue
            val row = pos.row
            val loco = event.tableView.items[row]
            if ((newVmax >= 30) and (newVmax <= 300)) {
                loco.vmax = 10 * (newVmax / 10)  // in steps of ten
            } else {  // this is a workaround for a bug in javafx, value not repainted
                vmaxCol.setVisible(false);
                vmaxCol.setVisible(true);
            }
        }  */

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
        locoCol.cellValueFactory = PropertyValueFactory("loco")
        stopdelayCol.cellValueFactory = PropertyValueFactory("stopdelay")

        tableView.items = trips

        println("tripsTable " + tableView.items.size + " trips")

        // textField.setTextFormatter(formatter);
        Utils.customResize(tableView)

    }

    companion object {

        private val tableView = TableView<Trip>()

        fun refresh() {
            tableView.refresh()
        }

    }

}
