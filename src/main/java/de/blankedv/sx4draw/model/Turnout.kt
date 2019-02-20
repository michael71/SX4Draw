package de.blankedv.sx4draw

import de.blankedv.sx4draw.Constants.ADDR0_TURNOUT
import de.blankedv.sx4draw.Constants.INVALID_INT
import de.blankedv.sx4draw.model.GenericPE
import de.blankedv.sx4draw.model.IntPoint

import javafx.util.Pair
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType


/**
 *
 * @author mblank
 */
@XmlRootElement(name = "turnout")
@XmlType
class Turnout : GenericPE {

    @get:XmlAttribute
    override var name: String? = null

    // if only defined in GenericPE, the order in the XML output does not look nice ("x" would be at the end)
    // therefore this (in principle unnecessary) "override"
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
    var inv: Int? = null  // 0 or null == not inverted

    @get:XmlAttribute
    var adr = ADDR0_TURNOUT

    override val ord = 2

    constructor()

    constructor(poi: IntPoint, closed: IntPoint, thrown: IntPoint) {
        x = poi.x
        y = poi.y
        x2 = closed.x
        y2 = closed.y
        xt = thrown.x
        yt = thrown.y
        adr = ADDR0_TURNOUT
    }

    override fun scalePlus() {
        val dx = x
        val dy = y
        x = 2 * x
        y = 2 * y

        // do not scale x2/y2 BUT TRANSLATE

        x2 += dx
        y2 += dy
        xt += dx
        yt += dy

    }

    override fun getAddr(): Int {
        return adr
    }


    override fun isTouched(touch: IntPoint): Pair<Boolean, Int> {
        // check first for (x2,y2) touch (state 0)
        return if (touch.x >= x2 - PanelElement.TOUCH_RADIUS
                && touch.x <= x2 + PanelElement.TOUCH_RADIUS
                && touch.y >= y2 - PanelElement.TOUCH_RADIUS
                && touch.y <= y2 + PanelElement.TOUCH_RADIUS) {
            Pair(true, 0)

        } else if (touch.x >= xt - PanelElement.TOUCH_RADIUS // thrown, state1

                && touch.x <= xt + PanelElement.TOUCH_RADIUS
                && touch.y >= yt - PanelElement.TOUCH_RADIUS
                && touch.y <= yt + PanelElement.TOUCH_RADIUS) {
            Pair(true, 1)  // thrown state
        } else if (touch.x >= x - PanelElement.TOUCH_RADIUS // near center

                && touch.x <= x + PanelElement.TOUCH_RADIUS
                && touch.y >= y - PanelElement.TOUCH_RADIUS
                && touch.y <= y + PanelElement.TOUCH_RADIUS) {
            Pair(true, 0)
        } else {
            Pair(false, 0)
        }

    }

    override fun translate (d : IntPoint) {
        x += d.x
        x2 += d.x
        xt += d.x
        y += d.y
        y2 += d.y
        yt += d.y
    }

}
