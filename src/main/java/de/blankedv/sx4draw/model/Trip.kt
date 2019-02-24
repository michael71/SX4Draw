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

import de.blankedv.sx4draw.Constants.ADDR0_TRIP
import de.blankedv.sx4draw.Constants.INVALID_INT
import de.blankedv.sx4draw.views.SX4Draw.Companion.trips
import java.util.Comparator

import org.w3c.dom.Node
import javax.xml.bind.annotation.*

/**
 *
 * <trip adr="3100" route="2300" sens1="924" sens2="902" loco="29,1,126" stopdelay="1500" />
 *
 * @author mblank
 */
@XmlRootElement(name = "trip")
//@XmlType() //TODO Does not work: propOrder = {"adr", "route", "sens1", "sens2", "loco", "stopdelay"})

class Trip(
        @get:XmlAttribute    // @get: WICHTIG für KOTLIN !!!
        var adr: Int = INVALID_INT,

        @get:XmlAttribute
        var route: Int = INVALID_INT,

        @get:XmlAttribute
        var sens1: Int = INVALID_INT,

        @get:XmlAttribute
        var sens2: Int = INVALID_INT,

        @get:XmlAttribute
        var loco: String = "",

        @get:XmlAttribute
        var stopdelay: Int = INVALID_INT) : Comparator<Trip>, Comparable<Trip> {


    fun autoAddress() {
        if (adr != INVALID_INT) return

        var a = ADDR0_TRIP  // minumum for trips
        for (tr in trips) {
            if (tr.adr >= a) {
                a = tr.adr + 1
            }
        }
        adr = a
    }

    override fun compare(o1: Trip, o2: Trip): Int {
        return o1.adr - o2.adr

    }


    override fun compareTo(other: Trip): Int {
        return adr - other.adr
    }

    override fun toString(): String {
        return "Trip: adr=$adr route=$route sens1=$sens1 sens2=$sens2"
    }

    companion object {

        fun add(a: Node) {
            val trip = Trip()
            val attributes = a.attributes

            for (i in 0 until attributes.length) {
                val theAttribute = attributes.item(i)
                if ((theAttribute.nodeName == "id") ||
                        (theAttribute.nodeName == "adr")) {
                    trip.adr = Integer.parseInt(theAttribute.nodeValue)
                } else if (theAttribute.nodeName == "sens1") {
                    trip.sens1 = Integer.parseInt(theAttribute.nodeValue)
                } else if (theAttribute.nodeName == "sens2") {
                    trip.sens2 = Integer.parseInt(theAttribute.nodeValue)
                } else if (theAttribute.nodeName == "route" || theAttribute.nodeName == "route") {  // old def.
                    trip.route = Integer.parseInt(theAttribute.nodeValue)
                } else if (theAttribute.nodeName == "stopdelay") {
                    trip.stopdelay = Integer.parseInt(theAttribute.nodeValue)
                } else if (theAttribute.nodeName == "loco") {
                    trip.loco = theAttribute.nodeValue
                }
            }
            if (trip.adr != INVALID_INT) {
                trips.add(trip)
            }

        }
    }
}
