package com.eu.habbo.messages.outgoing.guilds;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/guilds/GuildManageComposer.class */
public class GuildManageComposer extends MessageComposer {
    private final Guild guild;

    public GuildManageComposer(Guild guild) {
        this.guild = guild;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.GuildManageComposer);
        this.response.appendInt((Integer) 1);
        this.response.appendInt(Integer.valueOf(this.guild.getRoomId()));
        this.response.appendString(this.guild.getRoomName());
        this.response.appendBoolean(false);
        this.response.appendBoolean(true);
        this.response.appendInt(Integer.valueOf(this.guild.getId()));
        this.response.appendString(this.guild.getName());
        this.response.appendString(this.guild.getDescription());
        this.response.appendInt(Integer.valueOf(this.guild.getRoomId()));
        this.response.appendInt(Integer.valueOf(this.guild.getColorOne()));
        this.response.appendInt(Integer.valueOf(this.guild.getColorTwo()));
        this.response.appendInt(Integer.valueOf(this.guild.getState().state));
        this.response.appendInt(Integer.valueOf(this.guild.getRights() ? 0 : 1));
        this.response.appendBoolean(false);
        this.response.appendString(Emulator.PREVIEW);
        this.response.appendInt((Integer) 5);
        String[] strArrSplit = this.guild.getBadge().replace("b", Emulator.PREVIEW).split("s");
        int length = 5 - strArrSplit.length;
        for (String str : strArrSplit) {
            this.response.appendInt(Integer.valueOf(str.length() >= 6 ? Integer.parseInt(str.substring(0, 3)) : Integer.parseInt(str.substring(0, 2))));
            this.response.appendInt(Integer.valueOf(str.length() >= 6 ? Integer.parseInt(str.substring(3, 5)) : Integer.parseInt(str.substring(2, 4))));
            if (str.length() < 5) {
                this.response.appendInt((Integer) 0);
            } else if (str.length() >= 6) {
                this.response.appendInt(Integer.valueOf(Integer.parseInt(str.substring(5, 6))));
            } else {
                this.response.appendInt(Integer.valueOf(Integer.parseInt(str.substring(4, 5))));
            }
        }
        for (int i = 0; i != length; i++) {
            this.response.appendInt((Integer) 0);
            this.response.appendInt((Integer) 0);
            this.response.appendInt((Integer) 0);
        }
        this.response.appendString(this.guild.getBadge());
        this.response.appendInt(Integer.valueOf(this.guild.getMemberCount()));
        return this.response;
    }
}
