/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.blankedv.sx4draw;

import static de.blankedv.sx4draw.Constants.INVALID_INT;

/**
 * @author Michael Blank <mblank@bawue.de>
 */
public class Position {
    public int x; // starting point
    public int y;
    public int x2 = INVALID_INT; // endpoint - x2 always >x
    public int y2 = INVALID_INT;
    public int xt = INVALID_INT; // "thrown" position for turnout
    public int yt = INVALID_INT;
}
