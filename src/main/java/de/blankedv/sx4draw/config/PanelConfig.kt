package de.blankedv.sx4draw.config

import de.blankedv.sx4draw.*
import de.blankedv.sx4draw.model.CompRoute
import de.blankedv.sx4draw.model.Timetable
import de.blankedv.sx4draw.views.SX4Draw
import de.blankedv.sxdraw.Trip
import javafx.collections.FXCollections
import javafx.collections.ObservableList

import javax.xml.bind.annotation.*
import java.util.ArrayList


@XmlRootElement(name = "panel")
@XmlType(propOrder = ["tracks", "turnouts", "sensors", "signals", "routebuttons", "rts", "comprts", "trips", "timetables"])
class PanelConfig {

    @get:XmlAttribute
    var name: String = ""

    @XmlElementWrapper(name = "tracks")
    @get:XmlElement(name = "track")
    private val tracks = ArrayList<Track>()

    @XmlElementWrapper(name = "turnouts")
    @get:XmlElement(name = "turnout")
    private val turnouts = ArrayList<Turnout>()

    @XmlElementWrapper(name = "sensors")
    @get:XmlElement(name = "sensor")
    private val sensors = ArrayList<Sensor>()

    @XmlElementWrapper(name = "signals")
    @get:XmlElement(name = "signal")
    private val signals = ArrayList<Signal>()

    @XmlElementWrapper(name = "routebuttons")
    @get:XmlElement(name = "routebutton")
    private val routebuttons = ArrayList<RouteButton>()

    @XmlElementWrapper(name = "routes")
    @get:XmlElement(name = "route")
    private var rts: List<Route>? = null

    @XmlElementWrapper(name = "comproutes")
    @get:XmlElement(name = "comproute")
    private var comprts: List<CompRoute>? = null

    @XmlElementWrapper(name = "trips")
    @get:XmlElement(name = "trip")
    private var trips = ArrayList<Trip>()

    @XmlElementWrapper(name = "timetables")
    @get:XmlElement(name = "timetable")
    private var timetables = ArrayList<Timetable>()

    constructor()

    constructor (name: String,
                 pes: ArrayList<PanelElement>,
                 routes: ArrayList<Route>,
                 compRoutes: ArrayList<CompRoute>,
                 trips: ArrayList<Trip>,
                 timetables: ArrayList<Timetable>) {
        this.name = name
        setPanelElements(pes)
        this.rts = routes
        this.comprts = compRoutes
        this.trips = trips
        this.timetables = timetables
    }

    private fun setPanelElements(pes: ArrayList<PanelElement>) {
        for (pe in pes) {
            when (pe.gpe) {
                is Track -> tracks.add(pe.gpe as Track)
                is Turnout -> {
                    turnouts.add(pe.gpe as Turnout)
                    System.out.println("set / tu, adr="+pe.gpe.getAddr())
                }
                is Sensor -> sensors.add(pe.gpe as Sensor)
                is Signal -> signals.add(pe.gpe as Signal)
                is RouteButton -> routebuttons.add(pe.gpe as RouteButton)
            }
        }
    }

    fun getAllPanelElements()  : ArrayList<PanelElement> {
        val pes = ArrayList<PanelElement>()
        for (tr in tracks) {
            pes.add(PanelElement(tr))
        }
        for (tu in turnouts) {
            pes.add(PanelElement(tu))
            System.out.println("read / tu, adr="+tu.getAddr())
        }
        for (se in sensors) {
            pes.add(PanelElement(se))
        }
        for (si in signals) {
            pes.add(PanelElement(si))
        }
        for (rt in routebuttons) {
            pes.add(PanelElement(rt))
        }
        return pes
    }

    fun getRoutes() : List<Route>?{
        return rts
    }

    fun getCompoundRoutes() : List<CompRoute>?{
        return comprts
    }

    fun getAllTrips() : ArrayList<Trip> {
        return trips
    }

    fun getAllTimetables() : ArrayList<Timetable> {
        return timetables
    }

}
