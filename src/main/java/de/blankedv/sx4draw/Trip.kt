/*
 *<trip id="3100" routeid="2300" sens1="924" sens2="902" loco="29,1,126" stopdelay="1500" />
 */
package de.blankedv.sx4draw

import de.blankedv.sx4draw.Constants.INVALID_INT
import de.blankedv.sx4draw.SX4Draw.trips

import java.util.Comparator

import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node

/**
 * @author mblank
 */
class Trip : Comparator<Trip>, Comparable<Trip> {

    var id = INVALID_INT
    var routeid = INVALID_INT
    var sens1 = INVALID_INT
    var sens2 = INVALID_INT
    var loco = ""
    var stopdelay = INVALID_INT

    internal constructor()

    override fun compare(o1: Trip, o2: Trip): Int {
        return o1.id - o2.id

    }

    override fun compareTo(o: Trip): Int {
        return id - o.id
    }

    companion object {

        fun add(a: Node) {
            val trip = Trip()
            val attributes = a.attributes

            for (i in 0 until attributes.length) {
                val theAttribute = attributes.item(i)
                if (theAttribute.nodeName == "id") {
                    trip.id = Integer.parseInt(theAttribute.nodeValue)
                } else if (theAttribute.nodeName == "sens1") {
                    trip.sens1 = Integer.parseInt(theAttribute.nodeValue)
                } else if (theAttribute.nodeName == "sens2") {
                    trip.sens2 = Integer.parseInt(theAttribute.nodeValue)
                } else if (theAttribute.nodeName == "routeid" || theAttribute.nodeName == "route") {  // old def.
                    trip.routeid = Integer.parseInt(theAttribute.nodeValue)
                } else if (theAttribute.nodeName == "stopdelay") {
                    trip.stopdelay = Integer.parseInt(theAttribute.nodeValue)
                } else if (theAttribute.nodeName == "loco") {
                    trip.loco = theAttribute.nodeValue
                }
            }
            if (trip.id != INVALID_INT) {
                trips.add(trip)
            }

        }
    }
}
