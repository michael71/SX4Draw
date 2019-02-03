/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.blankedv.sx4draw;

import static de.blankedv.sx4draw.Constants.INVALID_INT;
import static de.blankedv.sx4draw.SX4Draw.routes;

import de.blankedv.sx4draw.PanelElement.PEState;

import java.util.ArrayList;
import java.util.Comparator;

import javafx.util.Pair;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * <route id="2201" btn1="1200" btn2="1203" route="854,1;853,0;852,0;765,2;767,0;761,0;781,0" sensors="924,928" offending="" />
 * for editing route data
 *
 * @author mblank
 */
public class Route implements Comparator<Route>, Comparable<Route> {

    int id = INVALID_INT;
    int btn1 = INVALID_INT;
    int btn2 = INVALID_INT;
    String route = "";
    String sensors = "";
    String offending = "";
    ArrayList<PanelElement> myPEs = null;

    Route() {

    }

    Route(int id) {
        this.id = id;
    }

    Route(int id, int btn1, int btn2, String route, String sensors, String offending) {

        this.id = id;
        this.btn1 = btn1;
        this.btn2 = btn2;
        this.route = route;
        this.sensors = sensors;
        this.offending = offending;
    }

    Route(Route r) {

        this.id = r.id;
        this.btn1 = r.btn1;
        this.btn2 = r.btn2;
        this.route = r.route;
        this.sensors = r.sensors;
        this.offending = r.offending;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBtn1() {
        return btn1;
    }

    public void setBtn1(int btn1) {
        this.btn1 = btn1;
    }

    public int getBtn2() {
        return btn2;
    }

    public void setBtn2(int btn2) {
        this.btn2 = btn2;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getSensors() {
        return sensors;
    }

    public void setSensors(String sensors) {
        this.sensors = sensors;
    }

    public String getOffending() {
        return offending;
    }

    public void setOffending(String offending) {
        this.offending = offending;
    }

    /**
     * add a turnout, signal or sensor to a route
     *
     * @param pe
     */
    public void addElement(PanelElement pe) {
        switch (pe.type) {
            case SENSOR:
                if (sensors.isEmpty()) {
                    sensors = "" + pe.adr;
                } else {
                    sensors = sensors + "," + pe.adr;
                }
                break;
            case TURNOUT:
            case SIGNAL:
                if (route.isEmpty()) {
                    route = "" + pe.adr + ",0";
                    ;
                } else {
                    route = route + ";" + pe.adr + ",0";
                    ;
                }
                break;
        }
    }

    /**
     * add a turnout, signal or sensor to a route
     *
     * @param peSt
     */
    public void addElement(Pair<PanelElement, Integer> peSt) {
        switch (peSt.getKey().type) {
            case SENSOR:
                if (sensors.isEmpty()) {
                    sensors = "" + peSt.getKey().adr;
                } else {
                    sensors = sensors + "," + peSt.getKey().adr;
                }
                break;
            case TURNOUT:
                if (route.isEmpty()) {
                    route = "" + peSt.getKey().adr + "," + peSt.getValue();
                } else {
                    route = route + ";" + peSt.getKey().adr + "," + peSt.getValue();
                }
                break;
            case SIGNAL:
                if (route.isEmpty()) {
                    route = "" + peSt.getKey().adr + ",0";
                } else {
                    route = route + ";" + peSt.getKey().adr + ",0";
                }
                break;
        }
    }

    /**
     * mark a route by marking all its panel elements
     *
     * @param mark
     */
    public void setMarked(boolean mark) {
        ArrayList<PanelElement> pes = getPEs();  // implicit setRouteState
        if (mark) {
            for (PanelElement pe : pes) {
                pe.createShapeAndSetState(PEState.MARKED);
            }
        } else {
            for (PanelElement pe : pes) {
                pe.createShapeAndSetState(PEState.DEFAULT);
            }
        }
    }

    public void setRouteStates() {

        PanelElement.resetState();
        // route buttons
        PanelElement.getPeByAddress(btn1).get(0).createShapeAndSetState(PEState.MARKED);
        PanelElement.getPeByAddress(btn2).get(0).createShapeAndSetState(PEState.MARKED);
        // extract PEs from string information

        // turnouts and signals
        // all have a state "rst" which is different from INVALID_ID
        String[] routeInfo = route.split(";");
        for (String rt : routeInfo) {
            System.out.println("routeInfo " + rt);
            String[] elementAndState = rt.split(",");
            try {
                int a = Integer.parseInt(elementAndState[0]);
                int rst = Integer.parseInt(elementAndState[1]);
                ArrayList<PanelElement> peList = PanelElement.getPeByAddress(a);
                for (PanelElement pe : peList) {
                    if (rst == 0) {
                        pe.createShapeAndSetState(PEState.STATE_0);
                    } else {
                        pe.createShapeAndSetState(PEState.STATE_1);
                        // TODO distinguish between 1 and 2 (=yellow)
                    }
                    System.out.println("set pe.adr=" + pe.adr + " rst=" + rst);
                }
            } catch (Exception e) {
                // do nothing
            }
        }

        // now sensors
        routeInfo = sensors.split(",");
        for (String s : routeInfo) {
            System.out.println("sensor " + s);
            try {
                int a = Integer.parseInt(s);
                ArrayList<PanelElement> sensList = PanelElement.getPeByAddress(a);
                for (PanelElement pe : sensList) {
                    pe.createShapeAndSetState(PEState.MARKED);
                }
            } catch (Exception e) {
                // do nothing
            }
        }

    }


    private ArrayList getPEs() {

        ArrayList<PanelElement> pes = new ArrayList<>();

        // route buttons
        pes.addAll(PanelElement.getPeByAddress(btn1));
        pes.addAll(PanelElement.getPeByAddress(btn2));
        // extract PEs from string information

        // turnouts and signals
        String[] routeInfo = route.split(";");
        for (String rt : routeInfo) {
            System.out.println("routeInfo " + rt);
            String[] elementAndState = rt.split(",");
            try {
                int a = Integer.parseInt(elementAndState[0]);
                ArrayList<PanelElement> peList = PanelElement.getPeByAddress(a);
                pes.addAll(peList);
            } catch (Exception e) {
                // do nothing
            }
        }

        // now sensors
        routeInfo = sensors.split(",");
        for (String s : routeInfo) {
            System.out.println("sensor " + s);
            try {
                int a = Integer.parseInt(s);
                ArrayList<PanelElement> peList = PanelElement.getPeByAddress(a);
                pes.addAll(peList);
            } catch (Exception e) {
                // do nothing
            }
        }

        System.out.println("route contains " + pes.size() + " elements");
        return pes;
    }


    public static void add(Node a) {
        Route rt = new Route();
        NamedNodeMap attributes = a.getAttributes();

        for (int i = 0; i < attributes.getLength(); i++) {
            Node theAttribute = attributes.item(i);
            if (theAttribute.getNodeName().equals("id")) {
                rt.id = Integer.parseInt(theAttribute.getNodeValue());
            } else if (theAttribute.getNodeName().equals("btn1")) {
                rt.btn1 = Integer.parseInt(theAttribute.getNodeValue());
            } else if (theAttribute.getNodeName().equals("btn2")) {
                rt.btn2 = Integer.parseInt(theAttribute.getNodeValue());
            } else if (theAttribute.getNodeName().equals("route")) {
                rt.route = theAttribute.getNodeValue();
            } else if (theAttribute.getNodeName().equals("sensors")) {
                rt.sensors = theAttribute.getNodeValue();
            } else if (theAttribute.getNodeName().equals("offending")) {
                rt.offending = theAttribute.getNodeValue();
            }
        }
        if (rt.id != INVALID_INT) {
            routes.add(rt);
        }
    }

    @Override
    public int compare(Route o1, Route o2) {
        return o1.id - o2.id;

    }

    @Override
    public int compareTo(Route o) {
        return id - o.id;
    }

    public static int getnewid() {
        int newID = 2200;  // start with 2200
        for (Route rt : routes) {
            if (rt.getId() > newID) {
                newID = rt.getId();
            }
        }
        return newID + 1;
    }
}
