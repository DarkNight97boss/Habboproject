package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.users.HabboManager;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertKeys;
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer;
import gnu.trove.map.hash.THashMap;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/GiftCommand.class */
public class GiftCommand extends Command {
    public GiftCommand() {
        super("cmd_gift", Emulator.getTexts().getValue("commands.keys.cmd_gift").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length < 3) {
            return false;
        }
        String str = strArr[1];
        try {
            int iIntValue = Integer.valueOf(strArr[2]).intValue();
            if (iIntValue <= 0) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_gift.not_a_number"), RoomChatMessageBubbles.ALERT);
                return true;
            }
            Item item = Emulator.getGameEnvironment().getItemManager().getItem(iIntValue);
            if (item == null) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_gift.not_found").replace("%itemid%", iIntValue + Emulator.PREVIEW), RoomChatMessageBubbles.ALERT);
                return true;
            }
            HabboInfo offlineHabboInfo = HabboManager.getOfflineHabboInfo(str);
            if (offlineHabboInfo == null) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_gift.user_not_found").replace("%username%", str), RoomChatMessageBubbles.ALERT);
                return true;
            }
            StringBuilder sb = new StringBuilder();
            if (strArr.length > 3) {
                for (int i = 3; i < strArr.length; i++) {
                    sb.append(strArr[i]).append(" ");
                }
            }
            String string = sb.toString();
            HabboItem habboItemCreateItem = Emulator.getGameEnvironment().getItemManager().createItem(0, item, 0, 0, Emulator.PREVIEW);
            Emulator.getGameEnvironment().getItemManager().createGift(str, Emulator.getGameEnvironment().getItemManager().getItem(((Integer) Emulator.getGameEnvironment().getCatalogManager().giftFurnis.values().toArray()[Emulator.getRandom().nextInt(Emulator.getGameEnvironment().getCatalogManager().giftFurnis.size())]).intValue()), ("1\t" + habboItemCreateItem.getId()) + "\t0\t0\t0\t" + string + "\t0\t0", 0, 0);
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_gift").replace("%username%", str).replace("%itemname%", habboItemCreateItem.getBaseItem().getName()), RoomChatMessageBubbles.ALERT);
            Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(offlineHabboInfo.getId());
            if (habbo == null) {
                return true;
            }
            habbo.getClient().sendResponse(new InventoryRefreshComposer());
            THashMap tHashMap = new THashMap();
            tHashMap.put("display", "BUBBLE");
            tHashMap.put("image", "${image.library.url}notifications/gift.gif");
            tHashMap.put("message", Emulator.getTexts().getValue("generic.gift.received.anonymous"));
            habbo.getClient().sendResponse(new BubbleAlertComposer(BubbleAlertKeys.RECEIVED_BADGE.key, (THashMap<String, String>) tHashMap));
            return true;
        } catch (Exception e) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_gift.not_a_number"), RoomChatMessageBubbles.ALERT);
            return true;
        }
    }
}
