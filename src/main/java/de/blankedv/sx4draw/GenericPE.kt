package de.blankedv.sx4draw;

import java.util.Comparator
import javax.xml.bind.annotation.XmlAttribute

abstract class GenericPE : Comparator<GenericPE>, Comparable<GenericPE> {

    abstract var name : String?

    abstract var x: Int

    abstract var y: Int

    override fun compare(o1: GenericPE, o2: GenericPE): Int {
        return o1.x - o2.x
    }

    override fun compareTo(other: GenericPE): Int {
        return x - other.x
    }
}
