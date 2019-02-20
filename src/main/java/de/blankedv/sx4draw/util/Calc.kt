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
package de.blankedv.sx4draw.util


import de.blankedv.sx4draw.PanelElement
import de.blankedv.sx4draw.Track
import de.blankedv.sx4draw.Turnout
import de.blankedv.sx4draw.model.GenericPE

import java.util.ArrayList

import de.blankedv.sx4draw.views.SX4Draw.Companion.panelElements

/**
 * @author Michael Blank <mblank></mblank>@bawue.de>
 */
object Calc {

    fun turnouts() {
        // check for intersection of track, if new, add a turnout with unknown SX address
        val tracks = ArrayList<Track>()
        for (pe in panelElements) {
            if (pe.gpe is Track) {
                tracks.add(pe.gpe as Track)

            }
        }

        for (i in tracks.indices) {
            val p1 = tracks[i]
            for (j in i + 1 until tracks.size) {
                val q1 = tracks[j]
                //System.out.println("checkin tracks at "+p.x+","+p.y+ " and "+q.x+","+ q.y);
                val tu = LinearMath.trackIntersect(p1, q1)

                if (tu != null) {
                    // there is an intersection => make new turnoout
                    //if (DEBUG) {
                    //    System.out.println("(i,j)=(" + i + "," + j + ") new? turnout found at x="
                    //            + turnout.x + " y=" + (turnout.y - YOFF) + " xc=" + turnout.x2 +
                    //            " yc=" + (turnout.y2 - YOFF) + " xt=" + turnout.xt + " yt=" + (turnout.yt - YOFF));
                    //}

                    // check whether this turnout is already known
                    var known = false
                    for (e in panelElements) {
                        val g = e.gpe
                        if (g is Turnout) {
                            val t2 = g as Turnout
                            if (t2.x == tu.x && t2.y == tu.y
                                    // at the approximately the same position => match
                                    && (Math.abs(t2.x2 - tu.x2) <= 5 && Math.abs(t2.y2 - tu.y2) <= 5 &&
                                            Math.abs(t2.xt - tu.xt) <= 5 && Math.abs(t2.yt - tu.yt) <= 5
                                            // thrown vs. closed position reversed => match
                                            || Math.abs(t2.x2 - tu.xt) <= 5 && Math.abs(t2.y2 - tu.yt) <= 5 &&
                                            Math.abs(t2.xt - tu.x2) <= 5 && Math.abs(t2.yt - tu.y2) <= 5)) {
                                known = true
                                break
                            }
                        }
                    }
                    if (!known) {
                        println("adding turnout at " + tu.x + "," + tu.y)
                        panelElements.add(PanelElement(tu))  // with unknown SX address
                    } else {
                        println("already known turnout at " + tu.x + "," + tu.y)
                    }
                }
            }
        }

    }
}
