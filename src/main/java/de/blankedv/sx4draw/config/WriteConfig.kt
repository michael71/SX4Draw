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


    /**
     * writeConfigToXML
     *
     *
     * saves all PanelElements (including deducted elements) to an XML file (=
     * simple XMLSerializer for lanbahn panel elements and routes)
     *
     * @param
     * @return true, if succeeds - false, if not.
     */
    private fun writeXml(layoutConfig: LayoutConfig, panelName: String, fullFilename: String, version: String): String {

        // create JAXB context and instantiate marshaller


        /*
        // get variables from our xml file, created before
        System.out.println();
        System.out.println("Output from our XML File: ");
        Unmarshaller um = context.createUnmarshaller();
        Bookstore bookstore2 = (Bookstore) um.unmarshal(new FileReader(
                FILENAME_XML));
        ArrayList<Book> list = bookstore2.getBooksList();
        for (Book book : list) {
            System.out.println("Book: " + book.getName() + " from "
                    + book.getAuthor());
        }

        //writer.write("<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>\n");
        //writer.write("<layout-config filename=\"" + filename + "\">\n");
        //writer.write("<locolist name=\"" + locolistName + "\" version=\"" + version+ "\">\n");
    */
        return "OK"

    }

}
