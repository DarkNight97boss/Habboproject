package com.eu.habbo.habbohotel.items.interactions.games.freeze;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.games.GameState;
import com.eu.habbo.habbohotel.games.freeze.FreezeGame;
import com.eu.habbo.habbohotel.games.freeze.FreezeGamePlayer;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.iterator.hash.TObjectHashIterator;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/games/freeze/InteractionFreezeBlock.class */
public class InteractionFreezeBlock extends HabboItem {
    public InteractionFreezeBlock(ResultSet resultSet, Item item) throws SQLException {
        super(resultSet, item);
        setExtradata("0");
    }

    public InteractionFreezeBlock(int i, int i2, Item item, String str, int i3, int i4) {
        super(i, i2, item, str, i3, i4);
        setExtradata("0");
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onClick(GameClient gameClient, Room room, Object[] objArr) throws Exception {
        FreezeGame freezeGame;
        if (gameClient == null) {
            return;
        }
        HabboItem habboItem = null;
        TObjectHashIterator it = room.getItemsAt(room.getLayout().getTile(getX(), getY())).iterator();
        while (it.hasNext()) {
            HabboItem habboItem2 = (HabboItem) it.next();
            if ((habboItem2 instanceof InteractionFreezeTile) && (habboItem == null || habboItem2.getZ() <= habboItem.getZ())) {
                habboItem = habboItem2;
            }
        }
        if (habboItem == null || (freezeGame = (FreezeGame) room.getGame(FreezeGame.class)) == null) {
            return;
        }
        freezeGame.throwBall(gameClient.getHabbo(), (InteractionFreezeTile) habboItem);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void serializeExtradata(ServerMessage serverMessage) {
        if (getExtradata().length() == 0) {
            setExtradata("0");
        }
        serverMessage.appendInt(Integer.valueOf(isLimited() ? 256 : 0));
        serverMessage.appendString(getExtradata());
        super.serializeExtradata(serverMessage);
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) {
        return isWalkable();
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public boolean isWalkable() {
        return (getExtradata().isEmpty() || getExtradata().equals("0")) ? false : true;
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem, com.eu.habbo.habbohotel.items.IEventTriggers
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objArr) throws Exception {
        FreezeGame freezeGame;
        Habbo habbo;
        FreezeGamePlayer freezeGamePlayer;
        int iIntValue;
        super.onWalkOn(roomUnit, room, objArr);
        if (getExtradata().isEmpty() || getExtradata().equalsIgnoreCase("0") || (freezeGame = (FreezeGame) room.getGame(FreezeGame.class)) == null || !freezeGame.state.equals(GameState.RUNNING) || (habbo = room.getHabbo(roomUnit)) == null || habbo.getHabboInfo().getCurrentGame() != FreezeGame.class || (freezeGamePlayer = (FreezeGamePlayer) habbo.getHabboInfo().getGamePlayer()) == null) {
            return;
        }
        try {
            iIntValue = Integer.valueOf(getExtradata()).intValue() / Outgoing.CraftableProductsComposer;
        } catch (NumberFormatException e) {
            iIntValue = 0;
        }
        if (iIntValue < 2 || iIntValue > 7) {
            return;
        }
        if (iIntValue != 6 || freezeGamePlayer.canPickupLife()) {
            setExtradata(((iIntValue + 10) * Outgoing.CraftableProductsComposer) + Emulator.PREVIEW);
            room.updateItem(this);
            freezeGame.givePowerUp(freezeGamePlayer, iIntValue);
            AchievementManager.progressAchievement(freezeGamePlayer.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("FreezePowerUp"));
        }
    }

    @Override // com.eu.habbo.habbohotel.users.HabboItem
    public void onPickUp(Room room) {
        setExtradata("0");
    }
}
