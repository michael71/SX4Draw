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

/**
 * @author mblank
 */

object Constants {

    const val versionNumber = 0.49
    private const val versionDate = "07 Mar 2019"
    const val progVersion = "$versionNumber - $versionDate"

    const val SXMIN_USED = 11
    const val SXMAX_USED = 1068 // max sx address used (106, bit 8)
    const val LBMIN = 1200  // minimum virtual address
    const val LBMAX = 9999

    const val INVALID_INT = -1
    const val DEBUG = true

    const val RASTER = 10
    const val RECT_X = 2000
    const val RECT_Y = 1200

    const val ADDR0_ROUTEBUTTON = 4000
    const val ADDR0_ROUTE = 4300
    const val ADDR0_COMPROUTE = 4600
    const val ADDR0_TRIP = 5000
    const val ADDR0_TIMETABLE = 5500
    const val ADDR0_SIGNAL = 3000
    const val ADDR0_TURNOUT = 801

    const val DEFAULT_SENSOR_ADRSTR = "901,1901"

    enum class PEState {
        DEFAULT, MARKED, SELECTED, STATE_0, STATE_1, STATE_2, STATE_3
    }



}
