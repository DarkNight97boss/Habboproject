package com.eu.habbo.habbohotel.games.tag;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.games.Game;
import com.eu.habbo.habbohotel.games.GamePlayer;
import com.eu.habbo.habbohotel.games.GameTeam;
import com.eu.habbo.habbohotel.games.GameTeamColors;
import com.eu.habbo.habbohotel.items.interactions.games.tag.InteractionTagField;
import com.eu.habbo.habbohotel.items.interactions.games.tag.InteractionTagPole;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomLayout;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboGender;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.plugin.EventHandler;
import com.eu.habbo.plugin.events.roomunit.RoomUnitLookAtPointEvent;
import com.eu.habbo.plugin.events.users.UserTakeStepEvent;
import com.eu.habbo.threading.runnables.HabboItemNewState;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import java.util.Iterator;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/games/tag/TagGame.class */
public abstract class TagGame extends Game {
    public THashMap<Habbo, InteractionTagPole> taggers;

    public TagGame(Class<? extends GameTeam> cls, Class<? extends GamePlayer> cls2, Room room) {
        super(cls, cls2, room, false);
        this.taggers = new THashMap<>();
    }

    @EventHandler
    public static void onUserLookAtPoint(RoomUnitLookAtPointEvent roomUnitLookAtPointEvent) {
        Habbo habbo;
        TagGame tagGame;
        if (roomUnitLookAtPointEvent.room == null || roomUnitLookAtPointEvent.roomUnit == null || roomUnitLookAtPointEvent.location == null || !RoomLayout.tilesAdjecent(roomUnitLookAtPointEvent.roomUnit.getCurrentLocation(), roomUnitLookAtPointEvent.location) || (habbo = roomUnitLookAtPointEvent.room.getHabbo(roomUnitLookAtPointEvent.roomUnit)) == null || habbo.getHabboInfo().getCurrentGame() == null || !TagGame.class.isAssignableFrom(habbo.getHabboInfo().getCurrentGame()) || (tagGame = (TagGame) roomUnitLookAtPointEvent.room.getGame(habbo.getHabboInfo().getCurrentGame())) == null || !tagGame.isTagger(habbo)) {
            return;
        }
        TObjectHashIterator it = roomUnitLookAtPointEvent.room.getHabbosAt(roomUnitLookAtPointEvent.location).iterator();
        while (it.hasNext()) {
            Habbo habbo2 = (Habbo) it.next();
            if (habbo2 != habbo && habbo2.getHabboInfo().getCurrentGame() != null && habbo2.getHabboInfo().getCurrentGame() == habbo.getHabboInfo().getCurrentGame()) {
                tagGame.tagged(roomUnitLookAtPointEvent.room, habbo, habbo2);
                return;
            }
        }
    }

    @EventHandler
    public static void onUserWalkEvent(UserTakeStepEvent userTakeStepEvent) {
        if (userTakeStepEvent.habbo.getHabboInfo().getCurrentGame() == null || !TagGame.class.isAssignableFrom(userTakeStepEvent.habbo.getHabboInfo().getCurrentGame())) {
            return;
        }
        THashSet<HabboItem> itemsAt = userTakeStepEvent.habbo.getHabboInfo().getCurrentRoom().getItemsAt(userTakeStepEvent.toLocation);
        TagGame tagGame = (TagGame) userTakeStepEvent.habbo.getHabboInfo().getCurrentRoom().getGame(userTakeStepEvent.habbo.getHabboInfo().getCurrentGame());
        if (tagGame != null) {
            TObjectHashIterator it = itemsAt.iterator();
            while (it.hasNext()) {
                HabboItem habboItem = (HabboItem) it.next();
                if ((habboItem instanceof InteractionTagField) && ((InteractionTagField) habboItem).gameClazz == userTakeStepEvent.habbo.getHabboInfo().getCurrentGame()) {
                    if (tagGame.taggers.isEmpty()) {
                        tagGame.tagged(userTakeStepEvent.habbo.getHabboInfo().getCurrentRoom(), null, userTakeStepEvent.habbo);
                        return;
                    }
                    return;
                }
            }
            tagGame.removeHabbo(userTakeStepEvent.habbo);
        }
    }

    public abstract Class<? extends InteractionTagPole> getTagPole();

    public abstract int getMaleEffect();

    public abstract int getMaleTaggerEffect();

    public abstract int getFemaleEffect();

    public abstract int getFemaleTaggerEffect();

    public void tagged(Room room, Habbo habbo, Habbo habbo2) {
        if (this.taggers.containsKey(habbo2)) {
            return;
        }
        THashSet<HabboItem> itemsOfType = room.getRoomSpecialTypes().getItemsOfType(getTagPole());
        InteractionTagPole interactionTagPole = (InteractionTagPole) this.taggers.get(habbo);
        room.giveEffect(habbo2, getTaggedEffect(habbo2), -1);
        if (itemsOfType.size() > this.taggers.size()) {
            Iterator it = this.taggers.entrySet().iterator();
            while (it.hasNext()) {
                itemsOfType.remove(((Map.Entry) it.next()).getValue());
            }
            TObjectHashIterator it2 = itemsOfType.iterator();
            while (it2.hasNext()) {
                HabboItem habboItem = (HabboItem) it2.next();
                habbo2.getHabboInfo().getCurrentRoom().giveEffect(habbo2, getTaggedEffect(habbo2), -1);
                this.taggers.put(habbo2, (InteractionTagPole) habboItem);
            }
        } else {
            if (habbo != null) {
                room.giveEffect(habbo, getEffect(habbo), -1);
                this.taggers.remove(habbo);
            }
            this.taggers.put(habbo2, interactionTagPole);
        }
        if (interactionTagPole != null) {
            interactionTagPole.setExtradata("1");
            room.updateItemState(interactionTagPole);
            Emulator.getThreading().run(new HabboItemNewState(interactionTagPole, room, "0"), 1000L);
        }
    }

    @Override // com.eu.habbo.habbohotel.games.Game
    public synchronized boolean addHabbo(Habbo habbo, GameTeamColors gameTeamColors) {
        super.addHabbo(habbo, GameTeamColors.RED);
        if (getTagPole() != null) {
            THashSet<HabboItem> itemsOfType = habbo.getHabboInfo().getCurrentRoom().getRoomSpecialTypes().getItemsOfType(getTagPole());
            if (itemsOfType.size() > this.taggers.size()) {
                Iterator it = this.taggers.entrySet().iterator();
                while (it.hasNext()) {
                    itemsOfType.remove(((Map.Entry) it.next()).getValue());
                }
                TObjectHashIterator it2 = itemsOfType.iterator();
                if (it2.hasNext()) {
                    HabboItem habboItem = (HabboItem) it2.next();
                    habbo.getHabboInfo().getCurrentRoom().giveEffect(habbo, getEffect(habbo), -1);
                    this.room.scheduledTasks.add(() -> {
                        habbo.getHabboInfo().getCurrentRoom().giveEffect(habbo, getTaggedEffect(habbo), -1);
                    });
                    this.taggers.put(habbo, (InteractionTagPole) habboItem);
                    return true;
                }
            }
        } else if (this.taggers.isEmpty()) {
            habbo.getHabboInfo().getCurrentRoom().giveEffect(habbo, getEffect(habbo), -1);
            this.room.scheduledTasks.add(() -> {
                habbo.getHabboInfo().getCurrentRoom().giveEffect(habbo, getTaggedEffect(habbo), -1);
            });
            this.taggers.put(habbo, (Object) null);
            return true;
        }
        habbo.getHabboInfo().getCurrentRoom().giveEffect(habbo, getEffect(habbo), -1);
        return true;
    }

    @Override // com.eu.habbo.habbohotel.games.Game
    public synchronized void removeHabbo(Habbo habbo) {
        super.removeHabbo(habbo);
        this.taggers.remove(habbo);
        habbo.getHabboInfo().getCurrentRoom().giveEffect(habbo, 0, -1);
    }

    @Override // com.eu.habbo.habbohotel.games.Game
    public void initialise() {
    }

    @Override // com.eu.habbo.habbohotel.games.Game, java.lang.Runnable
    public void run() {
    }

    public int getEffect(Habbo habbo) {
        return habbo.getHabboInfo().getGender().equals(HabboGender.M) ? getMaleEffect() : getFemaleEffect();
    }

    public int getTaggedEffect(Habbo habbo) {
        return habbo.getHabboInfo().getGender().equals(HabboGender.M) ? getMaleTaggerEffect() : getFemaleTaggerEffect();
    }

    public boolean isTagger(Habbo habbo) {
        return this.taggers.containsKey(habbo);
    }
}
