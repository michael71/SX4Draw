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
import de.blankedv.sx4draw.Constants.LBMAX
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
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Label
import javafx.scene.control.Spinner
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
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

    private val cbInv = CheckBox()
    private val lblSecondaryAdr = Label(" 2.Adr?")
    private val cbSec = CheckBox()
    private val cbRenumber = CheckBox()
    private val lblInv = Label(" invertiert")
    private val lblOrient = Label(" Orient.")
    private val lblRTRenumber = Label(" RT renum.")
    private var resultA = GenericAddress(0)
    private const val SX = "Typ: SX Adresse"
    private const val VIRT = "Typ: virtuelle Adr"

    // select orientation (ONLY for SIGNAL)
    private val choiceBoxOrient = ChoiceBox(FXCollections.observableArrayList(
            "0°", "45°", "90°", "135°", "180°", "225°", "270°", "315°")
    )
    private val choiceAddressType = ChoiceBox(FXCollections.observableArrayList(
            SX, VIRT)
    )

    fun open(pe: PanelElement, primaryStage: Stage, genA: GenericAddress): GenericAddress {

        resultA = GenericAddress(genA.addr, genA.addr2 )
        initSpinners(genA)

        if (resultA.addr < LBMIN) {
            choiceAddressType.value = SX
        } else {
            choiceAddressType.value = VIRT
        }

        cbInv.isSelected = genA.inv
        cbSec.isSelected = (genA.addr2 != INVALID_INT)
        cbRenumber.isSelected = false

        val title = ("Adresse des " + pe.gpe::class.simpleName
                + "s an Position= " + pe.gpe.x + "," + pe.gpe.y+ " ?")

        choiceBoxOrient.selectionModel.select(0)

        // hide or show type dependent checkboxes
        when (pe.gpe) {
            is Turnout -> {
                cbInv.isVisible = true
                lblInv.isVisible = true
                cbSec.isVisible = false
                lblSecondaryAdr.isVisible = false
            }
            is Signal,
            is Sensor -> {
                cbInv.isVisible = false
                lblInv.isVisible = false
                cbSec.isVisible = true
                lblSecondaryAdr.isVisible = true
            }
            else -> {
                println("pe.gpe -> else")
                println("ERROR: AddressDialog, class=" + pe.gpe::class + " not allowed")
            }
        }

        // display or hide choiceBox for (signal-) orientation
        if (pe.gpe is Signal) {
            choiceBoxOrient.selectionModel.select(genA.orient)
            //println("cb.select=${choiceBoxOrient.selectionModel.selectedIndex}")
            choiceBoxOrient.isDisable = false
            choiceBoxOrient.isVisible = true
            lblOrient.isVisible = true
        } else {
            choiceBoxOrient.isDisable = true
            choiceBoxOrient.isVisible = false
            lblOrient.isVisible = false
        }

        val addrListener = ChangeListener<Int> { _, _, _ -> updateAddress(pe) }
        val addrTypeListener = ChangeListener<String> { _, _, _ -> updateAddress(pe) }

        spinner1000.valueProperty().addListener(addrListener)
        spinner100.valueProperty().addListener(addrListener)
        spinner10.valueProperty().addListener(addrListener)
        spinner1.valueProperty().addListener(addrListener)
        choiceAddressType.valueProperty().addListener(addrTypeListener)

        val vb = VBox(5.0)
        vb.padding = Insets(15.0, 12.0, 15.0, 12.0)

        val grid = GridPane()
        vb.children.addAll(grid)

        grid.vgap = 10.0
        grid.hgap = 10.0

        grid.add(choiceAddressType, 1, 0, 2, 1)

        grid.add(spinner1000, 1, 2)
        grid.add(spinner100, 2, 2)
        grid.add(spinner10, 3, 2)
        grid.add(spinner1, 4, 2)

        if (pe.gpe is Turnout) {
            // only turnout is "invertable"
            grid.add(lblInv, 1, 3)
            grid.add(cbInv, 2, 3)
        } else {
            grid.add(lblSecondaryAdr, 1, 3)
            grid.add(cbSec, 2, 3)
        }

        grid.add(lblOrient, 3, 3)
        grid.add(choiceBoxOrient, 4, 3)

        grid.add(lblRTRenumber, 1, 4)
        grid.add(cbRenumber, 2, 4)

        val col1 = ColumnConstraints()
        col1.percentWidth = 6.0
        val col2 = ColumnConstraints()
        col2.percentWidth = 22.0
        val col3 = ColumnConstraints()
        col3.percentWidth = 22.0
        val col4 = ColumnConstraints()
        col4.percentWidth = 22.0
        val col5 = ColumnConstraints()
        col5.percentWidth = 22.0
        val col6 = ColumnConstraints()
        col6.percentWidth = 6.0
        grid.columnConstraints.addAll(col1, col2, col3, col4, col5, col6)

        GridPane.setMargin(grid, Insets(5.0, 5.0, 5.0, 5.0))

        val btnCancel = Button("zurück")
        //btnCancel.alignment = Pos.BASELINE_CENTER
        val btnSave = Button("  OK  ")
        //btnSave.alignment = Pos.CENTER

        grid.add(btnCancel, 2, 6, 1, 1)
        grid.add(btnSave, 3, 6, 1, 1)
        //GridPane.setMargin(btnCancel, new Insets(5, 5, 5, 5));

        val secondScene = Scene(vb, 420.0, 210.0)

        val newWindow = Stage()

        btnCancel.setOnAction {
            resultA.addr = INVALID_INT
            newWindow.close()
        }

        btnSave.setOnAction {
            updateAddress(pe)
            resultA.orient = choiceBoxOrient.selectionModel.selectedIndex
            resultA.inv = cbInv.isSelected
            resultA.reNumber = cbRenumber.isSelected
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

        return resultA
    }

    private fun calcAddressFromSpinners() {
        resultA.addr = (spinner1000.value * 1000
                + spinner100.value * 100
                + spinner10.value * 10
                + spinner1.value)
    }

    private fun initSpinners(genericA : GenericAddress) {
        val in1000 = genericA.addr / 1000
        val in100 = (genericA.addr - in1000 * 1000) / 100
        val in10 = (genericA.addr - in1000 * 1000 - in100 * 100) / 10
        val in1 = genericA.addr - in1000 * 1000 - in100 * 100 - in10 * 10
        spinner1000.valueFactory.value = in1000
        spinner100.valueFactory.value = in100
        spinner10.valueFactory.value = in10
        spinner1.valueFactory.value = in1
    }

    private fun updateAddress(pe: PanelElement) {

        calcAddressFromSpinners()

        if (choiceAddressType.value == SX) {
            // choose a valid sx address

            if (resultA.addr < SXMIN_USED) {
                spinner1000.valueFactory.value = 0
                spinner100.valueFactory.value = 0
                spinner10.valueFactory.value = 1
                spinner1.valueFactory.value = 1
            } else if (resultA.addr > SXMAX_USED) {
                spinner1000.valueFactory.value = 1
                spinner100.valueFactory.value = 0
                spinner10.valueFactory.value = 6
                spinner1.valueFactory.value = 8
            } else if (spinner1.value == 9) {
                spinner1.valueFactory.value = 8
            } else if (spinner1.value == 0) {
                spinner1.valueFactory.value = 1
            }

        } else { // virtual address
            if (resultA.addr < LBMIN) {
                spinner1000.valueFactory.value = 1
                spinner100.valueFactory.value = 2
                spinner10.valueFactory.value = 0
                spinner1.valueFactory.value = 0
            } else if (resultA.addr > LBMAX) {
                spinner1000.valueFactory.value = 9
                spinner100.valueFactory.value = 9
                spinner10.valueFactory.value = 9
                spinner1.valueFactory.value = 9
            }
        }
        calcAddressFromSpinners()    // recalc after spinners may have been adjusted


        // get secondary address, if cbSec is selected
        if (cbSec.isSelected) {
            when (pe.gpe) {
                is Sensor -> resultA.addr2 = resultA.addr + 1000
                is Signal -> resultA.addr2 = resultA.addr + 1
                else -> resultA.addr2 = INVALID_INT
            }
        } else {
            resultA.addr2 = INVALID_INT
        }

    }

}
