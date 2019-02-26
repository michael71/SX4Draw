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
import de.blankedv.sx4draw.Constants.ADDR0_TRIP
import de.blankedv.sx4draw.Constants.INVALID_INT
import de.blankedv.sx4draw.views.SX4Draw.Companion.trips
import java.util.Comparator

import org.w3c.dom.Node
import javax.xml.bind.annotation.*
import kotlin.properties.Delegates

/**
 *
 * <trip adr="3100" route="2300" sens1="924" sens2="902" loco="29,1,126" stopdelay="1500" />
 *
 * @author mblank
 */
@XmlRootElement(name = "trip")
//@XmlType() //TODO Does not work: propOrder = {"adr", "route", "sens1", "sens2", "loco", "stopdelay"})

class Trip(
        @XmlTransient
        private var _adr: Int = INVALID_INT,

        @get:XmlAttribute   // @get: WICHTIG für KOTLIN !!!
        var route: Int = INVALID_INT,

        @get:XmlAttribute
        var sens1: Int = INVALID_INT,

        @get:XmlAttribute
        var sens2: Int = INVALID_INT,

        @get:XmlAttribute
        var loco: String = "",

        @get:XmlAttribute
        var stopdelay: Int = INVALID_INT) : Comparator<Trip>, Comparable<Trip> {


    @get:XmlAttribute(name = "adr")
    var adr: Int by Delegates.observable(_adr) { prop, old, new ->
        if (old != INVALID_INT) {
            println("Trip adr changed from $old to $new")
            Timetable.addressInTripChanged(old.toString(), new.toString())
        }
        _adr = new
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
        fun getUnusedAddress(): Int {

            var a = ADDR0_TRIP  // minumum for trips
            for (tr in trips) {
                if (tr.adr >= a) {
                    a = tr.adr + 1
                }
            }
            return a

        }
    }
}
