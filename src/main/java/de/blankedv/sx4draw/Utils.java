/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.blankedv.sx4draw;

import static de.blankedv.sx4draw.Constants.*;

import java.util.concurrent.atomic.AtomicLong;

import javafx.scene.control.TableView;
import javafx.util.Pair;

/**
 * @author mblank
 */
public class Utils {

    // see https://en.wikipedia.org/wiki/Distance_from_a_point_to_a_line
    static double calcDistanceFromLine(IntPoint l1, IntPoint l2, IntPoint p) {
        int d1 = Math.abs((l2.y - l1.y) * p.x - (l2.x - l1.x) * p.y
                + l2.x * l1.y - l2.y * l1.x);
        double d2 = Math.sqrt((l2.y - l1.y) * (l2.y - l1.y) + (l2.x - l1.x) * (l2.x - l1.x));

        return (double) d1 / d2;
    }

    static double calcDistance(IntPoint p, IntPoint q) {

        double d = Math.sqrt((double) ((p.y - q.y) * (p.y - q.y) + (p.x - q.x) * (p.x - q.x)));

        return (double) d;
    }

    /**
     * calculate minimum of 3 integers, the first one is always a valid number,
     * the other can if INVALID_INT (=>not taken into account) or valid
     * integers, then they are evaluated
     */
    public static int min(int x, int xt, int x2) {
        int m = x;  // is always defined.
        if (x == INVALID_INT) {
            System.out.println("  Utils.min: x is undefined.");
        }
        if ((xt != INVALID_INT) && (xt < m)) {
            m = xt;
        }
        if ((x2 != INVALID_INT) && (x2 < m)) {
            m = x2;
        }
        return m;
    }

    /**
     * calculate maximum of 3 integers, the first one is always a valid number,
     * the other can if INVALID_INT (=>not taken into account) or valid
     * integers, then they are evaluated
     */
    public static int max(int x, int xt, int x2) {
        int m = x;
        if (x == INVALID_INT) {
            System.out.println("Utils.min: x is undefined.");
        }
        if ((xt != INVALID_INT) && (xt > m)) {
            m = xt;
        }
        if ((x2 != INVALID_INT) && (x2 > m)) {
            m = x2;
        }
        return m;
    }

    public static void customResize(TableView<?> view) {

        AtomicLong width = new AtomicLong();
        view.getColumns().forEach(col -> {
            width.addAndGet((long) col.getWidth() + 10);
        });
        double tableWidth = view.getWidth();

        if (tableWidth > width.get()) {
            view.getColumns().forEach(col -> {
                col.setPrefWidth(col.getWidth() + ((tableWidth - width.get()) / view.getColumns().size()));
            });
        }
    }

    public static Pair<Integer, Integer> swap(int a, int b) {
        return new Pair(b, a);
    }

    public static IntPoint signalOrientToDXY2(int orient) {
        int dx = 0, dy = 0;

        // evaluate orientation
        switch (orient) {
            case 0:   // 0 grad
                dx = -SIGLEN;
                dy = 0;
                break;
            case 1:   // 45
                dx = -SIGLEN2;
                dy = +SIGLEN2;
                break;
            case 2:   // 90
                dx = 0;
                dy = +SIGLEN;
                break;
            case 3:   // 135
                dx = +SIGLEN2;
                dy = +SIGLEN2;
                break;
            case 4:   // 180
                dx = +SIGLEN;
                dy = 0;
                break;
            case 5:   // 225
                dx = +SIGLEN2;
                dy = -SIGLEN2;
                break;
            case 6:   // 270
                dx = 0;
                dy = -SIGLEN;
                break;
            case 7:   // 315
                dx = -SIGLEN2;
                dy = -SIGLEN2;
                break;
        }
        return new IntPoint(dx, dy);
    }

    public static int signalDX2ToOrient(IntPoint delta) {
        for (int orient = 0; orient < 8; orient++) {
            IntPoint d1 = signalOrientToDXY2(orient);
            if ((d1.x == delta.x) && (d1.y == delta.y)) return orient;
        }
        return INVALID_INT;
    }
}
