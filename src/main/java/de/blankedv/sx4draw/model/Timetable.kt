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
import de.blankedv.sx4draw.Constants.INVALID_INT
import de.blankedv.sx4draw.views.RoutesTable
import de.blankedv.sx4draw.views.SX4Draw
import de.blankedv.sx4draw.views.SX4Draw.Companion.timetables
import java.util.Comparator

import org.w3c.dom.Node
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType

/**
 * <timetable adr="5500" trip="3100,3101,0" next="5501"/>
 *
 * @author mblank
 */
@XmlRootElement(name = "timetable")
@XmlType
class Timetable(

        @get:XmlAttribute
        var adr: Int = INVALID_INT,   // is mandatory

        @get:XmlAttribute
        var trip: String = "",   // is mandatory

        @get:XmlAttribute
        var name: String = "" ) : Comparator<Timetable>, Comparable<Timetable> {

    init {
        autoAddress()
    }

    fun copy() : Timetable {
        val t = Timetable();
        t.trip = trip
        t.name = name
        return t
    }

    private fun autoAddress() {
        if (adr != INVALID_INT) return

        var a = Constants.ADDR0_TIMETABLE
        for (tt in SX4Draw.timetables) {
            if (tt.adr >= a) {
                a = tt.adr + 1
            }
        }
        adr = a
    }

    override fun compare(o1: Timetable, o2: Timetable): Int {
        // if adr can be =null use:   val o2id = (o2.adr)?: -1
        return o1.adr - o2.adr
    }

    override fun compareTo(other: Timetable): Int {
        return adr - other.adr
    }

    companion object {

        fun add(a: Node) {
            val tt = Timetable()
            val attributes = a.attributes

            for (i in 0 until attributes.length) {
                val theAttribute = attributes.item(i)
                if ((theAttribute.nodeName == "id") ||
                        (theAttribute.nodeName == "adr")) {
                    tt.adr = Integer.parseInt(theAttribute.nodeValue)
                } else if (theAttribute.nodeName == "trip") {
                    tt.trip = theAttribute.nodeValue
                } else if (theAttribute.nodeName == "name") {
                    tt.name = theAttribute.nodeValue
                }
            }
            if (tt.adr != INVALID_INT) {
                timetables.add(tt)
            }

        }

    }
}
