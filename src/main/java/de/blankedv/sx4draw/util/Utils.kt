/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.blankedv.sx4draw.util

import de.blankedv.sx4draw.*
import de.blankedv.sx4draw.Constants.INVALID_INT
import de.blankedv.sx4draw.model.GenericPE
import de.blankedv.sx4draw.model.IntPoint
import java.util.concurrent.atomic.AtomicLong

import javafx.scene.control.TableView
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Line
import javafx.scene.shape.Shape
import javafx.scene.shape.StrokeLineCap
import java.net.URL


/**
 * @author mblank
 */
object Utils {

    const val SIGLEN = 13 // länge des Fusses eines Signals
    const val SIGLEN2 = 9  // dx des fusses eines Signals diagonal

    // see https://en.wikipedia.org/wiki/Distance_from_a_point_to_a_line
    fun calcDistanceFromLine(l1: IntPoint, l2: IntPoint, p: IntPoint): Double {
        val d1 = Math.abs((l2.y - l1.y) * p.x - (l2.x - l1.x) * p.y + l2.x * l1.y - l2.y * l1.x)
        val d2 = Math.sqrt(((l2.y - l1.y) * (l2.y - l1.y) + (l2.x - l1.x) * (l2.x - l1.x)).toDouble())

        return d1.toDouble() / d2
    }

    fun calcDistance(p: IntPoint, q: IntPoint): Double {

        return Math.sqrt(((p.y - q.y) * (p.y - q.y) + (p.x - q.x) * (p.x - q.x)).toDouble())
    }


    fun customResize(view: TableView<*>) {

        val width = AtomicLong()
        view.columns.forEach { col -> width.addAndGet(col.width.toLong() + 10) }
        val tableWidth = view.width

        if (tableWidth > width.get()) {
            view.columns.forEach { col -> col.setPrefWidth(col.width + (tableWidth - width.get()) / view.columns.size) }
        }
    }


    fun signalOrientToDXY2(orient: Int): IntPoint {
        var dx = 0
        var dy = 0

        // evaluate orientation
        when (orient) {
            0 -> {  // 0 grad
                dx = -SIGLEN
                dy = 0
            }
            1 -> {  // 45
                dx = -SIGLEN2
                dy = +SIGLEN2
            }
            2   // 90
            -> {
                dx = 0
                dy = +SIGLEN
            }
            3   // 135
            -> {
                dx = +SIGLEN2
                dy = +SIGLEN2
            }
            4   // 180
            -> {
                dx = +SIGLEN
                dy = 0
            }
            5   // 225
            -> {
                dx = +SIGLEN2
                dy = -SIGLEN2
            }
            6   // 270
            -> {
                dx = 0
                dy = -SIGLEN
            }
            7   // 315
            -> {
                dx = -SIGLEN2
                dy = -SIGLEN2
            }
        }
        return IntPoint(dx, dy)
    }

    fun signalDX2ToOrient(delta: IntPoint): Int {
        for (orient in 0..7) {
            val d1 = signalOrientToDXY2(orient)
            if (d1.x == delta.x && d1.y == delta.y) return orient
        }
        return INVALID_INT
    }

    @JvmStatic
    fun readLastVersionFromURL(): Double {
        val urlObject = URL("https://raw.githubusercontent.com/michael71/SX4Draw/master/version.txt")
        val versionFromGithub = urlObject.readText()
        return versionFromGithub.toDouble()

    }

    fun createShape(g : GenericPE, state : Constants.PEState) : Pair<Shape, Color> {
        var shape : Shape = Line(0.0,0.0,1.0,1.0)
        var defaultColor : Color = Color.BLACK
        when (g) {
            is Turnout -> {
                defaultColor = Color.ORANGE
                when (state) {
                    Constants.PEState.STATE_0 -> {
                        if (g.inv == 0) {   // not inverted
                            shape = Line(g.x.toDouble(), g.y.toDouble(), g.x2.toDouble(), g.y2.toDouble())
                            shape.strokeLineCap = StrokeLineCap.ROUND
                            shape.strokeWidth = PanelElement.TRACKWIDTH
                        } else {
                            shape = Line(g.x.toDouble(), g.y.toDouble(), g.xt.toDouble(), g.yt.toDouble())
                            shape.strokeLineCap = StrokeLineCap.ROUND
                            shape.strokeWidth = PanelElement.TRACKWIDTH
                        }
                        shape.fill = Color.GREEN
                        shape.stroke = Color.GREEN
                    }
                    Constants.PEState.STATE_1 -> {
                        if (g.inv == 0) {   // not inverted
                            shape = Line(g.x.toDouble(), g.y.toDouble(), g.xt.toDouble(), g.yt.toDouble())
                            shape.strokeLineCap = StrokeLineCap.ROUND
                            shape.strokeWidth = PanelElement.TRACKWIDTH
                        } else {
                            shape = Line(g.x.toDouble(), g.y.toDouble(), g.x2.toDouble(), g.y2.toDouble())
                            shape.strokeLineCap = StrokeLineCap.ROUND
                            shape.strokeWidth = PanelElement.TRACKWIDTH
                        }
                        shape.fill = Color.RED
                        shape.stroke = Color.RED
                    }
                    else -> {
                        val l1 = Line(g.x.toDouble(), g.y.toDouble(), g.x2.toDouble(), g.y2.toDouble())
                        l1.strokeLineCap = StrokeLineCap.ROUND
                        l1.strokeWidth = PanelElement.TRACKWIDTH
                        val l2 = Line(g.x.toDouble(), g.y.toDouble(), g.xt.toDouble(), g.yt.toDouble())
                        l2.strokeLineCap = StrokeLineCap.ROUND
                        l2.strokeWidth = PanelElement.TRACKWIDTH
                        shape = Shape.union(l1, l2)
                        shape.fill = defaultColor
                        shape.stroke = defaultColor
                    }
                }
            }
            is Track -> {
                defaultColor = Color.BLACK
                shape = Line(g.x.toDouble(), g.y.toDouble(), g.x2.toDouble(), g.y2.toDouble())
                shape.strokeWidth = PanelElement.TRACKWIDTH
                shape.strokeLineCap = StrokeLineCap.ROUND
            }
            is RouteButton -> {
                shape = Circle(g.x.toDouble(), g.y.toDouble(), 9.5, Color.DARKGREY)
                defaultColor = Color.DARKGREY
            }
            is Sensor -> {
                if (g.x2 != INVALID_INT) {//DE type of sensor
                    shape = Line(g.x.toDouble(), g.y.toDouble(), g.x2.toDouble(), g.y2.toDouble())
                    shape.strokeWidth = PanelElement.SENSORWIDTH
                    shape.strokeDashArray.addAll(15.0, 10.0)
                    shape.strokeLineCap = StrokeLineCap.ROUND
                    defaultColor = Color.YELLOW
                } else {  //US Type SENSOR
                    shape = Circle(g.x.toDouble(), g.y.toDouble(), 8.0, Color.ORANGE)
                    defaultColor = Color.ORANGE
                }
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




        return Pair(shape, defaultColor)

    }

}
