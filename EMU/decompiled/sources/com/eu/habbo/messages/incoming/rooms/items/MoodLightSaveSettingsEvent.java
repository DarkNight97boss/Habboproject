package com.eu.habbo.messages.incoming.rooms.items;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.interactions.InteractionMoodLight;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomMoodlightData;
import com.eu.habbo.habbohotel.rooms.RoomRightLevels;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.items.MoodLightDataComposer;
import gnu.trove.iterator.hash.TObjectHashIterator;
import java.util.Arrays;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/items/MoodLightSaveSettingsEvent.class */
public class MoodLightSaveSettingsEvent extends MessageHandler {
    public static List<String> MOODLIGHT_AVAILABLE_COLORS = Arrays.asList("#74F5F5,#0053F7,#E759DE,#EA4532,#F2F851,#82F349,#000000".split(","));
    public static int MIN_BRIGHTNESS = (int) Math.floor(76.5d);

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom.getGuildId() > 0 || !currentRoom.getGuildRightLevel(this.client.getHabbo()).isLessThan(RoomRightLevels.GUILD_RIGHTS) || currentRoom.hasRights(this.client.getHabbo())) {
            int iIntValue = this.packet.readInt().intValue();
            int iIntValue2 = this.packet.readInt().intValue();
            String string = this.packet.readString();
            int iIntValue3 = this.packet.readInt().intValue();
            boolean z = this.packet.readBoolean();
            if (Emulator.getConfig().getBoolean("moodlight.color_check.enabled", true) && !MOODLIGHT_AVAILABLE_COLORS.contains(string)) {
                ScripterManager.scripterDetected(this.client, "User tried to set a moodlight to a non-whitelisted color: " + string);
                return;
            }
            if (iIntValue3 > 255 || iIntValue3 < MIN_BRIGHTNESS) {
                ScripterManager.scripterDetected(this.client, "User tried to set a moodlight's brightness to out-of-bounds ([76, 255]): " + iIntValue3);
                return;
            }
            for (RoomMoodlightData roomMoodlightData : currentRoom.getMoodlightData().valueCollection()) {
                if (roomMoodlightData.getId() == iIntValue) {
                    roomMoodlightData.setBackgroundOnly(iIntValue2 == 2);
                    roomMoodlightData.setColor(string);
                    roomMoodlightData.setIntensity(iIntValue3);
                    if (z) {
                        roomMoodlightData.enable();
                    }
                    TObjectHashIterator it = currentRoom.getRoomSpecialTypes().getItemsOfType(InteractionMoodLight.class).iterator();
                    while (it.hasNext()) {
                        HabboItem habboItem = (HabboItem) it.next();
                        habboItem.setExtradata(roomMoodlightData.toString());
                        habboItem.needsUpdate(true);
                        currentRoom.updateItem(habboItem);
                        Emulator.getThreading().run(habboItem);
                    }
                } else if (z) {
                    roomMoodlightData.disable();
                }
            }
            currentRoom.setNeedsUpdate(true);
            this.client.sendResponse(new MoodLightDataComposer(currentRoom.getMoodlightData()));
        }
    }
}
