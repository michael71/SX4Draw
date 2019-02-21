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

import de.blankedv.sx4draw.Constants.INVALID_INT
import javafx.scene.shape.Line
import javafx.util.Pair
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType

/**
 *
 * @author mblank
 */
@XmlRootElement(name = "panel")
@XmlType
class Panel() {

    @get:XmlAttribute
    var name : String = ""

    constructor(panelName: String) : this() {
        name =  panelName
    }

}
