package com.eu.habbo.plugin.events.support;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/support/SupportUserAlertedReason.class */
public enum SupportUserAlertedReason {
    ALERT(0),
    CAUTION(1),
    KICKED(2),
    AMBASSADOR(3);

    private final int code;

    SupportUserAlertedReason(int i) {
        this.code = i;
    }

    public int getCode() {
        return this.code;
    }
}
