package de.blankedv.sx4draw;

import de.blankedv.sxdraw.Trip;
import javafx.collections.ObservableList;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

import static de.blankedv.sx4draw.SX4Draw.panelName;
import static de.blankedv.sx4draw.WriteConfigNew.FILENAME_XML;



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

        setTracks(pes);
        setTurnouts(pes);
        setSensors(pes);
        setSignals(pes);
        setRouteButtons(pes);
    }

    public void setRoutes(ObservableList<Route> routes, ObservableList<CompRoute> comproutes) {
        rts = routes;
        comprts = comproutes;
    }

    public void setTracks(ArrayList<PanelElement> pes) {
        for (PanelElement pe : pes) {
            if (pe.getType() == SX4Draw.PEType.TRACK)
                tracks.add(new Track(pe));
        }
    }

    public void setTurnouts(ArrayList<PanelElement> pes) {
        for (PanelElement pe : pes) {
            if (pe.getType() == SX4Draw.PEType.TURNOUT)
                turnouts.add(new Turnout(pe));
        }
    }

    public void setSensors(ArrayList<PanelElement> pes) {
        for (PanelElement pe : pes) {
            if (pe.getType() == SX4Draw.PEType.SENSOR)
                sensors.add(new Sensor(pe));
        }
    }


    public void setSignals(ArrayList<PanelElement> pes) {
        for (PanelElement pe : pes) {
            if (pe.getType() == SX4Draw.PEType.SIGNAL)
                signals.add(new Signal(pe));
        }
    }

    public void setRouteButtons(ArrayList<PanelElement> pes) {
        for (PanelElement pe : pes) {
            if (pe.getType() == SX4Draw.PEType.ROUTEBUTTON)
                routebuttons.add(new RouteButton(pe));
        }
    }

}
