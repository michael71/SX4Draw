/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.blankedv.sx4draw

import de.blankedv.sx4draw.Constants.INVALID_INT
import java.util.concurrent.atomic.AtomicLong

import javafx.scene.control.TableView
import javafx.util.Pair
import kotlin.reflect.KParameter

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

}
