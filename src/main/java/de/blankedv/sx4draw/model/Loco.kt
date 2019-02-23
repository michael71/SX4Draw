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

import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlRootElement

/** Loco class used for storing loco info from Config file
 *
 *
 * @author mblank
 */

@XmlRootElement(name = "loco")
class Loco {
    // example from xml file
    //<loco adr="97" name="SchoenBB" mass="2" vmax="120" />
    // "adr" NOT:addr
    @get:XmlAttribute
    var adr = 1

    @get:XmlAttribute
    var name = ""

    @get:XmlAttribute
    var mass = 3

    @get:XmlAttribute
    var vmax = 160

    constructor()


    override fun toString() : String {
        return "Loco: adr="+adr+" name="+name+" mass=" + mass +" vmax=" +vmax
    }
  /*  fun setMass(mass: Int) {
        if (mass in 1..5) {
            this.mass = mass
        } else {
            this.mass = 3
        }
    }

    fun getVmax(): Int {
        return vmax
    }

    fun setVmax(vmax: Int) {
        if (vmax in 30..300) {
            this.vmax = vmax
        } else {
            this.vmax = 160
        }
    }
 */

}
