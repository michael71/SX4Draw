package de.blankedv.sx4draw;

import de.blankedv.sx4draw.Constants.INVALID_INT
import javafx.util.Pair
import java.util.Comparator
import javax.xml.bind.annotation.XmlAttribute

abstract class GenericPE : Comparator<GenericPE>, Comparable<GenericPE> {

    abstract var name : String?

    abstract var x: Int

    abstract var y: Int

    open fun getAddr() = INVALID_INT

    open fun isTouched(touch : IntPoint) : Pair<Boolean, Int> {
        val dist = Math.sqrt(((touch.x - x) * (touch.x - x) + (touch.y - y) * (touch.y - y)).toDouble())
        val result = dist < PanelElementNew.TOUCH_RADIUS * 2
        return Pair(result, 0)
    }

    override fun compare(o1: GenericPE, o2: GenericPE): Int {
        return o1.x - o2.x
    }

    override fun compareTo(other: GenericPE): Int {
        return x - other.x
    }
}
