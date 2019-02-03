/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.blankedv.sx4draw;

import static de.blankedv.sx4draw.Constants.LBMIN;

/**
 * @author mblank
 */
public class GenericAddress {
    public int addr = 0;
    public int inv = 0;
    public int orient = 0;  // orientation of signals (0 = 0 grad, 1 = 90 etc)

    GenericAddress() {
    }


    GenericAddress(int a, int inverted, int o) {
        addr = a;
        inv = inverted;
        orient = o;
    }

    public String toLongString() {
        if (addr < LBMIN) {
            return "SX-Adr " + (addr / 10) + "." + (addr % 10);
        } else {
            return "V-Adr " + addr;
        }
    }

    public String toString() {
        if (addr < LBMIN) {
            return (addr / 10) + "." + (addr % 10);
        } else {
            return "" + addr;
        }
    }
}
