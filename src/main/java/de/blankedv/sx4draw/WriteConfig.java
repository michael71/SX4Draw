package de.blankedv.sx4draw;

import de.blankedv.sxdraw.Trip;

import static de.blankedv.sx4draw.Constants.DEBUG;
import static de.blankedv.sx4draw.Constants.INVALID_INT;

import static de.blankedv.sx4draw.ReadConfig.YOFF;
import static de.blankedv.sx4draw.SX4Draw.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * WriteConfig - Utility turnout save Panel Config
 *
 * @author Michael Blank
 * @version 1.0
 */
public class WriteConfig {

    static StringWriter writer;
    private static String localPanelName = "";

    /**
     * writeConfigToXML
     * <p>
     * saves all PanelElements to an XML file configFilename will have ".(date)"
     * appended, if no filename given
     *
     * @param fname File Name (can be empty)
     * @param pName Panel Name (can be empty)
     * @return true, if succeeds - false, if not.
     */
    public static boolean writeToXML(String fname, String pName) {
        if (pName.isEmpty()) {
            localPanelName = "paneltest";
        } else {
            localPanelName = pName;
        }

        FileWriter fWriter = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String version = df.format(new Date());
        try {
            fWriter = new FileWriter(fname);
            fWriter.write(writeXml(localPanelName, fname, version));
            fWriter.flush();
            fWriter.close();

            if (DEBUG) {
                System.out.println("Config File " + fname + " saved! ");
            }
            //configHasChanged = false; // reset flag

        } catch (Exception e) {
            System.out.println("fname = " + fname);
            System.out.println("ERROR: " + e.getMessage());
            return false;
        } finally {
            if (fWriter != null) {
                try {
                    fWriter.close();
                } catch (IOException e) {
                    System.out.println("ERROR: could not close output file!");
                }
            }
        }

        return true;
    }

    private static void writeStart(String tagname) {
        writer.write("<" + tagname);
    }

    private static void writeClose() {
        writer.write(" />\n");
    }

    private static void writeCloseTag(String tagname) {
        writer.write("</" + tagname + ">\n");
    }

    private static void writeAttribute(String name, int val) {
        writer.write(" " + name + "=\"" + val + "\"");
    }

    private static void writeAttribute(String name, String sval) {
        writer.write(" " + name + "=\"" + sval + "\"");
    }

    /**
     * writeConfigToXML
     * <p>
     * saves all PanelElements (including deducted elements) to an XML file (=
     * simple XMLSerializer for lanbahn panel elements and routes)
     *
     * @param
     * @return true, if succeeds - false, if not.
     */
    private static String writeXml(String panelName, String fullFilename, String version) {

        writer = new StringWriter();

        Path p = Paths.get(fullFilename);
        String filename = p.getFileName().toString();

        writer.write("<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>\n");
        writer.write("<layout-config filename=\"" + filename + "\">\n");
        writer.write("<locolist name=\"" + locolistName + "\" version=\"" + version+ "\">\n");

        if (!allLocos.isEmpty()) {
            //ArrayList<Loco> locosList = new ArrayList<>(allLocos);
            //Collections.sort(locosList);
            for (Loco lo : allLocos) {
                writeStart("loco");
                writeAttribute("adr", lo.getAddr());
                writeAttribute("name", lo.getName());
                writeAttribute("mass", lo.getMass());
                writeAttribute("vmax", lo.getVmax());
                writeClose();
            }
        }
        writer.write("</locolist>\n");

        writer.write("<panel name=\"" + panelName + "\" version=\"" + version + "\">\n");

        ArrayList<PanelElement> peList = new ArrayList<>(panelElements);
        Collections.sort(peList);
        // now write all panel elements to the file
        for (PanelElement pe : peList) {
            // if (DEBUG) {
            //    System.out.println("writing panel element " + pe.toString());
            //}
            writeStart(pe.getType().name().toLowerCase());
            if (pe.getName().length() > 0) {
                writeAttribute("name=", pe.getName());
            }
            writeAttribute("x", pe.getX());
            writeAttribute("y", pe.getY() - YOFF);
            if (pe.getX2() != INVALID_INT) { // save only valid attributes
                writeAttribute("x2", pe.getX2());
                writeAttribute("y2", pe.getY2() - YOFF);
            }
            if (pe.getXt() != INVALID_INT) {
                writeAttribute("xt", pe.getXt());
                writeAttribute("yt", pe.getYt() - YOFF);
            }
            if (pe.getAdr2() != INVALID_INT) {
                if (pe.getAdr() != INVALID_INT) {
                    writeAttribute("adr", pe.getAdr() + "," + pe.getAdr2());
                }
            } else if (pe.getAdr() != INVALID_INT) {
                writeAttribute("adr", pe.getAdr());
            }

            if (pe.getInv() != 0) {
                writeAttribute("inv", pe.getAdr());
            }
            writeClose();
        }

        if (!routes.isEmpty()) {
            ArrayList<Route> rtList = new ArrayList<>(routes);
            Collections.sort(rtList);
            for (Route rt : rtList) {
                writeStart("route");
                writeAttribute("id", rt.getId());
                if (rt.getBtn1() != INVALID_INT) {
                    writeAttribute("btn1", rt.getBtn1());
                }
                if (rt.getBtn2() != INVALID_INT) {
                    writeAttribute("btn2", rt.getBtn2());
                }
                writeAttribute("route", rt.getRoute());
                writeAttribute("sensors", rt.getSensors());
                writeAttribute("offending", rt.getOffending());
                writeClose();

            }
        }

        if (!compRoutes.isEmpty()) {
            ArrayList<CompRoute> crList = new ArrayList<>(compRoutes);
            Collections.sort(crList);
            for (CompRoute rt : crList) {
                writeStart("comproute");
                writeAttribute("id", rt.getId());
                if (rt.getBtn1() != INVALID_INT) {
                    writeAttribute("btn1", rt.getBtn1());
                }
                if (rt.getBtn2() != INVALID_INT) {
                    writeAttribute("btn2", rt.getBtn2());
                }
                writeAttribute("routes", rt.getRoutes());
                writeClose();
            }
        }

        if (!trips.isEmpty()) {
            ArrayList<Trip> tripsList = new ArrayList<>(trips);
            Collections.sort(tripsList);
            for (Trip rt : tripsList) {
                writeStart("trip");
                writeAttribute("id", rt.getId());
                writeAttribute("routeid", rt.getRouteid());
                writeAttribute("sens1", rt.getSens1());
                writeAttribute("sens2", rt.getSens2());
                writeAttribute("loco", rt.getLoco());
                writeAttribute("stopdelay", rt.getStopdelay());
                writeClose();
            }
        }
        if (!timetables.isEmpty()) {
            ArrayList<Timetable> ttList = new ArrayList<>(timetables);
            Collections.sort(ttList);
            for (Timetable rt : ttList) {
                writeStart("timetable");
                writeAttribute("id", rt.getId());
                writeAttribute("time", rt.getTime());
                writeAttribute("trip", rt.getTrip());
                writeAttribute("next", rt.getNext());
                writeClose();
            }
        }

        writer.write("</panel>\n");
        writer.write("</layout-config>\n");

        return writer.toString();

    }

}
