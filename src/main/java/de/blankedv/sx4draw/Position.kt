/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.blankedv.sx4draw

import de.blankedv.sx4draw.Constants.INVALID_INT

/**
 * @author Michael Blank <mblank></mblank>@bawue.de>
 */
data class Position (
    var x: Int = 0, // starting point
    var y: Int = 0,
    var x2: Int = INVALID_INT, // endpoint - x2 always >x, closed pos. for turnout
    var y2: Int = INVALID_INT,
    var xt: Int = INVALID_INT, // "thrown" position for turnout
    var yt: Int = INVALID_INT
)
