package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.interactions.InteractionYoutubeTV;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.outgoing.Outgoing;
import com.eu.habbo.messages.outgoing.rooms.items.youtube.YoutubeVideoComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/YoutubeAdvanceVideo.class */
public class YoutubeAdvanceVideo implements Runnable {
    private final InteractionYoutubeTV tv;

    public YoutubeAdvanceVideo(InteractionYoutubeTV interactionYoutubeTV) {
        this.tv = interactionYoutubeTV;
    }

    @Override // java.lang.Runnable
    public void run() {
        Room room;
        if (this.tv.autoAdvance == null || (room = Emulator.getGameEnvironment().getRoomManager().getRoom(this.tv.getRoomId())) == null) {
            return;
        }
        int iIndexOf = this.tv.currentPlaylist.getVideos().indexOf(this.tv.currentVideo) + 1;
        if (iIndexOf >= this.tv.currentPlaylist.getVideos().size()) {
            iIndexOf = 0;
        }
        this.tv.currentVideo = this.tv.currentPlaylist.getVideos().get(iIndexOf);
        this.tv.startedWatchingAt = Emulator.getIntUnixTimestamp();
        this.tv.offset = 0;
        room.updateItem(this.tv);
        room.sendComposer(new YoutubeVideoComposer(this.tv.getId(), this.tv.currentVideo, true, 0).compose());
        this.tv.autoAdvance = Emulator.getThreading().run(new YoutubeAdvanceVideo(this.tv), this.tv.currentVideo.getDuration() * Outgoing.CraftableProductsComposer);
    }
}
