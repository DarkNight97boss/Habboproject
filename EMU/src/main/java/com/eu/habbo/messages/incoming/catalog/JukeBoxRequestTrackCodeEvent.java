package com.eu.habbo.messages.incoming.catalog;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.SoundTrack;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.items.jukebox.JukeBoxTrackCodeComposer;

public class JukeBoxRequestTrackCodeEvent extends MessageHandler {
    @Override
    public void handle() throws Exception {
        // Song names map to in-game tracks via lookup. Hardware-bounded ID
        // is < 100 chars; cap the parse to defeat oversize allocation.
        String songName = this.packet.readString(128);

        final SoundTrack track = Emulator.getGameEnvironment().getItemManager().getSoundTrack(songName);

        if (track != null) {
            this.client.sendResponse(new JukeBoxTrackCodeComposer(track));
        }
    }
}
