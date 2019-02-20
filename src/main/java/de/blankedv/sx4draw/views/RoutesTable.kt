/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.blankedv.sx4draw.views

import de.blankedv.sx4draw.Route
import de.blankedv.sx4draw.util.Utils
import de.blankedv.sx4draw.views.SX4Draw.Companion.panelName
import de.blankedv.sx4draw.views.SX4Draw.Companion.routes
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.TableColumn
import javafx.scene.control.TableColumn.CellEditEvent
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import javafx.beans.binding.Bindings
import javafx.event.ActionEvent
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.TableRow
import javafx.util.Callback
import javafx.util.StringConverter

/**
 * @author mblank
 */
class RoutesTable internal constructor(primaryStage: Stage, private val app: SX4Draw) {

    private val tableView = TableView<Route>()

    internal var routingTableScene: Scene
    // New window (Stage)
    internal var routingWindow: Stage

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
            routingWindow.close();
        }); */

        routingWindow.title = "Fahrstraßen Tabelle " + panelName
        routingWindow.scene = routingTableScene

        // Specifies the modality for new window.
        //routingWindow.initModality(Modality.WINDOW_MODAL);
        // Specifies the owner Window (parent) for new window
        routingWindow.initOwner(primaryStage)

        // Set position of second window, related to primary window.
        routingWindow.x = primaryStage.x + 200
        routingWindow.y = primaryStage.y + 100

        createDataTables()

        routingWindow.show()

    }

    fun show() {
        routingWindow.show()
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
            removeMenuItem.onAction = EventHandler { tableView.items.remove(row.item) }
            val showMenuItem = MenuItem("Fahrstraße anzeigen")
            showMenuItem.onAction = EventHandler {
                val rtShown = row.item
                app.showRoute(rtShown, 6)
                /*rtShown.setRouteStates();
                        app.redrawPanelElements();
                        app.drawRTButtons(rtShown.getBtn1(), rtShown.getBtn2());
                        Timeline timeline = new Timeline(new KeyFrame(
                                Duration.millis(6000),
                                ae -> {
                                    rtShown.setMarked(false);
                                    app.redrawPanelElements();
                                }));

                        timeline.play(); */
            }
            contextMenu.items.addAll(showMenuItem, removeMenuItem)
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

        idCol.setCellValueFactory(PropertyValueFactory("adr"))
        btn1Col.setCellValueFactory(PropertyValueFactory("btn1"))
        btn2Col.setCellValueFactory(PropertyValueFactory("btn2"))
        routeCol.setCellValueFactory(PropertyValueFactory("route"))
        sensorsCol.setCellValueFactory(PropertyValueFactory("sensors"))

        tableView.setItems(routes)

        // textField.setTextFormatter(formatter);
        Utils.customResize(tableView)

    }

}
