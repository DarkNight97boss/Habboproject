package com.eu.habbo.messages.outgoing.rooms.items.youtube;

import com.eu.habbo.habbohotel.items.YoutubeManager;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/items/youtube/YoutubeVideoComposer.class */
public class YoutubeVideoComposer extends MessageComposer {
    private final int itemId;
    private final YoutubeManager.YoutubeVideo video;
    private final boolean playing;
    private final int startTime;

    public YoutubeVideoComposer(int i, YoutubeManager.YoutubeVideo youtubeVideo, boolean z, int i2) {
        this.itemId = i;
        this.video = youtubeVideo;
        this.playing = z;
        this.startTime = i2;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(1411);
        this.response.appendInt(Integer.valueOf(this.itemId));
        this.response.appendString(this.video.getId());
        this.response.appendInt(Integer.valueOf(this.startTime));
        this.response.appendInt(Integer.valueOf(this.video.getDuration()));
        this.response.appendInt(Integer.valueOf(this.playing ? 1 : 2));
        return this.response;
    }
}
