package com.eu.habbo.messages.outgoing.rooms.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.Collection;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/users/RoomUsersComposer.class */
public class RoomUsersComposer extends MessageComposer {
    private Habbo habbo;
    private Collection<Habbo> habbos;
    private Bot bot;
    private Collection<Bot> bots;

    public RoomUsersComposer(Habbo habbo) {
        this.habbo = habbo;
    }

    public RoomUsersComposer(Collection<Habbo> collection) {
        this.habbos = collection;
    }

    public RoomUsersComposer(Bot bot) {
        this.bot = bot;
    }

    public RoomUsersComposer(Collection<Bot> collection, boolean z) {
        this.bots = collection;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        Guild guild;
        Guild guild2;
        this.response.init(Outgoing.RoomUsersComposer);
        if (this.habbo != null) {
            this.response.appendInt((Integer) 1);
            this.response.appendInt(Integer.valueOf(this.habbo.getHabboInfo().getId()));
            this.response.appendString(this.habbo.getHabboInfo().getUsername());
            this.response.appendString(this.habbo.getHabboInfo().getMotto());
            this.response.appendString(this.habbo.getHabboInfo().getLook());
            this.response.appendInt(Integer.valueOf(this.habbo.getRoomUnit().getId()));
            this.response.appendInt(Short.valueOf(this.habbo.getRoomUnit().getX()));
            this.response.appendInt(Short.valueOf(this.habbo.getRoomUnit().getY()));
            this.response.appendString(this.habbo.getRoomUnit().getZ() + Emulator.PREVIEW);
            this.response.appendInt(Integer.valueOf(this.habbo.getRoomUnit().getBodyRotation().getValue()));
            this.response.appendInt((Integer) 1);
            this.response.appendString(this.habbo.getHabboInfo().getGender().name().toUpperCase());
            this.response.appendInt(Integer.valueOf(this.habbo.getHabboStats().guild != 0 ? this.habbo.getHabboStats().guild : -1));
            this.response.appendInt(Integer.valueOf(this.habbo.getHabboStats().guild != 0 ? 1 : -1));
            String name = Emulator.PREVIEW;
            if (this.habbo.getHabboStats().guild != 0 && (guild2 = Emulator.getGameEnvironment().getGuildManager().getGuild(this.habbo.getHabboStats().guild)) != null) {
                name = guild2.getName();
            }
            this.response.appendString(name);
            this.response.appendString(Emulator.PREVIEW);
            this.response.appendInt(Integer.valueOf(this.habbo.getHabboStats().getAchievementScore()));
            this.response.appendBoolean(true);
        } else if (this.habbos != null) {
            this.response.appendInt(Integer.valueOf(this.habbos.size()));
            for (Habbo habbo : this.habbos) {
                if (habbo != null) {
                    this.response.appendInt(Integer.valueOf(habbo.getHabboInfo().getId()));
                    this.response.appendString(habbo.getHabboInfo().getUsername());
                    this.response.appendString(habbo.getHabboInfo().getMotto());
                    this.response.appendString(habbo.getHabboInfo().getLook());
                    this.response.appendInt(Integer.valueOf(habbo.getRoomUnit().getId()));
                    this.response.appendInt(Short.valueOf(habbo.getRoomUnit().getX()));
                    this.response.appendInt(Short.valueOf(habbo.getRoomUnit().getY()));
                    this.response.appendString(habbo.getRoomUnit().getZ() + Emulator.PREVIEW);
                    this.response.appendInt(Integer.valueOf(habbo.getRoomUnit().getBodyRotation().getValue()));
                    this.response.appendInt((Integer) 1);
                    this.response.appendString(habbo.getHabboInfo().getGender().name().toUpperCase());
                    this.response.appendInt(Integer.valueOf(habbo.getHabboStats().guild != 0 ? habbo.getHabboStats().guild : -1));
                    this.response.appendInt(Integer.valueOf(habbo.getHabboStats().guild != 0 ? habbo.getHabboStats().guild : -1));
                    String name2 = Emulator.PREVIEW;
                    if (habbo.getHabboStats().guild != 0 && (guild = Emulator.getGameEnvironment().getGuildManager().getGuild(habbo.getHabboStats().guild)) != null) {
                        name2 = guild.getName();
                    }
                    this.response.appendString(name2);
                    this.response.appendString(Emulator.PREVIEW);
                    this.response.appendInt(Integer.valueOf(habbo.getHabboStats().getAchievementScore()));
                    this.response.appendBoolean(true);
                }
            }
        } else if (this.bot != null) {
            this.response.appendInt((Integer) 1);
            this.response.appendInt(Integer.valueOf(0 - this.bot.getId()));
            this.response.appendString(this.bot.getName());
            this.response.appendString(this.bot.getMotto());
            this.response.appendString(this.bot.getFigure());
            this.response.appendInt(Integer.valueOf(this.bot.getRoomUnit().getId()));
            this.response.appendInt(Short.valueOf(this.bot.getRoomUnit().getX()));
            this.response.appendInt(Short.valueOf(this.bot.getRoomUnit().getY()));
            this.response.appendString(this.bot.getRoomUnit().getZ() + Emulator.PREVIEW);
            this.response.appendInt(Integer.valueOf(this.bot.getRoomUnit().getBodyRotation().getValue()));
            this.response.appendInt((Integer) 4);
            this.response.appendString(this.bot.getGender().name().toUpperCase());
            this.response.appendInt(Integer.valueOf(this.bot.getOwnerId()));
            this.response.appendString(this.bot.getOwnerName());
            this.response.appendInt((Integer) 10);
            this.response.appendShort(0);
            this.response.appendShort(1);
            this.response.appendShort(2);
            this.response.appendShort(3);
            this.response.appendShort(4);
            this.response.appendShort(5);
            this.response.appendShort(6);
            this.response.appendShort(7);
            this.response.appendShort(8);
            this.response.appendShort(9);
        } else if (this.bots != null) {
            this.response.appendInt(Integer.valueOf(this.bots.size()));
            for (Bot bot : this.bots) {
                this.response.appendInt(Integer.valueOf(0 - bot.getId()));
                this.response.appendString(bot.getName());
                this.response.appendString(bot.getMotto());
                this.response.appendString(bot.getFigure());
                this.response.appendInt(Integer.valueOf(bot.getRoomUnit().getId()));
                this.response.appendInt(Short.valueOf(bot.getRoomUnit().getX()));
                this.response.appendInt(Short.valueOf(bot.getRoomUnit().getY()));
                this.response.appendString(bot.getRoomUnit().getZ() + Emulator.PREVIEW);
                this.response.appendInt(Integer.valueOf(bot.getRoomUnit().getBodyRotation().getValue()));
                this.response.appendInt((Integer) 4);
                this.response.appendString(bot.getGender().name().toUpperCase());
                this.response.appendInt(Integer.valueOf(bot.getOwnerId()));
                this.response.appendString(bot.getOwnerName());
                this.response.appendInt((Integer) 10);
                this.response.appendShort(0);
                this.response.appendShort(1);
                this.response.appendShort(2);
                this.response.appendShort(3);
                this.response.appendShort(4);
                this.response.appendShort(5);
                this.response.appendShort(6);
                this.response.appendShort(7);
                this.response.appendShort(8);
                this.response.appendShort(9);
            }
        }
        return this.response;
    }
}
