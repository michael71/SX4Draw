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

import de.blankedv.sx4draw.PanelElement
import javafx.util.Pair
import java.util.Comparator

abstract class GenericPE() : Comparator<GenericPE>, Comparable<GenericPE> {

    abstract var name : String?

    abstract var x: Int

    abstract var y: Int

    // order for graphics Z axis
    open val ord: Int = 0

    abstract fun getAddr() : Int

    abstract fun getAddr2() : Int

    open fun translate(d: IntPoint) {
        x += d.x
        y += d.y
    }

    open fun scalePlus() {
        x = 2 * x
        y = 2 * y
    }

    open fun scaleMinus() {
        x = x / 2
        y = y / 2
    }


    open fun isTouched(touch : IntPoint) : Pair<Boolean, Int> {
        val dist = Math.sqrt(((touch.x - x) * (touch.x - x) + (touch.y - y) * (touch.y - y)).toDouble())
        val result = dist < PanelElement.TOUCH_RADIUS * 2
        return Pair(result, 0)
    }

    override fun compare(o1: GenericPE, o2: GenericPE): Int {
        return o1.x - o2.x
    }

    override fun compareTo(other: GenericPE): Int {
        return x - other.x
    }

    fun onSamePoint(p2 : IntPoint) : Boolean {
        return ((x == p2.x) and (y == p2.y))
    }
}
