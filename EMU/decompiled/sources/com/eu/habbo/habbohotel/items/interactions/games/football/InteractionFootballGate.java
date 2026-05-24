package com.eu.habbo.habbohotel.items.interactions.games.football;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboGender;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.users.clothingvalidation.ClothingValidationManager;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserDataComposer;
import com.eu.habbo.messages.outgoing.users.UpdateUserLookComposer;
import com.eu.habbo.plugin.EventHandler;
import com.eu.habbo.plugin.events.users.UserDisconnectEvent;
import com.eu.habbo.plugin.events.users.UserExitRoomEvent;
import com.eu.habbo.plugin.events.users.UserSavedLookEvent;
import com.eu.habbo.util.figure.FigureUtil;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/games/football/InteractionFootballGate.class */
public class InteractionFootballGate extends HabboItem {
    private static final String CACHE_KEY = "fball_gate_look";
    private String figureM;
    private String figureF;

    public InteractionFootballGate(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        String[] strArrSplit = resultSet.getString("extra_data").split(";");
        this.figureM = strArrSplit.length > 0 ? strArrSplit[0] : Emulator.PREVIEW;
        this.figureF = strArrSplit.length > 1 ? strArrSplit[1] : Emulator.PREVIEW;
    }

    public InteractionFootballGate(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        String[] strArrSplit = str.split(";");
        this.figureM = strArrSplit.length > 0 ? strArrSplit[0] : Emulator.PREVIEW;
        this.figureF = strArrSplit.length > 1 ? strArrSplit[1] : Emulator.PREVIEW;
    }

    @EventHandler
    public static void onUserDisconnectEvent(UserDisconnectEvent userDisconnectEvent) {
        if (userDisconnectEvent.habbo != null) {
            removeLook(userDisconnectEvent.habbo);
        }
    }

    @EventHandler
    public static void onUserExitRoomEvent(UserExitRoomEvent userExitRoomEvent) {
        if (userExitRoomEvent.habbo != null) {
            removeLook(userExitRoomEvent.habbo);
        }
    }

    @EventHandler
    public static void onUserSavedLookEvent(UserSavedLookEvent userSavedLookEvent) {
        if (userSavedLookEvent.habbo != null) {
            removeLook(userSavedLookEvent.habbo);
        }
    }

    private static void removeLook(Habbo habbo) {
        if (habbo.getHabboStats().cache.containsKey(CACHE_KEY)) {
            habbo.getHabboInfo().setLook((String) habbo.getHabboStats().cache.get(CACHE_KEY));
            habbo.getHabboStats().cache.remove(CACHE_KEY);
            habbo.getClient().sendResponse(new UpdateUserLookComposer(habbo));
            if (habbo.getHabboInfo().getCurrentRoom() != null) {
                habbo.getHabboInfo().getCurrentRoom().sendComposer(new RoomUserDataComposer(habbo).compose());
            }
        }
    }

    public void setFigureM(String str) {
        this.figureM = str;
        setExtradata(this.figureM + ";" + this.figureF);
        needsUpdate(true);
        Emulator.getThreading().run(this);
    }

    public void setFigureF(String str) {
        this.figureF = str;
        setExtradata(this.figureM + ";" + this.figureF);
        needsUpdate(true);
        Emulator.getThreading().run(this);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void serializeExtradata(ServerMessage serverMessage) {
        serverMessage.appendInt(Integer.valueOf(isLimited() ? 256 : 0));
        serverMessage.appendString(this.figureM + "," + this.figureF);
        super.serializeExtradata(serverMessage);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean isWalkable() {
        return true;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        super.onWalkOn(roomUnit, room, objArr);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        Habbo habbo = room.getHabbo(roomUnit);
        if (habbo != null) {
            if (habbo.getHabboStats().cache.containsKey(CACHE_KEY)) {
                UserSavedLookEvent userSavedLookEvent = new UserSavedLookEvent(habbo, habbo.getHabboInfo().getGender(), (String) habbo.getHabboStats().cache.get(CACHE_KEY));
                Emulator.getPluginManager().fireEvent(userSavedLookEvent);
                if (!userSavedLookEvent.isCancelled()) {
                    habbo.getHabboInfo().setLook(ClothingValidationManager.VALIDATE_ON_FBALLGATE ? ClothingValidationManager.validateLook(habbo, userSavedLookEvent.newLook, userSavedLookEvent.gender.name()) : userSavedLookEvent.newLook);
                    Emulator.getThreading().run(habbo.getHabboInfo());
                    habbo.getClient().sendResponse(new UpdateUserLookComposer(habbo));
                    room.sendComposer(new RoomUserDataComposer(habbo).compose());
                }
                habbo.getHabboStats().cache.remove(CACHE_KEY);
            } else {
                UserSavedLookEvent userSavedLookEvent2 = new UserSavedLookEvent(habbo, habbo.getHabboInfo().getGender(), FigureUtil.mergeFigures(habbo.getHabboInfo().getLook(), habbo.getHabboInfo().getGender() == HabboGender.F ? this.figureF : this.figureM, new String[]{"hd", "hr", "ha", "he", "ea", "fa"}, new String[]{"ch", "ca", "cc", "cp", "lg", "wa", "sh"}));
                Emulator.getPluginManager().fireEvent(userSavedLookEvent2);
                if (!userSavedLookEvent2.isCancelled()) {
                    habbo.getHabboStats().cache.put(CACHE_KEY, habbo.getHabboInfo().getLook());
                    habbo.getHabboInfo().setLook(ClothingValidationManager.VALIDATE_ON_FBALLGATE ? ClothingValidationManager.validateLook(habbo, userSavedLookEvent2.newLook, userSavedLookEvent2.gender.name()) : userSavedLookEvent2.newLook);
                    Emulator.getThreading().run(habbo.getHabboInfo());
                    habbo.getClient().sendResponse(new UpdateUserLookComposer(habbo));
                    room.sendComposer(new RoomUserDataComposer(habbo).compose());
                }
            }
        }
        super.onWalkOn(roomUnit, room, objArr);
    }
}
