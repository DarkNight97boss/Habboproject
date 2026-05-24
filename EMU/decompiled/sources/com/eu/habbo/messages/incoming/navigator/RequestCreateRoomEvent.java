package com.eu.habbo.messages.incoming.navigator;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomCategory;
import com.eu.habbo.habbohotel.rooms.RoomManager;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.navigator.CanCreateRoomComposer;
import com.eu.habbo.messages.outgoing.navigator.RoomCreatedComposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/navigator/RequestCreateRoomEvent.class */
public class RequestCreateRoomEvent extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestCreateRoomEvent.class);

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        String string = this.packet.readString();
        String string2 = this.packet.readString();
        String string3 = this.packet.readString();
        int iIntValue = this.packet.readInt().intValue();
        int iIntValue2 = this.packet.readInt().intValue();
        int iIntValue3 = this.packet.readInt().intValue();
        if (!Emulator.getGameEnvironment().getRoomManager().layoutExists(string3)) {
            LOGGER.error("[SCRIPTER] Incorrect layout name \"" + string3 + "\". " + this.client.getHabbo().getHabboInfo().getUsername());
            return;
        }
        RoomCategory category = Emulator.getGameEnvironment().getRoomManager().getCategory(iIntValue);
        if (category == null || category.getMinRank() > this.client.getHabbo().getHabboInfo().getRank().getId()) {
            LOGGER.error("[SCRIPTER] Incorrect rank or non existing category ID: \"" + iIntValue + "\"." + this.client.getHabbo().getHabboInfo().getUsername());
            return;
        }
        if (iIntValue2 <= 250 && iIntValue3 <= 2 && string.trim().length() >= 3 && string.length() <= 25 && Emulator.getGameEnvironment().getWordFilter().filter(string, this.client.getHabbo()).equals(string) && string2.length() <= 128 && Emulator.getGameEnvironment().getWordFilter().filter(string2, this.client.getHabbo()).equals(string2)) {
            int size = Emulator.getGameEnvironment().getRoomManager().getRoomsForHabbo(this.client.getHabbo()).size();
            int i = this.client.getHabbo().getHabboStats().hasActiveClub() ? RoomManager.MAXIMUM_ROOMS_HC : RoomManager.MAXIMUM_ROOMS_USER;
            if (size >= i) {
                this.client.sendResponse(new CanCreateRoomComposer(size, i));
                return;
            }
            Room roomCreateRoomForHabbo = Emulator.getGameEnvironment().getRoomManager().createRoomForHabbo(this.client.getHabbo(), string, string2, string3, iIntValue2, iIntValue, iIntValue3);
            if (roomCreateRoomForHabbo != null) {
                this.client.sendResponse(new RoomCreatedComposer(roomCreateRoomForHabbo));
            }
        }
    }
}
