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
package de.blankedv.sx4draw.util;


import de.blankedv.sx4draw.PanelElement;
import de.blankedv.sx4draw.Track;
import de.blankedv.sx4draw.Turnout;
import de.blankedv.sx4draw.model.GenericPE;

import java.util.ArrayList;

import static de.blankedv.sx4draw.views.SX4Draw.panelElements;

/**
 * @author Michael Blank <mblank@bawue.de>
 */
public class Calc {

    public static void turnouts() {
        // check for intersection of track, if new, add a turnout with unknown SX address
        ArrayList<Track> tracks = new ArrayList<>();
        for (PanelElement pe : panelElements) {
            if (pe.gpe instanceof Track) {
                tracks.add((Track) pe.gpe);

            }
        }

        for (int i = 0; i < tracks.size(); i++) {
            Track p1 = tracks.get(i);
            for (int j = i + 1; j < tracks.size(); j++) {
                Track q1 = tracks.get(j);
                //System.out.println("checkin tracks at "+p.x+","+p.y+ " and "+q.x+","+ q.y);
                Turnout tu = LinearMath.trackIntersect(p1, q1);

                if (tu != null) {
                    // there is an intersection => make new turnoout
                    //if (DEBUG) {
                    //    System.out.println("(i,j)=(" + i + "," + j + ") new? turnout found at x="
                    //            + turnout.x + " y=" + (turnout.y - YOFF) + " xc=" + turnout.x2 +
                    //            " yc=" + (turnout.y2 - YOFF) + " xt=" + turnout.xt + " yt=" + (turnout.yt - YOFF));
                    //}

                    // check whether this turnout is already known
                    boolean known = false;
                    for (PanelElement e : panelElements) {
                        GenericPE g = e.gpe;
                        if (g instanceof Turnout) {
                            Turnout t2 = (Turnout) g;
                            if ( (t2.getX() == tu.getX()) && (t2.getY() == tu.getY())
                                    // at the approximately the same position => match
                                  && (
                                    ((Math.abs(t2.getX2() - tu.getX2()) <= 5) && (Math.abs(t2.getY2() - tu.getY2()) <= 5) &&
                                    (Math.abs(t2.getXt() - tu.getXt()) <= 5) && (Math.abs(t2.getYt() - tu.getYt()) <= 5))
                                    // thrown vs. closed position reversed => match
                                    || ((Math.abs(t2.getX2() - tu.getXt()) <= 5) && (Math.abs(t2.getY2() - tu.getYt()) <= 5) &&
                                    (Math.abs(t2.getXt() - tu.getX2()) <= 5) && (Math.abs(t2.getYt() - tu.getY2()) <= 5) ) )
                               ) {
                                known = true;
                                break;
                            }
                        }
                    }
                    if (!known) {
                        System.out.println("adding turnout at " + tu.getX() + "," + tu.getY() );
                        panelElements.add(new PanelElement(tu));  // with unknown SX address
                    } else {
                        System.out.println("already known turnout at " + tu.getX() + "," + tu.getY());
                    }
                }
            }
        }

    }
}
