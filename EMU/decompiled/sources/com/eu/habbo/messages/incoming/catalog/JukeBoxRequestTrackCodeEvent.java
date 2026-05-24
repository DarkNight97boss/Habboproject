package com.eu.habbo.messages.incoming.catalog;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.SoundTrack;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.items.jukebox.JukeBoxTrackCodeComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/catalog/JukeBoxRequestTrackCodeEvent.class */
public class JukeBoxRequestTrackCodeEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        SoundTrack soundTrack = Emulator.getGameEnvironment().getItemManager().getSoundTrack(this.packet.readString());
        if (soundTrack != null) {
            this.client.sendResponse(new JukeBoxTrackCodeComposer(soundTrack));
        }
    }
}
