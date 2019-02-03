/*
 * <comproute id="2300" btn1="1200" btn2="1204" routes="2201,2206" />
 */
package de.blankedv.sx4draw;

import static de.blankedv.sx4draw.Constants.INVALID_INT;
import static de.blankedv.sx4draw.SX4Draw.compRoutes;

import java.util.Comparator;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author mblank
 */
public class CompRoute implements Comparator<CompRoute>, Comparable<CompRoute> {

    int id;
    int btn1;
    int btn2;
    String routes;

    CompRoute() {

    }

    CompRoute(int id, int btn1, int btn2, String routes) {

        this.id = id;
        this.btn1 = btn1;
        this.btn2 = btn2;
        this.routes = routes;

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

    public String getRoutes() {
        return routes;
    }

    public void setRoutes(String routes) {
        this.routes = routes;
    }

    public static void add(Node a) {
        CompRoute rt = new CompRoute();
        NamedNodeMap attributes = a.getAttributes();

        for (int i = 0; i < attributes.getLength(); i++) {
            Node theAttribute = attributes.item(i);
            if (theAttribute.getNodeName().equals("id")) {
                rt.id = Integer.parseInt(theAttribute.getNodeValue());
            } else if (theAttribute.getNodeName().equals("btn1")) {
                rt.btn1 = Integer.parseInt(theAttribute.getNodeValue());
            } else if (theAttribute.getNodeName().equals("btn2")) {
                rt.btn2 = Integer.parseInt(theAttribute.getNodeValue());
            } else if (theAttribute.getNodeName().equals("routes")) {
                rt.routes = theAttribute.getNodeValue();
            }
        }
        if (rt.id != INVALID_INT) {
            compRoutes.add(rt);
        }
    }

    @Override
    public int compare(CompRoute o1, CompRoute o2) {
        return o1.id - o2.id;

    }

    @Override
    public int compareTo(CompRoute o) {
        return id - o.id;
    }
}
