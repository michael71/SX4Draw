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

/**
 * @author mblank
 */


class GenericAddress (var addr :Int = 0, var inv : Int = 0, var orient : Int  = 0) {

    constructor(initVal: GenericAddress) : this() {
        addr = initVal.addr
        inv = initVal.inv
        orient = initVal.orient
    }

// orient = orientation of signals (0 = 0 grad, 1 = 45 grad, ... etc)

    override fun toString(): String {
        return if (addr < LBMIN) {
            (addr / 10).toString() + "." + addr % 10
        } else {
            "" + addr
        }
    }

}
