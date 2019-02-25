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

import de.blankedv.sx4draw.Constants.ADDR0_TURNOUT
import de.blankedv.sx4draw.Constants.INVALID_INT
import de.blankedv.sx4draw.PanelElement
import de.blankedv.sx4draw.model.GenericPE
import de.blankedv.sx4draw.model.IntPoint

import javafx.util.Pair
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlTransient
import javax.xml.bind.annotation.XmlType
import kotlin.properties.Delegates


/**
 *
 * @author mblank
 */
@XmlRootElement(name = "turnout")
@XmlType
class Turnout : GenericPE {

    @get:XmlAttribute
    override var name: String? = null

    // if only defined in GenericPE, the order in the XML output does not look nice ("x" would be at the end)
    // therefore this (in principle unnecessary) "override"
    @get:XmlAttribute
    override var x: Int = 0 // starting point

    @get:XmlAttribute
    override var y: Int = 0

    @get:XmlAttribute
    var x2 = INVALID_INT // endpoint - x2 always >x

    @get:XmlAttribute
    var y2 = INVALID_INT

    @get:XmlAttribute
    var xt = INVALID_INT // "thrown" position for turnout

    @get:XmlAttribute
    var yt = INVALID_INT

    @get:XmlAttribute
    var inv: Int? = null

    @XmlTransient
    private var _adr = ADDR0_TURNOUT

    @get:XmlAttribute(name = "adr")
    var adr: Int by Delegates.observable(_adr) { prop, old, new ->
        println("Turnout adr changed from $old to $new")
        Route.addressInRouteChanged(old.toString(),new.toString())
        _adr = new
    }

    @get:XmlAttribute
    var sxadr: Int? = null

    @get:XmlAttribute
    var sxbit: Int? = null

    override val ord = 2

    constructor()

    constructor(poi: IntPoint, closed: IntPoint, thrown: IntPoint) {
        x = poi.x
        y = poi.y
        x2 = closed.x
        y2 = closed.y
        xt = thrown.x
        yt = thrown.y
        adr = ADDR0_TURNOUT
    }

    override fun scalePlus() {
        val dx = x
        val dy = y
        x = 2 * x
        y = 2 * y

        // do not scale x2/y2 BUT TRANSLATE

        x2 += dx
        y2 += dy
        xt += dx
        yt += dy

    }

    override fun getAddr(): Int {
        return adr
    }


    override fun getAddr2(): Int {
        return INVALID_INT
    }


    override fun isTouched(touch: IntPoint): Pair<Boolean, Int> {
        // check first for (x2,y2) touch (state 0)
        return if (touch.x >= x2 - PanelElement.TOUCH_RADIUS
                && touch.x <= x2 + PanelElement.TOUCH_RADIUS
                && touch.y >= y2 - PanelElement.TOUCH_RADIUS
                && touch.y <= y2 + PanelElement.TOUCH_RADIUS) {
            Pair(true, 0)

        } else if (touch.x >= xt - PanelElement.TOUCH_RADIUS // thrown, state1

                && touch.x <= xt + PanelElement.TOUCH_RADIUS
                && touch.y >= yt - PanelElement.TOUCH_RADIUS
                && touch.y <= yt + PanelElement.TOUCH_RADIUS) {
            Pair(true, 1)  // thrown state
        } else if (touch.x >= x - PanelElement.TOUCH_RADIUS // near center

                && touch.x <= x + PanelElement.TOUCH_RADIUS
                && touch.y >= y - PanelElement.TOUCH_RADIUS
                && touch.y <= y + PanelElement.TOUCH_RADIUS) {
            Pair(true, 0)
        } else {
            Pair(false, 0)
        }

    }

    override fun translate (d : IntPoint) {
        x += d.x
        x2 += d.x
        xt += d.x
        y += d.y
        y2 += d.y
        yt += d.y
    }

    // conv sxadr/sxbit -> adr
    fun evalOldAddress() {
        if ((sxadr != null) && (sxbit != null)) {
            val a = sxadr!!
            val b = sxbit!!
            adr = (a * 10 + b)
            sxadr = null  // only once do "evalOldAddress()"
            sxbit = null
        }
    }

}
