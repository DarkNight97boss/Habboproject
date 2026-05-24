package com.eu.habbo.messages.outgoing.rooms.items.jukebox;

import com.eu.habbo.habbohotel.items.SoundTrack;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/items/jukebox/JukeBoxPlayListAddSongComposer.class */
public class JukeBoxPlayListAddSongComposer extends MessageComposer {
    private final SoundTrack track;

    public JukeBoxPlayListAddSongComposer(SoundTrack soundTrack) {
        this.track = soundTrack;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.JukeBoxPlayListAddSongComposer);
        this.response.appendInt(Integer.valueOf(this.track.getId()));
        this.response.appendInt(Integer.valueOf(this.track.getLength() * Outgoing.CraftableProductsComposer));
        this.response.appendString(this.track.getCode());
        this.response.appendString(this.track.getAuthor());
        return this.response;
    }
}
