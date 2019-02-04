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
package de.blankedv.sx4draw

import de.blankedv.sx4draw.Constants.INVALID_INT
import de.blankedv.sx4draw.SX4Draw.compRoutes

import java.util.Comparator

import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node

/**
 * * <comproute id="2300" btn1="1200" btn2="1204" routes="2201,2206" />
 * @author mblank
 */
data class CompRoute (var id: Int = 0, var btn1: Int = 0, var btn2: Int = 0, var routes: String = "")
                                     : Comparator<CompRoute>, Comparable<CompRoute> {


    override fun compare(o1: CompRoute, o2: CompRoute): Int {
        return o1.id - o2.id
    }


    override fun compareTo(other: CompRoute): Int {
        return id - other.id
    }

    companion object {

        fun add(a: Node) {
            val rt = CompRoute()
            val attributes = a.attributes

            for (i in 0 until attributes.length) {
                val theAttribute = attributes.item(i)
                if (theAttribute.nodeName == "id") {
                    rt.id = Integer.parseInt(theAttribute.nodeValue)
                } else if (theAttribute.nodeName == "btn1") {
                    rt.btn1 = Integer.parseInt(theAttribute.nodeValue)
                } else if (theAttribute.nodeName == "btn2") {
                    rt.btn2 = Integer.parseInt(theAttribute.nodeValue)
                } else if (theAttribute.nodeName == "routes") {
                    rt.routes = theAttribute.nodeValue
                }
            }
            if (rt.id != INVALID_INT) {
                compRoutes.add(rt)
            }
        }
    }
}
