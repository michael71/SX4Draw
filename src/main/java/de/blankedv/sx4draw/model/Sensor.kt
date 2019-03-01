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

import de.blankedv.sx4draw.Constants
import de.blankedv.sx4draw.Constants.DEF_SENSOR_ADRSTR
import de.blankedv.sx4draw.Constants.INVALID_INT
import de.blankedv.sx4draw.PanelElement
import de.blankedv.sx4draw.util.Utils
import javafx.scene.shape.Line
import javafx.util.Pair
import java.lang.NumberFormatException
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlTransient
import javax.xml.bind.annotation.XmlType
import kotlin.properties.Delegates

/**
 *
 * @author mblank
 */
@XmlRootElement(name = "sensor")
@XmlType
class Sensor : GenericPE {

    @get:XmlAttribute
    override var name: String? = null  //optional

    @get:XmlAttribute
    override var x: Int = 0 // starting point

    @get:XmlAttribute
    override var y: Int = 0

    @get:XmlAttribute
    var x2: Int? = null

    @get:XmlAttribute
    var y2: Int? = null

    @XmlTransient
    private var _adrStr = DEF_SENSOR_ADRSTR

    @get:XmlAttribute(name = "adr")
    var adrStr: String by Delegates.observable(_adrStr) { _, old, new ->
        if (old.isNotEmpty() && !old.equals(DEF_SENSOR_ADRSTR)) {
            println("Sensor adrStr changed from $old to $new")
            val oldArry = old.split(",")
            val newArry = new.split(",")
            if ((oldArry[0].toInt() > Constants.SXMIN_USED) && (newArry[0].toInt() > Constants.SXMIN_USED)) {
                Route.sensorAddressChanged(oldArry[0], newArry[0])
            }
        }
        _adrStr = new
    }

    @get:XmlAttribute
    var sxadr: Int? = null

    @get:XmlAttribute
    var sxbit: Int? = null

    override val ord = 1


    constructor() {}

    constructor(l: Line) {
        this.x = l.startX.toInt()
        this.x2 = l.endX.toInt()
        this.y = l.startY.toInt()
        this.y2 = l.endY.toInt()
        orderX()
    }

    constructor(poi: IntPoint) {
        this.x = poi.x
        this.x2 = null
        this.y = poi.y
        this.y2 = null
    }

    override fun translate(d: IntPoint) {
        x += d.x
        y += d.y
        if (x2 != null)
            x2 = x2!! + d.x
        if (y2 != null)
            y2 = y2!! + d.y
    }

    override fun getAddr(): Int {
        val aArry = adrStr.split(",")
        var a = INVALID_INT
        try {
            if (aArry.isNotEmpty()) {
                a = aArry[0].toInt()
            }
        } catch (e: NumberFormatException) {

        }
        return a
    }

    override fun getAddr2(): Int {
        val aArry = adrStr.split(",")
        var a = INVALID_INT
        try {
            if (aArry.size >= 2) {
                a = aArry[1].toInt()
            }
        } catch (e: NumberFormatException) {

        }
        return a
    }

    override fun scalePlus() {
        x = 2 * x
        y = 2 * y
        if (x2 != null) {
            x2 = 2 * x2!!
        }
        if (y2 != null) {
            y2 = 2 * y2!!
        }
    }

    override fun scaleMinus() {
        x = x / 2
        y = y / 2
        if (x2 != null) {
            x2 = x2!! / 2
        }
        if (y2 != null) {
            y2 = y2!! / 2
        }
    }

    // conv sxadr/sxbit -> adr
    fun evalOldAddress() {
        if ((sxadr != null) && (sxbit != null)) {
            val a = sxadr!!
            val b = sxbit!!
            adrStr = "" + (a * 10 + b)
            sxadr = null  // only once do "evalOldAddress()"
            sxbit = null
        }
    }

    private fun orderX() {

        if (x == x2) {
            if (y2!! < y) {
                val temp = y2
                y2 = y
                y = temp!!
            }
        } else if (x2!! > x) {
            // everything is fine ....
        } else {
            var temp = x2
            x2 = x
            x = temp!!
            temp = y2
            y2 = y
            y = temp!!

        }

    }

    override fun isTouched(touch: IntPoint): Pair<Boolean, Int> {
        val xx2 = x2 ?: x    // map "null" to a useful value for US Type sensor
        val yy2 = y2 ?: y
        val ymin = Math.min(y, yy2)
        val ymax = Math.max(y, yy2)

        return if (touch.x >= x - PanelElement.TOUCH_RADIUS
                && touch.x <= xx2 + PanelElement.TOUCH_RADIUS
                && touch.y >= ymin - PanelElement.TOUCH_RADIUS
                && touch.y <= ymax + PanelElement.TOUCH_RADIUS) {
            if (Utils.calcDistanceFromLine(IntPoint(x, y), IntPoint(xx2, yy2), touch) < PanelElement.TOUCH_RADIUS) {
                Pair(true, 0)
            } else {
                Pair(false, 0)
            }
        } else {
            Pair(false, 0)
        }

    }
}
