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

import de.blankedv.sx4draw.Constants.ADDR0_ROUTE
import de.blankedv.sx4draw.Constants.INVALID_INT
import de.blankedv.sx4draw.views.SX4Draw.Companion.routes

import de.blankedv.sx4draw.Constants.PEState
import de.blankedv.sx4draw.PanelElement
import de.blankedv.sx4draw.model.*

import java.util.ArrayList
import java.util.Comparator

import javafx.util.Pair
import org.w3c.dom.Node
import java.lang.NumberFormatException
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType

/**
 * <route adr="2201" btn1="1200" btn2="1203" route="854,1;853,0;852,0;765,2;767,0;761,0;781,0" sensors="924,928"></route>
 * for editing route data
 *
 * @author mblank
 */
@XmlRootElement(name = "route")
@XmlType
class Route : Comparator<Route>, Comparable<Route> {

    @get:XmlAttribute
    var adr = INVALID_INT

    @get:XmlAttribute
    var btn1 = INVALID_INT

    @get:XmlAttribute
    var btn2 = INVALID_INT

    @get:XmlAttribute
    var route = ""

    @get:XmlAttribute
    var sensors = ""

    // extract PEs from string information
    private val pES: ArrayList<PanelElement>
        get() {

            val pes = ArrayList<PanelElement>()
            pes.addAll(PanelElement.getPeByAddress(btn1))
            pes.addAll(PanelElement.getPeByAddress(btn2))
            var routeInfo = route.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (rt in routeInfo) {
                println("routeInfo $rt")
                val elementAndState = rt.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                try {
                    val a = Integer.parseInt(elementAndState[0])
                    if (a != INVALID_INT) {
                        val peList = PanelElement.getPeByAddress(a)
                        pes.addAll(peList)
                    }
                } catch (e: Exception) {
                }

            }
            routeInfo = sensors.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (s in routeInfo) {
                println("sensor $s")
                try {
                    val a = Integer.parseInt(s)
                    if (a != INVALID_INT) {
                        val peList = PanelElement.getPeByAddress(a)
                        pes.addAll(peList)
                    }
                } catch (e: Exception) {
                }

            }

            println("route contains " + pes.size + " elements")
            return pes
        }

    internal constructor() {

    }

    internal constructor(id: Int) {
        this.adr = id
    }

    internal constructor(r: Route) {

        this.adr = r.adr
        this.btn1 = r.btn1
        this.btn2 = r.btn2
        this.route = r.route
        this.sensors = r.sensors
    }

    internal constructor (a : Int, b1 : Int, b2 : Int, rt : String, sens : String) {
        adr = a
        btn1 = b1
        btn2 = b2
        route = rt
        sensors = sens
    }

    /**
     * add a turnout, signal or sensor to a route
     *
     * @param pe
     */
    fun addElement(pe: PanelElement) {
        when (pe.gpe) {
            is Sensor -> if (sensors.isEmpty()) {
                sensors = "" + pe.gpe.getAddr()
            } else {
                sensors = sensors + "," + pe.gpe.getAddr()
            }
            is Turnout -> if (route.isEmpty()) {
                route = "" + pe.gpe.getAddr() + ",0"
            } else {
                route = route + ";" + pe.gpe.getAddr() + ",0"
            }
            else -> {}
        }
    }

    fun reverseRoute() : Route {
        val allsens = sensors.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var revSens = ""
        // order sensors in string in reverse order of original route
        for (i in (allsens.size -1) downTo 0) {
            val s = allsens[i]
              if (revSens.isNotEmpty()) revSens += ","
            revSens += s
        }
        return Route(adr+1, btn2, btn1, route, revSens)
    }

    /**
     * add a turnout, signal or sensor to a route
     *
     * @param peSt
     */
    fun addElement(peSt: Pair<PanelElement, Int>) {
        when (peSt.key.gpe) {
            is Sensor -> if (sensors.isEmpty()) {
                sensors = "" + peSt.key.gpe.getAddr()
            } else {
                sensors = sensors + "," + peSt.key.gpe.getAddr()
            }
            is Turnout -> if (route.isEmpty()) {
                route = "" + peSt.key.gpe.getAddr() + "," + peSt.value
            } else {
                route = route + ";" + peSt.key.gpe.getAddr() + "," + peSt.value
            }
            is Signal -> if (route.isEmpty()) {
                route = "" + peSt.key.gpe.getAddr() + ",0"
            } else {
                route = route + ";" + peSt.key.gpe.getAddr() + ",0"
            }
            else -> {}
        }
    }

    /**
     * mark a route by marking all its panel elements
     *
     * @param mark
     */
    fun setMarked(mark: Boolean) {
        val pes = pES  // implicit setRouteState
        println("setMarked, n=${pes.size}")
        if (mark) {
            for (pe : PanelElement in pes) {
                pe.createShapeAndSetState(PEState.MARKED)
            }
        } else {
            for (pe in pes) {
                pe.createShapeAndSetState(PEState.DEFAULT)
            }
        }
    }

    fun setRouteStates() {
        println("setRouteStates")
        // PanelElement.resetState() TODO
        // route buttons
        PanelElement.getPeByAddress(btn1)[0].createShapeAndSetState(PEState.MARKED)
        PanelElement.getPeByAddress(btn2)[0].createShapeAndSetState(PEState.MARKED)
        // extract PEs from string information

        // turnouts and signals
        // all have a state "rst" which is different from INVALID_ID
        var routeInfo = route.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (rt in routeInfo) {
            val elementAndState = rt.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            try {
                val a = Integer.parseInt(elementAndState[0])
                if (a != INVALID_INT) {
                    val rst = Integer.parseInt(elementAndState[1])
                    val peList = PanelElement.getPeByAddress(a)
                    for (pe in peList) {
                        if (rst == 0) {
                            pe.createShapeAndSetState(PEState.STATE_0)
                        } else {
                            pe.createShapeAndSetState(PEState.STATE_1)
                            // TODO distinguish between 1 and 2 (=yellow)
                        }
                        println("set pe.adr=" + pe.gpe.getAddr() + " rst=" + rst)
                    }
                }
            } catch (e: Exception) {
                // do nothing
            }

        }

        // now sensors
        routeInfo = sensors.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (s in routeInfo) {
            try {
                val a = Integer.parseInt(s)
                if (a != INVALID_INT) {
                    val sensList = PanelElement.getPeByAddress(a)
                    for (pe in sensList) {
                        pe.createShapeAndSetState(PEState.MARKED)
                        println("sensor="+(pe.gpe  as Sensor).getAddr())
                    }
                }
            } catch (e: Exception) {
                // do nothing
            }

        }

    }

    override fun compare(o1: Route, o2: Route): Int {
        return o1.adr - o2.adr

    }

    override fun compareTo(other: Route): Int {
        return adr - other.adr
    }

    companion object {


        fun add(a: Node) {
            val rt = Route()
            val attributes = a.attributes

            for (i in 0 until attributes.length) {
                val theAttribute = attributes.item(i)
                if ((theAttribute.nodeName == "id") ||
                     (theAttribute.nodeName == "adr") ) {
                    rt.adr = Integer.parseInt(theAttribute.nodeValue)
                } else if (theAttribute.nodeName == "btn1") {
                    rt.btn1 = Integer.parseInt(theAttribute.nodeValue)
                } else if (theAttribute.nodeName == "btn2") {
                    rt.btn2 = Integer.parseInt(theAttribute.nodeValue)
                } else if (theAttribute.nodeName == "route") {
                    rt.route = theAttribute.nodeValue
                } else if (theAttribute.nodeName == "sensors") {
                    rt.sensors = theAttribute.nodeValue
                }
            }
            if (rt.adr != INVALID_INT) {
                routes.add(rt)
            }
        }

        fun getAutoAddress(): Int {
            var newID = ADDR0_ROUTE  // start with 2200
            for (rt in routes) {
                if (rt.adr > newID) {
                    newID = rt.adr
                }
            }
            return newID + 1
        }

        fun getByAddress(a : Int): Route? {
            for (rt in routes) {
                if (rt.adr == a) return rt
            }
            return null
        }
    }
}
