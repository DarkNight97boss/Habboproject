package com.eu.habbo.messages.outgoing.rooms.pets.breeding;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.PetBreedingReward;
import com.eu.habbo.habbohotel.pets.PetManager;
import com.eu.habbo.messages.ISerialize;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.commons.math3.distribution.NormalDistribution;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/pets/breeding/PetBreedingResultComposer.class */
public class PetBreedingResultComposer extends MessageComposer {
    private final int boxId;
    private final int petType;
    private final PetBreedingPet petOne;
    private final PetBreedingPet petTwo;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/pets/breeding/PetBreedingResultComposer$PetBreedingPet.class */
    public class PetBreedingPet implements ISerialize {
        public final Pet pet;
        public final String ownerName;

        public PetBreedingPet(Pet pet, String str) {
            this.pet = pet;
            this.ownerName = str;
        }

        @Override // com.eu.habbo.messages.ISerialize
        public void serialize(ServerMessage serverMessage) {
            serverMessage.appendInt(Integer.valueOf(this.pet.getId()));
            serverMessage.appendString(this.pet.getName());
            serverMessage.appendInt(Integer.valueOf(this.pet.getLevel()));
            serverMessage.appendString(this.pet.getColor());
            serverMessage.appendString(this.ownerName);
        }
    }

    public PetBreedingResultComposer(int i, int i2, Pet pet, String str, Pet pet2, String str2) {
        this.boxId = i;
        this.petType = i2;
        this.petOne = new PetBreedingPet(pet, str);
        this.petTwo = new PetBreedingPet(pet2, str2);
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.PetBreedingResultComposer);
        this.response.appendInt(Integer.valueOf(this.boxId));
        this.petOne.serialize(this.response);
        this.petTwo.serialize(this.response);
        NormalDistribution normalDistributionForBreeding = PetManager.getNormalDistributionForBreeding((this.petOne.pet.getLevel() + this.petTwo.pet.getLevel()) / 2.0f);
        TIntObjectHashMap<ArrayList<PetBreedingReward>> breedingRewards = Emulator.getGameEnvironment().getPetManager().getBreedingRewards(this.petType);
        this.response.appendInt((Integer) 4);
        int iCumulativeProbability = (int) (normalDistributionForBreeding.cumulativeProbability(10.0d) * 100.0d);
        int iCumulativeProbability2 = ((int) (normalDistributionForBreeding.cumulativeProbability(15.0d) * 100.0d)) - iCumulativeProbability;
        int iCumulativeProbability3 = (((int) (normalDistributionForBreeding.cumulativeProbability(18.0d) * 100.0d)) - iCumulativeProbability) - iCumulativeProbability2;
        int iCumulativeProbability4 = ((((int) (normalDistributionForBreeding.cumulativeProbability(20.0d) * 100.0d)) - iCumulativeProbability) - iCumulativeProbability2) - iCumulativeProbability3;
        int i = 100 - (((iCumulativeProbability + iCumulativeProbability2) + iCumulativeProbability3) + iCumulativeProbability4);
        if (i > 0) {
            iCumulativeProbability += i;
        } else {
            iCumulativeProbability4 -= i;
        }
        this.response.appendInt(Integer.valueOf(iCumulativeProbability4));
        this.response.appendInt(Integer.valueOf(((ArrayList) breedingRewards.get(4)).size()));
        Iterator it = ((ArrayList) breedingRewards.get(4)).iterator();
        while (it.hasNext()) {
            this.response.appendInt(Integer.valueOf(((PetBreedingReward) it.next()).breed));
        }
        this.response.appendInt(Integer.valueOf(iCumulativeProbability3));
        this.response.appendInt(Integer.valueOf(((ArrayList) breedingRewards.get(3)).size()));
        Iterator it2 = ((ArrayList) breedingRewards.get(3)).iterator();
        while (it2.hasNext()) {
            this.response.appendInt(Integer.valueOf(((PetBreedingReward) it2.next()).breed));
        }
        this.response.appendInt(Integer.valueOf(iCumulativeProbability2));
        this.response.appendInt(Integer.valueOf(((ArrayList) breedingRewards.get(2)).size()));
        Iterator it3 = ((ArrayList) breedingRewards.get(2)).iterator();
        while (it3.hasNext()) {
            this.response.appendInt(Integer.valueOf(((PetBreedingReward) it3.next()).breed));
        }
        this.response.appendInt(Integer.valueOf(iCumulativeProbability));
        this.response.appendInt(Integer.valueOf(((ArrayList) breedingRewards.get(1)).size()));
        Iterator it4 = ((ArrayList) breedingRewards.get(1)).iterator();
        while (it4.hasNext()) {
            this.response.appendInt(Integer.valueOf(((PetBreedingReward) it4.next()).breed));
        }
        this.response.appendInt(Integer.valueOf(this.petType));
        return this.response;
    }
}
