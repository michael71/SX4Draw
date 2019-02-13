package de.blankedv.sx4draw;

import de.blankedv.sxdraw.Trip;

import java.util.ArrayList;

import javax.xml.bind.annotation.*;

import static de.blankedv.sx4draw.WriteConfigNew.FILENAME_XML;

//This statement means that class "LayoutConfig.java" is the root-element
@XmlRootElement(name="layout-config")
@XmlType(propOrder = { "trips" })
public class LayoutConfig {
    /* // XmLElementWrapper generates a wrapper element around XML representation
    @XmlElementWrapper(name = "bookList")
    // XmlElement sets the name of the entities
    @XmlElement(name = "book")
    private ArrayList<Book> bookList; */

    @XmlElementWrapper(name = "trips")
    // XmlElement sets the name of the entities
    @XmlElement(name = "trip")
    private ArrayList<Trip> trips;

    @XmlAttribute
    private String name = "noName";

    @XmlAttribute
    private String filename = FILENAME_XML;


    /*public void setBookList(ArrayList<Book> bookList) {
        this.bookList = bookList;
    }

    public ArrayList<Book> getBooksList() {
        return bookList;
    } */

    public ArrayList<Trip> getTripsList() {
        return trips;
    }

    public void setTrips(ArrayList<Trip> trips) {
        this.trips = trips;
    }

    public String getName2() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
