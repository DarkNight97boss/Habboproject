package com.eu.habbo.habbohotel.wired;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/wired/WiredTriggerType.class */
public enum WiredTriggerType {
    SAY_SOMETHING(0),
    WALKS_ON_FURNI(1),
    WALKS_OFF_FURNI(2),
    AT_GIVEN_TIME(3),
    STATE_CHANGED(4),
    PERIODICALLY(6),
    ENTER_ROOM(7),
    GAME_STARTS(8),
    GAME_ENDS(9),
    SCORE_ACHIEVED(10),
    COLLISION(11),
    PERIODICALLY_LONG(12),
    BOT_REACHED_STF(13),
    BOT_REACHED_AVTR(14),
    SAY_COMMAND(0),
    IDLES(11),
    UNIDLES(11),
    CUSTOM(13),
    STARTS_DANCING(11),
    STOPS_DANCING(11);

    public final int code;

    WiredTriggerType(int i) {
        this.code = i;
    }
}
