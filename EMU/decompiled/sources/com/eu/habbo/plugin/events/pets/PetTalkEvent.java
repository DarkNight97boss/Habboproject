package com.eu.habbo.plugin.events.pets;

import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.rooms.RoomChatMessage;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/pets/PetTalkEvent.class */
public class PetTalkEvent extends PetEvent {
    public RoomChatMessage message;

    public PetTalkEvent(Pet pet, RoomChatMessage roomChatMessage) {
        super(pet);
        this.message = roomChatMessage;
    }
}
