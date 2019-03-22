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
import de.blankedv.sx4draw.views.RoutesTable
import de.blankedv.sx4draw.views.SX4Draw.Companion.trips

import java.util.ArrayList
import java.util.Comparator

import javafx.util.Pair
import org.w3c.dom.Node
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlTransient
import javax.xml.bind.annotation.XmlType
import kotlin.properties.Delegates

/**
 * <route adr="2201" btn1="1200" btn2="1203" route="854,1;853,0;852,0;765,2;767,0;761,0;781,0" sensors="924,928"></route>
 * for editing route data
 *
 * @author mblank
 */
@XmlRootElement(name = "route")
@XmlType
class Route(

        @get:XmlTransient
        private var _adr: Int = INVALID_INT,

        @get:XmlAttribute
        var btn1: Int = INVALID_INT,

        @get:XmlAttribute
        var btn2: Int = INVALID_INT,

        @get:XmlAttribute
        var route: String = "",

        @get:XmlAttribute
        var sensors: String = ""

) : Comparator<Route>, Comparable<Route> {

    @get:XmlAttribute(name = "adr")
    var adr: Int by Delegates.observable(_adr) { prop, old, new ->
        if (old != INVALID_INT) {
            println("Route adr changed from $old to $new")
            CompRoute.addressInRoutesChanged(old.toString(), new.toString())
        }
        _adr = new
    }


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

    fun findSens1(): Int {
        val allsens = sensors.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (allsens.size >= 1) {
            return allsens[0].toInt()
        } else {
            return INVALID_INT
        }
    }

    fun findSens2(): Int {
        val allsens = sensors.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (allsens.size >= 1) {
            return allsens[allsens.size - 1].toInt()
        } else {
            return INVALID_INT
        }
    }

    fun reverseRoute(): Route {
        val allsens = sensors.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var revSens = ""
        // order sensors in string in reverse order of original route
        for (i in (allsens.size - 1) downTo 0) {
            val s = allsens[i]
            if (revSens.isNotEmpty()) revSens += ","
            revSens += s
        }
        return Route(adr + 1, btn2, btn1, route, revSens)
    }

    fun copy(): Route {
        return Route(adr, btn1, btn2, route, sensors)
    }

    /**
     * add a turnout, signal or sensor to a route (with a state)
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
                route = "" + peSt.key.gpe.getAddr() + "," + peSt.value
            } else {
                route = route + ";" + peSt.key.gpe.getAddr() + "," + peSt.value
            }
            else -> {
            }
        }
    }

    // when building routes, the desired signal state can be set
    // when addr2!=INVALID_INT, state can be 0...3   (else: 0/1 only)
    fun incrementSignalState(pe: PanelElement): PEState {
        val addr = pe.gpe.getAddr()
        val nBit2 = (pe.gpe.getAddr2() != INVALID_INT)   // second address => 2 bit signal, values 0..3
        var oldState = route.substringAfter("$addr,")
        //println("oldSt-1 $oldState")
        oldState = oldState.substring(0..0)
        //println("oldSt-2 $oldState")
        var newState = "1"
        var result: PEState
        when (oldState) {
            "0" -> {
                newState = "1"; result = PEState.STATE_1      // hp1 green
            }
            "1" -> {
                if (nBit2) {
                    newState = "2"; result = PEState.STATE_2    // hp2 yellow
                }
                else {
                    newState = "0"; result = PEState.STATE_0
                }
            }
            "2" -> {
                if (nBit2) {
                    newState = "3"; result = PEState.STATE_3   // sh0 red/white
                }
                else {
                    newState = "0"; result = PEState.STATE_0
                }
            }
            "3" -> {
                newState = "0"; result = PEState.STATE_0      // hp0 red
            }
            else -> {
                newState = "0"; result = PEState.STATE_0
            }
        }
        val o = "$addr,$oldState"
        val n = "$addr,$newState"
        //println("oldSt=$oldState newSt=$newState, old.route=$route")
        route = route.replace(o, n)
        //println("new.route=$route")
        return result
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
            for (pe: PanelElement in pes) {
                pe.createShapeAndSetState(PEState.MARKED)
            }
        } else {
            for (pe in pes) {
                pe.createShapeAndSetState(PEState.DEFAULT)
            }
        }
    }

    fun uniqueAccessories() {
        // remove all "doubled sensors" BUT keep start sensor (sens1) and end sensor (sens2) at the same position !!!
        var s2 = sensors.split(",")
        if (s2.size >=3) {
            val sens1 = s2[0]   // the start and end sensors must remain at the same position !!!
            val sens2 = s2[s2.size-1]
            val s2Mid = s2.slice(1 until s2.size-1).distinct().sorted()
            sensors = sens1 + "," + s2Mid.joinToString(separator = ",") + "," + sens2
        }
        // remove all "doubled" turnouts and signals
        var rt = route.split(";")
        rt = rt.distinct().sorted()
        route = rt.joinToString(separator = ";")
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
                        when (rst) {
                            0 -> pe.createShapeAndSetState(PEState.STATE_0)
                            1 -> pe.createShapeAndSetState(PEState.STATE_1)
                            2 -> pe.createShapeAndSetState(PEState.STATE_2)
                            3 -> pe.createShapeAndSetState(PEState.STATE_3)
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
                        println("sensor=" + (pe.gpe as Sensor).getAddr())
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
                        (theAttribute.nodeName == "adr")) {
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

        fun getByAddress(a: Int): Route? {
            for (rt in routes) {
                if (rt.adr == a) return rt
            }
            return null
        }



        // change sensor addresses in all routes and trips, if the sensor address was changed
        fun sensorAddressChanged(oAdr: String, nAdr: String) {
            for (rt in routes) {
                val oldSensors = "," + rt.sensors +","
                rt.sensors = oldSensors.replace(","+oAdr+",", ","+nAdr+",")

                // remove leading and trailing ","
                if (rt.sensors[0] == ',') rt.sensors = rt.sensors.substring(1)
                if (rt.sensors[rt.sensors.length-1] == ',') rt.sensors = rt.sensors.dropLast(1)
                println("rt.sensors old=$oldSensors new=${rt.sensors}")

                for (tr in trips.filter { it -> it.route == rt.adr }) {
                    if (tr.sens1 == oAdr.toInt()) {
                        tr.sens1 = nAdr.toInt()
                    }
                    if (tr.sens2 == oAdr.toInt()) {
                        tr.sens2 = nAdr.toInt()
                    }
                }
            }
            RoutesTable.refresh()
        }

        // change turnout/signal addresses in all routes and trips, if the turnout/signal address was changed
        fun addressInRouteChanged(oAdr: String, nAdr: String) {
            for (rt in routes) {
                val oldRoute = ";"+rt.route
                rt.route = oldRoute.replace(";"+oAdr+",", ";"+nAdr+",")
                // remove leading ";"
                if (rt.route[0] == ';') rt.route = rt.route.substring(1)
                println("rt.route old=$oldRoute new=${rt.route}")
            }
            RoutesTable.refresh()
        }


    }
}
