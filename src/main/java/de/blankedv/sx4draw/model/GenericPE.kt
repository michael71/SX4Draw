package de.blankedv.sx4draw.model;

import de.blankedv.sx4draw.Constants.INVALID_INT
import de.blankedv.sx4draw.PanelElement
import javafx.util.Pair
import java.util.Comparator

abstract class GenericPE() : Comparator<GenericPE>, Comparable<GenericPE> {

    abstract var name : String?

    abstract var x: Int

    abstract var y: Int

    // order for graphics Z axis
    open val ord: Int = 0

    open fun getAddr() = INVALID_INT

    open fun getAddr2() = INVALID_INT

    open fun translate(d: IntPoint) {
        x += d.x
        y += d.y
    }

    open fun scalePlus() {
        x = 2 * x
        y = 2 * y
    }

    open fun scaleMinus() {
        x = x / 2
        y = y / 2
    }


    open fun isTouched(touch : IntPoint) : Pair<Boolean, Int> {
        val dist = Math.sqrt(((touch.x - x) * (touch.x - x) + (touch.y - y) * (touch.y - y)).toDouble())
        val result = dist < PanelElement.TOUCH_RADIUS * 2
        return Pair(result, 0)
    }

    override fun compare(o1: GenericPE, o2: GenericPE): Int {
        return o1.x - o2.x
    }

    override fun compareTo(other: GenericPE): Int {
        return x - other.x
    }
}
