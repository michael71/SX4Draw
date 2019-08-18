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
import de.blankedv.sx4draw.Constants.SXMAX_USED
import de.blankedv.sx4draw.GenericAddress
import de.blankedv.sx4draw.model.Loco
import de.blankedv.sx4draw.model.Timetable
import de.blankedv.sx4draw.util.Utils
import de.blankedv.sx4draw.views.SX4Draw.Companion.locos
import de.blankedv.sx4draw.views.SX4Draw.Companion.panelName

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
class LocosTable internal constructor(primaryStage: Stage, private val app: SX4Draw) {



    internal var locosTableSceen: Scene
    // New window (Stage)
    internal var locosWindow: Stage

    init {

        val bp = BorderPane()
        val btnAddLoco = Button("+ NEUE Lok");
        bp.top = btnAddLoco

        locosTableSceen = Scene(bp, 500.0, 300.0)
        bp.center = tableView

        // New window (Stage)
        locosWindow = Stage()
        /* btnClose.setOnAction((e) -> {
            //sxAddress.addr = -1;
            locosWindow.close();
        }); */

        locosWindow.title = "Loks Tabelle"
        locosWindow.scene = locosTableSceen

        // Specifies the modality for new window.
        //locosWindow.initModality(Modality.WINDOW_MODAL);
        // Specifies the owner Window (parent) for new window
        locosWindow.initOwner(primaryStage)

        // Set position of second window, related to primary window.
        locosWindow.x = primaryStage.x + 300
        locosWindow.y = primaryStage.y + 250


        btnAddLoco.setOnAction {  SX4Draw.locos.add(Loco(3,"name???",3,160) ) }

        show()

    }

    fun show() {

        createDataTables()
        locosWindow.show()
    }

    fun close() {
        locosWindow.close()
    }

    private fun createDataTables() {

        val adrCol = TableColumn<Loco, Int>("Lok Adr")
        val nameCol = TableColumn<Loco, String>("Name")
        val massCol = TableColumn<Loco, Int>("Masse")
        val vmaxCol = TableColumn<Loco, Int>("Maximalgeschw.")

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

        tableView.columns.setAll(adrCol, nameCol, massCol, vmaxCol)
        tableView.isEditable = true

        nameCol.cellFactory = TextFieldTableCell.forTableColumn();
        adrCol.cellFactory = TextFieldTableCell.forTableColumn(myStringIntConverter);
        massCol.cellFactory = TextFieldTableCell.forTableColumn(myStringIntConverter);
        vmaxCol.cellFactory = TextFieldTableCell.forTableColumn(myStringIntConverter);

        adrCol.setOnEditCommit { event: TableColumn.CellEditEvent<Loco, Int> ->
            val pos = event.tablePosition
            val newAdr = event.newValue
            val row = pos.row
            val loco = event.tableView.items[row]
            if ((newAdr > 0) and (newAdr <= SXMAX_USED/10 )) {
                loco.adr = newAdr
            } else {
                // this is a workaround for a bug in javafx, else (old) value is not repainted
                adrCol.setVisible(false);
                adrCol.setVisible(true);
            }
        }

        nameCol.setOnEditCommit { event: TableColumn.CellEditEvent<Loco, String> ->
            val pos = event.tablePosition
            val newName = event.newValue
            val row = pos.row
            val loco = event.tableView.items[row]
            loco.name = newName
        }

        massCol.setOnEditCommit { event: TableColumn.CellEditEvent<Loco, Int> ->
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

        vmaxCol.setOnEditCommit { event: TableColumn.CellEditEvent<Loco, Int> ->
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
        }
        //adrCol.setOnEditCommit( { ev -> (ev.tableView.items[ev.tablePosition.row] as Integer).adr = ev.newValue }

        /*massCol.setCellFactory(TextFieldTableCell.forTableColumn())
        massCol.setOnEditCommit { ev -> (ev.tableView.items[ev.tablePosition.row] as Integer).mass = ev.newValue }

        vmaxCol.setCellFactory(TextFieldTableCell.forTableColumn())
        vmaxCol.setOnEditCommit { ev -> (ev.tableView.items[ev.tablePosition.row] as Integer).vmax = ev.newValue } */

        /*      tableViewData[i].setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            chanCol.setMaxWidth(1f * Integer.MAX_VALUE * 18); // 30% width
            chanCol.setStyle("-fx-alignment: CENTER;");
            dataCol.setMaxWidth(1f * Integer.MAX_VALUE * 18); // 70% width
            dataCol.setStyle("-fx-alignment: CENTER;"); */
        tableView.isCenterShape = true
        tableView.setRowFactory { tableView ->
            val row = TableRow<Loco>()
            val contextMenu = ContextMenu()
            val removeMenuItem = MenuItem("Lok löschen")
            removeMenuItem.onAction = EventHandler {
                tableView.items.remove(row.item)
            }
            val editMenuItem = MenuItem("+ NEUE LOK")
            editMenuItem.onAction = EventHandler {
                locos.add(Loco(3,"name",3,160))
            }
            /*val hideMenuItem = MenuItem("Fahrstraßen nicht mehr anzeigen")
            hideMenuItem.onAction = EventHandler {
                app.hideAllRoutes()
            } */
            contextMenu.items.addAll(editMenuItem, /* hideMenuItem, */ removeMenuItem)
            // Set context menu on row, but use a binding to make it only show for non-empty rows:
            row.contextMenuProperty().bind(
                    Bindings.`when`(row.emptyProperty())
                            .then<ContextMenu>(null as ContextMenu?)
                            .otherwise(contextMenu)
            )
            row
        }

        adrCol.cellValueFactory = PropertyValueFactory("adr")
        nameCol.cellValueFactory = PropertyValueFactory("name")
        massCol.cellValueFactory = PropertyValueFactory("mass")
        vmaxCol.cellValueFactory = PropertyValueFactory("vmax")

        tableView.items = locos

        println("locosTable " + tableView.items.size + " locos")

        // textField.setTextFormatter(formatter);
        Utils.customResize(tableView)

    }

    //private fun editLoco(a : Int, st : Stage) {
    //    LocoDialog.open(a , st)
    //}

    companion object {
        private val tableView = TableView<Loco>()

        fun refresh() {
            tableView.refresh()
        }
    }
}
