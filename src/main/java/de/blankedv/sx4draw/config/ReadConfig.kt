/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.blankedv.sx4draw.config


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import de.blankedv.sx4draw.views.SX4Draw.*

import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller
import java.io.FileReader
import java.util.Arrays

import de.blankedv.sx4draw.config.WriteConfig.FILENAME_XML

/**
 * utility function for the mapping of lanbahn addresses to DCC addresses (and
 * bits) and vice versa
 *
 * @author mblank
 */
object ReadConfig {

    //TODO read/write locolist (see SX4)

    fun readXML(fname: String): LayoutConfig? {

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
