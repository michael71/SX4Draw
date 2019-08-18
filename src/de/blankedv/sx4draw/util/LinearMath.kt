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

package de.blankedv.sx4draw.util

import de.blankedv.sx4draw.model.Track
import de.blankedv.sx4draw.model.Turnout
import de.blankedv.sx4draw.model.IntPoint


object LinearMath {

    private const val DEBUG_MATH = false
    private const val TURNOUT_LENGTH = 10
    private const val TURNOUT_LENGTH_LONG = 14

    /**
     * Computes the intersection between two lines (=tracks). The calculated point is
     * approximate, since integers are used. If you need a more precise result,
     * use doubles everywhere. (c) 2007 Alexander Hristov. Use Freely (LGPL
     * license). http://www.ahristov.com (c) 2012 Michael Blank, for lines with
     * endpoints
     */
    fun trackIntersect( e1: Track, f1: Track): Turnout? {

        var e = e1
        var f = f1   // => var because they might be swapped below

        val x1: Int = e.x
        val y1: Int = e.y
        val x2: Int = e.x2
        val y2: Int = e.y2

        val x3: Int = f.x
        val y3: Int = f.y
        val x4: Int = f.x2
        val y4: Int = f.y2

        val d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4)
        if (d == 0) {
            return null
        }

        val xi = ((x3 - x4) * (x1 * y2 - y1 * x2) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d
        val yi = ((y3 - y4) * (x1 * y2 - y1 * x2) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d
        val px = IntPoint(xi, yi)

        // additional code by Michael Blank
        // check if within limit of lines
        var xt: Int
        var yt: Int
        var xc1: Int
        val yc1: Int
        val xc2: Int
        var yc2: Int // for turnout x-thrown, y-thrown, x-closed 1/2,
        // y-closed
        if (xi >= Math.min(x1, x2) && xi <= Math.max(x1, x2) // within

                // x-limits of first line
                && xi >= Math.min(x3, x4) && xi <= Math.max(x3, x4) // within

                // x-limits of second line
                && yi >= Math.min(y1, y2) && yi <= Math.max(y1, y2) // within

                // y-limits of first line
                && yi >= Math.min(y3, y4) && yi <= Math.max(y3, y4)) { // within
            // y-limits of second line

            // check if point is not endpoint of both tracks => no turnout
            if (xi == e.x && yi == e.y || // startpoint of e ( e=thrown, f=closed)
                    xi == e.x2 && yi == e.y2) { // endpoint of e =>
                // delta-x immer negativ start/endpoint of e
                if (xi == f.x && yi == f.y || xi == f.x2 && yi == f.y2) {
                    // AND start/endpoint of f
                    return null // => no turnout
                }
            }

            // check if point is not endpoint of first track
            // and not endpoint of second track
            // => double crossover
            // swap e and f if e is not "durchgehendes Gleis"
            var doubleslip = true
            if (xi == e.x && yi == e.y || // startpoint of e ( e=thrown, f=closed)
                    xi == e.x2 && yi == e.y2) { // endpoint of e =>
                // delta-x immer negativ
                doubleslip = false
                // swap e and f => e is always "durchgehendes Gleis" (=close)
                var temp = e
                e = f
                f = temp
                if (DEBUG_MATH) println("swapped e/f")
            } // else start/endpoint of f
            else if (xi == f.x && yi == f.y || xi == f.x2 && yi == f.y2) {
                doubleslip = false
            }

            if (!doubleslip) {
                // =========== this is a turnout !! ======================
                // find closed and thrown positions (x2>x) both for e and f !!
                // 1. check, turnout which lines' endpoint (xi,yi) belongs
                val es = "E x,y=(" + e.x + "," + e.y + ") x2,y2=(" + e.x2 + "," + e.y2 + ")"
                val fs = "F x,y=(" + f.x + "," + f.y + ") x2,y2=(" + f.x2 + "," + f.y2 + ")"
                if (DEBUG_MATH) println(es)
                if (DEBUG_MATH) println(fs)
                xt = xi
                yt = yi

                if (xi == f.x && yi == f.y) { // startpoint of f
                    if (sgn(f.x2, f.x) == 0 || sgn(f.y2, f.y) == 0) {
                        xt = xi + TURNOUT_LENGTH_LONG * sgn(f.x2, f.x)
                        yt = yi + TURNOUT_LENGTH_LONG * sgn(f.y2, f.y)
                    } else {
                        xt = xi + TURNOUT_LENGTH * sgn(f.x2, f.x)
                        yt = yi + TURNOUT_LENGTH * sgn(f.y2, f.y)
                    }


                } else if (xi == f.x2 && yi == f.y2) { // endpoint of f
                    if (sgn(f.x, f.x2) == 0 || sgn(f.y, f.y2) == 0) {
                        xt = xi + TURNOUT_LENGTH_LONG * sgn(f.x, f.x2)
                        yt = yi + TURNOUT_LENGTH_LONG * sgn(f.y, f.y2)
                    } else {
                        xt = xi + TURNOUT_LENGTH * sgn(f.x, f.x2)
                        yt = yi + TURNOUT_LENGTH * sgn(f.y, f.y2)
                    }

                }

                // calculate possible "closed" turnout lines in both
                // directions of "durchgehendes Gleis"
                // choose the one which is nearer to "thrown" line end
                if (e.x2 == e.x) {
                    if (DEBUG_MATH) println("e senkrecht")
                    xc1 = xi
                    xc2 = xi
                    yc1 = yi + TURNOUT_LENGTH_LONG * sgn(e.y2, e.y)
                    yc2 = yi - TURNOUT_LENGTH_LONG * sgn(e.y2, e.y)
                } else if (e.y2 == e.y) {
                    if (DEBUG_MATH) println("e waagerecht")
                    xc1 = xi + TURNOUT_LENGTH_LONG * sgn(e.x2, e.x)
                    xc2 = xi - TURNOUT_LENGTH_LONG * sgn(e.x2, e.x)
                    yc1 = yi
                    yc2 = yi
                } else {
                    if ((e.y2 - e.y) / (e.x2 - e.x) > 0) {
                        if (DEBUG_MATH) println("e s>0")
                        xc1 = xi + TURNOUT_LENGTH
                        yc1 = yi + TURNOUT_LENGTH
                        xc2 = xi - TURNOUT_LENGTH
                        yc2 = yi - TURNOUT_LENGTH
                    } else {
                        if (DEBUG_MATH) println("e s<0")
                        xc1 = xi - TURNOUT_LENGTH
                        yc1 = yi + TURNOUT_LENGTH
                        xc2 = xi + TURNOUT_LENGTH
                        yc2 = yi - TURNOUT_LENGTH
                    }
                }

                val p1 = IntPoint(xc1, yc1)
                val p2 = IntPoint(xc2, yc2)
                if (DEBUG_MATH)
                    println("pi=$xi,$yi  p1=$p1 p2=$p2")
                val d1 = Utils.calcDistance(IntPoint(xt, yt), p1)
                val d2 = Utils.calcDistance(IntPoint(xt, yt), IntPoint(xc2, yc2))
                if (DEBUG_MATH) println("d1 = $d1 d2=$d2")
                return if (Math.abs(d1 - d2) < 1.0) {
                    // gleise kreuzen bei 90grad => keine Weiche
                    null
                } else if (d1 < d2) {
                    Turnout(px, IntPoint(xc1, yc1),
                            IntPoint(xt, yt))
                } else {
                    Turnout(px, IntPoint(xc2, yc2),
                            IntPoint(xt, yt))
                }
            } else {
                if (DEBUG_MATH) println("doubleslip at " + px.x + "," + px.y + " ??")
                //return new DoubleSlipElement(px, px, px);
                return null
            }
        } else {
            return null // for debugging: new PanelElement("turnok", px);
        }
    }

    private fun sgn(a: Int, b: Int): Int {
        val res: Int
        if (a == b) {
            res = 0
        } else if (a > b) {
            res = 1
        } else {
            res = -1
        }
        //System.out.println("a=" + a + " b=" + b + " sgn=" + res);
        return res
    }
}
