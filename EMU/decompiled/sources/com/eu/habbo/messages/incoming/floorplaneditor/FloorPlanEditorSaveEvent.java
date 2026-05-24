package com.eu.habbo.messages.incoming.floorplaneditor;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.CustomRoomLayout;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomLayout;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomTileState;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertKeys;
import com.eu.habbo.messages.outgoing.generic.alerts.GenericAlertComposer;
import com.eu.habbo.messages.outgoing.rooms.ForwardToRoomComposer;
import gnu.trove.set.hash.THashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.StringJoiner;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/floorplaneditor/FloorPlanEditorSaveEvent.class */
public class FloorPlanEditorSaveEvent extends MessageHandler {
    public static int MAXIMUM_FLOORPLAN_WIDTH_LENGTH = 64;
    public static int MAXIMUM_FLOORPLAN_SIZE = 4096;

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        if (!this.client.getHabbo().hasPermission(Permission.ACC_FLOORPLAN_EDITOR)) {
            this.client.sendResponse(new GenericAlertComposer(Emulator.getTexts().getValue("floorplan.permission")));
            return;
        }
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom == null) {
            return;
        }
        if (currentRoom.getOwnerId() == this.client.getHabbo().getHabboInfo().getId() || this.client.getHabbo().hasPermission(Permission.ACC_ANYROOMOWNER)) {
            StringJoiner stringJoiner = new StringJoiner("<br />");
            String strReplace = this.packet.readString().replace("X", "x");
            String[] strArrSplit = strReplace.split("\r");
            int length = strArrSplit[0].length();
            if (Emulator.getConfig().getBoolean("hotel.room.floorplan.check.enabled")) {
                if (!strReplace.matches("[a-zA-Z0-9\r]+")) {
                    stringJoiner.add("${notification.floorplan_editor.error.title}");
                }
                Arrays.stream(strArrSplit).filter(str -> {
                    return str.length() != length;
                }).findAny().ifPresent(str2 -> {
                    stringJoiner.add("(General): Line " + (Arrays.asList(strArrSplit).indexOf(str2) + 1) + " is of different length than line 1");
                });
                if (strReplace.isEmpty() || strReplace.replace("x", Emulator.PREVIEW).replace("\r", Emulator.PREVIEW).isEmpty()) {
                    stringJoiner.add("${notification.floorplan_editor.error.message.effective_height_is_0}");
                }
                if (strReplace.length() > MAXIMUM_FLOORPLAN_SIZE) {
                    stringJoiner.add("${notification.floorplan_editor.error.message.too_large_area}");
                }
                if (strArrSplit.length > MAXIMUM_FLOORPLAN_WIDTH_LENGTH) {
                    stringJoiner.add("${notification.floorplan_editor.error.message.too_large_height}");
                } else if (Arrays.stream(strArrSplit).anyMatch(str3 -> {
                    return str3.length() > MAXIMUM_FLOORPLAN_WIDTH_LENGTH || str3.length() == 0;
                })) {
                    stringJoiner.add("${notification.floorplan_editor.error.message.too_large_width}");
                }
                if (stringJoiner.length() > 0) {
                    this.client.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FLOORPLAN_EDITOR_ERROR.key, stringJoiner.toString()));
                    return;
                }
            }
            int iIntValue = this.packet.readInt().intValue();
            int iIntValue2 = this.packet.readInt().intValue();
            if (iIntValue < 0 || iIntValue > length || iIntValue2 < 0 || iIntValue2 >= strArrSplit.length) {
                stringJoiner.add("${notification.floorplan_editor.error.message.entry_tile_outside_map}");
            }
            if (iIntValue2 < strArrSplit.length && iIntValue < strArrSplit[iIntValue2].length() && strArrSplit[iIntValue2].charAt(iIntValue) == 'x') {
                stringJoiner.add("${notification.floorplan_editor.error.message.entry_not_on_tile}");
            }
            int iIntValue3 = this.packet.readInt().intValue();
            if (iIntValue3 < 0 || iIntValue3 > 7) {
                stringJoiner.add("${notification.floorplan_editor.error.message.invalid_entry_tile_direction}");
            }
            int iIntValue4 = this.packet.readInt().intValue();
            if (iIntValue4 < -2 || iIntValue4 > 1) {
                stringJoiner.add("${notification.floorplan_editor.error.message.invalid_wall_thickness}");
            }
            int iIntValue5 = this.packet.readInt().intValue();
            if (iIntValue5 < -2 || iIntValue5 > 1) {
                stringJoiner.add("${notification.floorplan_editor.error.message.invalid_floor_thickness}");
            }
            int iIntValue6 = this.packet.bytesAvailable() >= 4 ? this.packet.readInt().intValue() : -1;
            if (iIntValue6 < -1 || iIntValue6 > 15) {
                stringJoiner.add("${notification.floorplan_editor.error.message.invalid_walls_fixed_height}");
            }
            THashSet<RoomTile> lockedTiles = currentRoom.getLockedTiles();
            THashSet tHashSet = new THashSet();
            int i = 0;
            loop0: while (true) {
                if (i >= strArrSplit.length) {
                    break;
                }
                for (int i2 = 0; i2 < length; i2++) {
                    RoomTile tile = currentRoom.getLayout().getTile((short) i2, (short) i);
                    tHashSet.add(tile);
                    String strValueOf = String.valueOf(strArrSplit[i].charAt(i2));
                    if (strValueOf.equalsIgnoreCase("x") && currentRoom.getTopItemAt(i2, i) != null) {
                        stringJoiner.add("${notification.floorplan_editor.error.message.change_blocked_by_room_item}");
                        break loop0;
                    }
                    short s = strValueOf.isEmpty() ? (short) 0 : Emulator.isNumeric(strValueOf) ? Short.parseShort(strValueOf) : (short) (10 + "ABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(strValueOf.toUpperCase()));
                    if (tile != null && tile.state != RoomTileState.INVALID && s != tile.z && currentRoom.getTopItemAt(i2, i) != null) {
                        stringJoiner.add("${notification.floorplan_editor.error.message.change_blocked_by_room_item}");
                        break loop0;
                    }
                }
                i++;
            }
            lockedTiles.removeAll(tHashSet);
            if (!lockedTiles.isEmpty()) {
                stringJoiner.add("${notification.floorplan_editor.error.message.change_blocked_by_room_item}");
            }
            if (stringJoiner.length() > 0) {
                this.client.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FLOORPLAN_EDITOR_ERROR.key, stringJoiner.toString()));
                return;
            }
            RoomLayout layout = currentRoom.getLayout();
            if (layout instanceof CustomRoomLayout) {
                layout.setDoorX((short) iIntValue);
                layout.setDoorY((short) iIntValue2);
                layout.setDoorDirection(iIntValue3);
                layout.setHeightmap(strReplace);
                layout.parse();
                if (layout.getDoorTile() == null) {
                    this.client.getHabbo().alert("Error");
                    ((CustomRoomLayout) layout).needsUpdate(false);
                    Emulator.getGameEnvironment().getRoomManager().unloadRoom(currentRoom);
                    return;
                }
                ((CustomRoomLayout) layout).needsUpdate(true);
                Emulator.getThreading().run((CustomRoomLayout) layout);
            } else {
                layout = Emulator.getGameEnvironment().getRoomManager().insertCustomLayout(currentRoom, strReplace, iIntValue, iIntValue2, iIntValue3);
            }
            if (layout != null) {
                currentRoom.setHasCustomLayout(true);
                currentRoom.setNeedsUpdate(true);
                currentRoom.setLayout(layout);
                currentRoom.setWallSize(iIntValue4);
                currentRoom.setFloorSize(iIntValue5);
                currentRoom.setWallHeight(iIntValue6);
                currentRoom.save();
                ArrayList arrayList = new ArrayList(currentRoom.getUserCount());
                arrayList.addAll(currentRoom.getHabbos());
                Emulator.getGameEnvironment().getRoomManager().unloadRoom(currentRoom);
                ServerMessage serverMessageCompose = new ForwardToRoomComposer(Emulator.getGameEnvironment().getRoomManager().loadRoom(currentRoom.getId()).getId()).compose();
                Iterator it = arrayList.iterator();
                while (it.hasNext()) {
                    ((Habbo) it.next()).getClient().sendResponse(serverMessageCompose);
                }
            }
        }
    }
}
