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
import java.util.Collections

import de.blankedv.sx4draw.model.ChangeTrainDialogResult
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.util.Pair

import de.blankedv.sx4draw.Constants.INVALID_INT
import de.blankedv.sx4draw.model.Trip
import javafx.scene.control.*

/**
 *
 * @author mblank
 */
object ChangeTrainDialog {

    private val speed2 = Spinner<Int>(1, 31, 0)
    private val speed1 = Label()
    private val invDir = CheckBox("ändern")


    internal fun open(primaryStage: Stage): ChangeTrainDialogResult {

        val result = ChangeTrainDialogResult()

        val locoAddresses = ArrayList<Int>()
        // get all locos from all trips
        for (lo in SX4Draw.locos) {
            val a = lo.adr
            if (!locoAddresses.contains(a)) {
                locoAddresses.add(a)
            }
        }
        if (locoAddresses.isEmpty()) {
            return ChangeTrainDialogResult()
        }

        Collections.sort(locoAddresses)

        val locos1 = ChoiceBox(FXCollections.observableArrayList(
                locoAddresses)
        )
        locos1.selectionModel.selectFirst()
        speed1.text = "" + getSpeed(locos1.value)

        locos1.selectionModel.selectedItemProperty().addListener { observableValue: ObservableValue<out Int>, oIndex: Int, nIndex: Int ->
            val s = getSpeed(nIndex)
            println("new loco1 sel=$nIndex s=$s")
            speed1.text = "" + s
        }

        val locos2 = ChoiceBox(FXCollections.observableArrayList(
                locoAddresses)
        )
        locos2.selectionModel.selectFirst()
        locos2.selectionModel.selectedItemProperty().addListener { _ : ObservableValue<out Int>, oIndex: Int, nIndex: Int ->
            val s = getSpeed(nIndex)
            speed2.valueFactory.setValue(s)
        }

        speed2.valueFactory.setValue(getSpeed(locoAddresses[0]))

        val lblSensor = Label("Sensor")
        lblSensor.alignment = Pos.CENTER
        val lblLoco = Label("Zug")
        lblLoco.alignment = Pos.CENTER

        val grid = GridPane()
        grid.vgap = 20.0
        grid.hgap = 20.0
        grid.add(Label("von"), 1, 0)
        grid.add(Label("ändern zu"), 2, 0)
        grid.add(Label("Zug-Nr."), 0, 1)
        grid.add(Label("Geschw."), 0, 2)
        grid.add(Label("Richtung"), 0, 3)

        grid.add(locos1, 1, 1)
        grid.add(locos2, 2, 1)
        grid.add(speed1, 1, 2)
        grid.add(speed2, 2, 2)
        grid.add(invDir, 2, 3)

        val col1 = ColumnConstraints()
        col1.percentWidth = 30.0
        col1.halignment = HPos.CENTER
        val col2 = ColumnConstraints()
        col2.percentWidth = 30.0
        val col3 = ColumnConstraints()
        col3.percentWidth = 30.0
        col3.halignment = HPos.CENTER
        val col4 = ColumnConstraints()
        col4.percentWidth = 10.0
        col4.halignment = HPos.CENTER

        grid.columnConstraints.addAll(col1, col2, col3, col4)

        GridPane.setMargin(grid, Insets(5.0, 5.0, 5.0, 5.0))

        val btnCancel = Button("zurück")

        val btnSave = Button("  OK  ")

        grid.add(btnCancel, 1, 4)
        grid.add(btnSave, 2, 4)
        //GridPane.setMargin(btnCancel, new Insets(5, 5, 5, 5));

        val secondScene = Scene(grid, 320.0, 200.0)
        // New window (Stage)
        val newWindow = Stage()
        btnCancel.setOnAction { e ->
            result.loco1 = INVALID_INT
            newWindow.close()
        }
        btnSave.setOnAction { e ->
            result.loco1 = locos1.selectionModel.selectedItem
            result.speed1 = getSpeed(result.loco1)
            result.loco2 = locos2.selectionModel.selectedItem
            result.speed2 = speed2.value
            result.invertDir = invDir.isSelected
            newWindow.close()
        }
        newWindow.title = "Zug-Nummer ändern"
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

    fun getSpeed(loco: Int): Int {
        for (tr in SX4Draw.trips) {
            val pAS = getLocoAndSpeed(tr)
            if (pAS.key == loco) {
                return pAS.value
            }
        }
        return 1
    }

    fun update(loco: Int) {

    }

    fun getLocoAndSpeed(tr: Trip): Pair<Int, Int> {
        // convert locoString string to int values for address, direction and speed
        val lData = tr.loco.split(",")
        if (lData.size < 2) {
            return Pair(INVALID_INT, 0)
        }

        val locoAddr: Int
        val locoSpeed: Int
        try {
            locoAddr = Integer.parseInt(lData[0])

            if (lData.size >= 3) {
                locoSpeed = Integer.parseInt(lData[2])
            } else {
                locoSpeed = 28
            }
            return Pair(locoAddr, locoSpeed)

        } catch (e: NumberFormatException) {
            return Pair(INVALID_INT, 0)

        }

    }
}
