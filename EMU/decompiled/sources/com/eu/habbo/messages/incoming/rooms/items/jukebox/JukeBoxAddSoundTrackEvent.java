package com.eu.habbo.messages.incoming.rooms.items.jukebox;

import com.eu.habbo.habbohotel.items.interactions.InteractionMusicDisc;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/items/jukebox/JukeBoxAddSoundTrackEvent.class */
public class JukeBoxAddSoundTrackEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        if (this.client.getHabbo().getHabboInfo().getCurrentRoom().hasRights(this.client.getHabbo())) {
            int iIntValue = this.packet.readInt().intValue();
            this.packet.readInt().intValue();
            Habbo habbo = this.client.getHabbo();
            if (habbo != null) {
                HabboItem habboItem = habbo.getInventory().getItemsComponent().getHabboItem(iIntValue);
                if ((habboItem instanceof InteractionMusicDisc) && habboItem.getRoomId() == 0) {
                    this.client.getHabbo().getHabboInfo().getCurrentRoom().getTraxManager().addSong((InteractionMusicDisc) habboItem, habbo);
                }
            }
        }
    }
}
