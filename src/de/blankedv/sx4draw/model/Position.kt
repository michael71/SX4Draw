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
 * @author Michael Blank <mblank></mblank>@bawue.de>
 */
data class Position (
    var x: Int = 0, // starting point
    var y: Int = 0,
    var x2: Int = INVALID_INT, // endpoint - x2 always >x, closed pos. for turnout
    var y2: Int = INVALID_INT,
    var xt: Int = INVALID_INT, // "thrown" position for turnout
    var yt: Int = INVALID_INT
)
