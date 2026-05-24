package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.users.clothingvalidation.ClothingValidationManager;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserDataComposer;
import com.eu.habbo.messages.outgoing.users.UserDataComposer;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/InteractionMannequin.class */
public class InteractionMannequin extends HabboItem {
    public InteractionMannequin(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
    }

    public InteractionMannequin(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public int getMaximumRotations() {
        return 8;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void serializeExtradata(ServerMessage serverMessage) {
        serverMessage.appendInt(Integer.valueOf(1 + (isLimited() ? 256 : 0)));
        serverMessage.appendInt((Integer) 3);
        if (getExtradata().split(":").length >= 2) {
            String[] strArrSplit = getExtradata().split(":");
            serverMessage.appendString("GENDER");
            serverMessage.appendString(strArrSplit[0].toLowerCase());
            serverMessage.appendString("FIGURE");
            serverMessage.appendString(strArrSplit[1]);
            serverMessage.appendString("OUTFIT_NAME");
            serverMessage.appendString(strArrSplit.length >= 3 ? strArrSplit[2] : Emulator.PREVIEW);
        } else {
            serverMessage.appendString("GENDER");
            serverMessage.appendString("m");
            serverMessage.appendString("FIGURE");
            serverMessage.appendString(Emulator.PREVIEW);
            serverMessage.appendString("OUTFIT_NAME");
            serverMessage.appendString("My Look");
            setExtradata("m: :My look");
            needsUpdate(true);
            Emulator.getThreading().run(this);
        }
        super.serializeExtradata(serverMessage);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean isWalkable() {
        return false;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        String[] strArrSplit = getExtradata().split(":");
        if (strArrSplit.length < 2) {
            return;
        }
        String str = strArrSplit[0];
        String str2 = strArrSplit[1];
        if (str.isEmpty() || str2.isEmpty()) {
            return;
        }
        if ((str.equalsIgnoreCase("m") || str.equalsIgnoreCase("f")) && gameClient.getHabbo().getHabboInfo().getGender().name().equalsIgnoreCase(str)) {
            String str3 = Emulator.PREVIEW;
            for (String str4 : gameClient.getHabbo().getHabboInfo().getLook().split("\\.")) {
                if (!str4.startsWith("ch") && !str4.startsWith("lg")) {
                    str3 = str3 + str4 + ".";
                }
            }
            String strReplace = str2;
            for (String str5 : strReplace.split("\\.")) {
                if (str5.startsWith("hd")) {
                    strReplace = strReplace.replace(str5, Emulator.PREVIEW);
                }
            }
            if (strReplace.equals(Emulator.PREVIEW)) {
                return;
            }
            String str6 = str3 + strReplace;
            if (str6.length() > 512) {
                return;
            }
            gameClient.getHabbo().getHabboInfo().setLook(ClothingValidationManager.VALIDATE_ON_MANNEQUIN ? ClothingValidationManager.validateLook(gameClient.getHabbo(), str6, gameClient.getHabbo().getHabboInfo().getGender().name()) : str6);
            room.sendComposer(new RoomUserDataComposer(gameClient.getHabbo()).compose());
            gameClient.sendResponse(new UserDataComposer(gameClient.getHabbo()));
        }
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOff(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
    }
}
