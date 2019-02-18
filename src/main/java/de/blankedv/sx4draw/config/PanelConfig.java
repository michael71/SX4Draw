package de.blankedv.sx4draw.config;

import de.blankedv.sx4draw.*;
import de.blankedv.sx4draw.model.CompRoute;
import de.blankedv.sx4draw.model.Timetable;
import de.blankedv.sx4draw.views.SX4Draw;
import de.blankedv.sxdraw.Trip;
import javafx.collections.ObservableList;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


@XmlRootElement(name = "panel")
@XmlType(propOrder = {"tracks", "turnouts", "sensors", "signals", "routebuttons", "rts", "comprts", "trips", "timetables"})
public class PanelConfig {

    @XmlAttribute
    private String name;

    @XmlElementWrapper(name = "tracks")
    @XmlElement(name = "track")
    private ArrayList<Track> tracks = new ArrayList<>();

    @XmlElementWrapper(name = "turnouts")
    @XmlElement(name = "turnout")
    private ArrayList<Turnout> turnouts = new ArrayList<>();

    @XmlElementWrapper(name = "sensors")
    @XmlElement(name = "sensor")
    private ArrayList<Sensor> sensors = new ArrayList<>();

    @XmlElementWrapper(name = "signals")
    @XmlElement(name = "signal")
    private ArrayList<Signal> signals = new ArrayList<>();

    @XmlElementWrapper(name = "routebuttons")
    @XmlElement(name = "routebutton")
    private ArrayList<RouteButton> routebuttons = new ArrayList<>();

    @XmlElementWrapper(name = "routes")
    @XmlElement(name = "route")
    private List<Route> rts;

    @XmlElementWrapper(name = "comproutes")
    @XmlElement(name = "comproute")
    private List<CompRoute> comprts;

    @XmlElementWrapper(name = "trips")
    @XmlElement(name = "trip")
    private ArrayList<Trip> trips = new ArrayList<>();

    @XmlElementWrapper(name = "timetables")
    @XmlElement(name = "timetable")
    private ArrayList<Timetable> timetables = new ArrayList<>();



    public void setName(String n) {
        name = n;
    }

    public void setTrips(ArrayList<Trip> allTrips, ArrayList<Timetable> allTTs) {
        this.trips = allTrips;
        this.timetables = allTTs;

    }

    public ArrayList<PanelElement> initPanelElement() {

        ArrayList<PanelElement> panelElements = new ArrayList<>();
      /*  for (Track tr : tracks) {
            PanelElement pe = new PanelElement((GenericPE)tr);
            panelElements.add(pe)
        } */
      return panelElements;

    }

    public void setPanelElements(ArrayList<PanelElement> pes) {
        for (PanelElement pe : pes) {
            switch (pe.getType()) {
                case TRACK:
                    tracks.add((Track)pe.gpe);
                    break;
                case TURNOUT:
                    turnouts.add((Turnout)pe.gpe);
                    break;
                case SENSOR:
                    sensors.add((Sensor)pe.gpe);
                    break;
                case SIGNAL:
                    signals.add((Signal)pe.gpe);
                    break;
                case ROUTEBUTTON:
                    routebuttons.add((RouteButton)pe.gpe);
                    break;

            }

        }
    }

    public void setRoutes(ObservableList<Route> routes, ObservableList<CompRoute> comproutes) {
        rts = routes;
        comprts = comproutes;
    }

    public void clear() {
        name = "?";

        tracks.clear();
        turnouts.clear();
        sensors.clear();
        routebuttons.clear();
        signals.clear();

        rts.clear();
        comprts.clear();
        trips.clear();
        timetables.clear();
    }
}
