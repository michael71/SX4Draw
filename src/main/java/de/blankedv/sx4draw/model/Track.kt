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

import de.blankedv.sx4draw.Constants.INVALID_INT
import de.blankedv.sx4draw.PanelElement
import de.blankedv.sx4draw.model.GenericPE
import de.blankedv.sx4draw.model.IntPoint
import de.blankedv.sx4draw.util.Utils
import javafx.scene.shape.Line
import javafx.util.Pair

import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType


/**
 * generic panel element can be any of PEType types
 *
 * @author mblank
 */
@XmlRootElement(name = "track")
@XmlType
class Track : GenericPE {

    @get:XmlAttribute
    override var name: String? = null

    @get:XmlAttribute
    override var x: Int = 0 // starting point

    @get:XmlAttribute
    override var y: Int = 0

    @get:XmlAttribute
    var x2 = INVALID_INT // endpoint - x2 always >x

    @get:XmlAttribute
    var y2 = INVALID_INT

    override val ord = 0

    constructor()

    constructor(l: Line) {
        this.x = l.startX.toInt()
        this.x2 = l.endX.toInt()
        this.y = l.startY.toInt()
        this.y2 = l.endY.toInt()
        orderX()
    }

    /* constructor (pe : PanelElement) {
         if (!pe.name.isBlank()) {
             this.name = pe.name
         }
         this.x = pe.x
         this.y = pe.y
         this.x2 = pe.x2
         this.y2 = pe.y2
     } */

    override fun getAddr() : Int {
        return INVALID_INT
    }

    override fun getAddr2() : Int {
        return INVALID_INT
    }

    override fun translate(d: IntPoint) {
        x += d.x
        y += d.y
        x2 += d.x
        y2 += d.y
    }

    override fun scalePlus() {
        x = 2 * x
        y = 2 * y
        x2 = 2 * x2
        y2 = 2 * y2
    }

    override fun scaleMinus() {
        x = x / 2
        y = y / 2
        x2 = x2 / 2
        y2 = y2 / 2
    }

    override fun isTouched(touch: IntPoint): Pair<Boolean, Int> {
        val xmin = Math.min(x, x2)
        val xmax = Math.max(x, x2)
        val ymin = Math.min(y, y2)
        val ymax = Math.max(y, y2)
        return if (touch.x >= xmin - PanelElement.TOUCH_RADIUS
                && touch.x <= xmax + PanelElement.TOUCH_RADIUS
                && touch.y >= ymin - PanelElement.TOUCH_RADIUS
                && touch.y <= ymax + PanelElement.TOUCH_RADIUS) {
            if (Utils.calcDistanceFromLine(IntPoint(x, y), IntPoint(x2, y2), touch) < PanelElement.TOUCH_RADIUS) {
                Pair(true, 0)
            } else {
                Pair(false, 0)
            }
        } else {
            Pair(false, 0)
        }
    }

    private fun orderX() {
        if (x == x2) {
            if (y2 < y) {
                val temp = y2
                y2 = y
                y = temp
            }
        } else if (x2 > x) {
            // everything is fine ....
        } else {
            var temp = x2
            x2 = x
            x = temp
            temp = y2
            y2 = y
            y = temp

        }
    }


    companion object {


    }
}
