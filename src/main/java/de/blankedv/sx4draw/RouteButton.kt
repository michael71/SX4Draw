package de.blankedv.sx4draw

import de.blankedv.sx4draw.Constants.INVALID_INT

import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType


/**
 * generic panel element can be any of PEType types
 *
 * @author mblank
 */
@XmlRootElement(name = "routebutton")
@XmlType
class RouteButton : GenericPE {

    @get:XmlAttribute
    override var name : String? = null

    @get:XmlAttribute
    override var x: Int = 0 // starting point

    @get:XmlAttribute
    override var y: Int = 0

    @get:XmlAttribute
    var adr= 1200

    constructor() {}

    constructor (pe : PanelElement) {
        if (!pe.name.isBlank()) {
            this.name = pe.name
        }
        this.x = pe.x
        this.y = pe.y
        autoAddress()
    }

    override fun getAddr() : Int {
        return adr
    }

    private fun autoAddress() {
        var a = 1200  // minumum for route buttons
        for (pe in SX4Draw.panelElementsNew) {
            if (pe.gpe is RouteButton) {
                if (pe.gpe.getAddr() >= a) {
                    a = pe.gpe.getAddr() + 1
                }
            }
        }
        adr = a
    }
}
