package de.blankedv.sx4draw

import de.blankedv.sx4draw.Constants.LBMIN
import de.blankedv.sx4draw.Constants.INVALID_INT
import de.blankedv.sx4draw.ReadConfig.YOFF
import de.blankedv.sx4draw.SX4Draw.*

import de.blankedv.sx4draw.SX4Draw.PEType.TRACK
import de.blankedv.sx4draw.SX4Draw.PEType.SENSOR
import de.blankedv.sx4draw.SX4Draw.PEType.TURNOUT
import de.blankedv.sx4draw.SX4Draw.PEType.SIGNAL

import java.util.ArrayList
import java.util.Comparator

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Line
import javafx.scene.shape.Shape
import javafx.scene.shape.StrokeLineCap



/**
 * generic panel element can be any of PEType types
 *
 * @author mblank
 */

class PanelElementNew  {

    lateinit var gpe: GenericPE

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

    constructor(pe: PanelElementNew) {  // copy
        // TODO
    }


    constructor(type: PEType, l: Line) {
        when (type) {
            TRACK -> this.gpe = Track(l)
            SENSOR -> this.gpe = Sensor(l)
        }

        //orderXY()
        createShapeAndSetState(PEState.DEFAULT)
    }

    constructor(poi: IntPoint, closed: IntPoint, thrown: IntPoint) {
        this.gpe = Turnout(poi, closed, thrown)
        createShapeAndSetState(PEState.DEFAULT)
    }

    constructor(type: PEType, pos: Position) {
        System.out.println("UNHandled type=" + type)
        //createShapeAndSetState(PEState.DEFAULT)
        //autoAddress()
    }

    constructor(type: PEType, poi: IntPoint) {
        when (type) {
            SIGNAL -> {
                this.gpe = Signal(poi)
                createShapeAndSetState(PEState.DEFAULT)
            }
        }
   }


    private fun createShape() {
        val g = gpe
        when (g) {
            is Turnout -> {
                defaultColor = Color.ORANGE
                when (state) {
                    PanelElementNew.PEState.STATE_0 -> {
                        if (g.inv == 0) {   // not inverted
                            shape = Line(g.x.toDouble(), g.y.toDouble(), g.x2.toDouble(), g.y2.toDouble())
                            shape.strokeLineCap = StrokeLineCap.ROUND
                            shape.strokeWidth = TRACKWIDTH
                        } else {
                            shape = Line(g.x.toDouble(), g.y.toDouble(), g.xt.toDouble(), g.yt.toDouble())
                            shape.strokeLineCap = StrokeLineCap.ROUND
                            shape.strokeWidth = TRACKWIDTH
                        }
                        shape.fill = Color.GREEN
                        shape.stroke = Color.GREEN
                    }
                    PanelElementNew.PEState.STATE_1 -> {
                        if (g.inv == 0) {   // not inverted
                            shape = Line(g.x.toDouble(), g.y.toDouble(), g.xt.toDouble(), g.yt.toDouble())
                            shape.strokeLineCap = StrokeLineCap.ROUND
                            shape.strokeWidth = TRACKWIDTH
                        } else {
                            shape = Line(g.x.toDouble(), g.y.toDouble(), g.x2.toDouble(), g.y2.toDouble())
                            shape.strokeLineCap = StrokeLineCap.ROUND
                            shape.strokeWidth = TRACKWIDTH
                        }
                        shape.fill = Color.RED
                        shape.stroke = Color.RED
                    }
                    else -> {
                        val l1 = Line(g.x.toDouble(), g.y.toDouble(), g.x2.toDouble(), g.y2.toDouble())
                        l1.strokeLineCap = StrokeLineCap.ROUND
                        l1.strokeWidth = TRACKWIDTH
                        val l2 = Line(g.x.toDouble(), g.y.toDouble(), g.xt.toDouble(), g.yt.toDouble())
                        l2.strokeLineCap = StrokeLineCap.ROUND
                        l2.strokeWidth = TRACKWIDTH
                        shape = Shape.union(l1, l2)
                        shape.fill = defaultColor
                        shape.stroke = defaultColor
                    }
                }
            }
            is Track -> {
                defaultColor = Color.BLACK
                shape = Line(g.x.toDouble(), g.y.toDouble(), g.x2.toDouble(), g.y2.toDouble())
                shape.strokeWidth = TRACKWIDTH
                shape.strokeLineCap = StrokeLineCap.ROUND
            }
            is RouteButton -> {
                shape = Circle(g.x.toDouble(), g.y.toDouble(), 9.5, Color.DARKGREY)
                defaultColor = Color.DARKGREY
            }
            is Sensor -> {  //DE type of sensor
                shape = Line(g.x.toDouble(), g.y.toDouble(), g.x2.toDouble(), g.y2.toDouble())
                shape.strokeWidth = SENSORWIDTH
                shape.strokeDashArray.addAll(15.0, 10.0)
                shape.strokeLineCap = StrokeLineCap.ROUND
                defaultColor = Color.YELLOW
            }
            is SensorUS -> { //US Type SENSOR
                shape = Circle(g.x.toDouble(), g.y.toDouble(), 8.0, Color.ORANGE)
                defaultColor = Color.ORANGE
            }
            is Signal -> {
                defaultColor = Color.BLACK
                val ls = Line(g.x.toDouble(), g.y.toDouble(), g.x2.toDouble(), g.y2.toDouble())
                val c = Circle(g.x.toDouble(), g.y.toDouble(), 5.0)
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
            PanelElementNew.PEState.DEFAULT -> {
                shape.fill = defaultColor
                shape.stroke = defaultColor
            }
            PanelElementNew.PEState.SELECTED -> {
                shape.fill = Color.RED
                shape.stroke = Color.RED
            }
            PanelElementNew.PEState.MARKED -> {
                shape.fill = Color.AQUA
                shape.stroke = Color.AQUA
            }
            PanelElementNew.PEState.STATE_0 -> {
                shape.fill = Color.RED
                shape.stroke = Color.RED
            }
            PanelElementNew.PEState.STATE_1 -> {
                shape.fill = Color.GREEN
                shape.stroke = Color.GREEN
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
        val addr = gpe.getAddr()
        if (addr == INVALID_INT) {
            return   // don't draw invalid int
        }
        val sAddr: String
        if (addr < LBMIN) {
            gc.fill = Color.LIGHTBLUE
            sAddr = (addr / 10).toString() + "." + addr % 10
        } else {
            gc.fill = Color.LIGHTSALMON
            sAddr = "" + addr
        }

        gc.fillRect(gpe.x.toDouble(), (gpe.y - YOFF).toDouble(), (8 * sAddr.length).toDouble(), 12.0)
        gc.strokeText(sAddr, gpe.x.toDouble(), (gpe.y - YOFF + 10).toDouble())
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
        fun getPeByAddress(address: Int): ArrayList<PanelElementNew> {
            val pelist = ArrayList<PanelElementNew>()
            for (pe in panelElementsNew) {
                if (pe.gpe.getAddr() == address) {
                    pelist.add(pe)
                }
            }
            return pelist
        }

        fun resetState() {
            for (pe in panelElementsNew) {
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
        /*  TODO
            var xmin = INVALID_INT
            var xmax = INVALID_INT
            var ymin = INVALID_INT
            var ymax = INVALID_INT
            var first = true
            for (pe in panelElementsNew) {
                if (first) {
                    xmax = pe.gpe.x
                    xmin = xmax
                    ymax = pe.gpe.y
                    ymin = ymax
                    first = false
                }

                if (pe.gpe.x != INVALID_INT && pe.gpe.x < xmin) {
                    xmin = pe.gpe.x
                }
                if (pe.gpe.x != INVALID_INT && pe.gpe.x > xmax) {
                    xmax = pe.gpe.x
                }
                if (pe.gpe.x2 != INVALID_INT && pe.gpe.x2 < xmin) {
                    xmin = pe.gpe.x2
                }
                if (pe.gpe.x2 != INVALID_INT && pe.gpe.x2 > xmax) {
                    xmax = pe.gpe.x2
                }
                if (pe.gpe.xt != INVALID_INT && pe.gpe.xt < xmin) {
                    xmin = pe.gpe.xt
                }
                if (pe.gpe.xt != INVALID_INT && pe.gpe.xt > xmax) {
                    xmax = pe.gpe.xt
                }

                if (pe.gpe.y != INVALID_INT && pe.gpe.y < ymin) {
                    ymin = pe.gpe.y
                }
                if (pe.gpe.y != INVALID_INT && pe.gpe.y > ymax) {
                    ymax = pe.gpe.y
                }
                if (pe.gpe.y2 != INVALID_INT && pe.gpe.y2 < ymin) {
                    ymin = pe.gpe.y2
                }
                if (pe.gpe.y2 != INVALID_INT && pe.gpe.y2 > ymax) {
                    ymax = pe.gpe.y2
                }
                if (pe.gpe.yt != INVALID_INT && pe.gpe.yt < ymin) {
                    ymin = pe.gpe.yt
                }
                if (pe.gpe.yt != INVALID_INT && pe.gpe.yt > ymax) {
                    ymax = pe.gpe.yt
                }

            }

            val flipUpsideDown = false
            // now move origin to (20,20+YOFF)
            for (pe in panelElementsNew) {
                if (!flipUpsideDown) {
                    if (pe.gpe.x != INVALID_INT) {
                        pe.gpe.x = 20 + (pe.gpe.x - xmin)
                    }
                    if (pe.gpe.x2 != INVALID_INT) {
                        pe.gpe.x2 = 20 + (pe.gpe.x2 - xmin)
                    }
                    if (pe.gpe.xt != INVALID_INT) {
                        pe.gpe.xt = 20 + (pe.gpe.xt - xmin)
                    }
                    if (pe.gpe.y != INVALID_INT) {
                        pe.gpe.y = YOFF + 20 + (pe.gpe.y - ymin)
                    }
                    if (pe.gpe.y2 != INVALID_INT) {
                        pe.gpe.y2 = YOFF + 20 + (pe.gpe.y2 - ymin)
                    }
                    if (pe.gpe.yt != INVALID_INT) {
                        pe.gpe.yt = YOFF + 20 + (pe.gpe.yt - ymin)
                    }
                } else {
                    if (pe.gpe.x != INVALID_INT) {
                        pe.gpe.x = 20 + (xmax - pe.gpe.x)
                    }
                    if (pe.gpe.x2 != INVALID_INT) {
                        pe.gpe.x2 = 20 + (xmax - pe.gpe.x2)
                    }
                    if (pe.gpe.xt != INVALID_INT) {
                        pe.gpe.xt = 20 + (xmax - pe.gpe.xt)
                    }
                    if (pe.gpe.y != INVALID_INT) {
                        pe.gpe.y = YOFF + 20 + (ymax - pe.gpe.y)
                    }
                    if (pe.gpe.y2 != INVALID_INT) {
                        pe.gpe.y2 = YOFF + 20 + (ymax - pe.gpe.y2)
                    }
                    if (pe.gpe.yt != INVALID_INT) {
                        pe.gpe.yt = YOFF + 20 + (ymax - pe.yt)
                    }
                }
                pe.createShapeAndSetState(PEState.DEFAULT)

            }

            if (DEBUG) {
                println(" xmin=" + xmin + " xmax=" + xmax + " ymin=" + ymin
                        + " ymax=" + ymax)
            }

            //configHasChanged = true;   ==> will not saved in xml file
            */
        }


        fun atLeastOneSelected(): Boolean {
            for (pe in panelElementsNew) {
                if (pe.state == PEState.SELECTED) {
                    return true
                }
            }
            return false
        }

        fun scalePlus() {

            for (pe in panelElementsNew) {
                val g = pe.gpe
                val dx = g.x
                val dy = g.y
                g.x = 2 * g.x
                g.y = 2 * g.y

                when (g) {
                    is Track -> {
                        if (g.x2 != INVALID_INT) {
                            g.x2 = 2 * g.x2
                        }
                        if (g.y2 != INVALID_INT) {
                            g.y2 = 2 * g.y2
                        }
                    }
                    is Sensor -> {
                        if (g.x2 != INVALID_INT) {
                            g.x2 = 2 * g.x2
                        }
                        if (g.y2 != INVALID_INT) {
                            g.y2 = 2 * g.y2
                        }
                    }
                    is Signal -> {
                        // do not scale x2/y2 BUT TRANSLATE
                        if (g.x2 != INVALID_INT) {
                            g.x2 += dx
                        }
                        if (g.y2 != INVALID_INT) {
                            g.y2 += dy
                        }
                    }
                    is Turnout -> {
                        // do not scale x2/y2 BUT TRANSLATE
                        if (g.x2 != INVALID_INT) {
                            g.x2 += dx
                        }
                        if (g.y2 != INVALID_INT) {
                            g.y2 += dy
                        }
                        if (g.xt != INVALID_INT) {
                            g.xt += dx
                        }
                        if (g.yt != INVALID_INT) {
                            g.yt += dy
                        }
                    }
                }

                pe.createShapeAndSetState(PEState.DEFAULT)
            }

        }

        fun scaleMinus() {

            for (pe in panelElementsNew) {
                val g = pe.gpe
                val dx = g.x / 2
                val dy = g.y / 2
                g.x = g.x / 2
                g.y = g.y / 2
                when (g) {
                    is Track -> {
                        if (g.x2 != INVALID_INT) {
                            g.x2 = g.x2 / 2
                        }
                        if (g.y2 != INVALID_INT) {
                            g.y2 = g.y2 / 2
                        }
                    }
                    is Sensor -> {
                        if (g.x2 != INVALID_INT) {
                            g.x2 = g.x2 / 2
                        }
                        if (g.y2 != INVALID_INT) {
                            g.y2 = g.y2 / 2
                        }
                    }
                    is Turnout -> {
                        // do not scale x2/y2 BUT TRANSLATE
                        if (g.x2 != INVALID_INT) {
                            g.x2 -= dx
                        }
                        if (g.y2 != INVALID_INT) {
                            g.y2 -= dy
                        }
                        if (g.xt != INVALID_INT) {
                            g.xt -= dx
                        }
                        if (g.yt != INVALID_INT) {
                            g.yt -= dy
                        }
                    }
                    is Signal -> {
                        // do not scale x2/y2 BUT TRANSLATE
                        if (g.x2 != INVALID_INT) {
                            g.x2 -= dx
                        }
                        if (g.y2 != INVALID_INT) {
                            g.y2 -= dy
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