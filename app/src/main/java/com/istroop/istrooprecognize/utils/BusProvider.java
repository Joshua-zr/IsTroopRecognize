package com.istroop.istrooprecognize.utils;

import com.squareup.otto.Bus;

/**
 * Created by joshua-zr on 8/20/15.
 */
public class BusProvider {
    private static final Bus bus = new Bus();

    private BusProvider() {}

    public static Bus getInstance() {
        return bus;
    }
}
