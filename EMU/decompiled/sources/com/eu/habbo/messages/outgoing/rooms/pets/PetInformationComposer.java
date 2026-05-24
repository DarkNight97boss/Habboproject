package com.eu.habbo.messages.outgoing.rooms.pets;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.pets.MonsterplantPet;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.PetManager;
import com.eu.habbo.habbohotel.pets.RideablePet;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/pets/PetInformationComposer.class */
public class PetInformationComposer extends MessageComposer {
    private final Pet pet;
    private final Room room;
    private final Habbo requestingHabbo;

    public PetInformationComposer(Pet pet, Room room, Habbo habbo) {
        this.pet = pet;
        this.room = room;
        this.requestingHabbo = habbo;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        double dFloor = Math.floor((Emulator.getIntUnixTimestamp() - this.pet.getCreated()) / 86400);
        this.response.init(Outgoing.PetInformationComposer);
        this.response.appendInt(Integer.valueOf(this.pet.getId()));
        this.response.appendString(this.pet.getName());
        if (this.pet instanceof MonsterplantPet) {
            this.response.appendInt(Integer.valueOf(((MonsterplantPet) this.pet).getGrowthStage()));
            this.response.appendInt((Integer) 7);
        } else {
            this.response.appendInt(Integer.valueOf(this.pet.getLevel()));
            this.response.appendInt((Integer) 20);
        }
        this.response.appendInt(Integer.valueOf(this.pet.getExperience()));
        if (this.pet.getLevel() < PetManager.experiences.length + 1) {
            this.response.appendInt(Integer.valueOf(PetManager.experiences[this.pet.getLevel() - 1]));
        } else {
            this.response.appendInt(Integer.valueOf(this.pet.getExperience()));
        }
        this.response.appendInt(Integer.valueOf(this.pet.getEnergy()));
        this.response.appendInt(Integer.valueOf(this.pet.getMaxEnergy()));
        this.response.appendInt(Integer.valueOf(this.pet.getHappyness()));
        this.response.appendInt((Integer) 100);
        this.response.appendInt(Integer.valueOf(this.pet.getRespect()));
        this.response.appendInt(Integer.valueOf(this.pet.getUserId()));
        this.response.appendInt(Integer.valueOf(((int) dFloor) + 1));
        this.response.appendString(this.room.getFurniOwnerName(this.pet.getUserId()));
        this.response.appendInt(Integer.valueOf(this.pet instanceof MonsterplantPet ? ((MonsterplantPet) this.pet).getRarity() : 0));
        this.response.appendBoolean(Boolean.valueOf((this.pet instanceof RideablePet) && this.requestingHabbo != null && (((RideablePet) this.pet).getRider() == null || this.pet.getUserId() == this.requestingHabbo.getHabboInfo().getId()) && ((RideablePet) this.pet).hasSaddle()));
        this.response.appendBoolean(Boolean.valueOf((this.pet instanceof RideablePet) && ((RideablePet) this.pet).getRider() != null && this.requestingHabbo != null && ((RideablePet) this.pet).getRider().getHabboInfo().getId() == this.requestingHabbo.getHabboInfo().getId()));
        this.response.appendInt((Integer) 0);
        this.response.appendInt(Integer.valueOf(((this.pet instanceof RideablePet) && ((RideablePet) this.pet).anyoneCanRide()) ? 1 : 0));
        this.response.appendBoolean(Boolean.valueOf((this.pet instanceof MonsterplantPet) && ((MonsterplantPet) this.pet).canBreed()));
        this.response.appendBoolean(Boolean.valueOf(((this.pet instanceof MonsterplantPet) && ((MonsterplantPet) this.pet).isFullyGrown()) ? false : true));
        this.response.appendBoolean(Boolean.valueOf((this.pet instanceof MonsterplantPet) && ((MonsterplantPet) this.pet).isDead()));
        this.response.appendInt(Integer.valueOf(this.pet instanceof MonsterplantPet ? ((MonsterplantPet) this.pet).getRarity() : 0));
        this.response.appendInt(Integer.valueOf(MonsterplantPet.timeToLive));
        this.response.appendInt(Integer.valueOf(this.pet instanceof MonsterplantPet ? ((MonsterplantPet) this.pet).remainingTimeToLive() : 0));
        this.response.appendInt(Integer.valueOf(this.pet instanceof MonsterplantPet ? ((MonsterplantPet) this.pet).remainingGrowTime() : 0));
        this.response.appendBoolean(Boolean.valueOf((this.pet instanceof MonsterplantPet) && ((MonsterplantPet) this.pet).isPubliclyBreedable()));
        return this.response;
    }
}
