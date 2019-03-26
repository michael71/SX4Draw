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

import de.blankedv.sx4draw.model.Route
import de.blankedv.sx4draw.util.Utils
import de.blankedv.sx4draw.views.SX4Draw.Companion.routes
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import javafx.beans.binding.Bindings
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.TableRow
import javafx.util.StringConverter

/**
 * @author mblank
 */
class RoutesTable internal constructor(primaryStage: Stage, private val app: SX4Draw) {

    private var routingTableScene: Scene

    private var routingWindow: Stage

    init {

        val bp = BorderPane()

        routingTableScene = Scene(bp, 700.0, 300.0)
        bp.center = tableView

        // New window (Stage)
        routingWindow = Stage()

        routingWindow.title = "Fahrstraßen (Route) Tabelle"
        routingWindow.scene = routingTableScene
        routingWindow.initOwner(primaryStage)

        // Set position of second window, related to primary window.
        routingWindow.x = primaryStage.x + 100
        routingWindow.y = primaryStage.y + 50

        createDataTables()

        routingWindow.show()

    }

    fun show() {
        routingWindow.show()
    }

    fun close() {
        routingWindow.close()
    }

    private fun createDataTables() {

        val idCol = TableColumn<Route, Int>("ID")
        idCol.prefWidthProperty().bind(RoutesTable.tableView.widthProperty().multiply(0.1));
        val btn1Col = TableColumn<Route, Int>("Start")
        btn1Col.prefWidthProperty().bind(RoutesTable.tableView.widthProperty().multiply(0.1));
        val btn2Col = TableColumn<Route, Int>("Ende")
        btn2Col.prefWidthProperty().bind(RoutesTable.tableView.widthProperty().multiply(0.1));
        val routeCol = TableColumn<Route, String>("Fahrstraße")
        routeCol.prefWidthProperty().bind(RoutesTable.tableView.widthProperty().multiply(0.55));
        val sensorsCol = TableColumn<Route, String>("Sensoren")
        sensorsCol.prefWidthProperty().bind(RoutesTable.tableView.widthProperty().multiply(0.15));

        /* val myStringIntConverter = object : StringConverter<Int>() {
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
        } */

        tableView.columns.setAll(idCol, btn1Col, btn2Col, routeCol, sensorsCol)
        tableView.isEditable = true
        /*idCol.setCellFactory(TextFieldTableCell.forTableColumn());
        idCol.setCellFactory(TextFieldTableCell.forTableColumn(myStringIntConverter));
        idCol.setOnEditCommit(new EventHandler<CellEditEvent<Route, Integer>>() {
            @Override
            public void handle(CellEditEvent<Route, Integer> ev) {
                ((Route) ev.getTableView().getItems().get(
                        ev.getTablePosition().getRow())).setRoute("" + ev.getNewValue());
            }
        }); */

        routeCol.setCellFactory(TextFieldTableCell.forTableColumn())
        routeCol.setOnEditCommit { ev -> (ev.tableView.items[ev.tablePosition.row] as Route).route = ev.newValue }
        sensorsCol.setCellFactory(TextFieldTableCell.forTableColumn())
        sensorsCol.setOnEditCommit { ev -> (ev.tableView.items[ev.tablePosition.row] as Route).sensors = ev.newValue }
        tableView.isCenterShape = true
        tableView.setRowFactory { tableView ->
            val row = TableRow<Route>()
            val contextMenu = ContextMenu()
            val removeMenuItem = MenuItem("Fahrstraße löschen")
            removeMenuItem.onAction = EventHandler {
                app.hideAllRoutes()
                tableView.items.remove(row.item)
            }
            val showMenuItem = MenuItem("Fahrstraße anzeigen")
            showMenuItem.onAction = EventHandler {
                app.hideAllRoutes()
                val rtShown = row.item
                app.showRoute(rtShown)
            }
            val hideMenuItem = MenuItem("Fahrstraßen nicht mehr anzeigen")
            hideMenuItem.onAction = EventHandler {
                app.hideAllRoutes()
            }
            contextMenu.items.addAll(showMenuItem, hideMenuItem, removeMenuItem)
            // Set context menu on row, but use a binding to make it only show for non-empty rows:
            row.contextMenuProperty().bind(
                    Bindings.`when`(row.emptyProperty())
                            .then<ContextMenu>(null as ContextMenu?)
                            .otherwise(contextMenu)
            )
            row
        }

        idCol.cellValueFactory = PropertyValueFactory("adr")
        btn1Col.cellValueFactory = PropertyValueFactory("btn1")
        btn2Col.cellValueFactory = PropertyValueFactory("btn2")
        routeCol.cellValueFactory = PropertyValueFactory("route")
        sensorsCol.cellValueFactory = PropertyValueFactory("sensors")

        tableView.items = routes

        Utils.customResize(tableView)

    }

    companion object {

        private val tableView = TableView<Route>()

        fun refresh() {
            tableView.refresh()
        }
    }
}
