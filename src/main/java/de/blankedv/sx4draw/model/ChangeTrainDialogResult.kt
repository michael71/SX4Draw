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


import de.blankedv.sx4draw.Constants.INVALID_INT

/**
 *
 * @author mblank
 */
class ChangeTrainDialogResult {

    var loco1 = INVALID_INT
    var speed1 = INVALID_INT
    var loco2 = INVALID_INT
    var speed2 = INVALID_INT
    var invertDir = false


    override fun toString(): String {
        return if (loco1 == INVALID_INT || speed1 == INVALID_INT) {
            ""
        } else {
            "$loco1/$speed1 -> $loco2/$speed2 -> "
        }
    }
}
