package com.eu.habbo.messages.outgoing.rooms;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/RoomDataComposer.class */
public class RoomDataComposer extends MessageComposer {
    private final Room room;
    private final Habbo habbo;
    private final boolean roomForward;
    private final boolean enterRoom;

    public RoomDataComposer(Room room, Habbo habbo, boolean z, boolean z2) {
        this.room = room;
        this.habbo = habbo;
        this.roomForward = z;
        this.enterRoom = z2;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomDataComposer);
        this.response.appendBoolean(Boolean.valueOf(this.enterRoom));
        this.response.appendInt(Integer.valueOf(this.room.getId()));
        this.response.appendString(this.room.getName());
        if (this.room.isPublicRoom()) {
            this.response.appendInt((Integer) 0);
            this.response.appendString(Emulator.PREVIEW);
        } else {
            this.response.appendInt(Integer.valueOf(this.room.getOwnerId()));
            this.response.appendString(this.room.getOwnerName());
        }
        this.response.appendInt(Integer.valueOf(this.room.getState().getState()));
        this.response.appendInt(Integer.valueOf(this.room.getUserCount()));
        this.response.appendInt(Integer.valueOf(this.room.getUsersMax()));
        this.response.appendString(this.room.getDescription());
        this.response.appendInt(Integer.valueOf(this.room.getTradeMode()));
        this.response.appendInt(Integer.valueOf(this.room.getScore()));
        this.response.appendInt((Integer) 2);
        this.response.appendInt(Integer.valueOf(this.room.getCategory()));
        if (this.room.getTags().isEmpty()) {
            this.response.appendInt((Integer) 0);
        } else {
            String[] strArrSplit = this.room.getTags().split(";");
            this.response.appendInt(Integer.valueOf(strArrSplit.length));
            for (String str : strArrSplit) {
                this.response.appendString(str);
            }
        }
        int i = this.room.getGuildId() > 0 ? 0 | 2 : 0;
        if (!this.room.isPublicRoom()) {
            i |= 8;
        }
        if (this.room.isPromoted()) {
            i |= 4;
        }
        if (this.room.isAllowPets()) {
            i |= 16;
        }
        this.response.appendInt(Integer.valueOf(i));
        if (this.room.getGuildId() > 0) {
            Guild guild = Emulator.getGameEnvironment().getGuildManager().getGuild(this.room.getGuildId());
            if (guild != null) {
                this.response.appendInt(Integer.valueOf(guild.getId()));
                this.response.appendString(guild.getName());
                this.response.appendString(guild.getBadge());
            } else {
                this.response.appendInt((Integer) 0);
                this.response.appendString(Emulator.PREVIEW);
                this.response.appendString(Emulator.PREVIEW);
            }
        }
        if (this.room.isPromoted()) {
            this.response.appendString(this.room.getPromotion().getTitle());
            this.response.appendString(this.room.getPromotion().getDescription());
            this.response.appendInt(Integer.valueOf((this.room.getPromotion().getEndTimestamp() - Emulator.getIntUnixTimestamp()) / 60));
        }
        this.response.appendBoolean(Boolean.valueOf(this.roomForward));
        this.response.appendBoolean(Boolean.valueOf(this.room.isStaffPromotedRoom()));
        this.response.appendBoolean(Boolean.valueOf(this.room.hasGuild() && Emulator.getGameEnvironment().getGuildManager().getGuildMember(this.room.getGuildId(), this.habbo.getHabboInfo().getId()) != null));
        this.response.appendBoolean(Boolean.valueOf(this.room.isMuted()));
        this.response.appendInt(Integer.valueOf(this.room.getMuteOption()));
        this.response.appendInt(Integer.valueOf(this.room.getKickOption()));
        this.response.appendInt(Integer.valueOf(this.room.getBanOption()));
        this.response.appendBoolean(Boolean.valueOf(this.room.hasRights(this.habbo)));
        this.response.appendInt(Integer.valueOf(this.room.getChatMode()));
        this.response.appendInt(Integer.valueOf(this.room.getChatWeight()));
        this.response.appendInt(Integer.valueOf(this.room.getChatSpeed()));
        this.response.appendInt(Integer.valueOf(this.room.getChatDistance()));
        this.response.appendInt(Integer.valueOf(this.room.getChatProtection()));
        return this.response;
    }
}
