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
import de.blankedv.sx4draw.views.SX4Draw
import de.blankedv.sx4draw.views.SX4Draw.Companion.compRoutes
import java.util.Comparator

import org.w3c.dom.Node
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType

/**
 * * <comproute adr="2300" btn1="1200" btn2="1204" routes="2201,2206" />
 * @author mblank
 */
@XmlRootElement(name = "comproute")
@XmlType
data class CompRoute (
        @get:XmlAttribute
        var adr: Int = getAutoAddress(),

        @get:XmlAttribute
        var btn1: Int = 0,   // TODO remove btn1 and btn2 from comproute vars (already implicit in route#)

        @get:XmlAttribute
        var btn2: Int = 0,

        @get:XmlAttribute
        var routes: String = "")
         : Comparator<CompRoute>, Comparable<CompRoute> {


    constructor(cr: CompRoute) : this (cr.adr, cr.btn1, cr.btn2, cr.routes)

    fun reverseCompRoute() : CompRoute {
        val allsens = routes.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var revRoutes = ""
        // order sensors in string in reverse order of original route
        for (i in (allsens.size -1) downTo 0) {
            val s = allsens[i]
            if (revRoutes.isNotEmpty()) revRoutes += ","
            revRoutes += s
        }
        return CompRoute(adr+1, btn2, btn1, revRoutes)
    }

    override fun compare(o1: CompRoute, o2: CompRoute): Int {
        return o1.adr - o2.adr
    }


    override fun compareTo(other: CompRoute): Int {
        return adr - other.adr
    }

    companion object {

        fun add(a: Node) {
            val crt = CompRoute()
            val attributes = a.attributes

            for (i in 0 until attributes.length) {
                val theAttribute = attributes.item(i)
                if ((theAttribute.nodeName == "id") ||
                        (theAttribute.nodeName == "adr") ) {
                    crt.adr = Integer.parseInt(theAttribute.nodeValue)
                } else if (theAttribute.nodeName == "btn1") {
                    crt.btn1 = Integer.parseInt(theAttribute.nodeValue)
                } else if (theAttribute.nodeName == "btn2") {
                    crt.btn2 = Integer.parseInt(theAttribute.nodeValue)
                } else if (theAttribute.nodeName == "routes") {
                    crt.routes = theAttribute.nodeValue
                }
            }
            if (crt.adr != INVALID_INT) {
                compRoutes.add(crt)
            }
        }

        fun getAutoAddress(): Int {
            var newID = Constants.ADDR0_COMPROUTE
            for (crt in SX4Draw.compRoutes) {
                if (crt.adr > newID) {
                    newID = crt.adr
                }
            }
            return newID + 1
        }

        // find a compRoute from a start routeBtn to an end routeBtn
        fun findCompRoute(from : Int, to : Int) : String? {
            return null
            // TODO : Algorithm for compound route detection
        }
    }
}
