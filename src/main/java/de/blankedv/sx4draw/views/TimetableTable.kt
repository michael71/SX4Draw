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
import de.blankedv.sx4draw.model.Timetable
import de.blankedv.sx4draw.model.Trip
import de.blankedv.sx4draw.util.Utils
import de.blankedv.sx4draw.views.SX4Draw.Companion.timetables
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
class TimetableTable internal constructor(primaryStage: Stage, private val app: SX4Draw) {


    internal var timetablesTableScene: Scene
    // New window (Stage)
    internal var timetablesWindow: Stage

    init {

        val bp = BorderPane()

        val btnAddTimetable = Button("+ NEUER Fahrplan");
        timetablesTableScene = Scene(bp, 700.0, 300.0)
        bp.top = btnAddTimetable
        bp.center = tableView

        // New window (Stage)
        timetablesWindow = Stage()

        timetablesWindow.title = "Fahrpläne (Timetables) Tabelle"
        timetablesWindow.scene = timetablesTableScene

        // Specifies the modality for new window.
        //locosWindow.initModality(Modality.WINDOW_MODAL);
        // Specifies the owner Window (parent) for new window
        timetablesWindow.initOwner(primaryStage)

        // Set position of second window, related to primary window.
        timetablesWindow.x = primaryStage.x + 250
        timetablesWindow.y = primaryStage.y + 200

        btnAddTimetable.setOnAction { timetables.add(Timetable(INVALID_INT)) }

        show()

    }

    fun show() {

        createDataTables()
        timetablesWindow.show()
    }

    fun close() {
        timetablesWindow.close()
    }

    private fun createDataTables() {
        //    <timetable adr="3300" time="0,20,40" trip="3100,3101,0" next=""/>
        val adrCol = TableColumn<Timetable, Int>("Adr (ID)")
        adrCol.prefWidthProperty().bind(TimetableTable.tableView.widthProperty().multiply(0.1));
        val tripCol = TableColumn<Timetable, String>("Fahrten")
        tripCol.prefWidthProperty().bind(TimetableTable.tableView.widthProperty().multiply(0.7));
        val nextCol = TableColumn<Timetable, Int>("nächster Fahrplan")
        nextCol.prefWidthProperty().bind(TimetableTable.tableView.widthProperty().multiply(0.2));

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

        tableView.columns.setAll(adrCol, tripCol, nextCol)
        tableView.isEditable = true

        tripCol.cellFactory = TextFieldTableCell.forTableColumn();
        adrCol.cellFactory = TextFieldTableCell.forTableColumn(myStringIntConverter);
        nextCol.cellFactory = TextFieldTableCell.forTableColumn(myStringIntConverter);

        tripCol.setOnEditCommit { event: TableColumn.CellEditEvent<Timetable, String> ->
            val pos = event.tablePosition
            val newTrips = event.newValue.replace("\\s".toRegex(), "")   // remove whitespace
            val oldTrips = event.oldValue
            val row = pos.row
            val timetable = event.tableView.items[row]
            val validNewTrips = validateTripsString(newTrips)
            if (validNewTrips.isNotEmpty()) {
                timetable.trip = validNewTrips
                if (!newTrips.equals(validNewTrips)) {
                    Dialogs.buildErrorAlert("Fahrpläne", "",
                            "Für die Fahrten Liste dürfen nur existierende Fahrten ausgewählt werden.")

                }
            } else {
                Dialogs.buildErrorAlert("Fahrpläne", "",
                        "Fahrten Liste nicht möglich, es dürfen nur existierende Fahrten ausgewählt werden.")
                timetable.trip = oldTrips

            }
            tripCol.isVisible = false
            tripCol.isVisible = true

        }

        nextCol.setOnEditCommit { event: TableColumn.CellEditEvent<Timetable, Int> ->
            val pos = event.tablePosition
            val newNext = event.newValue
            val row = pos.row
            val timetable = event.tableView.items[row]
            if (newNext != null) {
                var found = false
                for (tt in timetables) {
                    if (newNext == tt.adr) found = true
                }
                if (found or (newNext == 0) or (newNext == INVALID_INT)) {
                    timetable.next = newNext
                    return@setOnEditCommit
                }
            }
            // this is a workaround for a bug in javafx, value not repainted
            Dialogs.buildErrorAlert("Fahrpläne", "",
                    "Nicht erlaubt, es muss eine existierende Fahrplannummer oder 0 gewählt werden.")
            nextCol.isVisible = false
            nextCol.isVisible = true
        }


        tableView.isCenterShape = true
        tableView.setRowFactory { tableView ->
            val row = TableRow<Timetable>()
            val contextMenu = ContextMenu()
            val removeMenuItem = MenuItem("Fahrt löschen")
            removeMenuItem.onAction = EventHandler {
                tableView.items.remove(row.item)
            }
            val newMenuItem = MenuItem("+ NEUE Fahrt")
            newMenuItem.onAction = EventHandler {
                timetables.add(Timetable(INVALID_INT))
            }
            /*val hideMenuItem = MenuItem("Fahrstraßen nicht mehr anzeigen")
            hideMenuItem.onAction = EventHandler {
                app.hideAllRoutes()
            } */
            contextMenu.items.addAll(newMenuItem, /* hideMenuItem, */ removeMenuItem)
            // Set context menu on row, but use a binding to make it only show for non-empty rows:
            row.contextMenuProperty().bind(
                    Bindings.`when`(row.emptyProperty())
                            .then<ContextMenu>(null as ContextMenu?)
                            .otherwise(contextMenu)
            )
            row
        }

        adrCol.cellValueFactory = PropertyValueFactory("adr")
        tripCol.cellValueFactory = PropertyValueFactory("trip")
        nextCol.cellValueFactory = PropertyValueFactory("next")



        tableView.items = timetables

        println("timetablesTable " + tableView.items.size + " timetables")

        // textField.setTextFormatter(formatter);
        Utils.customResize(tableView)

    }

    private fun validateTripsString(tripStr : String) : String {
        val tripArray = tripStr.split(",")
        var result = ""
        for (trAdrStr in tripArray) {
            var trAdr = 0
            try {
                trAdr = trAdrStr.toInt()
            } catch (e : NumberFormatException) {
                // irregular address of trip
                return result
            }
            var found = false
            // check if this address is a trip address
            for (trip in trips) {
                if (trip.adr == trAdr) {
                    found = true
                    // found this trip
                    if (result.isEmpty()) {
                        result = trip.adr.toString()
                    } else {
                        result += "," + trip.adr.toString()
                    }
                    break
                }
            }
            if (!found) {
                return result
            }
        }
        return result  // every single trip is valid
    }

    //private fun editLoco(a : Int, st : Stage) {
//    LocoDialog.open(a , st)
//}
    companion object {
        private val tableView = TableView<Timetable>()

        fun refresh() {
            tableView.refresh()
        }

    }
}
