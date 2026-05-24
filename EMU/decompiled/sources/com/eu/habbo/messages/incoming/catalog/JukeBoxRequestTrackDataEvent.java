package com.eu.habbo.messages.incoming.catalog;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.SoundTrack;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.items.jukebox.JukeBoxTrackDataComposer;
import java.util.ArrayList;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/catalog/JukeBoxRequestTrackDataEvent.class */
public class JukeBoxRequestTrackDataEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        ArrayList arrayList = new ArrayList(iIntValue);
        for (int i = 0; i < iIntValue; i++) {
            SoundTrack soundTrack = Emulator.getGameEnvironment().getItemManager().getSoundTrack(this.packet.readInt().intValue());
            if (soundTrack != null) {
                arrayList.add(soundTrack);
            }
        }
        this.client.sendResponse(new JukeBoxTrackDataComposer(arrayList));
    }
}
