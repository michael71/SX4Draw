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
import de.blankedv.sx4draw.Constants.LBMIN
import de.blankedv.sx4draw.GenericAddress
import de.blankedv.sx4draw.PanelElement
import de.blankedv.sx4draw.model.Loco
import de.blankedv.sx4draw.model.Sensor
import de.blankedv.sx4draw.model.Signal
import de.blankedv.sx4draw.model.Turnout

import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Label
import javafx.scene.control.Spinner
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.stage.Modality
import javafx.stage.Stage

/**
 * @author Michael Blank <mblank></mblank>@bawue.de>
 *
 *
 */
object LocoDialog {

    internal val spinner10 = Spinner<Int>(0, 9, 0)
    internal val spinner1 = Spinner<Int>(0, 9, 0)

    internal val massSpinner = Spinner<Int>(1,5,3)

    internal val lblAdr = Label(" Adresse")
    internal val lblMass = Label(" Masse")
    internal val lblVmax = Label(" max. Geschw.")

    internal val choiceBoxVmax = ChoiceBox(FXCollections.observableArrayList(
            "30", "60", "90", "120°", "160", "200°", "250", "300")
    )


/*    fun open(a : Int, primaryStage: Stage): Loco {

        println("adr=" + a)

        val title = ("Lok editieren - Adresse " + a)

        val in10 = a  / 10
        val in1 = a - in10 * 10

        spinner10.valueFactory.value = in10
        spinner1.valueFactory.value = in1




        val addrListener = ChangeListener<Int> { _, _, _ -> updateAddress(pe, a) }

        val invListener = ChangeListener<Boolean> { _, _, _ -> updateAddress(pe, genA) }


        spinner10.valueProperty().addListener(addrListener)
        spinner1.valueProperty().addListener(addrListener)


        val grid = GridPane()
        grid.vgap = 10.0
        grid.hgap = 10.0
        grid.add(lblAdr, 0, 1)

        grid.add(spinner10, 3, 1)
        grid.add(spinner1, 4, 1)


        val col1 = ColumnConstraints()
        col1.percentWidth = 19.0
        val col2 = ColumnConstraints()
        col2.percentWidth = 19.0
        val col3 = ColumnConstraints()
        col3.percentWidth = 19.0
        val col4 = ColumnConstraints()
        col4.percentWidth = 19.0
        val col5 = ColumnConstraints()
        col5.percentWidth = 19.0
        val col6 = ColumnConstraints()
        col6.percentWidth = 5.0
        grid.columnConstraints.addAll(col1, col2, col3, col4, col5, col6)

        GridPane.setMargin(grid, Insets(5.0, 5.0, 5.0, 5.0))

        val btnCancel = Button("zurück")
        val btnSave = Button("  OK  ")
        grid.add(btnCancel, 1, 5, 1, 1)
        grid.add(btnSave, 3, 5, 1, 1)
        //GridPane.setMargin(btnCancel, new Insets(5, 5, 5, 5));

        val secondScene = Scene(grid, 420.0, 160.0)
        // New window (Stage)
        val newWindow = Stage()
        btnCancel.setOnAction { _ ->
            genA.addr = -1
            newWindow.close()
        }
        btnSave.setOnAction { _ ->
            updateAddress(pe, genA)
            newWindow.close()
        }
        newWindow.title = title
        newWindow.scene = secondScene

        // Specifies the modality for new window.
        newWindow.initModality(Modality.WINDOW_MODAL)

        // Specifies the owner Window (parent) for new window
        newWindow.initOwner(primaryStage)

        // Set position of second window, related to primary window.
        newWindow.x = primaryStage.x + 200
        newWindow.y = primaryStage.y + 100

        newWindow.showAndWait()

        return genA
    }

    private fun updateAddress(pe: PanelElement,a : Int) {
        a = (spinner10.value * 10
                + spinner1.value)
        if (a >= LBMIN) {
            lblAdr.text = " Virt.-Adr"
        } else {
            lblAdr.text = " SX-Adr"
            if (spinner1.value == 0) {
                spinner1.valueFactory.value = 1
                genA.addr = (
                        + spinner10.value * 10
                        + spinner1.value)
            } else if (spinner1.value == 9) {
                spinner1.valueFactory.value = 8
                genA.addr = (
                        + spinner10.value * 10
                        + spinner1.value)
            }
        }

       /* if (cbInv.isSelected) {
            genA.inv = 1
        } else {
            genA.inv = 0
        }
        if (cbSec.isSelected) {
            when (pe.gpe) {
                is Sensor -> genA.addr2 = genA.addr + 1000
                is Signal -> genA.addr2 = genA.addr + 1
                else -> genA.addr2 = INVALID_INT
            }
        } else {
            genA.addr2 = INVALID_INT
        } */
        genA.orient = choiceBoxVmax.selectionModel.selectedIndex
    }
*/

}
