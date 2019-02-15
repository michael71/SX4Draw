package de.blankedv.sx4draw

import de.blankedv.sx4draw.Constants.INVALID_INT
import javafx.scene.shape.Line
import javafx.util.Pair
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType

/**
 *
 * @author mblank
 */
@XmlRootElement(name = "panel")
@XmlType
class Panel() {

    @get:XmlAttribute
    var name : String = ""

    constructor(panelName: String) : this() {
        name =  panelName
    }

}
