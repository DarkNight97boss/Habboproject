package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.messages.ServerMessage;
import gnu.trove.set.hash.THashSet;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionGuildFurni.class */
public class InteractionGuildFurni extends InteractionDefault {
    private int guildId;
    private static final THashSet<String> ROTATION_8_ITEMS = new THashSet<String>() { // from class: com.eu.habbo.habbohotel.items.interactions.InteractionGuildFurni.1
        {
            add("gld_wall_tall");
        }
    };

    public InteractionGuildFurni(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.guildId = resultSet.getInt("guild_id");
    }

    public InteractionGuildFurni(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.guildId = 0;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public int getMaximumRotations() {
        if (ROTATION_8_ITEMS.stream().anyMatch(str -> {
            return str.equalsIgnoreCase(getBaseItem().getName());
        })) {
            return 8;
        }
        return getBaseItem().getRotations();
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem
    public void serializeExtradata(ServerMessage serverMessage) {
        Guild guild = Emulator.getGameEnvironment().getGuildManager().getGuild(this.guildId);
        if (guild != null) {
            serverMessage.appendInt(Integer.valueOf(2 + (isLimited() ? 256 : 0)));
            serverMessage.appendInt((Integer) 5);
            serverMessage.appendString(getExtradata());
            serverMessage.appendString(guild.getId() + Emulator.PREVIEW);
            serverMessage.appendString(guild.getBadge());
            serverMessage.appendString(Emulator.getGameEnvironment().getGuildManager().getSymbolColor(guild.getColorOne()).valueA);
            serverMessage.appendString(Emulator.getGameEnvironment().getGuildManager().getBackgroundColor(guild.getColorTwo()).valueA);
        } else {
            serverMessage.appendInt(Integer.valueOf(isLimited() ? 256 : 0));
            serverMessage.appendString(getExtradata());
        }
        if (isLimited()) {
            serverMessage.appendInt(Integer.valueOf(getLimitedSells()));
            serverMessage.appendInt(Integer.valueOf(getLimitedStack()));
        }
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem
    public boolean isWalkable() {
        return getBaseItem().allowWalk();
    }

    public int getGuildId() {
        return this.guildId;
    }

    public void setGuildId(int i) {
        this.guildId = i;
    }

    @Override // com.eu.habbo.habbohotel.items.interactions.InteractionDefault, com.eu.habbo.habbohotel.users.HabboItem
    public boolean allowWiredResetState() {
        return true;
    }
}
