/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.blankedv.sx4draw

import de.blankedv.sx4draw.Constants.INVALID_INT
import de.blankedv.sx4draw.SX4Draw.routes

import de.blankedv.sx4draw.PanelElement.PEState

import java.util.ArrayList
import java.util.Comparator

import javafx.util.Pair
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node

/**
 * <route id="2201" btn1="1200" btn2="1203" route="854,1;853,0;852,0;765,2;767,0;761,0;781,0" sensors="924,928" offending=""></route>
 * for editing route data
 *
 * @author mblank
 */
class Route : Comparator<Route>, Comparable<Route> {

    var id = INVALID_INT
    var btn1 = INVALID_INT
    var btn2 = INVALID_INT
    var route = ""
    var sensors = ""
    var offending = ""

    // extract PEs from string information
    private val pEs: ArrayList<PanelElement>
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
                    val peList = PanelElement.getPeByAddress(a)
                    pes.addAll(peList)
                } catch (e: Exception) {
                }

            }
            routeInfo = sensors.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (s in routeInfo) {
                println("sensor $s")
                try {
                    val a = Integer.parseInt(s)
                    val peList = PanelElement.getPeByAddress(a)
                    pes.addAll(peList)
                } catch (e: Exception) {
                }

            }

            println("route contains " + pes.size + " elements")
            return pes
        }

    internal constructor() {

    }

    internal constructor(id: Int) {
        this.id = id
    }

    internal constructor(r: Route) {

        this.id = r.id
        this.btn1 = r.btn1
        this.btn2 = r.btn2
        this.route = r.route
        this.sensors = r.sensors
        this.offending = r.offending
    }

    /**
     * add a turnout, signal or sensor to a route
     *
     * @param pe
     */
    fun addElement(pe: PanelElement) {
        when (pe.type) {
            SX4Draw.PEType.SENSOR -> if (sensors.isEmpty()) {
                sensors = "" + pe.adr
            } else {
                sensors = sensors + "," + pe.adr
            }
            SX4Draw.PEType.TURNOUT, SX4Draw.PEType.SIGNAL -> if (route.isEmpty()) {
                route = "" + pe.adr + ",0"
            } else {
                route = route + ";" + pe.adr + ",0"
            }
        }
    }

    /**
     * add a turnout, signal or sensor to a route
     *
     * @param peSt
     */
    fun addElement(peSt: Pair<PanelElement, Int>) {
        when (peSt.key.type) {
            SX4Draw.PEType.SENSOR -> if (sensors.isEmpty()) {
                sensors = "" + peSt.key.adr
            } else {
                sensors = sensors + "," + peSt.key.adr
            }
            SX4Draw.PEType.TURNOUT -> if (route.isEmpty()) {
                route = "" + peSt.key.adr + "," + peSt.value
            } else {
                route = route + ";" + peSt.key.adr + "," + peSt.value
            }
            SX4Draw.PEType.SIGNAL -> if (route.isEmpty()) {
                route = "" + peSt.key.adr + ",0"
            } else {
                route = route + ";" + peSt.key.adr + ",0"
            }
        }
    }

    /**
     * mark a route by marking all its panel elements
     *
     * @param mark
     */
    fun setMarked(mark: Boolean) {
        val pes = pEs  // implicit setRouteState
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

        PanelElement.resetState()
        // route buttons
        PanelElement.getPeByAddress(btn1)[0].createShapeAndSetState(PEState.MARKED)
        PanelElement.getPeByAddress(btn2)[0].createShapeAndSetState(PEState.MARKED)
        // extract PEs from string information

        // turnouts and signals
        // all have a state "rst" which is different from INVALID_ID
        var routeInfo = route.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (rt in routeInfo) {
            println("routeInfo $rt")
            val elementAndState = rt.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            try {
                val a = Integer.parseInt(elementAndState[0])
                val rst = Integer.parseInt(elementAndState[1])
                val peList = PanelElement.getPeByAddress(a)
                for (pe in peList) {
                    if (rst == 0) {
                        pe.createShapeAndSetState(PEState.STATE_0)
                    } else {
                        pe.createShapeAndSetState(PEState.STATE_1)
                        // TODO distinguish between 1 and 2 (=yellow)
                    }
                    println("set pe.adr=" + pe.adr + " rst=" + rst)
                }
            } catch (e: Exception) {
                // do nothing
            }

        }

        // now sensors
        routeInfo = sensors.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (s in routeInfo) {
            println("sensor $s")
            try {
                val a = Integer.parseInt(s)
                val sensList = PanelElement.getPeByAddress(a)
                for (pe in sensList) {
                    pe.createShapeAndSetState(PEState.MARKED)
                }
            } catch (e: Exception) {
                // do nothing
            }

        }

    }

    override fun compare(o1: Route, o2: Route): Int {
        return o1.id - o2.id

    }

    override fun compareTo(o: Route): Int {
        return id - o.id
    }

    companion object {


        fun add(a: Node) {
            val rt = Route()
            val attributes = a.attributes

            for (i in 0 until attributes.length) {
                val theAttribute = attributes.item(i)
                if (theAttribute.nodeName == "id") {
                    rt.id = Integer.parseInt(theAttribute.nodeValue)
                } else if (theAttribute.nodeName == "btn1") {
                    rt.btn1 = Integer.parseInt(theAttribute.nodeValue)
                } else if (theAttribute.nodeName == "btn2") {
                    rt.btn2 = Integer.parseInt(theAttribute.nodeValue)
                } else if (theAttribute.nodeName == "route") {
                    rt.route = theAttribute.nodeValue
                } else if (theAttribute.nodeName == "sensors") {
                    rt.sensors = theAttribute.nodeValue
                } else if (theAttribute.nodeName == "offending") {
                    rt.offending = theAttribute.nodeValue
                }
            }
            if (rt.id != INVALID_INT) {
                routes.add(rt)
            }
        }

        fun getnewid(): Int {
            var newID = 2200  // start with 2200
            for (rt in routes) {
                if (rt.id > newID) {
                    newID = rt.id
                }
            }
            return newID + 1
        }
    }
}
