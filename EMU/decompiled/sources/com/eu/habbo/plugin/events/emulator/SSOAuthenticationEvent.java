package com.eu.habbo.plugin.events.emulator;

import com.eu.habbo.plugin.Event;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/emulator/SSOAuthenticationEvent.class */
public class SSOAuthenticationEvent extends Event {
    public final String sso;

    public SSOAuthenticationEvent(String str) {
        this.sso = str;
    }
}
