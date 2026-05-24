package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.YoutubeManager;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.rooms.items.youtube.YoutubeVideoComposer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ScheduledFuture;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionYoutubeTV.class */
public class InteractionYoutubeTV extends HabboItem {
    public YoutubeManager.YoutubePlaylist currentPlaylist;
    public YoutubeManager.YoutubeVideo currentVideo;
    public int startedWatchingAt;
    public int offset;
    public boolean playing;
    public ScheduledFuture autoAdvance;

    public InteractionYoutubeTV(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        this.currentPlaylist = null;
        this.currentVideo = null;
        this.startedWatchingAt = 0;
        this.offset = 0;
        this.playing = true;
        this.autoAdvance = null;
    }

    public InteractionYoutubeTV(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        this.currentPlaylist = null;
        this.currentVideo = null;
        this.startedWatchingAt = 0;
        this.offset = 0;
        this.playing = true;
        this.autoAdvance = null;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) {
        return false;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean isWalkable() {
        return false;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void serializeExtradata(ServerMessage serverMessage) {
        if (getExtradata().length() == 0) {
            setExtradata(Emulator.PREVIEW);
        }
        serverMessage.appendInt(Integer.valueOf(1 + (isLimited() ? 256 : 0)));
        serverMessage.appendInt((Integer) 1);
        serverMessage.appendString("THUMBNAIL_URL");
        if (this.currentVideo == null) {
            serverMessage.appendString(Emulator.PREVIEW);
        } else {
            serverMessage.appendString(Emulator.getConfig().getValue("imager.url.youtube").replace("%video%", this.currentVideo.getId()));
        }
        super.serializeExtradata(serverMessage);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onPickUp(Room room) {
        super.onPickUp(room);
        if (this.autoAdvance != null) {
            cancelAdvancement();
        }
        this.currentVideo = null;
        this.currentPlaylist = null;
        this.startedWatchingAt = 0;
        this.offset = 0;
    }

    public void cancelAdvancement() {
        if (this.autoAdvance == null) {
            return;
        }
        this.autoAdvance.cancel(true);
        this.autoAdvance = null;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        super.onClick(gameClient, room, objArr);
        if (this.currentVideo != null) {
            int intUnixTimestamp = this.offset;
            if (this.playing) {
                intUnixTimestamp += Emulator.getIntUnixTimestamp() - this.startedWatchingAt;
            }
            gameClient.sendResponse(new YoutubeVideoComposer(getId(), this.currentVideo, this.playing, intUnixTimestamp));
        }
    }
}
