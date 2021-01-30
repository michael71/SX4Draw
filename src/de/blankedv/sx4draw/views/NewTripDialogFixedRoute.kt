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
object NewTripDialogFixedRoute {

    internal fun open(primaryStage: Stage, rtAddress : Int): Trip {

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
        grid.setPadding(Insets(10.0));
        grid.vgap = 20.0
        grid.hgap = 20.0

        grid.add(Label("Lok/Zug"), 0, 0)
        grid.add(Label("Richtung"), 1, 0)
        grid.add(Label("Geschw."), 2, 0)


        grid.add(cbLoco, 0, 1)
        grid.add(cbDir, 1, 1)
        grid.add(cbSpeed, 2, 1)


        val col1 = ColumnConstraints()
        col1.percentWidth = 33.3
        col1.halignment = HPos.CENTER
        val col2 = ColumnConstraints()
        col2.percentWidth = 33.3
        col2.halignment = HPos.CENTER
        val col3 = ColumnConstraints()
        col3.percentWidth = 33.3
        col3.halignment = HPos.CENTER

        grid.columnConstraints.addAll(col1, col2, col3)

        GridPane.setMargin(grid, Insets(5.0, 5.0, 5.0, 5.0))

        val btnCancel = Button("zurück")

        val btnSave = Button("  OK  ")

        grid.add(btnCancel, 0, 3)
        grid.add(btnSave, 2, 3)
        //GridPane.setMargin(btnCancel, new Insets(5, 5, 5, 5));

        val secondScene = Scene(grid, 300.0, 200.0)
        // New window (Stage)
        val newWindow = Stage()
        btnCancel.setOnAction {
              newWindow.close()
        }
        btnSave.setOnAction {
            val locoAddr = cbLoco.selectionModel.selectedItem
            val speed = cbSpeed.value
            val dir = cbDir.value
            val locoString = locoAddr.toString()+","+dir.toString()+","+speed.toString()

            val rt = Route.getByAddress(rtAddress)
            if (rt != null) {
                val sens1 = rt.findSens1()
                val sens2 = rt.findSens2()
                result = Trip(addr, rtAddress, sens1, sens2,
                        startdelay = 1000, stopdelay = 1000, loco = locoString)
            } else {
                val crt = CompRoute.getByAddress(rtAddress)
                if (crt != null) {
                    val sens1 : Int = crt.getStartSensor()
                    val sens2 = crt.getEndSensor()
                    result = Trip(addr, rtAddress, sens1, sens2,
                            startdelay = 1000, stopdelay = 1000, loco = locoString)
                } else {
                    Dialogs.buildErrorAlert("ERROR",
                            "keine Start/Endsensoren gefunden","kann keine Fahrt erzeugen.")
                }
            }

            newWindow.close()
        }
        newWindow.title = "Lok/Zug auswählen"
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
