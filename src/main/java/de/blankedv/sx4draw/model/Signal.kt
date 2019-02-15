package de.blankedv.sx4draw

import de.blankedv.sx4draw.Constants.ADDR0_SIGNAL
import de.blankedv.sx4draw.Constants.INVALID_INT
import de.blankedv.sx4draw.views.SX4Draw.*
import de.blankedv.sx4draw.model.GenericPE
import de.blankedv.sx4draw.model.IntPoint
import de.blankedv.sx4draw.util.Utils

import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType


/**
 * generic panel element can be any of PEType types
 *
 * @author mblank
 */
@XmlRootElement(name = "signal")
@XmlType
class Signal : GenericPE {

    @get:XmlAttribute
    override var name : String? = null

    @get:XmlAttribute
    override var x: Int = 0 // starting point

    @get:XmlAttribute
    override var y: Int = 0

    @get:XmlAttribute
    var x2 = INVALID_INT // endpoint - x2 always >x

    @get:XmlAttribute
    var y2 = INVALID_INT

    @get:XmlAttribute(name = "adr")
    var adrStr = ""


    constructor() {}

    constructor(poi : IntPoint) {
        x = poi.x
        y = poi.y
        val d = Utils.signalOrientToDXY2(0) // 0 (= 0 grad) is default orientation for signal
        x2 = x + d.x
        y2 = y + d.y
        autoAddress()
    }


    constructor (pe : PanelElement) {
        if (!pe.name.isBlank()) {
            this.name = pe.name
        }
        this.x = pe.x
        this.y = pe.y
        this.x2 = pe.x2
        this.y2 = pe.y2
        this.adrStr = "" + pe.adr
        if (pe.adr2 != INVALID_INT) {
            this.adrStr += "," + pe.adr2
        }
    }

    private fun autoAddress() {
        var a = ADDR0_SIGNAL // default for signal
        for (pe in panelElementsNew) {
            if (pe.gpe is Signal) {
                if (pe.gpe.getAddr() >= a) {
                    a = pe.gpe.getAddr() + 1
                }
            }
        }
        adrStr = "" + a
    }

    companion object {


    }
}
