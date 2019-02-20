package de.blankedv.sx4draw.config

import de.blankedv.sx4draw.*
import de.blankedv.sx4draw.model.CompRoute
import de.blankedv.sx4draw.model.Timetable
import de.blankedv.sx4draw.views.SX4Draw
import de.blankedv.sxdraw.Trip
import javafx.collections.ObservableList

import javax.xml.bind.annotation.*
import java.util.ArrayList


@XmlRootElement(name = "panel")
@XmlType(propOrder = ["tracks", "turnouts", "sensors", "signals", "routebuttons", "rts", "comprts", "trips", "timetables"])
class PanelConfig {

    @XmlAttribute
    private var name: String? = null

    @XmlElementWrapper(name = "tracks")
    @XmlElement(name = "track")
    private val tracks = ArrayList<Track>()

    @XmlElementWrapper(name = "turnouts")
    @XmlElement(name = "turnout")
    private val turnouts = ArrayList<Turnout>()

    @XmlElementWrapper(name = "sensors")
    @XmlElement(name = "sensor")
    private val sensors = ArrayList<Sensor>()

    @XmlElementWrapper(name = "signals")
    @XmlElement(name = "signal")
    private val signals = ArrayList<Signal>()

    @XmlElementWrapper(name = "routebuttons")
    @XmlElement(name = "routebutton")
    private val routebuttons = ArrayList<RouteButton>()

    @XmlElementWrapper(name = "routes")
    @XmlElement(name = "route")
    private var rts: List<Route>? = null

    @XmlElementWrapper(name = "comproutes")
    @XmlElement(name = "comproute")
    private var comprts: List<CompRoute>? = null

    @XmlElementWrapper(name = "trips")
    @XmlElement(name = "trip")
    private var trips = ArrayList<Trip>()

    @XmlElementWrapper(name = "timetables")
    @XmlElement(name = "timetable")
    private var timetables = ArrayList<Timetable>()


    fun setName(n: String) {
        name = n
    }

    fun setTrips(allTrips: ArrayList<Trip>, allTTs: ArrayList<Timetable>) {
        this.trips = allTrips
        this.timetables = allTTs

    }

    fun initPanelElement(): ArrayList<PanelElement> {

/*  for (Track tr : tracks) {
            PanelElement pe = new PanelElement((GenericPE)tr);
            panelElements.add(pe)
        } */
        return ArrayList()

    }

    fun setPanelElements(pes: ArrayList<PanelElement>) {
        for (pe in pes) {
            when (pe.gpe) {
               is Track -> tracks.add(pe.gpe as Track)
               is Turnout -> turnouts.add(pe.gpe as Turnout)
               is Sensor -> sensors.add(pe.gpe as Sensor)
               is Signal -> signals.add(pe.gpe as Signal)
               is RouteButton -> routebuttons.add(pe.gpe as RouteButton)
            }
        }
    }

    fun setRoutes(routes: ObservableList<Route>, comproutes: ObservableList<CompRoute>) {
        rts = routes
        comprts = comproutes
    }

}
