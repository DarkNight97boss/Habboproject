package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.PetManager;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import gnu.trove.procedure.TIntObjectProcedure;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/PetInfoCommand.class */
public class PetInfoCommand extends Command {
    public PetInfoCommand() {
        super("cmd_pet_info", Emulator.getTexts().getValue("commands.keys.cmd_pet_info").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(final GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length <= 1) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_pet_info.pet_not_found"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (gameClient.getHabbo().getHabboInfo().getCurrentRoom() == null) {
            return false;
        }
        final String str = strArr[1];
        gameClient.getHabbo().getHabboInfo().getCurrentRoom().getCurrentPets().forEachEntry(new TIntObjectProcedure<Pet>() { // from class: com.eu.habbo.habbohotel.commands.PetInfoCommand.1
            public boolean execute(int i, Pet pet) {
                if (!pet.getName().equalsIgnoreCase(str)) {
                    return true;
                }
                gameClient.getHabbo().alert(Emulator.PREVIEW + Emulator.getTexts().getValue("commands.generic.cmd_pet_info.title") + ": " + pet.getName() + "\r\n" + Emulator.getTexts().getValue("generic.pet.id") + ": " + pet.getId() + "\r" + Emulator.getTexts().getValue("generic.pet.name") + ": " + pet.getName() + "\r" + Emulator.getTexts().getValue("generic.pet.age") + ": " + pet.daysAlive() + " " + Emulator.getTexts().getValue("generic.pet.days.alive") + "\r" + Emulator.getTexts().getValue("generic.pet.level") + ": " + pet.getLevel() + "\r\r" + Emulator.getTexts().getValue("commands.generic.cmd_pet_info.stats") + "\r\n" + Emulator.getTexts().getValue("generic.pet.scratches") + ": " + pet.getRespect() + "\r" + Emulator.getTexts().getValue("generic.pet.energy") + ": " + pet.getEnergy() + "/" + PetManager.maxEnergy(pet.getLevel()) + "\r" + Emulator.getTexts().getValue("generic.pet.happyness") + ": " + pet.getHappyness() + "\r" + Emulator.getTexts().getValue("generic.pet.level.thirst") + ": " + pet.levelThirst + "\r" + Emulator.getTexts().getValue("generic.pet.level.hunger") + ": " + pet.levelHunger + "\r" + Emulator.getTexts().getValue("generic.pet.current_action") + ": " + (pet.getTask() == null ? Emulator.getTexts().getValue("generic.nothing") : pet.getTask().name()) + "\r" + Emulator.getTexts().getValue("generic.can.walk") + ": " + (pet.getRoomUnit().canWalk() ? Emulator.getTexts().getValue("generic.yes") : Emulator.getTexts().getValue("generic.no")) + Emulator.PREVIEW);
                return true;
            }
        });
        return true;
    }
}
