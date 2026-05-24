package com.eu.habbo.messages.outgoing.rooms.items.jukebox;

import com.eu.habbo.habbohotel.items.SoundTrack;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/items/jukebox/JukeBoxNowPlayingMessageComposer.class */
public class JukeBoxNowPlayingMessageComposer extends MessageComposer {
    private final SoundTrack track;
    private final int playListId;
    private final int msPlayed;

    public JukeBoxNowPlayingMessageComposer(SoundTrack soundTrack, int i, int i2) {
        this.track = soundTrack;
        this.playListId = i;
        this.msPlayed = i2;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.JukeBoxNowPlayingMessageComposer);
        if (this.track != null) {
            this.response.appendInt(Integer.valueOf(this.track.getId()));
            this.response.appendInt(Integer.valueOf(this.playListId));
            this.response.appendInt(Integer.valueOf(this.track.getId()));
            this.response.appendInt(Integer.valueOf(this.track.getLength()));
            this.response.appendInt(Integer.valueOf(this.msPlayed));
        } else {
            this.response.appendInt((Integer) (-1));
            this.response.appendInt((Integer) (-1));
            this.response.appendInt((Integer) (-1));
            this.response.appendInt((Integer) (-1));
            this.response.appendInt((Integer) (-1));
        }
        return this.response;
    }
}
