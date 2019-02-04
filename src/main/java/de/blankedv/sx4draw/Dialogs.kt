/*
 * Copyright (C) 2018 mblank
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.blankedv.sx4draw


import de.blankedv.sx4draw.Constants.LBMAX
import de.blankedv.sx4draw.Constants.LBMIN
import de.blankedv.sx4draw.Constants.SXMAX_USED
import de.blankedv.sx4draw.SX4Draw.panelElements


import javafx.application.Application
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType

/**
 * @author mblank
 */
object Dialogs {

    fun InfoAlert(title: String, header: String, msg: String, app: Application) {
        val alert = Alert(Alert.AlertType.INFORMATION)
        alert.contentText = msg
        alert.title = title
        alert.headerText = header
        val window = alert.dialogPane.scene.window
        window.setOnCloseRequest { event -> window.hide() }

        val openOpensx = ButtonType("-> opensx.net/sx4")
        alert.buttonTypes.addAll(openOpensx)
        val option = alert.showAndWait()

        if (option.isPresent && option.get() == openOpensx) {
            try {
                app.hostServices.showDocument("https://opensx.net/sx4")
            } catch (e: Exception) {
                println(e.message)
            }

        }
    }


    fun checkAddress(oldValue: GenericAddress, newAddress: GenericAddress): Boolean {
        println("check address = " + newAddress.addr)
        if ((newAddress.addr in 0..SXMAX_USED &&
                newAddress.addr % 10 != 0 && newAddress.addr % 10 != 9 ) || // =valid sx address
                newAddress.addr in LBMIN..LBMAX) { // =valid lanbahn address
            // the address is valid
            var found = false
            var foundPE: PanelElement? = null
            for (pe in panelElements) {
                if (newAddress.addr != oldValue.addr && newAddress.addr == pe.adr) {
                    found = true
                    foundPE = pe
                    foundPE!!.toggleShapeSelected()
                    break
                }
            }
            if (found) {
                val alert = Alert(Alert.AlertType.CONFIRMATION)
                alert.title = "Warnung"
                alert.headerText = "Die Adresse " + newAddress.toString() + "wird bereits verwendet!"
                alert.contentText = "Trotzdem verwenden?"

                val result = alert.showAndWait()
                foundPE!!.toggleShapeSelected()   // never =null
                return (result.get() == ButtonType.OK)
            }
            return true

        } else {
            val alert = Alert(Alert.AlertType.ERROR)
            alert.title = "ERROR: $newAddress ist ungültig!"
            alert.headerText = "Bitte eine gültige Adresse eingeben"
            alert.contentText = "SX Bereich: 0..106[1..8] - virtuell: 1200-9999"
            alert.showAndWait()
            return false
        }
    }

}
