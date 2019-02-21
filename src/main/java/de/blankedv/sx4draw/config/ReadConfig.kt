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


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller
import java.io.FileReader


/**
 * utility function to write the layout config to an xml file
 *
 * @author mblank
 */
object ReadConfig {

    //TODO read/write locolist (see SX4)

    fun fromXML(fname: String): LayoutConfig? {

        try {
            val context = JAXBContext.newInstance(LayoutConfig::class.java)
            val um = context.createUnmarshaller()
            return um.unmarshal(FileReader(fname)) as LayoutConfig

        } catch (e: Exception) {
            println("ERROR " + e.message)
            e.printStackTrace()
            return null
        }


    }
}
