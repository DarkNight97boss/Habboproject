package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer;
import com.eu.habbo.messages.outgoing.wired.WiredRewardAlertComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/RoomGiftCommand.class */
public class RoomGiftCommand extends Command {
    public RoomGiftCommand() {
        super("cmd_roomgift", Emulator.getTexts().getValue("commands.keys.cmd_roomgift").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length < 2) {
            return false;
        }
        try {
            int iIntValue = Integer.valueOf(strArr[1]).intValue();
            if (iIntValue <= 0) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_gift.not_a_number"), RoomChatMessageBubbles.ALERT);
                return true;
            }
            Item item = Emulator.getGameEnvironment().getItemManager().getItem(iIntValue);
            if (item == null) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_gift.not_found").replace("%itemid%", iIntValue + Emulator.PREVIEW), RoomChatMessageBubbles.ALERT);
                return true;
            }
            StringBuilder sb = new StringBuilder();
            if (strArr.length > 2) {
                for (int i = 2; i < strArr.length; i++) {
                    sb.append(strArr[i]).append(" ");
                }
            }
            String string = sb.toString();
            for (Habbo habbo : gameClient.getHabbo().getHabboInfo().getCurrentRoom().getHabbos()) {
                Emulator.getGameEnvironment().getItemManager().createGift(habbo.getHabboInfo().getUsername(), Emulator.getGameEnvironment().getItemManager().getItem(((Integer) Emulator.getGameEnvironment().getCatalogManager().giftFurnis.values().toArray()[Emulator.getRandom().nextInt(Emulator.getGameEnvironment().getCatalogManager().giftFurnis.size())]).intValue()), ("1\t" + Emulator.getGameEnvironment().getItemManager().createItem(0, item, 0, 0, Emulator.PREVIEW).getId()) + "\t0\t0\t0\t" + string + "\t0\t0", 0, 0);
                habbo.getClient().sendResponse(new InventoryRefreshComposer());
                habbo.getClient().sendResponse(new WiredRewardAlertComposer(6));
            }
            return true;
        } catch (Exception e) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_gift.not_a_number"), RoomChatMessageBubbles.ALERT);
            return true;
        }
    }
}
