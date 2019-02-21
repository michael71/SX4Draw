/*
SX4Draw
Copyright (C) 2019 Michael Blank

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.blankedv.sx4draw

import de.blankedv.sx4draw.Constants.LBMIN
import de.blankedv.sx4draw.Constants.INVALID_INT
import de.blankedv.sx4draw.Constants.PEState
import de.blankedv.sx4draw.Constants.PEState.*
import de.blankedv.sx4draw.model.GenericPE
import de.blankedv.sx4draw.model.IntPoint
import de.blankedv.sx4draw.model.RouteButton
import de.blankedv.sx4draw.model.Track
import de.blankedv.sx4draw.util.Utils
import de.blankedv.sx4draw.views.SX4Draw.Companion.panelElements

import java.util.ArrayList

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import javafx.scene.shape.Shape

/**
 * generic panel element - contains data model (Track, Sensor, ...) plus
 * state plus graphical representation (Shape)
 *
 * @author mblank
 */

class PanelElement : Comparator<PanelElement>, Comparable<PanelElement> {

    var gpe: GenericPE =Track(Line(0.0, 0.0, 1.0, 1.0))

    // elements for graphics

    var shape : Shape = Line(0.0, 0.0, 1.0, 1.0)
    var state : PEState = DEFAULT

    var defaultColor : Color = javafx.scene.paint.Color.ALICEBLUE

    constructor()

    constructor(tu: GenericPE) {
        this.gpe = tu
        createShapeAndSetState(PEState.DEFAULT)
    }

    fun recreateShape() {
        // create shape but don't change state
        createShapeAndSetState(state)
    }

    fun createShapeAndSetState(st: PEState) {
        state = st
        val (s,c)  = Utils.createShape(gpe, state)
        shape = s
        defaultColor = c
        setColorFromState()
    }

    private fun setColorFromState() {
        when (state) {
            DEFAULT -> {
                shape.fill = defaultColor
                shape.stroke = defaultColor
            }
            SELECTED -> {
                shape.fill = Color.RED
                shape.stroke = Color.RED
            }
            MARKED -> {
                shape.fill = Color.AQUA
                shape.stroke = Color.AQUA
            }
            STATE_0 -> {
                shape.fill = Color.RED
                shape.stroke = Color.RED
            }
            STATE_1 -> {
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

        gc.fillRect(gpe.x.toDouble(), gpe.y.toDouble() - 11.0, (8 * sAddr.length).toDouble(), 12.0)
        gc.strokeText(sAddr, gpe.x.toDouble(), gpe.y.toDouble())
    }

    override fun compare(o1: PanelElement, o2: PanelElement): Int {
        return if (o1.gpe.ord == o2.gpe.ord) {
            o1.gpe.x - o2.gpe.x
        } else {
            o1.gpe.ord - o2.gpe.ord
        }
    }

    override fun compareTo(other: PanelElement): Int {
        return if (gpe.ord == other.gpe.ord) {
            gpe.x - other.gpe.x
        } else {
            gpe.ord - other.gpe.ord
        }
    }

    companion object {

        const val TOUCH_RADIUS = 7
        const val TRACK_WIDTH = 5.0
        const val SENSOR_WIDTH = 4.0

        /**
         * search for a panel element(or elements) when only the address is known
         *
         * @param address
         * @return
         */
        fun getPeByAddress(address: Int): ArrayList<PanelElement> {
            val pelist = ArrayList<PanelElement>()
            for (pe in panelElements) {
                if (pe.gpe.getAddr() == address) {
                    pelist.add(pe)
                }
            }
            return pelist
        }

        fun resetState() {
            for (pe in panelElements) {
                pe.createShapeAndSetState(pe.state)
            }
        }

        /**
         * check if at least 60 % of (signal, turnout, sensor) addresses have been
         * entereded and at least 1 PE of this type
         *
         * @param
         * @return
         */
        fun addressesAvail(): Boolean {
            var adrOK = 0
            var adrNOK = 0
            var percentage = 0.0
            for (pe in panelElements) {
                if (!(pe.gpe is Track) && (pe.gpe is RouteButton)) {
                    if (pe.gpe.getAddr() != INVALID_INT
                            && pe.gpe.getAddr() != 0
                            && pe.gpe.getAddr() != 1) {
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
            for (pe in panelElements) {
                if (pe.state == PEState.SELECTED) {
                    return true
                }
            }
            return false
        }

        fun translate(d: IntPoint) {
            for (pe in panelElements) {
                pe.gpe.translate(d)
                pe.createShapeAndSetState(pe.state)
                 // state will be reset to DEFAULT also
            }
        }

        fun moveSelected(d: IntPoint) {
            for (pe in panelElements.filter{ el -> el.state == PEState.SELECTED}) {
                pe.gpe.translate(d)
                pe.createShapeAndSetState(PEState.DEFAULT)
            }
        }


        fun scalePlus() {
            for (pe in panelElements) {
                pe.gpe.scalePlus()
                pe.createShapeAndSetState(PEState.DEFAULT)
            }
        }

        fun scaleMinus() {
            for (pe in panelElements) {
                pe.gpe.scaleMinus()
                pe.createShapeAndSetState(PEState.DEFAULT)
            }
        }
    }
}
