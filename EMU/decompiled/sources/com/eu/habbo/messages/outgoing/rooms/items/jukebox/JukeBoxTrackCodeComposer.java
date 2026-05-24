package com.eu.habbo.messages.outgoing.rooms.items.jukebox;

import com.eu.habbo.habbohotel.items.SoundTrack;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/items/jukebox/JukeBoxTrackCodeComposer.class */
public class JukeBoxTrackCodeComposer extends MessageComposer {
    private final SoundTrack track;

    public JukeBoxTrackCodeComposer(SoundTrack soundTrack) {
        this.track = soundTrack;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.JukeBoxTrackCodeComposer);
        this.response.appendString(this.track.getCode());
        this.response.appendInt(Integer.valueOf(this.track.getId()));
        return this.response;
    }
}
