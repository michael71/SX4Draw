package de.blankedv.sx4draw

import de.blankedv.sx4draw.Constants.INVALID_INT
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType

/**
 *
 * @author mblank
 */
@XmlRootElement(name = "sensor")
@XmlType
class Sensor {

    //var adr = INVALID_INT
    //var adr2 = INVALID_INT  //optional

    @get:XmlAttribute
    var name : String? = null  //optional

    @get:XmlAttribute
    var x: Int = 0 // starting point

    @get:XmlAttribute
    var y: Int = 0

    @get:XmlAttribute
    var x2 = INVALID_INT // endpoint - x2 always >x

    @get:XmlAttribute
    var y2 = INVALID_INT

    @get:XmlAttribute(name = "adr")
    var adrStr = ""

    constructor() {}

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

}
