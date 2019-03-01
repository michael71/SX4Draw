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
import de.blankedv.sx4draw.Constants.SXMAX_USED
import de.blankedv.sx4draw.Constants.SXMIN_USED
import de.blankedv.sx4draw.GenericAddress
import de.blankedv.sx4draw.PanelElement
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
object AddressDialog {

    private val spinner1000 = Spinner<Int>(0, 9, 0)
    private val spinner100 = Spinner<Int>(0, 9, 0)
    private val spinner10 = Spinner<Int>(0, 9, 0)
    private val spinner1 = Spinner<Int>(0, 9, 0)
    //internal var spin1 = arrayOf(spinner1000, spinner100, spinner10, spinner1)
    private val lblAdr = Label(" Adresse")
    private val cbInv = CheckBox()
    private val lblSecondaryAdr = Label(" 2.Adr?")
    private val cbSec = CheckBox()

    // select orientation (ONLY for SIGNAL)
    private val choiceBoxOrient = ChoiceBox(FXCollections.observableArrayList(
            "0°", "45°", "90°", "135°", "180°", "225°", "270°", "315°")
    )


    fun open(pe: PanelElement, primaryStage: Stage, genA: GenericAddress): GenericAddress {

        println("genAdr=${genA}")

        val title = ("Adresse " + pe.gpe::class.simpleName
                + " Pos.= " + pe.gpe.x + "," + pe.gpe.y)
        val in1000 = genA.addr / 1000
        val in100 = (genA.addr - in1000 * 1000) / 100
        val in10 = (genA.addr - in1000 * 1000 - in100 * 100) / 10
        val in1 = genA.addr - in1000 * 1000 - in100 * 100 - in10 * 10
        spinner1000.valueFactory.value = in1000
        spinner100.valueFactory.value = in100
        spinner10.valueFactory.value = in10
        spinner1.valueFactory.value = in1
        updateAddress(pe, genA) // for address label display

        val lblInv = Label(" invertiert")

        when (pe.gpe) {
            is Turnout -> {
                cbInv.isSelected = (genA.inv != 0)
                cbInv.isVisible = true
                lblInv.isVisible = true
                genA.addr2 = INVALID_INT
                cbSec.isVisible = false
                lblSecondaryAdr.isVisible = false
            }
            is Signal,
            is Sensor -> {
                genA.inv = 0
                cbInv.isVisible = false
                lblInv.isVisible = false
                cbSec.isSelected = (genA.addr2 != INVALID_INT)
                cbSec.isVisible = true
                lblSecondaryAdr.isVisible = true
            }
            else -> {
                println("ERROR: AddressDialog, not allowed class="+pe.gpe::class)
            }
        }

        val lblOrient = Label(" Orient.")

        if (pe.gpe is Signal) {
            genA.orient = genA.orient
            choiceBoxOrient.selectionModel.select(genA.orient)
            choiceBoxOrient.isDisable = false
            choiceBoxOrient.isVisible = true
            lblOrient.isVisible = true
        } else {
            genA.orient = 0
            choiceBoxOrient.isDisable = true
            choiceBoxOrient.isVisible = false
            lblOrient.isVisible = false
        }

        val addrListener = ChangeListener<Int> { _, _, _ -> updateAddress(pe, genA) }

        val invListener = ChangeListener<Boolean> { _, _, _ -> updateAddress(pe, genA) }

        spinner1000.valueProperty().addListener(addrListener)
        spinner100.valueProperty().addListener(addrListener)
        spinner10.valueProperty().addListener(addrListener)
        spinner1.valueProperty().addListener(addrListener)
        cbInv.selectedProperty().addListener(invListener)

        val grid = GridPane()
        grid.vgap = 10.0
        grid.hgap = 10.0
        grid.add(lblAdr, 0, 1)
        grid.add(spinner1000, 1, 1)
        grid.add(spinner100, 2, 1)
        grid.add(spinner10, 3, 1)
        grid.add(spinner1, 4, 1)

        if (pe.gpe is Turnout) {
            grid.add(lblInv, 0, 3)
            grid.add(cbInv, 1, 3)
        } else {
            grid.add(lblSecondaryAdr, 0, 3)
            grid.add(cbSec, 1, 3)
        }
        grid.add(lblOrient, 2, 3)
        grid.add(choiceBoxOrient, 3, 3)

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
        btnCancel.setOnAction {
            genA.addr = -1
            newWindow.close()
        }
        btnSave.setOnAction {
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

    private fun updateAddress(pe: PanelElement, genA : GenericAddress) {
        genA.addr = (spinner1000.value * 1000
                + spinner100.value * 100
                + spinner10.value * 10
                + spinner1.value)
        if (genA.addr >= LBMIN) {
            lblAdr.text = " Virt.-Adr\n >=1200"
        } else if ((genA.addr >= SXMIN_USED) && (genA.addr <= SXMAX_USED)) {
            lblAdr.text = " SX-Adr\n 1.1-106.8"
            if (spinner1.value == 0) {
                spinner1.valueFactory.value = 1
                genA.addr = (spinner1000.value * 1000
                        + spinner100.value * 100
                        + spinner10.value * 10
                        + spinner1.value)
            } else if (spinner1.value == 9) {
                spinner1.valueFactory.value = 8
                genA.addr = (spinner1000.value * 1000
                        + spinner100.value * 100
                        + spinner10.value * 10
                        + spinner1.value)
            }
        } else if (genA.addr < SXMIN_USED){
            // too low, invalid adresse, reset spinners to 11 (SXMIN_USED)

            spinner1000.valueFactory.value = 0
            spinner100.valueFactory.value = 0
            spinner10.valueFactory.value = 1
            spinner1.valueFactory.value = 1

             genA.addr = (spinner1000.value * 1000
                    + spinner100.value * 100
                    + spinner10.value * 10
                    + spinner1.value)
        } else  {
            // too high, reset to 1200 (lbmin)
            spinner1000.valueFactory.value = 1
            spinner100.valueFactory.value = 2
            spinner10.valueFactory.value = 0
            spinner1.valueFactory.value = 0

            genA.addr = (spinner1000.value * 1000
                    + spinner100.value * 100
                    + spinner10.value * 10
                    + spinner1.value)
        }

        if (cbInv.isSelected) {
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
        }
        genA.orient = choiceBoxOrient.selectionModel.selectedIndex
    }


}
