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

import java.util.ArrayList

import javafx.collections.FXCollections
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.stage.Modality
import javafx.stage.Stage

import de.blankedv.sx4draw.model.CompRoute
import de.blankedv.sx4draw.model.Route
import de.blankedv.sx4draw.model.Trip
import de.blankedv.sx4draw.views.SX4Draw.Companion.compRoutes
import de.blankedv.sx4draw.views.SX4Draw.Companion.locos
import de.blankedv.sx4draw.views.SX4Draw.Companion.routes
import javafx.scene.control.*

/**
 *
 * @author mblank
 */
object NewTripDialog {

    internal fun open(primaryStage: Stage): Trip {

        var result = Trip()
        val addr = Trip.getUnusedAddress()

        if (locos.isEmpty()) {
            Dialogs.buildErrorAlert("ERROR",
                    "keine Loks/Züge!","kann keine Fahrt erzeugen.")
            return Trip()
        }

        if (routes.isEmpty() and compRoutes.isEmpty()) {
            Dialogs.buildErrorAlert("ERROR",
                    "keine Fahrstraßen oder zus.ges.Fahrstraßen","kann keine Fahrt erzeugen.")
            return Trip()
        }

        val locoAddresses = ArrayList<Int>()
        // get all locos from all trips
        for (lo in locos) {
            val a = lo.adr
            if (!locoAddresses.contains(a)) {
                locoAddresses.add(a)
            }
        }
        locoAddresses.sort()
        val cbLoco = ChoiceBox(FXCollections.observableArrayList(
                locoAddresses)
        )
        cbLoco.selectionModel.selectFirst()

        val routeAddresses = ArrayList<Int>()
        // get all routes and comproutes
        for (rt in routes) {
            val a = rt.adr
            if (!routeAddresses.contains(a)) {
                routeAddresses.add(a)
            }
        }
        for (rt in compRoutes) {
            val a = rt.adr
            if (!routeAddresses.contains(a)) {
                routeAddresses.add(a)
            }
        }
        routeAddresses.sort()
        val cbRoute = ChoiceBox(FXCollections.observableArrayList(
                routeAddresses)
        )
        cbRoute.selectionModel.selectFirst()


        val dirs = ArrayList<Int>()
        dirs.add(0) ; dirs.add(1)
        val cbDir = ChoiceBox(FXCollections.observableArrayList(
                dirs)
        )
        cbDir.selectionModel.selectFirst()

        val speeds = ArrayList<Int>()
        for (i in 1..31) speeds.add(i)
        val cbSpeed = ChoiceBox(FXCollections.observableArrayList(
                speeds)
        )
        cbSpeed.selectionModel.selectLast()

        val lblLoco = Label("Lok/Zug")
        lblLoco.alignment = Pos.CENTER

        val grid = GridPane()
        grid.setPadding(Insets(20.0));
        grid.vgap = 20.0
        grid.hgap = 20.0
        grid.add(Label("Fahrstr"), 0, 0)
        grid.add(Label("Lok/Zug"), 1, 0)
        grid.add(Label("Richtung"), 2, 0)
        grid.add(Label("Geschw."), 3, 0)

        grid.add(cbRoute, 0, 1)
        grid.add(cbLoco, 1, 1)
        grid.add(cbDir, 2, 1)
        grid.add(cbSpeed, 3, 1)


        val col1 = ColumnConstraints()
        col1.percentWidth = 25.0
        col1.halignment = HPos.CENTER
        val col2 = ColumnConstraints()
        col2.percentWidth = 25.0
        col2.halignment = HPos.CENTER
        val col3 = ColumnConstraints()
        col3.percentWidth = 25.0
        col3.halignment = HPos.CENTER
        val col4 = ColumnConstraints()
        col4.percentWidth = 25.0
        col4.halignment = HPos.CENTER

        grid.columnConstraints.addAll(col1, col2, col3, col4)

        GridPane.setMargin(grid, Insets(5.0, 5.0, 5.0, 5.0))

        val btnCancel = Button("zurück")

        val btnSave = Button("  OK  ")

        grid.add(btnCancel, 1, 3)
        grid.add(btnSave, 2, 3)
        //GridPane.setMargin(btnCancel, new Insets(5, 5, 5, 5));

        val secondScene = Scene(grid, 400.0, 200.0)
        // New window (Stage)
        val newWindow = Stage()
        btnCancel.setOnAction { e ->
              newWindow.close()
        }
        btnSave.setOnAction { e ->
            val locoAddr = cbLoco.selectionModel.selectedItem
            val speed = cbSpeed.value
            val dir = cbDir.value
            val locoString = locoAddr.toString()+","+dir.toString()+","+speed.toString()
            val routeAddr = cbRoute.selectionModel.selectedItem
            val rt = Route.getByAddress(routeAddr)
            if (rt != null) {
                val sens1 = rt.findSens1()
                val sens2 = rt.findSens2()
                result = Trip(addr, routeAddr, sens1, sens2,
                        startdelay = 1000, stopdelay = 1000, loco = locoString)
            } else {
                val crt = CompRoute.getByAddress(routeAddr)
                if (crt != null) {
                    val sens1 : Int = crt.getStartSensor()
                    val sens2 = crt.getEndSensor()
                    result = Trip(addr, routeAddr, sens1, sens2,
                            startdelay = 1000, stopdelay = 1000, loco = locoString)
                } else {
                    Dialogs.buildErrorAlert("ERROR",
                            "keine Start/Endsensoren gefunden","kann keine Fahrt erzeugen.")
                }
            }

            newWindow.close()
        }
        newWindow.title = "neue Fahrt erzeugen"
        newWindow.scene = secondScene

        // Specifies the modality for new window.
        newWindow.initModality(Modality.WINDOW_MODAL)

        // Specifies the owner Window (parent) for new window
        newWindow.initOwner(primaryStage)

        // Set position of second window, related to primary window.
        newWindow.x = primaryStage.x + 200
        newWindow.y = primaryStage.y + 100

        newWindow.showAndWait()


        return result
    }


}
