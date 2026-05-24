package com.eu.habbo.messages.incoming.navigator;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.navigation.ListMode;
import com.eu.habbo.habbohotel.rooms.RoomCategory;
import com.eu.habbo.messages.incoming.MessageHandler;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/navigator/NavigatorCategoryListModeEvent.class */
public class NavigatorCategoryListModeEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        String string = this.packet.readString();
        int iIntValue = this.packet.readInt().intValue();
        RoomCategory category = Emulator.getGameEnvironment().getRoomManager().getCategory(string);
        this.client.getHabbo().getHabboStats().navigatorWindowSettings.setListMode(category != null ? category.getCaptionSave() : string, iIntValue == 1 ? ListMode.THUMBNAILS : ListMode.LIST);
    }
}
