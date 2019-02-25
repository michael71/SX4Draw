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
import de.blankedv.sx4draw.views.SX4Draw.Companion.panelName
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
    // New window (Stage)
    private var routingWindow: Stage

    init {

        val bp = BorderPane()
        /* HBox hb = new HBox(15);
        Button btnClose = new Button("close");
        hb.getChildren().addAll(btnClose);
        bp.setBottom(hb);
        hb.setAlignment(Pos.CENTER);
        BorderPane.setMargin(hb, new Insets(8, 8, 8, 8)); */
        routingTableScene = Scene(bp, 700.0, 300.0)
        bp.center = tableView

        // New window (Stage)
        routingWindow = Stage()
        /* btnClose.setOnAction((e) -> {
            //sxAddress.addr = -1;
            locosWindow.close();
        }); */

        routingWindow.title = "Fahrstraßen (Route) Tabelle"
        routingWindow.scene = routingTableScene

        // Specifies the modality for new window.
        //locosWindow.initModality(Modality.WINDOW_MODAL);
        // Specifies the owner Window (parent) for new window
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
        val btn1Col = TableColumn<Route, Int>("Start")
        val btn2Col = TableColumn<Route, Int>("Ende")
        val routeCol = TableColumn<Route, String>("Fahrstraße")
        val sensorsCol = TableColumn<Route, String>("Sensoren")



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

        tableView.columns.setAll(idCol, btn1Col, btn2Col, routeCol, sensorsCol)
        tableView.isEditable = true
        //idCol.setCellFactory(TextFieldTableCell.forTableColumn());
        /*idCol.setCellFactory(TextFieldTableCell.forTableColumn(myStringIntConverter));
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
        /*      tableViewData[i].setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            chanCol.setMaxWidth(1f * Integer.MAX_VALUE * 18); // 30% width
            chanCol.setStyle("-fx-alignment: CENTER;");
            dataCol.setMaxWidth(1f * Integer.MAX_VALUE * 18); // 70% width
            dataCol.setStyle("-fx-alignment: CENTER;"); */
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
        /*   tableView.setRowFactory(new Callback<TableView<RouteData>, TableRow<RouteData>>() {
                @Override
                public TableRow<RouteData> call(TableView<RouteData> tableView) {
                    final TableRow<RouteData> row = new TableRow<RouteData>() {
                        @Override
                        protected void updateItem(Route sxv, boolean empty) {
                            super.updateItem(sxv, empty);
                            if (!empty) {
                                if (sxv.isMarked()) {
                                    setStyle("-fx-background-color: yellow;");
                                } else {
                                    setStyle("");
                                }
                            } else {
                                setStyle("");
                            }
                        }
                    };
                    return row;
                }
            }); */

        idCol.cellValueFactory = PropertyValueFactory("adr")
        btn1Col.cellValueFactory = PropertyValueFactory("btn1")
        btn2Col.cellValueFactory = PropertyValueFactory("btn2")
        routeCol.cellValueFactory = PropertyValueFactory("route")
        sensorsCol.cellValueFactory = PropertyValueFactory("sensors")

        tableView.items = routes

        // textField.setTextFormatter(formatter);
        Utils.customResize(tableView)

    }

    companion object {

        private val tableView = TableView<Route>()

        fun refresh() {
            tableView.refresh()
        }
    }
}
