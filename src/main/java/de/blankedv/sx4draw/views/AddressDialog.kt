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


import de.blankedv.sx4draw.views.SX4Draw.PEType
import de.blankedv.sx4draw.Constants.LBMIN
import de.blankedv.sx4draw.GenericAddress
import de.blankedv.sx4draw.PanelElement
import de.blankedv.sx4draw.Signal
import de.blankedv.sx4draw.Turnout
import de.blankedv.sx4draw.config.ReadConfig.YOFF

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
 */
object AddressDialog {

    internal var genAddress = GenericAddress()
    internal val spinner1000 = Spinner<Int>(0, 9, 0)
    internal val spinner100 = Spinner<Int>(0, 9, 0)
    internal val spinner10 = Spinner<Int>(0, 9, 0)
    internal val spinner1 = Spinner<Int>(0, 9, 0)
    internal val lblAdr = Label(" Adresse")
    internal val inv = CheckBox()

    fun open(pe: PanelElement, primaryStage: Stage, initVal: GenericAddress): GenericAddress {

        genAddress = GenericAddress(initVal)


        val title = ("Adresse " + pe.gpe::class.simpleName
                 + " Pos.= " + pe.gpe.x + "," + (pe.gpe.y - YOFF))
        val in1000 = initVal.addr / 1000
        val in100 = (initVal.addr - in1000 * 1000) / 100
        val in10 = (initVal.addr - in1000 * 1000 - in100 * 100) / 10
        val in1 = initVal.addr - in1000 * 1000 - in100 * 100 - in10 * 10
        spinner1000.valueFactory.value = in1000
        spinner100.valueFactory.value = in100
        spinner10.valueFactory.value = in10
        spinner1.valueFactory.value = in1
        genAddress.addr = initVal.addr

        updateAddress()
        println("init address =" + initVal.addr)

        val lblInv = Label(" invertiert")

        // select inv (ONLY for TURNOUT)
        if (pe.gpe::class == Turnout::class) {
            genAddress.inv = initVal.inv
            inv.isSelected = (initVal.inv != null)
          } else {
            genAddress.inv = null
            inv.isVisible = false
            lblInv.isVisible = false
        }

        // select orientation (ONLY for SIGNAL)
        val orient = ChoiceBox(FXCollections.observableArrayList(
                "0°", "45°", "90°", "135°", "180°", "225°", "270°", "315°")
        )

        val lblOrient = Label(" Orient.")

        if (pe.gpe::class == Signal::class) {
            genAddress.orient = initVal.orient
            orient.selectionModel.select(genAddress.orient)
        } else {
            genAddress.orient = 0
            orient.isDisable = true
            orient.isVisible = false
            lblOrient.isVisible = false
        }

        val addrListener = ChangeListener<Int> { _, _, _ -> updateAddress() }

        val invListener = ChangeListener<Boolean> { _, _, _ -> updateAddress() }

        spinner1000.valueProperty().addListener(addrListener)
        spinner100.valueProperty().addListener(addrListener)
        spinner10.valueProperty().addListener(addrListener)
        spinner1.valueProperty().addListener(addrListener)
        inv.selectedProperty().addListener(invListener)

        val grid = GridPane()
        grid.vgap = 10.0
        grid.hgap = 10.0
        grid.add(lblAdr, 0, 1)
        grid.add(spinner1000, 1, 1)
        grid.add(spinner100, 2, 1)
        grid.add(spinner10, 3, 1)
        grid.add(spinner1, 4, 1)

        grid.add(lblInv, 0, 3)
        grid.add(inv, 1, 3)
        grid.add(lblOrient, 2, 3)
        grid.add(orient, 3, 3)

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
            genAddress.addr = -1
            newWindow.close()
        }
        btnSave.setOnAction { _ ->
            genAddress.orient = orient.selectionModel.selectedIndex
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

        return genAddress
    }

    private fun updateAddress() {
        genAddress.addr = (spinner1000.value * 1000
                + spinner100.value * 100
                + spinner10.value * 10
                + spinner1.value)
        if (genAddress.addr >= LBMIN) {
            lblAdr.text = " Virt.-Adr"
        } else {
            lblAdr.text = " SX-Adr"
        }
        if (inv.isSelected) {
            genAddress.inv = 1
        } else {
            genAddress.inv = null
        }
    }

}
