package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/ArcturusCommand.class */
public class ArcturusCommand extends Command {
    public ArcturusCommand() {
        super(null, new String[]{"arcturus", "emulator"});
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (gameClient.getHabbo().getHabboInfo().getCurrentRoom() == null) {
            return true;
        }
        gameClient.getHabbo().whisper("This hotel is powered by Arcturus Emulator! \rCet hôtel est alimenté par Arcturus émulateur! \rDit hotel draait op Arcturus Emulator! \rEste hotel está propulsado por Arcturus emulador! \rHotellet drivs av Arcturus Emulator! \rDas Hotel gehört zu Arcturus Emulator betrieben!", RoomChatMessageBubbles.ALERT);
        return true;
    }
}
