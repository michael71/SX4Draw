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

package de.blankedv.sx4draw.model

import de.blankedv.sx4draw.Constants.ADDR0_ROUTEBUTTON
import de.blankedv.sx4draw.Constants.INVALID_INT
import de.blankedv.sx4draw.views.SX4Draw.Companion.panelElements

import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType


/**
 * generic panel element can be any of PEType types
 *
 * @author mblank
 */
@XmlRootElement(name = "routebutton")
@XmlType
class RouteButton : GenericPE {

    @get:XmlAttribute
    var adr= INVALID_INT

    @get:XmlAttribute
    override var name : String? = null

    @get:XmlAttribute
    override var x: Int = 0 // starting point

    @get:XmlAttribute
    override var y: Int = 0

    override val ord = 2

    constructor() {}

    constructor (poi : IntPoint) {
        x = poi.x
        y = poi.y
        autoAddress()
    }

    override fun getAddr() : Int {
        return adr
    }

    override fun getAddr2() : Int {
        return INVALID_INT
    }

    private fun autoAddress() {
        var a = ADDR0_ROUTEBUTTON  // minumum for route buttons
        for (pe in panelElements) {
            if (pe.gpe is RouteButton) {
                if (pe.gpe.getAddr() >= a) {
                    a = pe.gpe.getAddr() + 1
                }
            }
        }
        adr = a
    }
}
