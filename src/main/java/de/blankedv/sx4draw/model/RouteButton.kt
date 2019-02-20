package de.blankedv.sx4draw

import de.blankedv.sx4draw.Constants.ADDR0_ROUTEBUTTON
import de.blankedv.sx4draw.Constants.INVALID_INT
import de.blankedv.sx4draw.model.GenericPE
import de.blankedv.sx4draw.model.IntPoint
import de.blankedv.sx4draw.views.SX4Draw
import de.blankedv.sx4draw.views.SX4Draw.Companion.panelElements

import javax.xml.bind.annotation.XmlAttribute
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
    var adr= INVALID_INT

    @get:XmlAttribute
    override var name : String? = null

    @get:XmlAttribute
    override var x: Int = 0 // starting point

    @get:XmlAttribute
    override var y: Int = 0

    constructor() {}

    /*constructor (pe : PanelElement) {
        if (!pe.name.isBlank()) {
            this.name = pe.name
        }
        this.x = pe.x
        this.y = pe.y
        if (pe.adr == INVALID_INT) {
            autoAddress()
        } else {
            this.adr = pe.adr
        }
    } */

    constructor (poi : IntPoint) {
        x = poi.x
        y = poi.y
        autoAddress()
    }

    override fun getAddr() : Int {
        return adr
    }

    private fun autoAddress() {
        var a = ADDR0_ROUTEBUTTON  // minumum for route buttons
        for (pe in panelElements) {
            if (pe.gpe is RouteButton) {
                if (pe.gpe.getAddr() >= a) {
                    a = pe.gpe.getAddr() + 1
                }
            }
        }
        adr = a
    }
}
