package com.eu.habbo.habbohotel.pets;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.habbohotel.users.Habbo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/pets/PetCommand.class */
public class PetCommand implements Comparable<PetCommand> {
    public final int id;
    public final String key;
    public final int level;
    public final int xp;
    public final int energyCost;
    public final int happynessCost;
    public final PetAction action;

    public PetCommand(ResultSet resultSet, PetAction petAction) throws SQLException {
        this.id = resultSet.getInt("command_id");
        this.key = resultSet.getString("text");
        this.level = resultSet.getInt("required_level");
        this.xp = resultSet.getInt("reward_xp");
        this.energyCost = resultSet.getInt("cost_energy");
        this.happynessCost = resultSet.getInt("cost_happyness");
        this.action = petAction;
    }

    @Override // java.lang.Comparable
    public int compareTo(PetCommand petCommand) {
        return this.level - petCommand.level;
    }

    public void handle(Pet pet, Habbo habbo, String[] strArr) {
        if (Emulator.getRandom().nextInt((pet.level - this.level <= 0 ? 2 : pet.level - this.level) + 2) == 0) {
            pet.say(pet.petData.randomVocal(PetVocalsType.DISOBEY));
            return;
        }
        if (this.action == null || this.action.petTask == pet.getTask()) {
            return;
        }
        if (this.action.stopsPetWalking) {
            pet.getRoomUnit().setGoalLocation(pet.getRoomUnit().getCurrentLocation());
        }
        if (this.action.apply(pet, habbo, strArr)) {
            Iterator<RoomUnitStatus> it = this.action.statusToRemove.iterator();
            while (it.hasNext()) {
                pet.getRoomUnit().removeStatus(it.next());
            }
            Iterator<RoomUnitStatus> it2 = this.action.statusToSet.iterator();
            while (it2.hasNext()) {
                pet.getRoomUnit().setStatus(it2.next(), "0");
            }
            pet.getRoomUnit().setStatus(RoomUnitStatus.GESTURE, this.action.gestureToSet);
            pet.addEnergy(-this.energyCost);
            pet.addHappyness(-this.happynessCost);
            pet.addExperience(this.xp);
        }
    }
}
