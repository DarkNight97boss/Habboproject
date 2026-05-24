package com.eu.habbo.messages.incoming.rooms.items;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.interactions.InteractionCustomValues;
import com.eu.habbo.habbohotel.items.interactions.InteractionRoomAds;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import gnu.trove.map.hash.THashMap;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/items/AdvertisingSaveEvent.class */
public class AdvertisingSaveEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        HabboItem habboItem;
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom == null || !currentRoom.hasRights(this.client.getHabbo()) || (habboItem = currentRoom.getHabboItem(this.packet.readInt().intValue())) == null) {
            return;
        }
        if ((habboItem instanceof InteractionRoomAds) && !this.client.getHabbo().hasPermission("acc_ads_background")) {
            this.client.getHabbo().alert(Emulator.getTexts().getValue("hotel.error.roomads.nopermission"));
            return;
        }
        if (habboItem instanceof InteractionCustomValues) {
            THashMap<String, String> tHashMap = new THashMap<>(((InteractionCustomValues) habboItem).values);
            int iIntValue = this.packet.readInt().intValue();
            for (int i = 0; i < iIntValue / 2; i++) {
                String string = this.packet.readString();
                String string2 = this.packet.readString();
                if (!Emulator.getConfig().getBoolean("camera.use.https")) {
                    string2 = string2.replace("https://", "http://");
                }
                ((InteractionCustomValues) habboItem).values.put(string, string2);
            }
            habboItem.setExtradata(((InteractionCustomValues) habboItem).toExtraData());
            habboItem.needsUpdate(true);
            Emulator.getThreading().run(habboItem);
            currentRoom.updateItem(habboItem);
            ((InteractionCustomValues) habboItem).onCustomValuesSaved(currentRoom, this.client, tHashMap);
        }
    }
}
