package com.eu.habbo.habbohotel.pets;

import com.eu.habbo.Emulator;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/pets/PetTasks.class */
public enum PetTasks {
    NONE(Emulator.PREVIEW),
    FREE(Emulator.PREVIEW),
    SIT("sit"),
    DOWN("lay"),
    HERE(Emulator.PREVIEW),
    BEG("beg"),
    PLAY_DEAD("ded"),
    STAY(Emulator.PREVIEW),
    FOLLOW(Emulator.PREVIEW),
    STAND("std"),
    JUMP("jmp"),
    SPEAK(PetData.SPEAK),
    PLAY(Emulator.PREVIEW),
    SILENT(Emulator.PREVIEW),
    NEST(Emulator.PREVIEW),
    DRINK(Emulator.PREVIEW),
    FOLLOW_LEFT(Emulator.PREVIEW),
    FOLLOW_RIGHT(Emulator.PREVIEW),
    PLAY_FOOTBALL(Emulator.PREVIEW),
    COME_HERE(Emulator.PREVIEW),
    BOUNCE(Emulator.PREVIEW),
    FLAT(Emulator.PREVIEW),
    DANCE(Emulator.PREVIEW),
    SPIN(Emulator.PREVIEW),
    SWITCH_TV(Emulator.PREVIEW),
    MOVE_FORWARD(Emulator.PREVIEW),
    TURN_LEFT(Emulator.PREVIEW),
    TURN_RIGHT(Emulator.PREVIEW),
    RELAX(Emulator.PREVIEW),
    CROAK(Emulator.PREVIEW),
    DIP(Emulator.PREVIEW),
    WAVE(Emulator.PREVIEW),
    MAMBO(Emulator.PREVIEW),
    HIGH_JUMP(Emulator.PREVIEW),
    CHICKEN_DANCE(Emulator.PREVIEW),
    TRIPLE_JUMP(Emulator.PREVIEW),
    SPREAD_WINGS(Emulator.PREVIEW),
    BREATHE_FIRE(Emulator.PREVIEW),
    HANG(Emulator.PREVIEW),
    TORCH(Emulator.PREVIEW),
    SWING(Emulator.PREVIEW),
    ROLL(Emulator.PREVIEW),
    RING_OF_FIRE(Emulator.PREVIEW),
    EAT(PetData.EAT),
    WAG_TAIL(Emulator.PREVIEW),
    COUNT(Emulator.PREVIEW),
    BREED(Emulator.PREVIEW),
    RIDE(Emulator.PREVIEW);

    private final String status;

    PetTasks(String str) {
        this.status = str;
    }

    public String getStatus() {
        return this.status;
    }
}
