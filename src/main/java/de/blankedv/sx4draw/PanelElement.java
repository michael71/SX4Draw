package de.blankedv.sx4draw;

import de.blankedv.sx4draw.SX4Draw.PEType;

import static de.blankedv.sx4draw.Constants.*;
import static de.blankedv.sx4draw.SX4Draw.panelElements;
import static de.blankedv.sx4draw.ReadConfig.YOFF;

import java.util.ArrayList;
import java.util.Comparator;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Pair;

/**
 * generic panel element can be any of PEType types
 *
 * @author mblank
 */
public class PanelElement implements Comparator<PanelElement>, Comparable<PanelElement> {

    private static boolean scaledPlus = false;

    protected String name = "";
    protected PEType type = PEType.TRACK;
    protected int x; // starting point
    protected int y;
    protected int x2 = INVALID_INT; // endpoint - x2 always >x
    protected int y2 = INVALID_INT;
    protected int xt = INVALID_INT; // "thrown" position for turnout
    protected int yt = INVALID_INT;
    protected String route = "";
    //protected int orient = 0;
    protected int inv = 0;  // 0 == not inverted
    protected int adr = INVALID_INT;
    protected int adr2 = INVALID_INT;
    protected Shape shape;     // default shape
    private PEState state = PEState.DEFAULT;

    protected Color defaultColor = Color.BLACK;


    public enum PEState {
        DEFAULT, MARKED, SELECTED, STATE_0, STATE_1
    }


    public PanelElement() {
    }


    public PanelElement(PanelElement pe) {  // copy
        name = pe.name;
        type = pe.type;
        x = pe.x;
        x2 = pe.x2;
        y = pe.y;
        y2 = pe.y2;
        xt = pe.xt;
        yt = pe.yt;
        route = pe.route;
        inv = pe.inv;
        adr = pe.adr;
        adr2 = pe.adr2;
        shape = pe.shape;
        defaultColor = pe.defaultColor;
        state = pe.state;
        createShapeAndSetState(state);

    }


    public PanelElement(PEType type, Line l) {
        this.type = type;
        this.x = (int) l.getStartX();
        this.x2 = (int) l.getEndX();
        this.y = (int) l.getStartY();
        this.y2 = (int) l.getEndY();

        orderXY();
        createShapeAndSetState(PEState.DEFAULT);
        autoAddress();
    }

    private void orderXY() {
        if (x == x2) {
            if (y2 < y) {
                int temp = y2;
                y2 = y;
                y = temp;
            }
        } else if (x2 > x) {
            // everything is fine ....
        } else {
            int temp = x2;
            x2 = x;
            x = temp;
            temp = y2;
            y2 = y;
            y = temp;

        }
    }


    public PanelElement(PEType type, IntPoint poi, IntPoint closed, IntPoint thrown) {
        this.type = type;
        this.x = poi.x;
        this.y = poi.y;
        this.x2 = closed.x;
        this.y2 = closed.y;
        this.xt = thrown.x;
        this.yt = thrown.y;
        // NO ORDERING HERE
        name = "";
        createShapeAndSetState(PEState.DEFAULT);
        autoAddress();
    }

    public PanelElement(PEType type, Position pos) {
        this.type = type;
        this.x = pos.x;
        this.y = pos.y;
        this.x2 = pos.x2;
        this.y2 = pos.y2;
        this.xt = pos.xt;
        this.yt = pos.yt;
        name = "";
        createShapeAndSetState(PEState.DEFAULT);
        autoAddress();
    }

    public PanelElement(PEType type, IntPoint poi) {
        this.type = type;
        this.x = poi.x;
        this.y = poi.y;
        if (type == PEType.SIGNAL) {
            IntPoint d = Utils.signalOrientToDXY2(0); // 0 (= 0 grad) is default orientation for signal
            x2 = x + d.x;
            y2 = y + d.y;
        }
        name = "";
        createShapeAndSetState(PEState.DEFAULT);
        autoAddress();
    }

    public PEType getType() {
        return type;
    }

    public void setType(PEType type) {
        this.type = type;
    }

    public int getInv() {
        return inv;
    }

    public void setInv(int inv) {
        this.inv = inv;
    }

    private void autoAddress() {
        if (type == PEType.ROUTEBUTTON) {
            // automatically assign route btn address
            int a = 1200;  // minumum for route buttons
            for (PanelElement pe : panelElements) {
                if (pe.type == PEType.ROUTEBUTTON) {
                    if (pe.adr >= a) {
                        a = pe.adr + 1;
                    }
                }
            }
            adr = a;
        } else if ((type == PEType.SIGNAL) || (type == PEType.SENSOR)) {
            // assign a dummy address to have it stored in xml file (if not assigned by hand)
            // this is needed for 
            if (adr == INVALID_INT) {
                adr = 1;
            }
        }
    }

    private void createShape() {
        switch (type) {
            case TURNOUT:
                defaultColor = Color.ORANGE;
                switch (state) {
                    default:
                        Line l1 = new Line(x, y, x2, y2);
                        l1.setStrokeLineCap(StrokeLineCap.ROUND);
                        l1.setStrokeWidth(TRACKWIDTH);
                        Line l2 = new Line(x, y, xt, yt);
                        l2.setStrokeLineCap(StrokeLineCap.ROUND);
                        l2.setStrokeWidth(TRACKWIDTH);
                        shape = Shape.union(l1, l2);
                        shape.setFill(defaultColor);
                        shape.setStroke(defaultColor);
                        break;
                    case STATE_0:
                        if (inv == 0) {   // not inverted
                            shape = new Line(x, y, x2, y2);
                            shape.setStrokeLineCap(StrokeLineCap.ROUND);
                            shape.setStrokeWidth(TRACKWIDTH);
                        } else {
                            shape = new Line(x, y, xt, yt);
                            shape.setStrokeLineCap(StrokeLineCap.ROUND);
                            shape.setStrokeWidth(TRACKWIDTH);
                        }
                        shape.setFill(Color.GREEN);
                        shape.setStroke(Color.GREEN);
                        break;
                    case STATE_1:
                        if (inv == 0) {   // not inverted
                            shape = new Line(x, y, xt, yt);
                            shape.setStrokeLineCap(StrokeLineCap.ROUND);
                            shape.setStrokeWidth(TRACKWIDTH);
                        } else {
                            shape = new Line(x, y, x2, y2);
                            shape.setStrokeLineCap(StrokeLineCap.ROUND);
                            shape.setStrokeWidth(TRACKWIDTH);
                        }
                        shape.setFill(Color.RED);
                        shape.setStroke(Color.RED);
                        break;
                }
                break;
            case TRACK:
                defaultColor = Color.BLACK;
                shape = new Line(x, y, x2, y2);
                shape.setStrokeWidth(TRACKWIDTH);
                shape.setStrokeLineCap(StrokeLineCap.ROUND);
                break;
            case ROUTEBUTTON:
                shape = new Circle(x, y, 9.5, Color.DARKGREY);
                defaultColor = Color.DARKGREY;

                break;
            case SENSOR:
                if (x2 != INVALID_INT) {  //DE type of sensor
                    shape = new Line(x, y, x2, y2);
                    shape.setStrokeWidth(SENSORWIDTH);
                    shape.getStrokeDashArray().addAll(15d, 10d);
                    shape.setStrokeLineCap(StrokeLineCap.ROUND);
                    defaultColor = Color.YELLOW;

                } else { //US Type
                    shape = new Circle(x, y, 8.0, Color.ORANGE);
                    defaultColor = Color.ORANGE;
                }
                break;
            case SIGNAL:
                defaultColor = Color.BLACK;
                Line ls = new Line(x, y, x2, y2);
                Circle c = new Circle(x, y, 5.0);
                ls.setStrokeWidth(1.5f);
                ls.setStrokeLineCap(StrokeLineCap.ROUND);
                shape = Shape.union(ls, c);
                break;
        }

    }

    public void recreateShape() {
        // create shape but don't change state
        createShapeAndSetState(state);
    }


    public void createShapeAndSetState(PEState st) {
        state = st;
        createShape();
        switch (state) {
            case DEFAULT:
                shape.setFill(defaultColor);
                shape.setStroke(defaultColor);
                break;
            case SELECTED:
                shape.setFill(Color.RED);
                shape.setStroke(Color.RED);
                break;
            case MARKED:
                shape.setFill(Color.AQUA);
                shape.setStroke(Color.AQUA);
                break;
            case STATE_0:
                shape.setFill(Color.RED);
                shape.setStroke(Color.RED);
                break;
            case STATE_1:
                shape.setFill(Color.GREEN);
                shape.setStroke(Color.GREEN);
                break;
            default:
                shape.setFill(defaultColor);
                shape.setStroke(defaultColor);
                break;
        }

    }


    public Pair<Boolean, Integer> isTouched(IntPoint touch) {
        int ymin, ymax;
        switch (type) {
            default:
                if (x2 != INVALID_INT) {
                    ymin = Math.min(y, y2);
                    ymax = Math.max(y, y2);
                    if ((touch.x >= (x - TOUCH_RADIUS))
                            && (touch.x <= (x2 + TOUCH_RADIUS))
                            && (touch.y >= (ymin - TOUCH_RADIUS))
                            && (touch.y <= (ymax + TOUCH_RADIUS))) {
                        if (Utils.calcDistanceFromLine(new IntPoint(x, y), new IntPoint(x2, y2), touch) < TOUCH_RADIUS) {
                            return new Pair(true, 0);
                        } else {
                            return new Pair(false, 0);
                        }
                    } else {
                        return new Pair(false, 0);
                    }
                } else {
                    // US Sensor
                    if ((touch.x >= (x - TOUCH_RADIUS))
                            && (touch.x <= (x + TOUCH_RADIUS))
                            && (touch.y >= (y - TOUCH_RADIUS))
                            && (touch.y <= (y + TOUCH_RADIUS))) {
                        return new Pair(true, 0);
                    } else {
                        return new Pair(false, 0);
                    }
                }
            case SIGNAL:
            case ROUTEBUTTON:
                double dist = Math.sqrt((touch.x - x) * (touch.x - x) + (touch.y - y) * (touch.y - y));
                boolean result = (dist < TOUCH_RADIUS * 2);
                return new Pair(result, 0);
            case TURNOUT:
                // check first for (x2,y2) touch (state 0)
                if ((touch.x >= (x2 - TOUCH_RADIUS))
                        && (touch.x <= (x2 + TOUCH_RADIUS))
                        && (touch.y >= (y2 - TOUCH_RADIUS))
                        && (touch.y <= (y2 + TOUCH_RADIUS))) {
                    return new Pair(true, 0);

                } else if ((touch.x >= (xt - TOUCH_RADIUS)) // thrown, state1
                        && (touch.x <= (xt + TOUCH_RADIUS))
                        && (touch.y >= (yt - TOUCH_RADIUS))
                        && (touch.y <= (yt + TOUCH_RADIUS))) {
                    return new Pair(true, 1);  // thrown state
                } else if ((touch.x >= (x - TOUCH_RADIUS)) // next center
                        && (touch.x <= (x + TOUCH_RADIUS))
                        && (touch.y >= (y - TOUCH_RADIUS))
                        && (touch.y <= (y + TOUCH_RADIUS))) {
                    return new Pair(true, 0);
                } else {
                    return new Pair(false, 0);
                }
        }
    }

    public int getAdr() {
        return adr;
    }

    public void setAdr(int a) {
        adr = a;
    }

    public boolean hasAdrX(int address) {
        return false;
    }

    public void toggleShapeSelected() {
        if (state != PEState.SELECTED) {
            createShapeAndSetState(PEState.SELECTED);
        } else {
            createShapeAndSetState(PEState.DEFAULT);   // "marked" will be reset also !!
            // color will be reset to default value.
        }
    }

    public PEState getState() {
        return state;
    }

    public boolean isExpired() {
        return false;  // a non-active element is never expired
    }

    public void drawAddress(GraphicsContext gc) {
        if (adr == INVALID_INT) {
            return;  // don't draw invalid int
        }
        String sAddr;
        if (adr < LBMIN) {
            gc.setFill(Color.LIGHTBLUE);
            sAddr = (adr / 10) + "." + (adr % 10);
        } else {
            gc.setFill(Color.LIGHTSALMON);
            sAddr = "" + adr;
        }

        gc.fillRect(x, y - YOFF, 8 * sAddr.length(), 12);
        gc.strokeText(sAddr, x, y - YOFF + 10);
    }

    /**
     * search for a panel element(or elements) when only the address is known
     *
     * @param address
     * @return
     */
    public static ArrayList<PanelElement> getPeByAddress(int address) {
        ArrayList<PanelElement> pelist = new ArrayList<>();
        for (PanelElement pe : panelElements) {
            if (pe.getAdr() == address) {
                pelist.add(pe);
            }
        }
        return pelist;
    }

    public static void markRoute(int id, PEState st) {
        for (PanelElement pe : panelElements) {
            pe.createShapeAndSetState(PEState.DEFAULT);
        }
    }

    public static void resetState() {
        for (PanelElement pe : panelElements) {
            pe.createShapeAndSetState(PEState.DEFAULT);
        }
    }

    /**
     * check if at least 60 % of (signal, turnout, sensor) addresses have been
     * entereded and at least 1 PE of this type
     *
     * @param
     * @return
     */
    public static boolean addressCheck() {
        int adrOK = 0, adrNOK = 0;
        double percentage = 0;
        for (PanelElement pe : panelElements) {
            if ((pe.type != PEType.TRACK) && (pe.type != PEType.ROUTEBUTTON)) {
                if ((pe.getAdr() != INVALID_INT)
                        && (pe.getAdr() != 0)
                        && (pe.getAdr() != 1)) {
                    adrOK++;
                } else {
                    adrNOK++;
                }
            }
        }
        if (adrOK >= 1) {
            percentage = 100.0 * adrOK / (adrOK + adrNOK);
        }
        return (percentage >= 60.0);
    }

    /**
     * scale all panel elements for better fit on display and for possible
     * "upside down" display (=view from other side of the layout) currently
     * only called from readXMLConfigFile (i.e. NOT when flipUpsideDown is
     * changed in the prefs)
     */
    public static void scaleAll() {

        // in WriteConfig the NEW values are written !!
        int xmin = INVALID_INT;
        int xmax = INVALID_INT;
        int ymin = INVALID_INT;
        int ymax = INVALID_INT;
        boolean first = true;
        for (PanelElement pe : panelElements) {
            if (first) {
                xmin = xmax = pe.x;
                ymin = ymax = pe.y;
                first = false;
            }

            if ((pe.x != INVALID_INT) && (pe.x < xmin)) {
                xmin = pe.x;
            }
            if ((pe.x != INVALID_INT) && (pe.x > xmax)) {
                xmax = pe.x;
            }
            if ((pe.x2 != INVALID_INT) && (pe.x2 < xmin)) {
                xmin = pe.x2;
            }
            if ((pe.x2 != INVALID_INT) && (pe.x2 > xmax)) {
                xmax = pe.x2;
            }
            if ((pe.xt != INVALID_INT) && (pe.xt < xmin)) {
                xmin = pe.xt;
            }
            if ((pe.xt != INVALID_INT) && (pe.xt > xmax)) {
                xmax = pe.xt;
            }

            if ((pe.y != INVALID_INT) && (pe.y < ymin)) {
                ymin = pe.y;
            }
            if ((pe.y != INVALID_INT) && (pe.y > ymax)) {
                ymax = pe.y;
            }
            if ((pe.y2 != INVALID_INT) && (pe.y2 < ymin)) {
                ymin = pe.y2;
            }
            if ((pe.y2 != INVALID_INT) && (pe.y2 > ymax)) {
                ymax = pe.y2;
            }
            if ((pe.yt != INVALID_INT) && (pe.yt < ymin)) {
                ymin = pe.yt;
            }
            if ((pe.yt != INVALID_INT) && (pe.yt > ymax)) {
                ymax = pe.yt;
            }

        }

        boolean flipUpsideDown = false;
        // now move origin to (20,20+YOFF)
        for (PanelElement pe : panelElements) {
            if (!flipUpsideDown) {
                if (pe.x != INVALID_INT) {
                    pe.x = 20 + (pe.x - xmin);
                }
                if (pe.x2 != INVALID_INT) {
                    pe.x2 = 20 + (pe.x2 - xmin);
                }
                if (pe.xt != INVALID_INT) {
                    pe.xt = 20 + (pe.xt - xmin);
                }
                if (pe.y != INVALID_INT) {
                    pe.y = YOFF + 20 + (pe.y - ymin);
                }
                if (pe.y2 != INVALID_INT) {
                    pe.y2 = YOFF + 20 + (pe.y2 - ymin);
                }
                if (pe.yt != INVALID_INT) {
                    pe.yt = YOFF + 20 + (pe.yt - ymin);
                }
            } else {
                if (pe.x != INVALID_INT) {
                    pe.x = 20 + (xmax - pe.x);
                }
                if (pe.x2 != INVALID_INT) {
                    pe.x2 = 20 + (xmax - pe.x2);
                }
                if (pe.xt != INVALID_INT) {
                    pe.xt = 20 + (xmax - pe.xt);
                }
                if (pe.y != INVALID_INT) {
                    pe.y = YOFF + 20 + (ymax - pe.y);
                }
                if (pe.y2 != INVALID_INT) {
                    pe.y2 = YOFF + 20 + (ymax - pe.y2);
                }
                if (pe.yt != INVALID_INT) {
                    pe.yt = YOFF + 20 + (ymax - pe.yt);
                }
            }
            pe.createShape();

        }

        if (DEBUG) {
            System.out.println(" xmin=" + xmin + " xmax=" + xmax + " ymin=" + ymin
                    + " ymax=" + ymax);
        }

        //configHasChanged = true;   ==> will not saved in xml file
    }

    public static void translate(IntPoint d) {
        for (PanelElement pe : panelElements) {
            if (pe.state == PEState.SELECTED) {
                pe.x += d.x;
                pe.y += d.y;
                if (pe.x2 != INVALID_INT) {
                    pe.x2 += d.x;
                }
                if (pe.xt != INVALID_INT) {
                    pe.xt += d.x;
                }
                if (pe.y2 != INVALID_INT) {
                    pe.y2 += d.y;
                }
                if (pe.yt != INVALID_INT) {
                    pe.yt += d.y;
                }
            }
            pe.createShape();   // state will be reset to DEFAULT also
        }
    }

    public static boolean atLeastOneSelected() {
        for (PanelElement pe : panelElements) {
            if (pe.state == PEState.SELECTED) {
                return true;
            }
        }
        return false;
    }

    public static void scalePlus() {
        if (scaledPlus) {
            return;  // only once allowed !!!
        }
        for (PanelElement pe : panelElements) {
            int dx = pe.x;
            int dy = pe.y;
            pe.x = 2 * pe.x;
            pe.y = 2 * pe.y;
            switch (pe.type) {
                case TRACK:
                case SENSOR:
                    if (pe.x2 != INVALID_INT) {
                        pe.x2 = 2 * pe.x2;
                    }
                    if (pe.y2 != INVALID_INT) {
                        pe.y2 = 2 * pe.y2;
                    }
                    break;
                case TURNOUT:
                case SIGNAL:
                    // do not scale x2/y2 BUT TRANSLATE
                    if (pe.x2 != INVALID_INT) {
                        pe.x2 += dx;
                    }
                    if (pe.y2 != INVALID_INT) {
                        pe.y2 += dy;
                    }
                    if (pe.xt != INVALID_INT) {
                        pe.xt += dx;
                    }
                    if (pe.yt != INVALID_INT) {
                        pe.yt += dy;
                    }
                    break;
            }
            pe.createShape();  // must be redone
        }

        scaledPlus = true;
    }

    @Override
    public int compare(PanelElement o1, PanelElement o2) {
        if (o1.type.ordinal() == o2.type.ordinal()) {
            return o1.x - o2.x;
        } else {
            return (o1.type.ordinal() - o2.type.ordinal());
        }
    }

    @Override
    public int compareTo(PanelElement o) {
        if (type.ordinal() == o.type.ordinal()) {
            return x - o.x;
        } else {
            return (type.ordinal() - o.type.ordinal());
        }
    }
}
