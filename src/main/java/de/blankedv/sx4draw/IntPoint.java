/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.blankedv.sx4draw;

/**
 * @author mblank
 */
public class IntPoint {

    public int x;
    public int y;

    IntPoint() {
        this.x = 0;
        this.y = 0;
    }

    IntPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    IntPoint(double x, double y) {
        this.x = (int) x;
        this.y = (int) y;
    }

    /**
     * distance between two points, rounded to the next 10
     *
     * @param start
     * @param end
     * @return
     */
    static public IntPoint delta(IntPoint start, IntPoint end, int raster) {

        IntPoint d = new IntPoint(-start.x, -start.y);
        d.x += end.x;
        d.y += end.y;
        return d.toRaster(d, raster);
    }


    static public IntPoint toRaster(IntPoint mp, int raster) {
        if (raster == 1) return mp;
        //System.out.print("Raster in: "+mp.x+","+mp.y+" ");
        int rh = raster / 2;
        int nx = ((mp.x + rh) / raster) * raster;
        int ny = ((mp.y + rh) / raster) * raster;
        //System.out.println(" out: "+nx+","+ny);
        return new IntPoint(nx, ny);
    }

    static public IntPoint correctAngle(IntPoint s, IntPoint mp, int raster) {

        IntPoint result = mp;
        // calculate angle of line s -> mp
        double a = mp.y - s.y;
        double b = mp.x - s.x;
        double hyp = Math.sqrt(a * a + b * b);

        double tan = a / b;
        int dx;

        if (Math.abs(tan) < 0.5) {
            // force angle = 0
            if (b > 0) {
                result = new IntPoint(s.x + hyp, s.y);
            } else {
                result = new IntPoint(s.x - hyp, s.y);
            }
            return toRaster(result, raster);
        } else if (Math.abs(tan) <= 2) {
            int len = (int) Math.round(hyp / 1.4);
            dx = (len / raster) * raster;
            // force angle = 45
            if (b > 0) {
                if (a > 0) {
                    result = new IntPoint(s.x + dx, s.y + dx);
                } else {
                    result = new IntPoint(s.x + dx, s.y - dx);
                }
            } else {
                if (a > 0) {
                    result = new IntPoint(s.x - dx, s.y + dx);
                } else {
                    result = new IntPoint(s.x - dx, s.y - dx);
                }
            }
            return toRaster(result, raster);
        } else {
            // angle = 90 grad
            if (a > 0) {
                result = new IntPoint(s.x, s.y + hyp);
            } else {
                result = new IntPoint(s.x, s.y - hyp);
            }
            return toRaster(result, raster);
        }
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
