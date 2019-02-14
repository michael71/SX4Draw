/*
 *<timetable id="3300" time="0,20,40" trip="3100,3101,0" next=""/>
 */
package de.blankedv.sx4draw


import de.blankedv.sx4draw.Constants.INVALID_INT
import java.util.Comparator

import org.w3c.dom.NamedNodeMap
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
    var id : Int = INVALID_INT   // is mandatory

    @get:XmlAttribute
    var time : String? = ""

    @get:XmlAttribute
    var trip : String = ""   // is mandatory

    @get:XmlAttribute
    var next : String? = ""

    override fun compare(o1: Timetable, o2: Timetable): Int {
        // if id can be =null use:   val o2id = (o2.id)?: -1
        return o1.id - o2.id
    }

    override fun compareTo(other: Timetable): Int {
        return id - other.id
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
               // timetables.add(tt)
            }

        }
    }
}