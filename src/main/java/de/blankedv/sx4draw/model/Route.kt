/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.blankedv.sx4draw

import de.blankedv.sx4draw.Constants.INVALID_INT
import de.blankedv.sx4draw.views.SX4Draw.routes

import de.blankedv.sx4draw.PanelElement.PEState
import de.blankedv.sx4draw.views.SX4Draw

import java.util.ArrayList
import java.util.Comparator

import javafx.util.Pair
import org.w3c.dom.Node
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
        this.adr = id
    }

    internal constructor(r: Route) {

        this.adr = r.adr
        this.btn1 = r.btn1
        this.btn2 = r.btn2
        this.route = r.route
        this.sensors = r.sensors
    }

    /**
     * add a turnout, signal or sensor to a route
     *
     * @param pe
     */
    fun addElement(pe: PanelElement) {
        when (pe.gpe::class) {
            Sensor::class -> if (sensors.isEmpty()) {
                sensors = "" + pe.gpe.getAddr()
            } else {
                sensors = sensors + "," + pe.gpe.getAddr()
            }
            Turnout::class -> if (route.isEmpty()) {
                route = "" + pe.gpe.getAddr() + ",0"
            } else {
                route = route + ";" + pe.gpe.getAddr() + ",0"
            }
            else -> {}
        }
    }

    /**
     * add a turnout, signal or sensor to a route
     *
     * @param peSt
     */
    fun addElement(peSt: Pair<PanelElement, Int>) {
        when (peSt.key.gpe::class) {
            Sensor::class -> if (sensors.isEmpty()) {
                sensors = "" + peSt.key.gpe.getAddr()
            } else {
                sensors = sensors + "," + peSt.key.gpe.getAddr()
            }
            Turnout::class -> if (route.isEmpty()) {
                route = "" + peSt.key.gpe.getAddr() + "," + peSt.value
            } else {
                route = route + ";" + peSt.key.gpe.getAddr() + "," + peSt.value
            }
            Signal::class -> if (route.isEmpty()) {
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
                    println("set pe.adr=" + pe.gpe.getAddr() + " rst=" + rst)
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
            var newID = 2200  // start with 2200
            for (rt in routes) {
                if (rt.adr > newID) {
                    newID = rt.adr
                }
            }
            return newID + 1
        }
    }
}
