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

import de.blankedv.sx4draw.model.CompRoute
import de.blankedv.sx4draw.model.Loco
import de.blankedv.sx4draw.util.Utils
import de.blankedv.sx4draw.views.SX4Draw.Companion.compRoutes
import de.blankedv.sx4draw.views.SX4Draw.Companion.locos
import de.blankedv.sx4draw.views.SX4Draw.Companion.panelName

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
class CompRoutesTable internal constructor(primaryStage: Stage, private val app: SX4Draw) {

    private val tableView = TableView<CompRoute>()

    internal var compRoutesTableSceen: Scene
    // New window (Stage)
    internal var compRouteWindow: Stage

    init {

        val bp = BorderPane()
        /* HBox hb = new HBox(15);
        Button btnClose = new Button("close");
        hb.getChildren().addAll(btnClose);
        bp.setBottom(hb);
        hb.setAlignment(Pos.CENTER);
        BorderPane.setMargin(hb, new Insets(8, 8, 8, 8)); */
        compRoutesTableSceen = Scene(bp, 400.0, 300.0)
        bp.center = tableView

        // New window (Stage)
        compRouteWindow = Stage()
        /* btnClose.setOnAction((e) -> {
            //sxAddress.addr = -1;
            compRouteWindow.close();
        }); */

        compRouteWindow.title = "Zusammeng. Fahrstraßen (CompRoute)"
        compRouteWindow.scene = compRoutesTableSceen

        // Specifies the modality for new window.
        //compRouteWindow.initModality(Modality.WINDOW_MODAL);
        // Specifies the owner Window (parent) for new window
        compRouteWindow.initOwner(primaryStage)

        // Set position of second window, related to primary window.
        compRouteWindow.x = primaryStage.x + 150
        compRouteWindow.y = primaryStage.y + 100

        createDataTables()

        compRouteWindow.show()

    }

    fun show() {
        compRouteWindow.show()
    }

    fun close() {
        compRouteWindow.close()
    }

    private fun createDataTables() {

        val adrCol = TableColumn<CompRoute, Int>("Adr (ID)")
        val btn1Col = TableColumn<CompRoute, Int>("Start Btn")
        val btn2Col = TableColumn<CompRoute, Int>("Ende Btn")
        val routesCol = TableColumn<CompRoute, String>("Einzel-Fahrstrassen")

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

        tableView.columns.setAll(adrCol, btn1Col, btn2Col, routesCol)
        tableView.isEditable = true

        routesCol.cellFactory = TextFieldTableCell.forTableColumn();
        adrCol.cellFactory = TextFieldTableCell.forTableColumn(myStringIntConverter);
        btn1Col.cellFactory = TextFieldTableCell.forTableColumn(myStringIntConverter);
        btn2Col.cellFactory = TextFieldTableCell.forTableColumn(myStringIntConverter);

        adrCol.setOnEditCommit { event: TableColumn.CellEditEvent<CompRoute, Int> ->
            val pos = event.tablePosition
            val newAdr = event.newValue
            val row = pos.row
            val crt = event.tableView.items[row]
            //if ((newAdr > 0) and (newAdr < 100)) {
                crt.adr = newAdr
           ////    adrCol.setVisible(false);
             //   adrCol.setVisible(true);
            //}
        }

        btn1Col.setOnEditCommit { event: TableColumn.CellEditEvent<CompRoute, Int> ->
            val pos = event.tablePosition
            val newBtn1 = event.newValue
            val row = pos.row
            val crt = event.tableView.items[row]
            //if ((newAdr > 0) and (newAdr < 100)) {
            crt.btn1 = newBtn1
            ////    adrCol.setVisible(false);
            //   adrCol.setVisible(true);
            //}
        }

        btn2Col.setOnEditCommit { event: TableColumn.CellEditEvent<CompRoute, Int> ->
            val pos = event.tablePosition
            val newBtn2 = event.newValue
            val row = pos.row
            val crt = event.tableView.items[row]
            //if ((newAdr > 0) and (newAdr < 100)) {
            crt.btn2 = newBtn2
            ////    adrCol.setVisible(false);
            //   adrCol.setVisible(true);
            //}
        }

        routesCol.setOnEditCommit { event: TableColumn.CellEditEvent<CompRoute, String> ->
            val pos = event.tablePosition
            val newRoutes = event.newValue
            val row = pos.row
            val crt = event.tableView.items[row]
            crt.routes = newRoutes
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
            val row = TableRow<CompRoute>()
            val contextMenu = ContextMenu()
            val showMenuItem = MenuItem("Zusammeng. Fahrstraße anzeigen")
            showMenuItem.onAction = EventHandler {
                app.hideAllRoutes()
                val crtShown = row.item
                app.showCompRoute(crtShown)
            }

            val hideMenuItem = MenuItem("Zusammeng. Fahrstraße nicht mehr anzeigen")
            hideMenuItem.onAction = EventHandler {
                app.hideAllRoutes()
             }
            val removeMenuItem = MenuItem("Zusammeng. Fahrstraße löschen")
            removeMenuItem.onAction = EventHandler {
                tableView.items.remove(row.item)
            }

            val addMenuItem = MenuItem("+ neue Zusammeng. Fahrstraße hinzufügen")
            addMenuItem.onAction = EventHandler {
                compRoutes.add(CompRoute(9999,9999,9999,"2200,2201"))
            }

            contextMenu.items.addAll(showMenuItem, hideMenuItem, removeMenuItem, addMenuItem)
            // Set context menu on row, but use a binding to make it only show for non-empty rows:
            row.contextMenuProperty().bind(
                    Bindings.`when`(row.emptyProperty())
                            .then<ContextMenu>(null as ContextMenu?)
                            .otherwise(contextMenu)
            )
            row
        }

        adrCol.cellValueFactory = PropertyValueFactory("adr")
        btn1Col.cellValueFactory = PropertyValueFactory("btn1")
        btn2Col.cellValueFactory = PropertyValueFactory("btn2")
        routesCol.cellValueFactory = PropertyValueFactory("routes")

        tableView.items = compRoutes

        println("compRoutesTable " + tableView.items.size + " CompRoutes")

        // textField.setTextFormatter(formatter);
        Utils.customResize(tableView)

    }

}
