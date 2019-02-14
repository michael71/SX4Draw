package de.blankedv.sx4draw

import de.blankedv.sx4draw.SX4Draw.PEType
import de.blankedv.sx4draw.SX4Draw.PEType.SIGNAL
import de.blankedv.sx4draw.SX4Draw.PEType.TRACK
import de.blankedv.sx4draw.SX4Draw.PEType.ROUTEBUTTON
import de.blankedv.sx4draw.SX4Draw.PEType.TURNOUT
import de.blankedv.sx4draw.SX4Draw.PEType.SENSOR
import de.blankedv.sx4draw.Constants.SXMAX_USED
import de.blankedv.sx4draw.Constants.LBMIN
import de.blankedv.sx4draw.Constants.LBMAX
import de.blankedv.sx4draw.Constants.INVALID_INT
import de.blankedv.sx4draw.Constants.DEBUG
import de.blankedv.sx4draw.Constants.RASTER
import de.blankedv.sx4draw.Constants.RECT_X
import de.blankedv.sx4draw.Constants.RECT_Y
import de.blankedv.sx4draw.SX4Draw.panelElements
import de.blankedv.sx4draw.ReadConfig.YOFF

import java.util.ArrayList
import java.util.Comparator

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Line
import javafx.scene.shape.Shape
import javafx.scene.shape.StrokeLineCap
import javafx.util.Pair
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType


/**
 * generic panel element can be any of PEType types
 *
 * @author mblank
 */
@XmlRootElement(name = "signal")
@XmlType
class Signal {

    @get:XmlAttribute
    var name : String? = null

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

    companion object {


    }
}
