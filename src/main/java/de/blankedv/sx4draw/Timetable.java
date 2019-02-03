/*
 *<timetable id="3300" time="0,20,40" trip="3100,3101,0" next=""/>
 */
package de.blankedv.sx4draw;

import static de.blankedv.sx4draw.Constants.INVALID_INT;
import static de.blankedv.sx4draw.SX4Draw.timetables;

import java.util.Comparator;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author mblank
 */
public class Timetable implements Comparator<Timetable>, Comparable<Timetable> {

    int id = INVALID_INT;
    String time = "";
    String trip = "";
    String next = "";


    Timetable() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTrip() {
        return trip;
    }

    public void setTrip(String trip) {
        this.trip = trip;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }


    public static void add(Node a) {
        Timetable tt = new Timetable();
        NamedNodeMap attributes = a.getAttributes();

        for (int i = 0; i < attributes.getLength(); i++) {
            Node theAttribute = attributes.item(i);
            if (theAttribute.getNodeName().equals("id")) {
                tt.id = Integer.parseInt(theAttribute.getNodeValue());
            } else if (theAttribute.getNodeName().equals("time")) {
                tt.time = theAttribute.getNodeValue();
            } else if (theAttribute.getNodeName().equals("trip")) {
                tt.trip = theAttribute.getNodeValue();
            } else if (theAttribute.getNodeName().equals("next")) {
                tt.next = theAttribute.getNodeValue();
            }
        }
        if (tt.id != INVALID_INT) {
            timetables.add(tt);
        }

    }

    @Override
    public int compare(Timetable o1, Timetable o2) {
        return o1.id - o2.id;
    }

    @Override
    public int compareTo(Timetable o) {
        return id - o.id;
    }
}
