/*
SX4Draw
Copyright (C) 2019 Michael Blank

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.blankedv.sx4draw.config

import de.blankedv.sx4draw.*
import de.blankedv.sx4draw.model.*

import javax.xml.bind.annotation.*
import java.util.ArrayList


@XmlRootElement(name = "panel")
//@XmlType(propOrder = ["track", "turnout", "sensor", "signal", "routebutton", "route", "comproute", "trip", "timetable"])
class PanelConfig {

    @get:XmlAttribute
    var name: String = ""

    @XmlElementWrapper(name = "locos")
    @get:XmlElement(name = "loco")
    private var loco = ArrayList<Loco>()

    @XmlElementWrapper(name = "tracks")
    @get:XmlElement(name = "track")
    private val track = ArrayList<Track>()

    @XmlElementWrapper(name = "turnouts")
    @get:XmlElement(name = "turnout")
    private val turnout = ArrayList<Turnout>()

    @XmlElementWrapper(name = "sensors")
    @get:XmlElement(name = "sensor")
    private val sensor = ArrayList<Sensor>()

    @XmlElementWrapper(name = "signals")
    @get:XmlElement(name = "signal")
    private val signal = ArrayList<Signal>()

    @XmlElementWrapper(name = "routebuttons")
    @get:XmlElement(name = "routebutton")
    private val routebutton = ArrayList<RouteButton>()

    @XmlElementWrapper(name = "routes")
    @get:XmlElement(name = "route")
    private var route: List<Route>? = ArrayList<Route>()

    @XmlElementWrapper(name = "comproutes")
    @get:XmlElement(name = "comproute")
    private var comproute: List<CompRoute>? = ArrayList<CompRoute>()

    @XmlElementWrapper(name = "trips")
    @get:XmlElement(name = "trip")
    private var trip = ArrayList<Trip>()

    @XmlElementWrapper(name = "timetables")
    @get:XmlElement(name = "timetable")
    private var timetable = ArrayList<Timetable>()

    constructor()

    constructor (name: String,
                 ls: ArrayList<Loco>,
                 pes: ArrayList<PanelElement>,
                 routes: ArrayList<Route>,
                 compRoutes: ArrayList<CompRoute>,
                 trips: ArrayList<Trip>,
                 timetables: ArrayList<Timetable>) {
        this.name = name
        this.loco = ls
        setPanelElements(pes)
        this.route = routes
        this.comproute = compRoutes
        this.trip = trips
        this.timetable = timetables
    }

    private fun setPanelElements(pes: ArrayList<PanelElement>) {
        for (pe in pes) {
            when (pe.gpe) {
                is Track -> track.add(pe.gpe as Track)
                is Turnout -> turnout.add(pe.gpe as Turnout)
                is Sensor -> sensor.add(pe.gpe as Sensor)
                is Signal -> signal.add(pe.gpe as Signal)
                is RouteButton -> routebutton.add(pe.gpe as RouteButton)
            }
        }
    }

    fun getAllPanelElements() : ArrayList<PanelElement> {
        val pes = ArrayList<PanelElement>()
        for (tr in track) {
            pes.add(PanelElement(tr))
        }
        for (tu in turnout) {
            tu.evalOldAddress()  // conv sxadr/sxbit -> adr
            pes.add(PanelElement(tu))
        }
        for (se in sensor) {
            se.evalOldAddress()   // conv sxadr/sxbit -> adr
            pes.add(PanelElement(se))
        }
        for (si in signal) {
            pes.add(PanelElement(si))
        }
        for (rt in routebutton) {
            pes.add(PanelElement(rt))
        }
        return pes
    }

    fun getAllRoutes() : List<Route>?{
        return route
    }

    fun getAllCompRoutes() : List<CompRoute>?{
        return comproute
    }

    fun getAllTrips() : ArrayList<Trip> {
        return trip
    }

    fun getAllTimetables() : ArrayList<Timetable> {
        return timetable
    }

    fun getAllLocos() : ArrayList<Loco> {
        return loco
    }

}
