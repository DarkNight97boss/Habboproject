package com.eu.habbo.messages.incoming.rooms;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomState;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.RoomChatSettingsComposer;
import com.eu.habbo.messages.outgoing.rooms.RoomEditSettingsErrorComposer;
import com.eu.habbo.messages.outgoing.rooms.RoomSettingsSavedComposer;
import com.eu.habbo.messages.outgoing.rooms.RoomSettingsUpdatedComposer;
import com.eu.habbo.messages.outgoing.rooms.RoomThicknessComposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/RoomSettingsSaveEvent.class */
public class RoomSettingsSaveEvent extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoomSettingsSaveEvent.class);

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(this.packet.readInt().intValue());
        if (room == null || !room.isOwner(this.client.getHabbo())) {
            return;
        }
        String string = this.packet.readString();
        if (string.trim().isEmpty() || string.length() > 60) {
            this.client.sendResponse(new RoomEditSettingsErrorComposer(room.getId(), 7, Emulator.PREVIEW));
            return;
        }
        if (!Emulator.getGameEnvironment().getWordFilter().filter(string, this.client.getHabbo()).equals(string)) {
            this.client.sendResponse(new RoomEditSettingsErrorComposer(room.getId(), 8, Emulator.PREVIEW));
            return;
        }
        String string2 = this.packet.readString();
        if (string2.length() > 255) {
            return;
        }
        if (!Emulator.getGameEnvironment().getWordFilter().filter(string2, this.client.getHabbo()).equals(string2)) {
            this.client.sendResponse(new RoomEditSettingsErrorComposer(room.getId(), 10, Emulator.PREVIEW));
            return;
        }
        RoomState roomState = RoomState.values()[this.packet.readInt().intValue() % RoomState.values().length];
        String string3 = this.packet.readString();
        if (roomState == RoomState.PASSWORD && string3.isEmpty() && (room.getPassword() == null || room.getPassword().isEmpty())) {
            this.client.sendResponse(new RoomEditSettingsErrorComposer(room.getId(), 5, Emulator.PREVIEW));
            return;
        }
        int iIntValue = this.packet.readInt().intValue();
        int iIntValue2 = this.packet.readInt().intValue();
        StringBuilder sb = new StringBuilder();
        int iMin = Math.min(this.packet.readInt().intValue(), 2);
        for (int i = 0; i < iMin; i++) {
            String string4 = this.packet.readString();
            if (string4.length() > 15) {
                this.client.sendResponse(new RoomEditSettingsErrorComposer(room.getId(), 13, Emulator.PREVIEW));
                return;
            }
            sb.append(string4).append(";");
        }
        if (!Emulator.getGameEnvironment().getWordFilter().filter(sb.toString(), this.client.getHabbo()).equals(sb.toString())) {
            this.client.sendResponse(new RoomEditSettingsErrorComposer(room.getId(), 11, Emulator.PREVIEW));
            return;
        }
        if (sb.length() > 0) {
            for (String str : Emulator.getConfig().getValue("hotel.room.tags.staff").split(";")) {
                if (sb.toString().contains(str)) {
                    this.client.sendResponse(new RoomEditSettingsErrorComposer(room.getId(), 12, "1"));
                    return;
                }
            }
        }
        room.setName(string);
        room.setDescription(string2);
        room.setState(roomState);
        if (!string3.isEmpty()) {
            room.setPassword(string3);
        }
        room.setUsersMax(iIntValue);
        if (Emulator.getGameEnvironment().getRoomManager().hasCategory(iIntValue2, this.client.getHabbo())) {
            room.setCategory(iIntValue2);
        } else {
            String strReplace = Emulator.getGameEnvironment().getRoomManager().getCategory(iIntValue2) == null ? Emulator.getTexts().getValue("scripter.warning.roomsettings.category.nonexisting").replace("%username%", this.client.getHabbo().getHabboInfo().getUsername()) : Emulator.getTexts().getValue("scripter.warning.roomsettings.category.permission").replace("%username%", this.client.getHabbo().getHabboInfo().getUsername()).replace("%category%", Emulator.getGameEnvironment().getRoomManager().getCategory(iIntValue2) + Emulator.PREVIEW);
            ScripterManager.scripterDetected(this.client, strReplace);
            LOGGER.info(strReplace);
        }
        room.setTags(sb.toString());
        room.setTradeMode(this.packet.readInt().intValue());
        room.setAllowPets(this.packet.readBoolean());
        room.setAllowPetsEat(this.packet.readBoolean());
        room.setAllowWalkthrough(this.packet.readBoolean());
        room.setHideWall(this.packet.readBoolean());
        room.setWallSize(this.packet.readInt().intValue());
        room.setFloorSize(this.packet.readInt().intValue());
        room.setMuteOption(this.packet.readInt().intValue());
        room.setKickOption(this.packet.readInt().intValue());
        room.setBanOption(this.packet.readInt().intValue());
        room.setChatMode(this.packet.readInt().intValue());
        room.setChatWeight(this.packet.readInt().intValue());
        room.setChatSpeed(this.packet.readInt().intValue());
        room.setChatDistance(Math.abs(this.packet.readInt().intValue()));
        room.setChatProtection(this.packet.readInt().intValue());
        room.setNeedsUpdate(true);
        room.sendComposer(new RoomThicknessComposer(room).compose());
        room.sendComposer(new RoomChatSettingsComposer(room).compose());
        room.sendComposer(new RoomSettingsUpdatedComposer(room).compose());
        this.client.sendResponse(new RoomSettingsSavedComposer(room));
    }
}
