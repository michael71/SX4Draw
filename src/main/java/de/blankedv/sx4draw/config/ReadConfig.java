/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.blankedv.sx4draw.config;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import de.blankedv.sx4draw.PanelElement;
import de.blankedv.sx4draw.model.Position;
import de.blankedv.sx4draw.Route;
import de.blankedv.sx4draw.views.SX4Draw.*;
import de.blankedv.sx4draw.model.Timetable;
import de.blankedv.sx4draw.model.CompRoute;
import de.blankedv.sx4draw.model.Loco;
import de.blankedv.sxdraw.Trip;
import org.w3c.dom.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.blankedv.sx4draw.Constants.INVALID_INT;
import static de.blankedv.sx4draw.views.SX4Draw.*;
import static de.blankedv.sx4draw.config.WriteConfigNew.FILENAME_XML;

/**
 * utility function for the mapping of lanbahn addresses to DCC addresses (and
 * bits) and vice versa
 *
 * @author mblank
 */
public class ReadConfig {

    //TODO read/write locolist (see SX4)

    public static final int YOFF = 0;   // y values start at 60, this value get added when reading a config
    // and substracted when storing.

    private static final boolean CFG_DEBUG = true;
    private static List<RT> rtValues = Arrays.asList(RT.values());

    // code template taken from lanbahnPanel
    public static LayoutConfig readXML(String fname) {

        try {
        JAXBContext context = JAXBContext.newInstance(LayoutConfig.class);
        Unmarshaller um = context.createUnmarshaller();
        LayoutConfig lc = (LayoutConfig) um.unmarshal(new FileReader(
                FILENAME_XML));
            return lc;
        /* TODO ArrayList<Track> tracks = lc.get();
         for (Track tr : tracks) {
            System.out.println("track x=" +tr.getX());
        } */
        } catch (Exception e) {
            System.out.println("ERROR "+e.getMessage());
            e.printStackTrace();
            return null;
        }


    }
}
