package de.blankedv.sx4draw;

import de.blankedv.sxdraw.Trip;

import java.util.ArrayList;

import javax.xml.bind.annotation.*;

import static de.blankedv.sx4draw.WriteConfigNew.FILENAME_XML;

//This statement means that class "LayoutConfig.java" is the root-element
@XmlRootElement(name = "layout-config")

@XmlType(propOrder = {"tracks", "turnouts", "sensors", "signals", "trips"})

public class LayoutConfig {
    // XmLElementWrapper generates a wrapper element around XML representation

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

    @XmlElementWrapper(name = "trips")
    @XmlElement(name = "trip")
    private ArrayList<Trip> trips = new ArrayList<>();

    @XmlAttribute
    private String name = "noName";

    @XmlAttribute
    private String filename = FILENAME_XML;


    public void setTrips(ArrayList<Trip> allTrips) {
        this.trips = allTrips;

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

    public String getName2() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
