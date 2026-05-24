package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer;
import com.eu.habbo.threading.runnables.QueryDeleteHabboItems;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntIntProcedure;
import java.util.ArrayList;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/RedeemCommand.class */
public class RedeemCommand extends Command {
    public RedeemCommand() {
        super("cmd_redeem", Emulator.getTexts().getValue("commands.keys.cmd_redeem").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(final GameClient gameClient, String[] strArr) throws Exception {
        if (gameClient.getHabbo().getHabboInfo().getCurrentRoom().getActiveTradeForHabbo(gameClient.getHabbo()) != null) {
            return false;
        }
        ArrayList<HabboItem> arrayList = new ArrayList();
        int iIntValue = 0;
        int iIntValue2 = 0;
        TIntIntHashMap tIntIntHashMap = new TIntIntHashMap();
        TObjectHashIterator it = gameClient.getHabbo().getInventory().getItemsComponent().getItemsAsValueCollection().iterator();
        while (it.hasNext()) {
            HabboItem habboItem = (HabboItem) it.next();
            if (habboItem.getBaseItem().getName().startsWith("CF_") || habboItem.getBaseItem().getName().startsWith("CFC_") || habboItem.getBaseItem().getName().startsWith("DF_") || habboItem.getBaseItem().getName().startsWith("PF_")) {
                if (habboItem.getUserId() == gameClient.getHabbo().getHabboInfo().getId()) {
                    arrayList.add(habboItem);
                    if ((habboItem.getBaseItem().getName().startsWith("CF_") || habboItem.getBaseItem().getName().startsWith("CFC_")) && !habboItem.getBaseItem().getName().contains("_diamond_")) {
                        try {
                            iIntValue += Integer.valueOf(habboItem.getBaseItem().getName().split("_")[1]).intValue();
                        } catch (Exception e) {
                        }
                    } else if (habboItem.getBaseItem().getName().startsWith("PF_")) {
                        try {
                            iIntValue2 += Integer.valueOf(habboItem.getBaseItem().getName().split("_")[1]).intValue();
                        } catch (Exception e2) {
                        }
                    } else if (habboItem.getBaseItem().getName().startsWith("DF_")) {
                        int iIntValue3 = Integer.valueOf(habboItem.getBaseItem().getName().split("_")[1]).intValue();
                        int iIntValue4 = Integer.valueOf(habboItem.getBaseItem().getName().split("_")[2]).intValue();
                        tIntIntHashMap.adjustOrPutValue(iIntValue3, iIntValue4, iIntValue4);
                    } else if (habboItem.getBaseItem().getName().startsWith("CF_diamond_")) {
                        int iIntValue5 = Integer.valueOf(habboItem.getBaseItem().getName().split("_")[2]).intValue();
                        tIntIntHashMap.adjustOrPutValue(5, iIntValue5, iIntValue5);
                    }
                }
            }
        }
        TIntObjectHashMap tIntObjectHashMap = new TIntObjectHashMap();
        for (HabboItem habboItem2 : arrayList) {
            gameClient.getHabbo().getInventory().getItemsComponent().removeHabboItem(habboItem2);
            tIntObjectHashMap.put(habboItem2.getId(), habboItem2);
        }
        Emulator.getThreading().run(new QueryDeleteHabboItems(tIntObjectHashMap));
        gameClient.sendResponse(new InventoryRefreshComposer());
        gameClient.getHabbo().giveCredits(iIntValue);
        gameClient.getHabbo().givePixels(iIntValue2);
        final String[] strArr2 = {Emulator.getTexts().getValue("generic.redeemed")};
        strArr2[0] = strArr2[0] + Emulator.getTexts().getValue("generic.credits");
        strArr2[0] = strArr2[0] + ": " + iIntValue;
        if (iIntValue2 > 0) {
            strArr2[0] = strArr2[0] + ", " + Emulator.getTexts().getValue("generic.pixels");
            strArr2[0] = strArr2[0] + ": " + iIntValue2 + Emulator.PREVIEW;
        }
        if (!tIntIntHashMap.isEmpty()) {
            tIntIntHashMap.forEachEntry(new TIntIntProcedure() { // from class: com.eu.habbo.habbohotel.commands.RedeemCommand.1
                public boolean execute(int i, int i2) {
                    gameClient.getHabbo().givePoints(i, i2);
                    StringBuilder sb = new StringBuilder();
                    String[] strArr3 = strArr2;
                    strArr3[0] = sb.append(strArr3[0]).append(" ,").append(Emulator.getTexts().getValue("seasonal.name." + i)).append(": ").append(i2).toString();
                    return true;
                }
            });
        }
        gameClient.getHabbo().whisper(strArr2[0], RoomChatMessageBubbles.ALERT);
        return true;
    }
}
