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
import de.blankedv.sx4draw.views.SX4Draw.PEType;

import static de.blankedv.sx4draw.views.SX4Draw.panelElements;
import static de.blankedv.sx4draw.config.ReadConfig.YOFF;

/**
 * @author Michael Blank <mblank@bawue.de>
 */
public class Calc {

    public static void turnouts() {
        // check for intersection of track, if new, add a turnout with unknown SX address
        for (int i = 0; i < panelElements.size(); i++) {
            PanelElement p = panelElements.get(i);

            for (int j = i + 1; j < panelElements.size(); j++) {
                PanelElement q = panelElements.get(j);

                //System.out.println("checkin tracks at "+p.x+","+p.y+ " and "+q.x+","+ q.y);
                PanelElement turnout = LinearMath.trackIntersect(p, q);

                if (turnout != null) {
                    // there is an intersection => make new turnoout
                    //if (DEBUG) {
                    //    System.out.println("(i,j)=(" + i + "," + j + ") new? turnout found at x="
                    //            + turnout.x + " y=" + (turnout.y - YOFF) + " xc=" + turnout.x2 +
                    //            " yc=" + (turnout.y2 - YOFF) + " xt=" + turnout.xt + " yt=" + (turnout.yt - YOFF));
                    //}

                    // check whether this turnout is already known
                    boolean known = false;
                    for (PanelElement e : panelElements) {
                        if ((e.getType() == PEType.TURNOUT) && (e.getX() == turnout.getX()) && (e.getY() == turnout.getY())
                                // at the approximately the same position => match
                                && ((Math.abs(e.getX2() - turnout.getX2()) <= 5) && (Math.abs(e.getY2() - turnout.getY2()) <= 5) &&
                                (Math.abs(e.getXt() - turnout.getXt()) <= 5) && (Math.abs(e.getYt() - turnout.getYt()) <= 5))
                                // thrown vs. closed position reversed => match
                                || ((Math.abs(e.getX2() - turnout.getXt()) <= 5) && (Math.abs(e.getY2() - turnout.getYt()) <= 5) &&
                                (Math.abs(e.getXt() - turnout.getX2()) <= 5) && (Math.abs(e.getYt() - turnout.getY2()) <= 5))) {

                            known = true;
                            break;
                        }
                    }
                    if (!known) {
                        System.out.println("adding turnout at " + turnout.getX() + "," + (turnout.getY() - YOFF));
                        turnout.setAdr(1);  // dummy address to have an address stored as placeholder in XML File
                        panelElements.add(turnout);  // with unknown SX address
                    } else {
                        // System.out.println("already known at " + turnout.x + "," + (turnout.y- YOFF));
                    }
                }
            }
        }

    }
}
