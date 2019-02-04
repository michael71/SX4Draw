/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.blankedv.sx4draw;

/**
 * @author mblank
 */
public class Constants {

    public static final String version = "0.34 - 03 Feb 2019";
    public static final int SXMAX_USED = 1068; // max sx address used (106, bit 8)
    public static final int LBMIN = 1200;  // minimum virtual address
    public static final int LBMAX = 9999;

    public static final int INVALID_INT = -1;
    public static final boolean DEBUG = true;

    public static final int RASTER = 20;
    public static final int RECT_X = 1400;
    public static final int RECT_Y = 860;

    public static final int SIGLEN = 13; // länge des Fusses eines Signals
    public static final int SIGLEN2 = 9;  // dx des fusses eines Signals diagonal
    public static final int TURNOUT_LENGTH = 10; // NOT to be prescaled
    public static final int TURNOUT_LENGTH_LONG = 14;
    public static final int TOUCH_RADIUS = 7;

    public static final Double TRACKWIDTH = 5.0;
    public static final Double SENSORWIDTH = 4.0;
}
