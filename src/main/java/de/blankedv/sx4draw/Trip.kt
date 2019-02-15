/*
 *<trip adr="3100" routeid="2300" sens1="924" sens2="902" loco="29,1,126" stopdelay="1500" />
 */
package de.blankedv.sxdraw


import de.blankedv.sx4draw.Constants.INVALID_INT
import de.blankedv.sx4draw.SX4Draw.trips
import java.util.Comparator

import org.w3c.dom.Node
import javax.xml.bind.annotation.*

/**
 * @author mblank
 */
@XmlRootElement(name = "trip")
@XmlType() //TODO Does not work: propOrder = {"adr", "routeid", "sens1", "sens2", "loco", "stopdelay"})

class Trip : Comparator<Trip>, Comparable<Trip> {

    @get:XmlAttribute    // @get: WICHTIG für KOTLIN !!!
    var adr = INVALID_INT

    @get:XmlAttribute
    var routeid = INVALID_INT

    @get:XmlAttribute
    var sens1 = INVALID_INT

    @get:XmlAttribute
    var sens2  = INVALID_INT

    @get:XmlAttribute
    var loco = ""

    @get:XmlAttribute
    var stopdelay = INVALID_INT

    internal constructor()

    override fun compare(o1: Trip, o2: Trip): Int {
        return o1.adr - o2.adr

    }


    override fun compareTo(other: Trip): Int {
        return adr - other.adr
    }

    companion object {

        fun add(a: Node) {
            val trip = Trip()
            val attributes = a.attributes

            for (i in 0 until attributes.length) {
                val theAttribute = attributes.item(i)
                if ((theAttribute.nodeName == "id") ||
                        (theAttribute.nodeName == "adr")  )  {
                    trip.adr = Integer.parseInt(theAttribute.nodeValue)
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
            if (trip.adr != INVALID_INT) {
                trips.add(trip)
            }

        }
    }
}
