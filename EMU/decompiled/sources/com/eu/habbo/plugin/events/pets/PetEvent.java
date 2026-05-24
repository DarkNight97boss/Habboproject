package com.eu.habbo.plugin.events.pets;

import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.plugin.Event;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/pets/PetEvent.class */
public abstract class PetEvent extends Event {
    public final Pet pet;

    public PetEvent(Pet pet) {
        this.pet = pet;
    }
}
