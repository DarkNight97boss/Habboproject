package com.eu.habbo.habbohotel.rooms;

import com.eu.habbo.habbohotel.pets.PetData;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/rooms/RoomUnitStatus.class */
public enum RoomUnitStatus {
    MOVE("mv", true),
    SIT_IN("sit-in"),
    SIT("sit", true),
    SIT_OUT("sit-out"),
    LAY_IN("lay-in"),
    LAY("lay", true),
    LAY_OUT("lay-out"),
    FLAT_CONTROL("flatctrl"),
    SIGN("sign"),
    GESTURE("gst"),
    WAVE("wav"),
    TRADING("trd"),
    DIP("dip"),
    EAT_IN("eat-in"),
    EAT(PetData.EAT),
    EAT_OUT("eat-out"),
    BEG("beg", true),
    DEAD_IN("ded-in"),
    DEAD("ded", true),
    DEAD_OUT("ded-out"),
    JUMP_IN("jmp-in"),
    JUMP("jmp", true),
    JUMP_OUT("jmp-out"),
    PLAY_IN("pla-in"),
    PLAY(PetData.PLAYFUL, true),
    PLAY_OUT("pla-out"),
    SPEAK(PetData.SPEAK),
    CROAK("crk"),
    RELAX("rlx"),
    WINGS("wng", true),
    FLAME("flm"),
    RIP("rip"),
    GROW("grw"),
    GROW_1("grw1"),
    GROW_2("grw2"),
    GROW_3("grw3"),
    GROW_4("grw4"),
    GROW_5("grw5"),
    GROW_6("grw6"),
    GROW_7("grw7"),
    KICK("kck"),
    WAG_TAIL("wag"),
    DANCE("dan"),
    AMS("ams"),
    SWIM("swm"),
    TURN("trn"),
    SRP("srp"),
    SRP_IN("srp-in"),
    SLEEP_IN("slp-in"),
    SLEEP("slp", true),
    SLEEP_OUT("slp-out");

    public final String key;
    public final boolean removeWhenWalking;

    RoomUnitStatus(String str) {
        this.key = str;
        this.removeWhenWalking = false;
    }

    RoomUnitStatus(String str, boolean z) {
        this.key = str;
        this.removeWhenWalking = z;
    }

    public static RoomUnitStatus fromString(String str) {
        for (RoomUnitStatus roomUnitStatus : values()) {
            if (roomUnitStatus.key.equalsIgnoreCase(str)) {
                return roomUnitStatus;
            }
        }
        return null;
    }

    @Override // java.lang.Enum
    public String toString() {
        return this.key;
    }
}
