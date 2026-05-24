package com.eu.habbo.messages.outgoing.rooms.items.youtube;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.YoutubeManager;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.ArrayList;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/items/youtube/YoutubeDisplayListComposer.class */
public class YoutubeDisplayListComposer extends MessageComposer {
    private final int itemId;
    private final ArrayList<YoutubeManager.YoutubePlaylist> playlists;
    private final YoutubeManager.YoutubePlaylist currentPlaylist;

    public YoutubeDisplayListComposer(int i, ArrayList<YoutubeManager.YoutubePlaylist> arrayList, YoutubeManager.YoutubePlaylist youtubePlaylist) {
        this.itemId = i;
        this.playlists = arrayList;
        this.currentPlaylist = youtubePlaylist;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.YoutubeDisplayListComposer);
        this.response.appendInt(Integer.valueOf(this.itemId));
        this.response.appendInt(Integer.valueOf(this.playlists.size()));
        for (YoutubeManager.YoutubePlaylist youtubePlaylist : this.playlists) {
            this.response.appendString(youtubePlaylist.getId());
            this.response.appendString(youtubePlaylist.getName());
            this.response.appendString(youtubePlaylist.getDescription());
        }
        this.response.appendString(this.currentPlaylist == null ? Emulator.PREVIEW : this.currentPlaylist.getId());
        return this.response;
    }
}
