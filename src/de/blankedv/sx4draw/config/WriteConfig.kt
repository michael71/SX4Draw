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

import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller
import java.io.File

/**
 * WriteConfig - Utility to save Panel Config
 *
 * @author Michael Blank
 * @version 1.0
 */
object WriteConfig {

     /**
     * writeConfigToXML
     *
     *
     * saves all PanelElements to an XML file
     *
     *
     * @return true, if succeeds - false, if not.
     */
    fun toXML(fname: String, lc: LayoutConfig): Boolean {


        try {
            val context = JAXBContext.newInstance(LayoutConfig::class.java)
            val m = context.createMarshaller()
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, java.lang.Boolean.TRUE)
            // Write to System.out
            m.marshal(lc, System.out)

            // Write to File
            m.marshal(lc, File(fname))

        } catch (e: Exception) {
            println("ERROR " + e.message)
            e.printStackTrace()
            return false
        }


        return true
    }


}
