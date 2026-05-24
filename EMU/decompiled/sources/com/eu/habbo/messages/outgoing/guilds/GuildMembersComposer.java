package com.eu.habbo.messages.outgoing.guilds;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.guilds.GuildMember;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/guilds/GuildMembersComposer.class */
public class GuildMembersComposer extends MessageComposer {
    private final ArrayList<GuildMember> members;
    private final Guild guild;
    private final Habbo session;
    private final int pageId;
    private final int level;
    private final String searchValue;
    private final boolean isAdmin;
    private final int totalCount;

    public GuildMembersComposer(Guild guild, ArrayList<GuildMember> arrayList, Habbo habbo, int i, int i2, String str, boolean z, int i3) {
        this.guild = guild;
        this.members = arrayList;
        this.session = habbo;
        this.pageId = i;
        this.level = i2;
        this.searchValue = str;
        this.isAdmin = z;
        this.totalCount = i3;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.GuildMembersComposer);
        this.response.appendInt(Integer.valueOf(this.guild.getId()));
        this.response.appendString(this.guild.getName());
        this.response.appendInt(Integer.valueOf(this.guild.getRoomId()));
        this.response.appendString(this.guild.getBadge());
        this.response.appendInt(Integer.valueOf(this.totalCount));
        this.response.appendInt(Integer.valueOf(this.members.size()));
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        for (GuildMember guildMember : this.members) {
            calendar.setTimeInMillis(((long) guildMember.getJoinDate()) * 1000);
            this.response.appendInt(Integer.valueOf(guildMember.getRank().type));
            this.response.appendInt(Integer.valueOf(guildMember.getUserId()));
            this.response.appendString(guildMember.getUsername());
            this.response.appendString(guildMember.getLook());
            this.response.appendString(guildMember.getRank().type < 3 ? calendar.get(5) + "/" + (calendar.get(2) + 1) + "/" + calendar.get(1) : Emulator.PREVIEW);
        }
        this.response.appendBoolean(Boolean.valueOf(this.isAdmin));
        this.response.appendInt((Integer) 14);
        this.response.appendInt(Integer.valueOf(this.pageId));
        this.response.appendInt(Integer.valueOf(this.level));
        this.response.appendString(this.searchValue);
        return this.response;
    }
}
