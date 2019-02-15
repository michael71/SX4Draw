package de.blankedv.sx4draw

import de.blankedv.sx4draw.Constants.INVALID_INT
import de.blankedv.sx4draw.model.GenericPE
import de.blankedv.sx4draw.model.IntPoint
import de.blankedv.sx4draw.util.Utils
import javafx.scene.shape.Line
import javafx.util.Pair
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType

/**
 *
 * @author mblank
 */
@XmlRootElement(name = "sensor")
@XmlType
class Sensor : GenericPE {

    @get:XmlAttribute
    override var name : String? = null  //optional

    @get:XmlAttribute
    override var x: Int = 0 // starting point

    @get:XmlAttribute
    override var y: Int = 0

    @get:XmlAttribute
    var x2 = INVALID_INT // endpoint - x2 always >x

    @get:XmlAttribute
    var y2 = INVALID_INT

    @get:XmlAttribute(name = "adr")
    var adrStr = "900,1900"

    constructor() {}

    constructor(l : Line) {
        this.x = l.startX.toInt()
        this.x2 = l.endX.toInt()
        this.y = l.startY.toInt()
        this.y2 = l.endY.toInt()
        orderX()
    }

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

    private fun orderX() {
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
}
