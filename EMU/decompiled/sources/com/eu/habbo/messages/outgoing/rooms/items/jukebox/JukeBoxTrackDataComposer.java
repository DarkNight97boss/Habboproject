package com.eu.habbo.messages.outgoing.rooms.items.jukebox;

import com.eu.habbo.habbohotel.items.SoundTrack;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/items/jukebox/JukeBoxTrackDataComposer.class */
public class JukeBoxTrackDataComposer extends MessageComposer {
    private final List<SoundTrack> tracks;

    public JukeBoxTrackDataComposer(List<SoundTrack> list) {
        this.tracks = list;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(3365);
        this.response.appendInt(Integer.valueOf(this.tracks.size()));
        for (SoundTrack soundTrack : this.tracks) {
            this.response.appendInt(Integer.valueOf(soundTrack.getId()));
            this.response.appendString(soundTrack.getCode());
            this.response.appendString(soundTrack.getName());
            this.response.appendString(soundTrack.getData());
            this.response.appendInt(Integer.valueOf(soundTrack.getLength() * Outgoing.CraftableProductsComposer));
            this.response.appendString(soundTrack.getAuthor());
        }
        return this.response;
    }
}
