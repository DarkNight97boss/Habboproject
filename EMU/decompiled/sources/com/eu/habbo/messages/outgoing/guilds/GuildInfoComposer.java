package com.eu.habbo.messages.outgoing.guilds;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.guilds.GuildMember;
import com.eu.habbo.habbohotel.guilds.GuildMembershipStatus;
import com.eu.habbo.habbohotel.guilds.GuildRank;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.text.SimpleDateFormat;
import java.util.Date;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/guilds/GuildInfoComposer.class */
public class GuildInfoComposer extends MessageComposer {
    private final Guild guild;
    private final GameClient client;
    private final boolean newWindow;
    private final GuildMember member;

    public GuildInfoComposer(Guild guild, GameClient gameClient, boolean z, GuildMember guildMember) {
        this.guild = guild;
        this.client = gameClient;
        this.newWindow = z;
        this.member = guildMember;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        boolean z = (this.client.getHabbo().getHabboStats().hasGuild(this.guild.getId()) && this.client.getHabbo().hasPermission(Permission.ACC_GUILD_ADMIN)) || Emulator.getGameEnvironment().getGuildManager().getOnlyAdmins(this.guild).get(Integer.valueOf(this.client.getHabbo().getHabboInfo().getId())) != null;
        this.response.init(Outgoing.GuildInfoComposer);
        this.response.appendInt(Integer.valueOf(this.guild.getId()));
        this.response.appendBoolean(true);
        this.response.appendInt(Integer.valueOf(this.guild.getState().state));
        this.response.appendString(this.guild.getName());
        this.response.appendString(this.guild.getDescription());
        this.response.appendString(this.guild.getBadge());
        this.response.appendInt(Integer.valueOf(this.guild.getRoomId()));
        this.response.appendString(this.guild.getRoomName());
        this.response.appendInt(Integer.valueOf((this.member == null ? GuildMembershipStatus.NOT_MEMBER : this.member.getMembershipStatus()).getStatus()));
        this.response.appendInt(Integer.valueOf(this.guild.getMemberCount()));
        this.response.appendBoolean(Boolean.valueOf(this.client.getHabbo().getHabboStats().guild == this.guild.getId()));
        this.response.appendString(new SimpleDateFormat("dd-MM-yyyy").format(new Date(((long) this.guild.getDateCreated()) * 1000)));
        this.response.appendBoolean(Boolean.valueOf(z || this.guild.getOwnerId() == this.client.getHabbo().getHabboInfo().getId()));
        this.response.appendBoolean(Boolean.valueOf(z || (this.member != null && this.member.getRank().equals(GuildRank.ADMIN))));
        this.response.appendString(this.guild.getOwnerName());
        this.response.appendBoolean(Boolean.valueOf(this.newWindow));
        this.response.appendBoolean(Boolean.valueOf(this.guild.getRights()));
        this.response.appendInt(Integer.valueOf((z || this.guild.getOwnerId() == this.client.getHabbo().getHabboInfo().getId()) ? this.guild.getRequestCount() : 0));
        this.response.appendBoolean(Boolean.valueOf(this.guild.hasForum()));
        return this.response;
    }
}
