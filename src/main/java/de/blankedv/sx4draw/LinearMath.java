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

package de.blankedv.sx4draw;

import de.blankedv.sx4draw.SX4Draw.PEType;

import static de.blankedv.sx4draw.ReadConfig.YOFF;
//import java.awt.Point;

public class LinearMath {

    private static final boolean DEBUG_MATH = false;
    private static final int TURNOUT_LENGTH = 10;
    private static final int TURNOUT_LENGTH_LONG = 14;

    /**
     * Computes the intersection between two lines. The calculated point is
     * approximate, since integers are used. If you need a more precise result,
     * use doubles everywhere. (c) 2007 Alexander Hristov. Use Freely (LGPL
     * license). http://www.ahristov.com (c) 2012 Michael Blank, for lines with
     * endpoints
     */
    public static PanelElement trackIntersect(PanelElement e, PanelElement f) {

        // only look for crossing track elements
        if ((e.getType() != PEType.TRACK) || (f.getType() != PEType.TRACK)) {
            return null;
        }

        int x1, y1, x2, y2, x3, y3, x4, y4;

        x1 = e.getX();
        y1 = e.getY();
        x2 = e.getX2();
        y2 = e.getY2();

        x3 = f.getX();
        y3 = f.getY();
        x4 = f.getX2();
        y4 = f.getY2();

        int d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if (d == 0) {
            return null;
        }

        int xi = ((x3 - x4) * (x1 * y2 - y1 * x2) - (x1 - x2)
                * (x3 * y4 - y3 * x4))
                / d;
        int yi = ((y3 - y4) * (x1 * y2 - y1 * x2) - (y1 - y2)
                * (x3 * y4 - y3 * x4))
                / d;
        IntPoint px = new IntPoint(xi, yi);

        // additional code by Michael Blank
        // check if within limit of lines
        int xt, yt, xc1, yc1, xc2, yc2; // for turnout x-thrown, y-thrown, x-closed 1/2,
        // y-closed
        if ((xi >= Math.min(x1, x2)) && (xi <= Math.max(x1, x2)) // within
                // x-limits of first line
                && (xi >= Math.min(x3, x4)) && (xi <= Math.max(x3, x4)) // within
                // x-limits of second line
                && (yi >= Math.min(y1, y2)) && (yi <= Math.max(y1, y2)) // within
                // y-limits of first line
                && (yi >= Math.min(y3, y4)) && (yi <= Math.max(y3, y4))) { // within
            // y-limits of second line

            // check if point is not endpoint of both tracks => no turnout
            if (((xi == e.getX()) && (yi == e.getY()))
                    || // startpoint of e ( e=thrown, f=closed)
                    ((xi == e.getX2()) && (yi == e.getY2()))) { // endpoint of e =>
                // delta-x immer negativ start/endpoint of e
                if (((xi == f.getX()) && (yi == f.getY()))
                        || ((xi == f.getX2()) && (yi == f.getY2()))) {
                    // AND start/endpoint of f
                    return null; // => no turnout
                }
            }

            // check if point is not endpoint of first track
            // and not endpoint of second track
            // => double crossover
            // swap e and f if e is not "durchgehendes Gleis"
            boolean doubleslip = true;
            if (((xi == e.getX()) && (yi == e.getY()))
                    || // startpoint of e ( e=thrown, f=closed)
                    ((xi == e.getX2()) && (yi == e.getY2()))) { // endpoint of e =>
                // delta-x immer negativ
                doubleslip = false;
                // swap e and f => e is always "durchgehendes Gleis" (=close)
                PanelElement temp = new PanelElement(e);
                e = f;
                f = temp;
                if (DEBUG_MATH) System.out.println("swap e/f");
            } // else start/endpoint of f
            else if (((xi == f.getX()) && (yi == f.getY())) || ((xi == f.getX2()) && (yi == f.getY2()))) {
                doubleslip = false;
            }

            if (!doubleslip) {
                // =========== this is a turnout !! ======================
                // find closed and thrown positions (x2>x) both for e and f !!
                // 1. check, turnout which lines' endpoint (xi,yi) belongs
                String es = "E x,y=(" + e.getX() + "," + (e.getY() - YOFF) + ") x2,y2=(" + e.getX2() + "," + (e.getY2() - YOFF) + ")";
                String fs = "F x,y=(" + f.getX() + "," + (f.getY() - YOFF) + ") x2,y2=(" + f.getX2() + "," + (f.getY2() - YOFF) + ")";
                if (DEBUG_MATH) System.out.println(es);
                if (DEBUG_MATH) System.out.println(fs);
                xc1 = xt = xi;
                yc2 = yt = yi;

                if ((xi == f.getX()) && (yi == f.getY())) { // startpoint of f
                    if ((sgn(f.getX2(), f.getX()) == 0) || (sgn(f.getY2(), f.getY()) == 0)) {
                        xt = xi + TURNOUT_LENGTH_LONG * sgn(f.getX2(), f.getX());
                        yt = yi + TURNOUT_LENGTH_LONG * sgn(f.getY2(), f.getY());
                    } else {
                        xt = xi + TURNOUT_LENGTH * sgn(f.getX2(), f.getX());
                        yt = yi + TURNOUT_LENGTH * sgn(f.getY2(), f.getY());
                    }


                } else if ((xi == f.getX2()) && (yi == f.getY2())) { // endpoint of f
                    if ((sgn(f.getX(), f.getX2()) == 0) || (sgn(f.getY(), f.getY2()) == 0)) {
                        xt = xi + TURNOUT_LENGTH_LONG * sgn(f.getX(), f.getX2());
                        yt = yi + TURNOUT_LENGTH_LONG * sgn(f.getY(), f.getY2());
                    } else {
                        xt = xi + TURNOUT_LENGTH * sgn(f.getX(), f.getX2());
                        yt = yi + TURNOUT_LENGTH * sgn(f.getY(), f.getY2());
                    }

                }

                // calculate possible "closed" turnout lines in both
                // directions of "durchgehendes Gleis"
                // choose the one which is nearer to "thrown" line end
                if (e.getX2() == e.getX()) {
                    if (DEBUG_MATH) System.out.println("e senkrecht");
                    xc1 = xi;
                    xc2 = xi;
                    yc1 = yi + TURNOUT_LENGTH_LONG * sgn(e.getY2(), e.getY());
                    yc2 = yi - TURNOUT_LENGTH_LONG * sgn(e.getY2(), e.getY());
                } else if (e.getY2() == e.getY()) {
                    if (DEBUG_MATH) System.out.println("e waagerecht");
                    xc1 = xi + TURNOUT_LENGTH_LONG * sgn(e.getX2(), e.getX());
                    xc2 = xi - TURNOUT_LENGTH_LONG * sgn(e.getX2(), e.getX());
                    yc1 = yi;
                    yc2 = yi;
                } else {
                    if (((e.getY2() - e.getY()) / (e.getX2() - e.getX())) > 0) {
                        if (DEBUG_MATH) System.out.println("e s>0");
                        xc1 = xi + TURNOUT_LENGTH;
                        yc1 = yi + TURNOUT_LENGTH;
                        xc2 = xi - TURNOUT_LENGTH;
                        yc2 = yi - TURNOUT_LENGTH;
                    } else {
                        if (DEBUG_MATH) System.out.println("e s<0");
                        xc1 = xi - TURNOUT_LENGTH;
                        yc1 = yi + TURNOUT_LENGTH;
                        xc2 = xi + TURNOUT_LENGTH;
                        yc2 = yi - TURNOUT_LENGTH;
                    }
                }

                IntPoint p1 = new IntPoint(xc1, yc1);
                IntPoint p2 = new IntPoint(xc2, yc2);
                if (DEBUG_MATH)
                    System.out.println("pi=" + xi + "," + yi + "  p1=" + p1.toString() + " p2=" + p2.toString());
                double d1 = Utils.INSTANCE.calcDistance(new IntPoint(xt, yt), p1);
                double d2 = Utils.INSTANCE.calcDistance(new IntPoint(xt, yt), new IntPoint(xc2, yc2));
                if (DEBUG_MATH) System.out.println("d1 = " + d1 + " d2=" + d2);
                if (Math.abs(d1 - d2) < 1.0) {
                    // gleise kreuzen bei 90grad => keine Weiche
                    return null;
                } else if (d1 < d2) {
                    return new PanelElement(PEType.TURNOUT, px, new IntPoint(xc1, yc1),
                            new IntPoint(xt, yt));
                } else {
                    return new PanelElement(PEType.TURNOUT, px, new IntPoint(xc2, yc2),
                            new IntPoint(xt, yt));
                }
            } else {
                if (DEBUG_MATH) System.out.println("doubleslip at " + px.getX() + "," + px.getY() + " ??");
                //return new DoubleSlipElement(px, px, px);
                return null;
            }
        } else {
            return null; // for debugging: new PanelElement("turnok", px);
        }
    }

    private static int sgn(int a, int b) {
        int res;
        if (a == b) {
            res = 0;
        } else if (a > b) {
            res = 1;
        } else {
            res = -1;
        }
        //System.out.println("a=" + a + " b=" + b + " sgn=" + res);
        return res;
    }
}
