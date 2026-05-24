package com.eu.habbo.habbohotel.items;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/RandomStateParams.class */
public class RandomStateParams {
    private static final Logger LOGGER = LoggerFactory.getLogger(RandomStateParams.class);
    private int states = -1;
    private int delay = -1;

    public RandomStateParams(String str) throws Exception {
        Arrays.stream(str.split(",")).forEach(str2 -> {
            String[] strArrSplit = str2.split("=");
            if (strArrSplit.length != 2) {
            }
            switch (strArrSplit[0]) {
                case "states":
                    this.states = Integer.parseInt(strArrSplit[1]);
                    break;
                case "delay":
                    this.delay = Integer.parseInt(strArrSplit[1]);
                    break;
                default:
                    LOGGER.warn("RandomStateParams: unknown key: " + strArrSplit[0]);
                    break;
            }
        });
        if (this.states < 0) {
            throw new Exception("RandomStateParams: states not defined");
        }
        if (this.delay < 0) {
            throw new Exception("RandomStateParams: states not defined");
        }
    }

    public int getStates() {
        return this.states;
    }

    public int getDelay() {
        return this.delay;
    }
}
