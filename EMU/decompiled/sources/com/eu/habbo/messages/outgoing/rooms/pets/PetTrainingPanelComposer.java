package com.eu.habbo.messages.outgoing.rooms.pets;

import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.PetCommand;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/pets/PetTrainingPanelComposer.class */
public class PetTrainingPanelComposer extends MessageComposer {
    private final Pet pet;

    public PetTrainingPanelComposer(Pet pet) {
        this.pet = pet;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        ArrayList arrayList = new ArrayList();
        Collections.sort(this.pet.getPetData().getPetCommands());
        this.response.init(Outgoing.PetTrainingPanelComposer);
        this.response.appendInt(Integer.valueOf(this.pet.getId()));
        this.response.appendInt(Integer.valueOf(this.pet.getPetData().getPetCommands().size()));
        for (PetCommand petCommand : this.pet.getPetData().getPetCommands()) {
            this.response.appendInt(Integer.valueOf(petCommand.id));
            if (this.pet.getLevel() >= petCommand.level) {
                arrayList.add(petCommand);
            }
        }
        if (!arrayList.isEmpty()) {
            Collections.sort(arrayList);
        }
        this.response.appendInt(Integer.valueOf(arrayList.size()));
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            this.response.appendInt(Integer.valueOf(((PetCommand) it.next()).id));
        }
        return this.response;
    }
}
