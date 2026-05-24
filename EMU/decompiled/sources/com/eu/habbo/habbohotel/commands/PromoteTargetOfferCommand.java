package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.TargetOffer;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.catalog.TargetedOfferComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.MessagesForYouComposer;
import gnu.trove.map.hash.THashMap;
import java.util.ArrayList;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/PromoteTargetOfferCommand.class */
public class PromoteTargetOfferCommand extends Command {
    public PromoteTargetOfferCommand() {
        super("cmd_promote_offer", Emulator.getTexts().getValue("commands.keys.cmd_promote_offer").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length <= 1) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_promote_offer.not_found"));
            return true;
        }
        String str = strArr[1];
        if (!str.equalsIgnoreCase(Emulator.getTexts().getValue("commands.cmd_promote_offer.info"))) {
            int iIntValue = 0;
            try {
                iIntValue = Integer.valueOf(str).intValue();
            } catch (Exception e) {
            }
            if (iIntValue <= 0) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_promote_offer.not_found"));
                return true;
            }
            TargetOffer targetOffer = Emulator.getGameEnvironment().getCatalogManager().getTargetOffer(iIntValue);
            if (targetOffer == null) {
                return true;
            }
            TargetOffer.ACTIVE_TARGET_OFFER_ID = targetOffer.getId();
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_promote_offer").replace("%id%", str).replace("%title%", targetOffer.getTitle()));
            for (Habbo habbo : Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos().values()) {
                habbo.getClient().sendResponse(new TargetedOfferComposer(habbo, targetOffer));
            }
            return true;
        }
        THashMap<Integer, TargetOffer> tHashMap = Emulator.getGameEnvironment().getCatalogManager().targetOffers;
        String[] strArrSplit = Emulator.getTexts().getValue("commands.cmd_promote_offer.list").replace("%amount%", tHashMap.size() + Emulator.PREVIEW).split("<br>");
        String value = Emulator.getTexts().getValue("commands.cmd_promote_offer.list.entry");
        ArrayList arrayList = new ArrayList();
        for (String str2 : strArrSplit) {
            if (str2.contains("%list%")) {
                for (TargetOffer targetOffer2 : tHashMap.values()) {
                    arrayList.add(value.replace("%id%", targetOffer2.getId() + Emulator.PREVIEW).replace("%title%", targetOffer2.getTitle()).replace("%description%", targetOffer2.getDescription().substring(0, 25)));
                }
            } else {
                arrayList.add(str2);
            }
        }
        gameClient.sendResponse(new MessagesForYouComposer(arrayList));
        return true;
    }
}
