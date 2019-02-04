/*
 *<timetable id="3300" time="0,20,40" trip="3100,3101,0" next=""/>
 */
package de.blankedv.sx4draw

import de.blankedv.sx4draw.Constants.INVALID_INT
import de.blankedv.sx4draw.SX4Draw.timetables

import java.util.Comparator

import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node

/**
 * @author mblank
 */
class Timetable  : Comparator<Timetable>, Comparable<Timetable> {

    var id = INVALID_INT
    var time = ""
    var trip = ""
    var next = ""

    override fun compare(o1: Timetable, o2: Timetable): Int {
        return o1.id - o2.id
    }

    override fun compareTo(o1: Timetable): Int {
        return id - o1.id
    }

    companion object {


        fun add(a: Node) {
            val tt = Timetable()
            val attributes = a.attributes

            for (i in 0 until attributes.length) {
                val theAttribute = attributes.item(i)
                if (theAttribute.nodeName == "id") {
                    tt.id = Integer.parseInt(theAttribute.nodeValue)
                } else if (theAttribute.nodeName == "time") {
                    tt.time = theAttribute.nodeValue
                } else if (theAttribute.nodeName == "trip") {
                    tt.trip = theAttribute.nodeValue
                } else if (theAttribute.nodeName == "next") {
                    tt.next = theAttribute.nodeValue
                }
            }
            if (tt.id != INVALID_INT) {
                timetables.add(tt)
            }

        }
    }
}
