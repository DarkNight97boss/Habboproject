package com.eu.habbo.habbohotel.modtool;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/modtool/ModToolTicketState.class */
public enum ModToolTicketState {
    CLOSED(0),
    OPEN(1),
    PICKED(2);

    private final int state;

    ModToolTicketState(int i) {
        this.state = i;
    }

    public static ModToolTicketState getState(int i) {
        for (ModToolTicketState modToolTicketState : values()) {
            if (modToolTicketState.state == i) {
                return modToolTicketState;
            }
        }
        return CLOSED;
    }

    public int getState() {
        return this.state;
    }
}
