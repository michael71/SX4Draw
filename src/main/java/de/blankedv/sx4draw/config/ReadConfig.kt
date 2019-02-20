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

    val YOFF = 0   // y values start at 60, this value get added when reading a config
    // and substracted when storing.

    private val CFG_DEBUG = true
    private val rtValues = Arrays.asList(*RT.values())

    // code template taken from lanbahnPanel
    fun readXML(fname: String): LayoutConfig? {

        try {
            val context = JAXBContext.newInstance(LayoutConfig::class.java)
            val um = context.createUnmarshaller()
            return um.unmarshal(FileReader(
                    FILENAME_XML)) as LayoutConfig
            /* TODO ArrayList<Track> tracks = lc.get();
         for (Track tr : tracks) {
            System.out.println("track x=" +tr.getX());
        } */
        } catch (e: Exception) {
            println("ERROR " + e.message)
            e.printStackTrace()
            return null
        }


    }
}
