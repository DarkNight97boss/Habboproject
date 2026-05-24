package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.inventory.InventoryPetsComposer;
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer;
import gnu.trove.map.hash.TIntObjectHashMap;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/EmptyPetsInventoryCommand.class */
public class EmptyPetsInventoryCommand extends Command {
    public EmptyPetsInventoryCommand() {
        super("cmd_empty_pets", Emulator.getTexts().getValue("commands.keys.cmd_empty_pets").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length == 1 || (strArr.length >= 2 && !strArr[1].equals(Emulator.getTexts().getValue("generic.yes")))) {
            if (gameClient.getHabbo().getHabboInfo().getCurrentRoom() == null) {
                return true;
            }
            if (gameClient.getHabbo().getHabboInfo().getCurrentRoom().getUserCount() > 10) {
                gameClient.getHabbo().alert(Emulator.getTexts().getValue("commands.succes.cmd_empty_pets.verify").replace("%generic.yes%", Emulator.getTexts().getValue("generic.yes")));
                return true;
            }
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_empty_pets.verify").replace("%generic.yes%", Emulator.getTexts().getValue("generic.yes")), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (strArr.length < 2 || !strArr[1].equalsIgnoreCase(Emulator.getTexts().getValue("generic.yes"))) {
            return true;
        }
        Habbo habbo = (strArr.length == 3 && gameClient.getHabbo().hasPermission(Permission.ACC_EMPTY_OTHERS)) ? Emulator.getGameEnvironment().getHabboManager().getHabbo(strArr[2]) : gameClient.getHabbo();
        if (habbo == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_empty_pets"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        TIntObjectHashMap tIntObjectHashMap = new TIntObjectHashMap();
        tIntObjectHashMap.putAll(habbo.getInventory().getPetsComponent().getPets());
        habbo.getInventory().getPetsComponent().getPets().clear();
        tIntObjectHashMap.forEachValue(pet -> {
            Emulator.getGameEnvironment().getPetManager().deletePet(pet);
            return true;
        });
        habbo.getClient().sendResponse(new InventoryRefreshComposer());
        habbo.getClient().sendResponse(new InventoryPetsComposer(habbo));
        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_empty_pets.cleared").replace("%username%", habbo.getHabboInfo().getUsername()), RoomChatMessageBubbles.ALERT);
        return true;
    }
}
