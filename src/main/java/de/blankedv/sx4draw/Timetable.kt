/*
 *<timetable adr="3300" time="0,20,40" trip="3100,3101,0" next=""/>
 */
package de.blankedv.sx4draw


import de.blankedv.sx4draw.Constants.INVALID_INT
import de.blankedv.sx4draw.SX4Draw.timetables
import java.util.Comparator

import org.w3c.dom.Node
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType

/**
 * @author mblank
 */
@XmlRootElement(name = "timetable")
@XmlType
class Timetable  : Comparator<Timetable>, Comparable<Timetable> {

    @get:XmlAttribute
    var adr : Int = INVALID_INT   // is mandatory

    @get:XmlAttribute
    var time : String? = ""

    @get:XmlAttribute
    var trip : String = ""   // is mandatory

    @get:XmlAttribute
    var next : String? = ""

    override fun compare(o1: Timetable, o2: Timetable): Int {
        // if adr can be =null use:   val o2id = (o2.adr)?: -1
        return o1.adr - o2.adr
    }

    override fun compareTo(other: Timetable): Int {
        return adr - other.adr
    }

    companion object {


        fun add(a: Node) {
            val tt = Timetable()
            val attributes = a.attributes

            for (i in 0 until attributes.length) {
                val theAttribute = attributes.item(i)
                if ((theAttribute.nodeName == "id") ||
                        (theAttribute.nodeName == "adr")  )  {
                    tt.adr = Integer.parseInt(theAttribute.nodeValue)
                } else if (theAttribute.nodeName == "time") {
                    tt.time = theAttribute.nodeValue
                } else if (theAttribute.nodeName == "trip") {
                    tt.trip = theAttribute.nodeValue
                } else if (theAttribute.nodeName == "next") {
                    tt.next = theAttribute.nodeValue
                }
            }
            if (tt.adr != INVALID_INT) {
               timetables.add(tt)
            }

        }
    }
}
