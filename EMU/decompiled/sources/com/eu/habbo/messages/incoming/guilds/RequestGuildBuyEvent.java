package com.eu.habbo.messages.incoming.guilds;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.catalog.AlertPurchaseFailedComposer;
import com.eu.habbo.messages.outgoing.catalog.PurchaseOKComposer;
import com.eu.habbo.messages.outgoing.guilds.GuildBoughtComposer;
import com.eu.habbo.messages.outgoing.guilds.GuildEditFailComposer;
import com.eu.habbo.messages.outgoing.guilds.GuildInfoComposer;
import com.eu.habbo.plugin.events.guilds.GuildPurchasedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/guilds/RequestGuildBuyEvent.class */
public class RequestGuildBuyEvent extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestGuildBuyEvent.class);

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        String string = this.packet.readString();
        String string2 = this.packet.readString();
        if (string.length() > 29 || string2.length() > 254) {
            return;
        }
        if (Emulator.getConfig().getBoolean("catalog.guild.hc_required", true) && !this.client.getHabbo().getHabboStats().hasActiveClub()) {
            this.client.sendResponse(new GuildEditFailComposer(2));
            return;
        }
        if (!this.client.getHabbo().hasPermission(Permission.ACC_INFINITE_CREDITS)) {
            int i = Emulator.getConfig().getInt("catalog.guild.price");
            if (this.client.getHabbo().getHabboInfo().getCredits() < i) {
                this.client.sendResponse(new AlertPurchaseFailedComposer(0));
                return;
            }
            this.client.getHabbo().giveCredits(-i);
        }
        int iIntValue = this.packet.readInt().intValue();
        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(iIntValue);
        if (room != null) {
            if (room.hasGuild()) {
                this.client.sendResponse(new GuildEditFailComposer(0));
                return;
            }
            if (room.getOwnerId() != this.client.getHabbo().getHabboInfo().getId()) {
                String strReplace = Emulator.getTexts().getValue("scripter.warning.guild.buy.owner").replace("%username%", this.client.getHabbo().getHabboInfo().getUsername()).replace("%roomname%", room.getName().replace("%owner%", room.getOwnerName()));
                ScripterManager.scripterDetected(this.client, strReplace);
                LOGGER.info(strReplace);
                return;
            }
            if (room.getGuildId() == 0) {
                int iIntValue2 = this.packet.readInt().intValue();
                int iIntValue3 = this.packet.readInt().intValue();
                int iIntValue4 = this.packet.readInt().intValue();
                String str = Emulator.PREVIEW;
                byte b = 1;
                while (true) {
                    byte b2 = b;
                    if (b2 >= iIntValue4) {
                        break;
                    }
                    int iIntValue5 = this.packet.readInt().intValue();
                    int iIntValue6 = this.packet.readInt().intValue();
                    str = (b2 == 1 ? str + "b" : str + "s") + (iIntValue5 < 100 ? "0" : Emulator.PREVIEW) + (iIntValue5 < 10 ? "0" : Emulator.PREVIEW) + iIntValue5 + (iIntValue6 < 10 ? "0" : Emulator.PREVIEW) + iIntValue6 + Emulator.PREVIEW + this.packet.readInt().intValue();
                    b = (byte) (b2 + 3);
                }
                if (string.length() > 29) {
                    this.client.sendResponse(new GuildEditFailComposer(1));
                    return;
                }
                if (string2.length() > 254) {
                    return;
                }
                Guild guildCreateGuild = Emulator.getGameEnvironment().getGuildManager().createGuild(this.client.getHabbo(), iIntValue, room.getName(), string, string2, str, iIntValue2, iIntValue3);
                room.setGuild(guildCreateGuild.getId());
                room.removeAllRights();
                room.setNeedsUpdate(true);
                if (Emulator.getConfig().getBoolean("imager.internal.enabled")) {
                    Emulator.getBadgeImager().generate(guildCreateGuild);
                }
                this.client.sendResponse(new PurchaseOKComposer());
                this.client.sendResponse(new GuildBoughtComposer(guildCreateGuild));
                for (Habbo habbo : room.getHabbos()) {
                    habbo.getClient().sendResponse(new GuildInfoComposer(guildCreateGuild, habbo.getClient(), false, null));
                }
                room.refreshGuild(guildCreateGuild);
                Emulator.getPluginManager().fireEvent(new GuildPurchasedEvent(guildCreateGuild, this.client.getHabbo()));
                Emulator.getGameEnvironment().getGuildManager().addGuild(guildCreateGuild);
            }
        }
    }
}
