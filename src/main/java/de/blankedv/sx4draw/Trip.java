/*
 *<trip id="3100" routeid="2300" sens1="924" sens2="902" loco="29,1,126" stopdelay="1500" />
 */
package de.blankedv.sx4draw;

import static de.blankedv.sx4draw.Constants.INVALID_INT;
import static de.blankedv.sx4draw.SX4Draw.trips;

import java.util.Comparator;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author mblank
 */
public class Trip implements Comparator<Trip>, Comparable<Trip> {

    int id = INVALID_INT;
    int routeid = INVALID_INT;
    int sens1 = INVALID_INT;
    int sens2 = INVALID_INT;
    String loco = "";
    int stopdelay = INVALID_INT;

    Trip() {

    }

    Trip(int id, int routeid, int sens1, int sens2, String loco, int stopdelay) {

        this.id = id;
        this.routeid = routeid;
        this.sens1 = sens1;
        this.sens2 = sens2;
        this.loco = loco;
        this.stopdelay = stopdelay;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRouteid() {
        return routeid;
    }

    public void setRouteid(int routeid) {
        this.routeid = routeid;
    }

    public int getSens1() {
        return sens1;
    }

    public void setSens1(int sens1) {
        this.sens1 = sens1;
    }

    public int getSens2() {
        return sens2;
    }

    public void setSens2(int sens2) {
        this.sens2 = sens2;
    }

    public String getLoco() {
        return loco;
    }

    public void setLoco(String loco) {
        this.loco = loco;
    }

    public int getStopdelay() {
        return stopdelay;
    }

    public void setStopdelay(int stopdelay) {
        this.stopdelay = stopdelay;
    }

    public static void add(Node a) {
        Trip trip = new Trip();
        NamedNodeMap attributes = a.getAttributes();

        for (int i = 0; i < attributes.getLength(); i++) {
            Node theAttribute = attributes.item(i);
            if (theAttribute.getNodeName().equals("id")) {
                trip.id = Integer.parseInt(theAttribute.getNodeValue());
            } else if (theAttribute.getNodeName().equals("sens1")) {
                trip.sens1 = Integer.parseInt(theAttribute.getNodeValue());
            } else if (theAttribute.getNodeName().equals("sens2")) {
                trip.sens2 = Integer.parseInt(theAttribute.getNodeValue());
            } else if ((theAttribute.getNodeName().equals("routeid")) ||
                    (theAttribute.getNodeName().equals("route"))) {  // old def.
                trip.routeid = Integer.parseInt(theAttribute.getNodeValue());
            } else if (theAttribute.getNodeName().equals("stopdelay")) {
                trip.stopdelay = Integer.parseInt(theAttribute.getNodeValue());
            } else if (theAttribute.getNodeName().equals("loco")) {
                trip.loco = theAttribute.getNodeValue();
            }
        }
        if (trip.id != INVALID_INT) {
            trips.add(trip);
        }

    }

    @Override
    public int compare(Trip o1, Trip o2) {
        return o1.id - o2.id;

    }

    @Override
    public int compareTo(Trip o) {
        return id - o.id;
    }
}
