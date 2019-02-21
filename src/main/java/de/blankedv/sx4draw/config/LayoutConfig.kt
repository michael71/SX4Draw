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

package de.blankedv.sx4draw.config

import de.blankedv.sx4draw.model.Loco
import java.util.ArrayList

import javax.xml.bind.annotation.*


//This statement means that class "LayoutConfig.java" is the root-element
@XmlRootElement(name = "layout-config")
@XmlType(propOrder = ["loco","panel"])
class LayoutConfig {
    // see https://blog.scottlogic.com/2016/04/04/practical-kotlin.html

    @get:XmlAttribute
    var fileName = ""

    @get:XmlAttribute
    var version = "0001"

    @XmlElementWrapper(name = "locos")
    @get:XmlElement(name = "loco")
    private val loco = ArrayList<Loco>()

    @XmlElementWrapper(name = "panels")
    @get:XmlElement(name = "panel")
    private val panel  = ArrayList<PanelConfig>()

    constructor()

    constructor(fn : String, pc: PanelConfig, ve: String) {
        this.fileName = fn
        panel.add(pc)
        this.version = ve
    }

    fun getPC0() : PanelConfig? {
        if (panel.size >= 1) {
            return panel.get(0)
        } else {
            return null
        }
    }



}
