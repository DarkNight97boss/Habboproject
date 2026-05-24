package com.eu.habbo.messages.incoming.rooms.items.jukebox;

import com.eu.habbo.habbohotel.items.interactions.InteractionMusicDisc;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/items/jukebox/JukeBoxRemoveSoundTrackEvent.class */
public class JukeBoxRemoveSoundTrackEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        InteractionMusicDisc interactionMusicDisc = this.client.getHabbo().getHabboInfo().getCurrentRoom().getTraxManager().getSongs().get(this.packet.readInt().intValue());
        if (interactionMusicDisc != null) {
            this.client.getHabbo().getHabboInfo().getCurrentRoom().getTraxManager().removeSong(interactionMusicDisc.getId());
        }
    }
}
