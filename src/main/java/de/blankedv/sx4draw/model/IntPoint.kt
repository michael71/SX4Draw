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

package de.blankedv.sx4draw.model

import kotlin.math.roundToInt

/**
 * @author mblank
 */
class IntPoint {

    var x: Int = 0
    var y: Int = 0

    internal constructor() {
        this.x = 0
        this.y = 0
    }

    internal constructor(x: Int, y: Int) {
        this.x = x
        this.y = y
    }

    internal constructor(x: Double, y: Double) {
        this.x = x.roundToInt()
        this.y = y.roundToInt()
    }

    override fun toString(): String {
        return "($x,$y)"
    }

    companion object {

        /**
         * distance between two points, rounded to the next 10
         *
         * @param start
         * @param end
         * @return
         */
        fun delta(start: IntPoint, end: IntPoint, raster: Int): IntPoint {

            val d = IntPoint(-start.x, -start.y)
            d.x += end.x
            d.y += end.y
            return toRaster(d, raster)
        }


        fun toRaster(mp: IntPoint, raster: Int): IntPoint {
            if (raster == 1) return mp
            //System.out.print("Raster in: "+mp.x+","+mp.y+" ");
            val rh = raster / 2
            val nx = (mp.x + rh) / raster * raster
            val ny = (mp.y + rh) / raster * raster
            //System.out.println(" out: "+nx+","+ny);
            return IntPoint(nx, ny)
        }

        fun correctAngle(s: IntPoint, mp: IntPoint, raster: Int): IntPoint {

            var result = mp
            // calculate angle of line s -> mp
            val a = (mp.y - s.y).toDouble()
            val b = (mp.x - s.x).toDouble()
            val hyp = Math.sqrt(a * a + b * b)

            val tan = a / b
            val dx: Int

            if (Math.abs(tan) < 0.5) {
                // force angle = 0
                if (b > 0) {
                    result = IntPoint(s.x + hyp, s.y.toDouble())
                } else {
                    result = IntPoint(s.x - hyp, s.y.toDouble())
                }
                return toRaster(result, raster)
            } else if (Math.abs(tan) <= 2) {
                val len = Math.round(hyp / 1.4).toInt()
                dx = len / raster * raster
                // force angle = 45
                if (b > 0) {
                    if (a > 0) {
                        result = IntPoint(s.x + dx, s.y + dx)
                    } else {
                        result = IntPoint(s.x + dx, s.y - dx)
                    }
                } else {
                    if (a > 0) {
                        result = IntPoint(s.x - dx, s.y + dx)
                    } else {
                        result = IntPoint(s.x - dx, s.y - dx)
                    }
                }
                return toRaster(result, raster)
            } else {
                // angle = 90 grad
                if (a > 0) {
                    result = IntPoint(s.x.toDouble(), s.y + hyp)
                } else {
                    result = IntPoint(s.x.toDouble(), s.y - hyp)
                }
                return toRaster(result, raster)
            }
        }
    }
}
