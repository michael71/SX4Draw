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

class PanelElement : Comparator<PanelElement>, Comparable<PanelElement> {

    var type = TRACK
    var name = ""
    var x: Int = 0 // starting point
    var y: Int = 0


    var x2 = INVALID_INT // endpoint - x2 always >x
    var y2 = INVALID_INT
    var xt = INVALID_INT // "thrown" position for turnout
    var yt = INVALID_INT
    var inv = 0  // 0 == not inverted
    var adr = INVALID_INT
    var adr2 = INVALID_INT

    // elements for graphics

    protected var route = ""

    var shape: Shape = Line(0.0, 0.0, 1.0, 1.0)   // default shape
    var state = PEState.DEFAULT
        private set

    protected var defaultColor = Color.ALICEBLUE


    enum class PEState {
        DEFAULT, MARKED, SELECTED, STATE_0, STATE_1
    }


    constructor() {}


    constructor(pe: PanelElement) {  // copy
        name = pe.name
        type = pe.type
        x = pe.x
        x2 = pe.x2
        y = pe.y
        y2 = pe.y2
        xt = pe.xt
        yt = pe.yt
        route = pe.route
        inv = pe.inv
        adr = pe.adr
        adr2 = pe.adr2
        shape = pe.shape
        defaultColor = pe.defaultColor
        state = pe.state
        createShapeAndSetState(state)

    }


    constructor(type: de.blankedv.sx4draw.SX4Draw.PEType, l: Line) {
        this.type = type
        this.x = l.startX.toInt()
        this.x2 = l.endX.toInt()
        this.y = l.startY.toInt()
        this.y2 = l.endY.toInt()

        orderXY()
        createShapeAndSetState(PEState.DEFAULT)
        autoAddress()
    }

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

    constructor (gpe : GenericPE) {
        when (gpe) {
            is Track -> {
                type = TRACK

            }

        }
    }

    constructor(type: PEType, poi: IntPoint, closed: IntPoint, thrown: IntPoint) {
        this.type = type
        this.x = poi.x
        this.y = poi.y
        this.x2 = closed.x
        this.y2 = closed.y
        this.xt = thrown.x
        this.yt = thrown.y
        // NO ORDERING HERE
        name = ""
        createShapeAndSetState(PEState.DEFAULT)
        autoAddress()
    }

    constructor(type: PEType, pos: Position) {
        this.type = type
        this.x = pos.x
        this.y = pos.y
        this.x2 = pos.x2
        this.y2 = pos.y2
        this.xt = pos.xt
        this.yt = pos.yt
        name = ""
        createShapeAndSetState(PEState.DEFAULT)
        autoAddress()
    }

    constructor(type: PEType, poi: IntPoint) {
        this.type = type
        this.x = poi.x
        this.y = poi.y
        if (type == SIGNAL) {
            val d = Utils.signalOrientToDXY2(0) // 0 (= 0 grad) is default orientation for signal
            x2 = x + d.x
            y2 = y + d.y
        }
        name = ""
        createShapeAndSetState(PEState.DEFAULT)
        autoAddress()
    }

    private fun autoAddress() {
        if (type == ROUTEBUTTON) {
            // automatically assign route btn address
            var a = 1200  // minumum for route buttons
            for (pe in panelElements) {
                if (pe.type == ROUTEBUTTON) {
                    if (pe.adr >= a) {
                        a = pe.adr + 1
                    }
                }
            }
            adr = a
        } else if (type == SIGNAL) {
            // assign a dummy address to have it stored in xml file (if not assigned by hand)
            // this is needed for
            var a = 4000  // default for signal
            for (pe in panelElements) {
                if (pe.type == SIGNAL) {
                    if (pe.adr >= a) {
                        a = pe.adr + 1
                    }
                }
            }
            adr = a
        } else if (type == SENSOR) {
            if (adr == INVALID_INT) {
                adr = 1
            }
        }
    }

    private fun createShape() {
        when (type) {
            TURNOUT -> {
                defaultColor = Color.ORANGE
                when (state) {
                    PanelElement.PEState.STATE_0 -> {
                        if (inv == 0) {   // not inverted
                            shape = Line(x.toDouble(), y.toDouble(), x2.toDouble(), y2.toDouble())
                            shape.strokeLineCap = StrokeLineCap.ROUND
                            shape.strokeWidth = TRACKWIDTH
                        } else {
                            shape = Line(x.toDouble(), y.toDouble(), xt.toDouble(), yt.toDouble())
                            shape.strokeLineCap = StrokeLineCap.ROUND
                            shape.strokeWidth = TRACKWIDTH
                        }
                        shape.fill = Color.GREEN
                        shape.stroke = Color.GREEN
                    }
                    PanelElement.PEState.STATE_1 -> {
                        if (inv == 0) {   // not inverted
                            shape = Line(x.toDouble(), y.toDouble(), xt.toDouble(), yt.toDouble())
                            shape.strokeLineCap = StrokeLineCap.ROUND
                            shape.strokeWidth = TRACKWIDTH
                        } else {
                            shape = Line(x.toDouble(), y.toDouble(), x2.toDouble(), y2.toDouble())
                            shape.strokeLineCap = StrokeLineCap.ROUND
                            shape.strokeWidth = TRACKWIDTH
                        }
                        shape.fill = Color.RED
                        shape.stroke = Color.RED
                    }
                    else -> {
                        val l1 = Line(x.toDouble(), y.toDouble(), x2.toDouble(), y2.toDouble())
                        l1.strokeLineCap = StrokeLineCap.ROUND
                        l1.strokeWidth = TRACKWIDTH
                        val l2 = Line(x.toDouble(), y.toDouble(), xt.toDouble(), yt.toDouble())
                        l2.strokeLineCap = StrokeLineCap.ROUND
                        l2.strokeWidth = TRACKWIDTH
                        shape = Shape.union(l1, l2)
                        shape.fill = defaultColor
                        shape.stroke = defaultColor
                    }
                }
            }
            TRACK -> {
                defaultColor = Color.BLACK
                shape = Line(x.toDouble(), y.toDouble(), x2.toDouble(), y2.toDouble())
                shape.strokeWidth = TRACKWIDTH
                shape.strokeLineCap = StrokeLineCap.ROUND
            }
            ROUTEBUTTON -> {
                shape = Circle(x.toDouble(), y.toDouble(), 9.5, Color.DARKGREY)
                defaultColor = Color.DARKGREY
            }
            SENSOR -> if (x2 != INVALID_INT) {  //DE type of sensor
                shape = Line(x.toDouble(), y.toDouble(), x2.toDouble(), y2.toDouble())
                shape.strokeWidth = SENSORWIDTH
                shape.strokeDashArray.addAll(15.0, 10.0)
                shape.strokeLineCap = StrokeLineCap.ROUND
                defaultColor = Color.YELLOW

            } else { //US Type SENSOR
                shape = Circle(x.toDouble(), y.toDouble(), 8.0, Color.ORANGE)
                defaultColor = Color.ORANGE
            }
            SIGNAL -> {
                defaultColor = Color.BLACK
                val ls = Line(x.toDouble(), y.toDouble(), x2.toDouble(), y2.toDouble())
                val c = Circle(x.toDouble(), y.toDouble(), 5.0)
                ls.strokeWidth = 1.5
                ls.strokeLineCap = StrokeLineCap.ROUND
                shape = Shape.union(ls, c)
            }
        }

    }

    fun recreateShape() {
        // create shape but don't change state
        createShapeAndSetState(state)
    }


    fun createShapeAndSetState(st: PEState) {
        state = st
        createShape()
        when (state) {
            PanelElement.PEState.DEFAULT -> {
                shape.fill = defaultColor
                shape.stroke = defaultColor
            }
            PanelElement.PEState.SELECTED -> {
                shape.fill = Color.RED
                shape.stroke = Color.RED
            }
            PanelElement.PEState.MARKED -> {
                shape.fill = Color.AQUA
                shape.stroke = Color.AQUA
            }
            PanelElement.PEState.STATE_0 -> {
                shape.fill = Color.RED
                shape.stroke = Color.RED
            }
            PanelElement.PEState.STATE_1 -> {
                shape.fill = Color.GREEN
                shape.stroke = Color.GREEN
            }
        }
    }


    fun isTouched(touch: IntPoint): Pair<Boolean, Int> {
        val ymin: Int
        val ymax: Int
        when (type) {
            SX4Draw.PEType.SIGNAL, SX4Draw.PEType.ROUTEBUTTON -> {
                val dist = Math.sqrt(((touch.x - x) * (touch.x - x) + (touch.y - y) * (touch.y - y)).toDouble())
                val result = dist < TOUCH_RADIUS * 2
                return Pair(result, 0)
            }
            SX4Draw.PEType.TURNOUT ->
                // check first for (x2,y2) touch (state 0)
                return if (touch.x >= x2 - TOUCH_RADIUS
                        && touch.x <= x2 + TOUCH_RADIUS
                        && touch.y >= y2 - TOUCH_RADIUS
                        && touch.y <= y2 + TOUCH_RADIUS) {
                    Pair(true, 0)

                } else if (touch.x >= xt - TOUCH_RADIUS // thrown, state1

                        && touch.x <= xt + TOUCH_RADIUS
                        && touch.y >= yt - TOUCH_RADIUS
                        && touch.y <= yt + TOUCH_RADIUS) {
                    Pair(true, 1)  // thrown state
                } else if (touch.x >= x - TOUCH_RADIUS // next center

                        && touch.x <= x + TOUCH_RADIUS
                        && touch.y >= y - TOUCH_RADIUS
                        && touch.y <= y + TOUCH_RADIUS) {
                    Pair(true, 0)
                } else {
                    Pair(false, 0)
                }
            else -> if (x2 != INVALID_INT) {
                ymin = Math.min(y, y2)
                ymax = Math.max(y, y2)
                return if (touch.x >= x - TOUCH_RADIUS
                        && touch.x <= x2 + TOUCH_RADIUS
                        && touch.y >= ymin - TOUCH_RADIUS
                        && touch.y <= ymax + TOUCH_RADIUS) {
                    if (Utils.calcDistanceFromLine(IntPoint(x, y), IntPoint(x2, y2), touch) < TOUCH_RADIUS) {
                        Pair(true, 0)
                    } else {
                        Pair(false, 0)
                    }
                } else {
                    Pair(false, 0)
                }
            } else {
                // US Sensor
                return if (touch.x >= x - TOUCH_RADIUS
                        && touch.x <= x + TOUCH_RADIUS
                        && touch.y >= y - TOUCH_RADIUS
                        && touch.y <= y + TOUCH_RADIUS) {
                    Pair(true, 0)
                } else {
                    Pair(false, 0)
                }
            }
        }
    }


    fun toggleShapeSelected() {
        if (state != PEState.SELECTED) {
            createShapeAndSetState(PEState.SELECTED)
        } else {
            createShapeAndSetState(PEState.DEFAULT)   // "marked" will be reset also !!
            // color will be reset to default value.
        }
    }

    fun drawAddress(gc: GraphicsContext) {
        if (adr == INVALID_INT) {
            return   // don't draw invalid int
        }
        val sAddr: String
        if (adr < LBMIN) {
            gc.fill = Color.LIGHTBLUE
            sAddr = (adr / 10).toString() + "." + adr % 10
        } else {
            gc.fill = Color.LIGHTSALMON
            sAddr = "" + adr
        }

        gc.fillRect(x.toDouble(), (y - YOFF).toDouble(), (8 * sAddr.length).toDouble(), 12.0)
        gc.strokeText(sAddr, x.toDouble(), (y - YOFF + 10).toDouble())
    }

    override fun compare(o1: PanelElement, o2: PanelElement): Int {
        return if (o1.type.ordinal == o2.type.ordinal) {
            o1.x - o2.x
        } else {
            o1.type.ordinal - o2.type.ordinal
        }
    }

    override fun compareTo(other: PanelElement): Int {
        return if (type.ordinal == other.type.ordinal) {
            x - other.x
        } else {
            type.ordinal - other.type.ordinal
        }
    }

    companion object {

        const val TOUCH_RADIUS = 7
        const val TRACKWIDTH = 5.0
        const val SENSORWIDTH = 4.0

        /**
         * search for a panel element(or elements) when only the address is known
         *
         * @param address
         * @return
         */
        fun getPeByAddress(address: Int): ArrayList<PanelElement> {
            val pelist = ArrayList<PanelElement>()
            for (pe in panelElements) {
                if (pe.adr == address) {
                    pelist.add(pe)
                }
            }
            return pelist
        }

        fun resetState() {
            for (pe in panelElements) {
                pe.createShapeAndSetState(PEState.DEFAULT)
            }
        }

        /**
         * check if at least 60 % of (signal, turnout, sensor) addresses have been
         * entereded and at least 1 PE of this type
         *
         * @param
         * @return
         */
        fun addressCheck(): Boolean {
            var adrOK = 0
            var adrNOK = 0
            var percentage = 0.0
            for (pe in panelElements) {
                if (pe.type != PEType.TRACK && pe.type != PEType.ROUTEBUTTON) {
                    if (pe.adr != INVALID_INT
                            && pe.adr != 0
                            && pe.adr != 1) {
                        adrOK++
                    } else {
                        adrNOK++
                    }
                }
            }
            if (adrOK >= 1) {
                percentage = 100.0 * adrOK / (adrOK + adrNOK)
            }
            return percentage >= 60.0
        }

        /**
         * better fit on display (20,20) smallest (x,y) and for possible upside down" display
         * (=view from other side of the layout) currently
         */
        fun normPositions() {

            // in WriteConfig the NEW values are written !!
            var xmin = INVALID_INT
            var xmax = INVALID_INT
            var ymin = INVALID_INT
            var ymax = INVALID_INT
            var first = true
            for (pe in panelElements) {
                if (first) {
                    xmax = pe.x
                    xmin = xmax
                    ymax = pe.y
                    ymin = ymax
                    first = false
                }

                if (pe.x != INVALID_INT && pe.x < xmin) {
                    xmin = pe.x
                }
                if (pe.x != INVALID_INT && pe.x > xmax) {
                    xmax = pe.x
                }
                if (pe.x2 != INVALID_INT && pe.x2 < xmin) {
                    xmin = pe.x2
                }
                if (pe.x2 != INVALID_INT && pe.x2 > xmax) {
                    xmax = pe.x2
                }
                if (pe.xt != INVALID_INT && pe.xt < xmin) {
                    xmin = pe.xt
                }
                if (pe.xt != INVALID_INT && pe.xt > xmax) {
                    xmax = pe.xt
                }

                if (pe.y != INVALID_INT && pe.y < ymin) {
                    ymin = pe.y
                }
                if (pe.y != INVALID_INT && pe.y > ymax) {
                    ymax = pe.y
                }
                if (pe.y2 != INVALID_INT && pe.y2 < ymin) {
                    ymin = pe.y2
                }
                if (pe.y2 != INVALID_INT && pe.y2 > ymax) {
                    ymax = pe.y2
                }
                if (pe.yt != INVALID_INT && pe.yt < ymin) {
                    ymin = pe.yt
                }
                if (pe.yt != INVALID_INT && pe.yt > ymax) {
                    ymax = pe.yt
                }

            }

            val flipUpsideDown = false
            // now move origin to (20,20+YOFF)
            for (pe in panelElements) {
                if (!flipUpsideDown) {
                    if (pe.x != INVALID_INT) {
                        pe.x = 20 + (pe.x - xmin)
                    }
                    if (pe.x2 != INVALID_INT) {
                        pe.x2 = 20 + (pe.x2 - xmin)
                    }
                    if (pe.xt != INVALID_INT) {
                        pe.xt = 20 + (pe.xt - xmin)
                    }
                    if (pe.y != INVALID_INT) {
                        pe.y = YOFF + 20 + (pe.y - ymin)
                    }
                    if (pe.y2 != INVALID_INT) {
                        pe.y2 = YOFF + 20 + (pe.y2 - ymin)
                    }
                    if (pe.yt != INVALID_INT) {
                        pe.yt = YOFF + 20 + (pe.yt - ymin)
                    }
                } else {
                    if (pe.x != INVALID_INT) {
                        pe.x = 20 + (xmax - pe.x)
                    }
                    if (pe.x2 != INVALID_INT) {
                        pe.x2 = 20 + (xmax - pe.x2)
                    }
                    if (pe.xt != INVALID_INT) {
                        pe.xt = 20 + (xmax - pe.xt)
                    }
                    if (pe.y != INVALID_INT) {
                        pe.y = YOFF + 20 + (ymax - pe.y)
                    }
                    if (pe.y2 != INVALID_INT) {
                        pe.y2 = YOFF + 20 + (ymax - pe.y2)
                    }
                    if (pe.yt != INVALID_INT) {
                        pe.yt = YOFF + 20 + (ymax - pe.yt)
                    }
                }
                pe.createShapeAndSetState(PEState.DEFAULT)

            }

            if (DEBUG) {
                println(" xmin=" + xmin + " xmax=" + xmax + " ymin=" + ymin
                        + " ymax=" + ymax)
            }

            //configHasChanged = true;   ==> will not saved in xml file
        }

        fun translate(d: IntPoint) {
            for (pe in panelElements) {
                if (pe.state == PEState.SELECTED) {
                    pe.x += d.x
                    pe.y += d.y
                    if (pe.x2 != INVALID_INT) {
                        pe.x2 += d.x
                    }
                    if (pe.xt != INVALID_INT) {
                        pe.xt += d.x
                    }
                    if (pe.y2 != INVALID_INT) {
                        pe.y2 += d.y
                    }
                    if (pe.yt != INVALID_INT) {
                        pe.yt += d.y
                    }
                }
                pe.createShape()   // state will be reset to DEFAULT also
            }
        }

        fun atLeastOneSelected(): Boolean {
            for (pe in panelElements) {
                if (pe.state == PEState.SELECTED) {
                    return true
                }
            }
            return false
        }

        fun scalePlus() {

            for (pe in panelElements) {
                val dx = pe.x
                val dy = pe.y
                pe.x = 2 * pe.x
                pe.y = 2 * pe.y
                when (pe.type) {
                    SX4Draw.PEType.TRACK, SX4Draw.PEType.SENSOR -> {
                        if (pe.x2 != INVALID_INT) {
                            pe.x2 = 2 * pe.x2
                        }
                        if (pe.y2 != INVALID_INT) {
                            pe.y2 = 2 * pe.y2
                        }
                    }
                    SX4Draw.PEType.TURNOUT, SX4Draw.PEType.SIGNAL -> {
                        // do not scale x2/y2 BUT TRANSLATE
                        if (pe.x2 != INVALID_INT) {
                            pe.x2 += dx
                        }
                        if (pe.y2 != INVALID_INT) {
                            pe.y2 += dy
                        }
                        if (pe.xt != INVALID_INT) {
                            pe.xt += dx
                        }
                        if (pe.yt != INVALID_INT) {
                            pe.yt += dy
                        }
                    }
                }

                pe.createShapeAndSetState(PEState.DEFAULT)
            }

        }

        fun scaleMinus() {

            for (pe in panelElements) {
                val dx = pe.x / 2
                val dy = pe.y / 2
                pe.x = pe.x / 2
                pe.y = pe.y / 2
                when (pe.type) {
                    SX4Draw.PEType.TRACK, SX4Draw.PEType.SENSOR -> {
                        if (pe.x2 != INVALID_INT) {
                            pe.x2 = pe.x2 / 2
                        }
                        if (pe.y2 != INVALID_INT) {
                            pe.y2 = pe.y2 / 2
                        }
                    }
                    SX4Draw.PEType.TURNOUT, SX4Draw.PEType.SIGNAL -> {
                        // do not scale x2/y2 BUT TRANSLATE
                        if (pe.x2 != INVALID_INT) {
                            pe.x2 -= dx
                        }
                        if (pe.y2 != INVALID_INT) {
                            pe.y2 -= dy
                        }
                        if (pe.xt != INVALID_INT) {
                            pe.xt -= dx
                        }
                        if (pe.yt != INVALID_INT) {
                            pe.yt -= dy
                        }
                    }
                    else -> {
                        //do nothing
                    }
                }

                pe.createShapeAndSetState(PEState.DEFAULT)
            }

        }


    }
}
