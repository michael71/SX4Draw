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
@XmlRootElement(name = "panel")
@XmlType
open class PanelElementCore : Comparator<PanelElementCore>, Comparable<PanelElementCore> {

    @get:XmlElement
    var type = TRACK

    @get:XmlAttribute
    var name = ""

    @get:XmlAttribute
    var x: Int = 0 // starting point

    @get:XmlAttribute
    var y: Int = 0

    @get:XmlAttribute
    var x2 = INVALID_INT // endpoint - x2 always >x

    @get:XmlAttribute
    var y2 = INVALID_INT

    @get:XmlAttribute
    var xt = INVALID_INT // "thrown" position for turnout

    @get:XmlAttribute
    var yt = INVALID_INT

    @get:XmlAttribute
    var inv = 0  // 0 == not inverted

    @get:XmlAttribute
    var adr = INVALID_INT

    @get:XmlAttribute
    var adr2 = INVALID_INT


    private fun orderXY() {
        if (x == x2) {
            if (y2 < y) {
                val temp = y2
                y2 = y
                y = temp
            }
        } else if (x2 > x) {
            // everything is fine ....
        } else {
            var temp = x2
            x2 = x
            x = temp
            temp = y2
            y2 = y
            y = temp

        }
    }



    override fun compare(o1: PanelElementCore, o2: PanelElementCore): Int {
        return if (o1.type.ordinal == o2.type.ordinal) {
            o1.x - o2.x
        } else {
            o1.type.ordinal - o2.type.ordinal
        }
    }

    override fun compareTo(other: PanelElementCore): Int {
        return if (type.ordinal == other.type.ordinal) {
            x - other.x
        } else {
            type.ordinal - other.type.ordinal
        }
    }

    companion object {


    }
}
