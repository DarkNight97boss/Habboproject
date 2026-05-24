package com.eu.habbo.messages.outgoing.catalog;

import com.eu.habbo.habbohotel.pets.PetRace;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/catalog/PetBreedsComposer.class */
public class PetBreedsComposer extends MessageComposer {
    private final String petName;
    private final THashSet<PetRace> petRaces;

    public PetBreedsComposer(String str, THashSet<PetRace> tHashSet) {
        this.petName = str;
        this.petRaces = tHashSet;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        if (this.petRaces == null) {
            return null;
        }
        this.response.init(Outgoing.PetBreedsComposer);
        this.response.appendString(this.petName);
        this.response.appendInt(Integer.valueOf(this.petRaces.size()));
        TObjectHashIterator it = this.petRaces.iterator();
        while (it.hasNext()) {
            PetRace petRace = (PetRace) it.next();
            this.response.appendInt(Integer.valueOf(petRace.race));
            this.response.appendInt(Integer.valueOf(petRace.colorOne));
            this.response.appendInt(Integer.valueOf(petRace.colorTwo));
            this.response.appendBoolean(Boolean.valueOf(petRace.hasColorOne));
            this.response.appendBoolean(Boolean.valueOf(petRace.hasColorTwo));
        }
        return this.response;
    }
}
