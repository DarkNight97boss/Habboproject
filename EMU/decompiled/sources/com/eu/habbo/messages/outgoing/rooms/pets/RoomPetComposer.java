package com.eu.habbo.messages.outgoing.rooms.pets;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.pets.HorsePet;
import com.eu.habbo.habbohotel.pets.IPetLook;
import com.eu.habbo.habbohotel.pets.MonsterplantPet;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.RideablePet;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/pets/RoomPetComposer.class */
public class RoomPetComposer extends MessageComposer implements TIntObjectProcedure<Pet> {
    private final TIntObjectMap<Pet> pets;

    public RoomPetComposer(Pet pet) {
        this.pets = new TIntObjectHashMap();
        this.pets.put(pet.getId(), pet);
    }

    public RoomPetComposer(TIntObjectMap<Pet> tIntObjectMap) {
        this.pets = tIntObjectMap;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomUsersComposer);
        this.response.appendInt(Integer.valueOf(this.pets.size()));
        this.pets.forEachEntry(this);
        return this.response;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public boolean execute(int i, Pet pet) {
        String str;
        this.response.appendInt(Integer.valueOf(pet.getId()));
        this.response.appendString(pet.getName());
        this.response.appendString(Emulator.PREVIEW);
        if (pet instanceof IPetLook) {
            this.response.appendString(((IPetLook) pet).getLook());
        } else {
            ServerMessage serverMessage = this.response;
            StringBuilder sbAppend = new StringBuilder().append(pet.getPetData().getType()).append(" ").append(pet.getRace()).append(" ").append(pet.getColor()).append(" ");
            if (pet instanceof HorsePet) {
                str = (((HorsePet) pet).hasSaddle() ? "3" : "2") + " 2 " + ((HorsePet) pet).getHairStyle() + " " + ((HorsePet) pet).getHairColor() + " 3 " + ((HorsePet) pet).getHairStyle() + " " + ((HorsePet) pet).getHairColor() + (((HorsePet) pet).hasSaddle() ? " 4 9 0" : Emulator.PREVIEW);
            } else {
                str = pet instanceof MonsterplantPet ? ((MonsterplantPet) pet).look.isEmpty() ? "2 1 8 6 0 -1 -1" : ((MonsterplantPet) pet).look : "2 2 -1 0 3 -1 0";
            }
            serverMessage.appendString(sbAppend.append(str).toString());
        }
        this.response.appendInt(Integer.valueOf(pet.getRoomUnit().getId()));
        this.response.appendInt(Short.valueOf(pet.getRoomUnit().getX()));
        this.response.appendInt(Short.valueOf(pet.getRoomUnit().getY()));
        this.response.appendString(pet.getRoomUnit().getZ() + Emulator.PREVIEW);
        this.response.appendInt((Integer) 0);
        this.response.appendInt((Integer) 2);
        this.response.appendInt(Integer.valueOf(pet.getPetData().getType()));
        this.response.appendInt(Integer.valueOf(pet.getUserId()));
        this.response.appendString((String) pet.getRoom().getFurniOwnerNames().get(pet.getUserId()));
        this.response.appendInt(Integer.valueOf(pet instanceof MonsterplantPet ? ((MonsterplantPet) pet).getRarity() : 1));
        this.response.appendBoolean(Boolean.valueOf((pet instanceof RideablePet) && ((RideablePet) pet).hasSaddle()));
        this.response.appendBoolean(false);
        this.response.appendBoolean(Boolean.valueOf((pet instanceof MonsterplantPet) && ((MonsterplantPet) pet).canBreed()));
        this.response.appendBoolean(Boolean.valueOf(((pet instanceof MonsterplantPet) && ((MonsterplantPet) pet).isFullyGrown()) ? false : true));
        this.response.appendBoolean(Boolean.valueOf((pet instanceof MonsterplantPet) && ((MonsterplantPet) pet).isDead()));
        this.response.appendBoolean(Boolean.valueOf((pet instanceof MonsterplantPet) && ((MonsterplantPet) pet).isPubliclyBreedable()));
        this.response.appendInt(Integer.valueOf(pet instanceof MonsterplantPet ? ((MonsterplantPet) pet).getGrowthStage() : pet.getLevel()));
        this.response.appendString(Emulator.PREVIEW);
        return true;
    }
}
