package com.eu.habbo.messages.outgoing.rooms.items.jukebox;

import com.eu.habbo.habbohotel.items.interactions.InteractionMusicDisc;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/items/jukebox/JukeBoxPlayListComposer.class */
public class JukeBoxPlayListComposer extends MessageComposer {
    private final List<InteractionMusicDisc> songs;
    private final int totalLength;

    public JukeBoxPlayListComposer(List<InteractionMusicDisc> list, int i) {
        this.songs = list;
        this.totalLength = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(34);
        this.response.appendInt(Integer.valueOf(this.totalLength));
        this.response.appendInt(Integer.valueOf(this.songs.size()));
        for (InteractionMusicDisc interactionMusicDisc : this.songs) {
            this.response.appendInt(Integer.valueOf(interactionMusicDisc.getId()));
            this.response.appendInt(Integer.valueOf(interactionMusicDisc.getSongId()));
        }
        return this.response;
    }
}
