package com.eu.habbo.messages.incoming.rooms.items;

import com.eu.habbo.habbohotel.items.interactions.games.football.InteractionFootballGate;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.Incoming;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.items.rentablespaces.RentableSpaceInfoComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/items/FootballGateSaveLookEvent.class */
public class FootballGateSaveLookEvent extends MessageHandler {
    /* JADX WARN: Failed to restore switch over string. Please report as a decompilation issue */
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom == null || this.client.getHabbo().getHabboInfo().getId() != currentRoom.getOwnerId()) {
            return;
        }
        HabboItem habboItem = currentRoom.getHabboItem(this.packet.readInt().intValue());
        if (habboItem instanceof InteractionFootballGate) {
            String string = this.packet.readString();
            String string2 = this.packet.readString();
            String lowerCase = string.toLowerCase();
            byte b = -1;
            switch (lowerCase.hashCode()) {
                case RentableSpaceInfoComposer.SPACE_EXTEND_NOT_RENTED_BY_YOU /* 102 */:
                    if (lowerCase.equals("f")) {
                        b = 2;
                    }
                    break;
                case Incoming.GetPollDataEvent /* 109 */:
                    if (lowerCase.equals("m")) {
                        b = 1;
                    }
                    break;
            }
            switch (b) {
                case 1:
                default:
                    ((InteractionFootballGate) habboItem).setFigureM(string2);
                    currentRoom.updateItem(habboItem);
                    break;
                case 2:
                    ((InteractionFootballGate) habboItem).setFigureF(string2);
                    currentRoom.updateItem(habboItem);
                    break;
            }
        }
    }
}
