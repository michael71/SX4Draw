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
@XmlRootElement(name = "sensorus")
@XmlType
class SensorUS {

    @get:XmlAttribute
    var name : String? = null

    @get:XmlAttribute
    var x: Int = 0 // starting point

    @get:XmlAttribute
    var y: Int = 0

    @get:XmlAttribute
    var adr = INVALID_INT


    constructor() {}

    constructor (pe : PanelElement) {
        if (!pe.name.isBlank()) {
            this.name = pe.name
        }
        this.x = pe.x
        this.y = pe.y
        this.adr = pe.adr
    }

}
