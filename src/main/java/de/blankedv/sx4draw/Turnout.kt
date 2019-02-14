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
@XmlRootElement(name = "turnout")
@XmlType
class Turnout : GenericPE {

    @get:XmlAttribute
    override var name : String? = null

    // if defined in GenericPE, the order in the XML output does not look nice ("x" would be at the end)
    @get:XmlAttribute
    override var x: Int = 0 // starting point

    @get:XmlAttribute
    override var y: Int = 0

    @get:XmlAttribute
    var x2 = INVALID_INT // endpoint - x2 always >x

    @get:XmlAttribute
    var y2 = INVALID_INT

    @get:XmlAttribute
    var xt = INVALID_INT // "thrown" position for turnout

    @get:XmlAttribute
    var yt = INVALID_INT

    @get:XmlAttribute
    var inv : Int? = null  // 0 or null == not inverted

    @get:XmlAttribute
    var adr = 800

    constructor()

    constructor(poi: IntPoint, closed: IntPoint, thrown: IntPoint) {
        x = poi.x
        y = poi.y
        x2 = closed.x
        y2 = closed.y
        xt = thrown.x
        yt = thrown.y
    }

    constructor (pe : PanelElement) {
        if (!pe.name.isBlank()) {
            this.name = pe.name
        }
        this.x = pe.x
        this.y = pe.y
        this.x2 = pe.x2
        this.y2 = pe.y2
        this.xt = pe.xt
        this.yt = pe.yt
        this.adr = pe.adr
        if (pe.inv != 0) {
            this.inv = pe.inv
        }
    }


    override fun isTouched(touch : IntPoint) : Pair<Boolean, Int> {
        // check first for (x2,y2) touch (state 0)
        return if (touch.x >= x2 - PanelElementNew.TOUCH_RADIUS
                && touch.x <= x2 + PanelElementNew.TOUCH_RADIUS
                && touch.y >= y2 - PanelElementNew.TOUCH_RADIUS
                && touch.y <= y2 + PanelElementNew.TOUCH_RADIUS) {
            Pair(true, 0)

        } else if (touch.x >= xt - PanelElementNew.TOUCH_RADIUS // thrown, state1

                && touch.x <= xt + PanelElementNew.TOUCH_RADIUS
                && touch.y >= yt - PanelElementNew.TOUCH_RADIUS
                && touch.y <= yt + PanelElementNew.TOUCH_RADIUS) {
            Pair(true, 1)  // thrown state
        } else if (touch.x >= x - PanelElementNew.TOUCH_RADIUS // near center

                && touch.x <= x + PanelElementNew.TOUCH_RADIUS
                && touch.y >= y - PanelElementNew.TOUCH_RADIUS
                && touch.y <= y + PanelElementNew.TOUCH_RADIUS) {
            Pair(true, 0)
        } else {
            Pair(false, 0)
        }

    }
    /* private fun orderXY() {
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
    } */


}
