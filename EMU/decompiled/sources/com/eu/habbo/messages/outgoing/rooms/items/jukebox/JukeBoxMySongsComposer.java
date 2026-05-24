package com.eu.habbo.messages.outgoing.rooms.items.jukebox;

import com.eu.habbo.habbohotel.items.interactions.InteractionMusicDisc;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/items/jukebox/JukeBoxMySongsComposer.class */
public class JukeBoxMySongsComposer extends MessageComposer {
    private final List<InteractionMusicDisc> items;

    public JukeBoxMySongsComposer(List<InteractionMusicDisc> list) {
        this.items = list;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.JukeBoxMySongsComposer);
        this.response.appendInt(Integer.valueOf(this.items.size()));
        for (InteractionMusicDisc interactionMusicDisc : this.items) {
            this.response.appendInt(Integer.valueOf(interactionMusicDisc.getId()));
            this.response.appendInt(Integer.valueOf(interactionMusicDisc.getSongId()));
        }
        return this.response;
    }
}
