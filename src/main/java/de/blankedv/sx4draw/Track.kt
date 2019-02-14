package de.blankedv.sx4draw

import de.blankedv.sx4draw.Constants.INVALID_INT
import javafx.scene.shape.Line
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
@XmlRootElement(name = "track")
@XmlType
class Track : GenericPE {

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

    constructor() {}

    constructor(l : Line) {
        this.x = l.startX.toInt()
        this.x2 = l.endX.toInt()
        this.y = l.startY.toInt()
        this.y2 = l.endY.toInt()
    }

    constructor (pe : PanelElement) {
        if (!pe.name.isBlank()) {
            this.name = pe.name
        }
        this.x = pe.x
        this.y = pe.y
        this.x2 = pe.x2
        this.y2 = pe.y2
    }

    override fun isTouched(touch: IntPoint): Pair<Boolean, Int> {

        val ymin = Math.min(y, y2)
        val ymax = Math.max(y, y2)
        return if (touch.x >= x - PanelElementNew.TOUCH_RADIUS
                && touch.x <= x2 + PanelElementNew.TOUCH_RADIUS
                && touch.y >= ymin - PanelElementNew.TOUCH_RADIUS
                && touch.y <= ymax + PanelElementNew.TOUCH_RADIUS) {
            if (Utils.calcDistanceFromLine(IntPoint(x, y), IntPoint(x2, y2), touch) < PanelElementNew.TOUCH_RADIUS) {
                Pair(true, 0)
            } else {
                Pair(false, 0)
            }
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


    companion object {


    }
}
