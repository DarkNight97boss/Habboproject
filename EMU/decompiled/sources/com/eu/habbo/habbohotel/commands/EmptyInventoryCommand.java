package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.inventory.InventoryItemsComposer;
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer;
import com.eu.habbo.threading.runnables.QueryDeleteHabboItems;
import gnu.trove.map.hash.TIntObjectHashMap;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/EmptyInventoryCommand.class */
public class EmptyInventoryCommand extends Command {
    public EmptyInventoryCommand() {
        super("cmd_empty", Emulator.getTexts().getValue("commands.keys.cmd_empty").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length == 1 || (strArr.length == 2 && !strArr[1].equals(Emulator.getTexts().getValue("generic.yes")))) {
            if (gameClient.getHabbo().getHabboInfo().getCurrentRoom() == null) {
                return true;
            }
            if (gameClient.getHabbo().getHabboInfo().getCurrentRoom().getUserCount() > 10) {
                gameClient.getHabbo().alert(Emulator.getTexts().getValue("commands.succes.cmd_empty.verify").replace("%generic.yes%", Emulator.getTexts().getValue("generic.yes")));
                return true;
            }
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_empty.verify").replace("%generic.yes%", Emulator.getTexts().getValue("generic.yes")), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (strArr.length < 2 || !strArr[1].equalsIgnoreCase(Emulator.getTexts().getValue("generic.yes"))) {
            return true;
        }
        Habbo habbo = (strArr.length == 3 && gameClient.getHabbo().hasPermission(Permission.ACC_EMPTY_OTHERS)) ? Emulator.getGameEnvironment().getHabboManager().getHabbo(strArr[2]) : gameClient.getHabbo();
        if (habbo == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_empty"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        TIntObjectHashMap tIntObjectHashMap = new TIntObjectHashMap();
        tIntObjectHashMap.putAll(habbo.getInventory().getItemsComponent().getItems());
        habbo.getInventory().getItemsComponent().getItems().clear();
        Emulator.getThreading().run(new QueryDeleteHabboItems(tIntObjectHashMap));
        habbo.getClient().sendResponse(new InventoryRefreshComposer());
        habbo.getClient().sendResponse(new InventoryItemsComposer(0, 1, gameClient.getHabbo().getInventory().getItemsComponent().getItems()));
        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_empty.cleared").replace("%username%", habbo.getHabboInfo().getUsername()), RoomChatMessageBubbles.ALERT);
        return true;
    }
}
