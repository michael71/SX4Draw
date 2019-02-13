/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.blankedv.sx4draw;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import static de.blankedv.sx4draw.Constants.INVALID_INT;
import static de.blankedv.sx4draw.SX4Draw.*;

import de.blankedv.sx4draw.SX4Draw.PEType;
import de.blankedv.sx4draw.SX4Draw.RT;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import de.blankedv.sxdraw.Trip;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
    public static String readXML(String fname) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;

        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e1) {
            System.out.println("ParserConfigException Exception - " + e1.getMessage());
            return "ParserConfigException";
        }
        Document doc;
        try {
            doc = builder.parse(new File(fname));
            parsePanelElements(doc);
            parseLocoList(doc);
        } catch (SAXException e) {
            System.out.println("SAX Exception - " + e.getMessage());
            return "SAX Exception - " + e.getMessage();
        } catch (IOException e) {
            System.out.println("IO Exception - " + e.getMessage());
            return "IO Exception - " + e.getMessage();
        } catch (Exception e) {
            System.out.println("other Exception - " + e.getMessage());
            return "other Exception - " + e.getMessage();
        }

        return "OK";
    }

    // code template from lanbahnPanel
    private static void parsePanelElements(Document doc) {
        // delete old info completely

        panelElements.clear();
        routes.clear();
        compRoutes.clear();
        trips.clear();
        timetables.clear();

        NodeList items;
        Element root = doc.getDocumentElement();

        items = root.getElementsByTagName("panel");
        if (items.getLength() == 0) {
            return;
        }

        String panelProtocol = parsePanelAttribute(items.item(0), "protocol");

        if (CFG_DEBUG) {
            System.out.println("panelProtocol =" + panelProtocol);
        }

        panelName = parsePanelAttribute(items.item(0), "name");

        if (CFG_DEBUG) {
            System.out.println("panelName=" + panelName);
        }

        items = root.getElementsByTagName("track");
        if (CFG_DEBUG) {
            System.out.println("config: " + items.getLength() + " track");
        }
        for (int i = 0; i < items.getLength(); i++) {
            addPanelElement(PEType.TRACK, items.item(i));
        }
        // NamedNodeMap attributes = item.getAttributes();
        // Node theAttribute = attributes.items.item(i);
        // look for TrackElements - this is the lowest layer
        items = root.getElementsByTagName("turnout");
        if (CFG_DEBUG) {
            System.out.println("config: " + items.getLength() + " turnouts");
        }
        for (int i = 0; i < items.getLength(); i++) {
            addPanelElement(PEType.TURNOUT, items.item(i));
        }

        items = root.getElementsByTagName("signal");
        if (CFG_DEBUG) {
            System.out.println("config: " + items.getLength() + " signals");
        }

        for (int i = 0; i < items.getLength(); i++) {
            addPanelElement(PEType.SIGNAL, items.item(i));
        }

        items = root.getElementsByTagName("sensor");
        if (CFG_DEBUG) {
            System.out.println("config: " + items.getLength() + " sensors");
        }
        for (int i = 0; i < items.getLength(); i++) {
            addPanelElement(PEType.SENSOR, items.item(i));
        }

        items = root.getElementsByTagName("routebutton");
        if (CFG_DEBUG) {
            System.out.println("config: " + items.getLength() + " routebuttons");
        }
        for (int i = 0; i < items.getLength(); i++) {
            addPanelElement(PEType.ROUTEBUTTON, items.item(i));
        }

        // read all Route info
        routes.clear();
        items = root.getElementsByTagName("route");
        for (int i = 0; i < items.getLength(); i++) {
            Route.Companion.add(items.item(i));
        }
        System.out.println(routes.size() + " routes read");

        // read all CompRoute info

        items = root.getElementsByTagName("comproute");
        for (int i = 0; i < items.getLength(); i++) {
            CompRoute.Companion.add(items.item(i));
        }
        System.out.println(compRoutes.size() + " compRoutes read");

        // read all Trip info

        items = root.getElementsByTagName("trip");
        for (int i = 0; i < items.getLength(); i++) {
            Trip.Companion.add(items.item(i));
        }
        System.out.println(trips.size() + " trips read");

        // read all TimeTable info
        timetables.clear();
        items = root.getElementsByTagName("timetable");
        for (int i = 0; i < items.getLength(); i++) {
            Timetable.Companion.add(items.item(i));
        }
        System.out.println(timetables.size() + " timetables read");

    }

    private static void parseLocoList(Document doc) {
        // <loco adr="97" name="SchoenBB" mass="2" vmax="120" />

        allLocos.clear();

        NodeList items;
        Element root = doc.getDocumentElement();

        items = root.getElementsByTagName("locolist");
        if (items.getLength() == 0) {
            return;
        }

        locolistName = parsePanelAttribute(items.item(0), "name");
        String locolistVersion = parsePanelAttribute(items.item(0), "version");  // NOT USED

        items = root.getElementsByTagName("loco");
        for (int i = 0; i < items.getLength(); i++) {
            Loco loco = parseLoco(items.item(i));
            if (loco != null ) allLocos.add(loco);
        }
        System.out.println("config: " + allLocos.size() + " locos");

    }

    private static Loco parseLoco(Node item) {
//<loco adr="97" name="SchoenBB" mass="2" vmax="120" />
        Loco lo = new Loco();

        NamedNodeMap attributes = item.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node theAttribute = attributes.item(i);
            switch (theAttribute.getNodeName()) {
                case "adr":
                case "addr":
                    lo.setAddr(getIntValueOfNode(theAttribute));
                    break;
                case "name":
                    lo.setName(theAttribute.getNodeValue());
                    break;
                case "mass":
                    lo.setMass(getIntValueOfNode(theAttribute));
                    break;
                case "vmax":
                    lo.setVmax(getIntValueOfNode(theAttribute));
                    break;
                default:
                    break;
            }
        }

        // check if Loco is valid
        if (lo.getAddr() != INVALID_INT)  {
            // we have the minimum info needed
            return lo;
        } else {
            return null;
        }
    }

    // code from lanbahnPanel
    private static int getIntValueOfNode(Node a) {
        return Integer.parseInt(a.getNodeValue());
    }
    // code from lanbahnPanel

    /* private static float getFloatValueOfNode(String s) {
        float b = Float.parseFloat(s);
        return  b;
    } */
    private static ArrayList<Integer> parseAddressArray(Node a) {
        NamedNodeMap attributes = a.getAttributes();

        // determine type - OLD (sxadr/sxbit/nbit) or NEW (lanbahn addresses, sep. by comma)
        boolean newFormat = false;
        for (int i = 0; i < attributes.getLength(); i++) {
            Node theAttribute = attributes.item(i);
            if (theAttribute.getNodeName().equals("adr")) {
                newFormat = true;
                break;
            }
        }
        if (newFormat) {
            for (int i = 0; i < attributes.getLength(); i++) {
                Node theAttribute = attributes.item(i);
                if (theAttribute.getNodeName().equals("adr")) {
                    String s = theAttribute.getNodeValue();
                    s = s.replace(".", "");
                    //s = s.replace("\\s+", "");
                    String[] sArr = s.split(",");
                    ArrayList<Integer> iArr = new ArrayList<>();

                    for (String s2 : sArr) {
                        int addr = INVALID_INT;
                        try {
                            addr = Integer.parseInt(s2);
                        } catch (NumberFormatException ex) {
                        }
                        iArr.add(addr);
                    }
                    return iArr;
                }
            }
        } else {  // old format like sxadr="92" sxbit="3"
            int sxadr = INVALID_INT;
            int sxbit = 1;
            for (int i = 0; i < attributes.getLength(); i++) {
                Node theAttribute = attributes.item(i);
                if (theAttribute.getNodeName().equals("sxadr")) {
                    String s = theAttribute.getNodeValue();
                    try {
                        sxadr = Integer.parseInt(s);
                    } catch (NumberFormatException ex) {
                    }
                } else if (theAttribute.getNodeName().equals("sxbit")) {
                    String s = theAttribute.getNodeValue();
                    try {
                        sxbit = Integer.parseInt(s);
                    } catch (NumberFormatException ex) {
                    }
                }
            }
            if (sxadr != INVALID_INT) {
                ArrayList<Integer> iArr = new ArrayList<>();
                iArr.add(sxadr * 10 + sxbit);
                return iArr;
            }
        }
        return null;
    }
    // code from lanbahnPanel

    private static String parsePanelAttribute(Node item, String att) {
        NamedNodeMap attributes = item.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node theAttribute = attributes.item(i);

            if (theAttribute.getNodeName().equals(att)) {
                String attrib = theAttribute.getNodeValue();
                return attrib;

            }
        }
        return "";
    }

    private static void addPanelElement(PEType type, Node a) {
        Position p = parsePositions(type, a);  // get x,y,x2,...

        if (type != PEType.TRACK) {
            ArrayList<Integer> addressArr = parseAddressArray(a);
            if (addressArr != null) {
                int lba = addressArr.get(0);
                PanelElement pe = new PanelElement(type, p);
                switch (addressArr.size()) {
                    case 1:
                        pe.setAdr(lba);
                        panelElements.add(pe);
                        break;
                    case 2:
                        pe.setAdr(lba);
                        pe.setAdr2(addressArr.get(1));
                        panelElements.add(pe);
                        break;
                    default:
                        System.out.println("ERROR in XML definition, more than 2 adresses");
                }
            } else {
                System.out.println("ERROR in XML definition, no address found for type=" + type.name() + " at (" + p.getX() + "," + p.getY() + ")");
            }
        } else {
            // create a new track panel element
            PanelElement pt = new PanelElement(type, p);
            //pt.createShape();
            panelElements.add(pt);
        }

    }

    private static Position parsePositions(PEType type, Node a) {
        Position pos = new Position();
        NamedNodeMap attributes = a.getAttributes();

        for (int i = 0; i < attributes.getLength(); i++) {
            Node theAttribute = attributes.item(i);
            if (theAttribute.getNodeName().equals("x")) {
                pos.setX(Integer.parseInt(theAttribute.getNodeValue()));
            } else if (theAttribute.getNodeName().equals("y")) {
                pos.setY(YOFF + Integer.parseInt(theAttribute.getNodeValue()));
            } else if (theAttribute.getNodeName().equals("x2")) {
                pos.setX2(Integer.parseInt(theAttribute.getNodeValue()));
            } else if (theAttribute.getNodeName().equals("y2")) {
                pos.setY2(YOFF + Integer.parseInt(theAttribute.getNodeValue()));
            } else if (theAttribute.getNodeName().equals("xt")) {
                pos.setXt(Integer.parseInt(theAttribute.getNodeValue()));
            } else if (theAttribute.getNodeName().equals("yt")) {
                pos.setYt(YOFF + Integer.parseInt(theAttribute.getNodeValue()));
            }
        }
        return pos;

    }
}
