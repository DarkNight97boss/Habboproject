package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.core.RoomUserPetComposer;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.pets.PetData;
import com.eu.habbo.habbohotel.rooms.RoomUnitType;
import com.eu.habbo.messages.outgoing.generic.alerts.MessagesForYouComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserRemoveComposer;
import java.util.ArrayList;
import java.util.Collections;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/TransformCommand.class */
public class TransformCommand extends Command {
    protected TransformCommand() {
        super("cmd_transform", new String[]{"transform"});
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length == 1) {
            StringBuilder sb = new StringBuilder();
            sb.append(Emulator.getTexts().getValue("commands.generic.cmd_transform.title"));
            sb.append("\r------------------------------------------------------------------------------\r");
            ArrayList<PetData> arrayList = new ArrayList(Emulator.getGameEnvironment().getPetManager().getPetData());
            Collections.sort(arrayList);
            String value = Emulator.getTexts().getValue("commands.generic.cmd_transform.line");
            for (PetData petData : arrayList) {
                sb.append(value.replace("%id%", petData.getType() + Emulator.PREVIEW).replace("%name%", petData.getName())).append("\r");
            }
            gameClient.sendResponse(new MessagesForYouComposer(new String[]{sb.toString()}));
            return true;
        }
        PetData petData2 = Emulator.getGameEnvironment().getPetManager().getPetData(strArr[1]);
        int iIntValue = 0;
        if (strArr.length >= 3) {
            try {
                iIntValue = Integer.valueOf(strArr[2]).intValue();
            } catch (Exception e) {
                return true;
            }
        }
        String str = strArr.length >= 4 ? strArr[3] : "FFFFFF";
        if (petData2 == null) {
            return true;
        }
        gameClient.getHabbo().getRoomUnit().setRoomUnitType(RoomUnitType.PET);
        gameClient.getHabbo().getHabboStats().cache.put("pet_type", petData2);
        gameClient.getHabbo().getHabboStats().cache.put("pet_race", Integer.valueOf(iIntValue));
        gameClient.getHabbo().getHabboStats().cache.put("pet_color", str);
        gameClient.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new RoomUserRemoveComposer(gameClient.getHabbo().getRoomUnit()).compose());
        gameClient.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new RoomUserPetComposer(petData2.getType(), iIntValue, str, gameClient.getHabbo()).compose());
        return true;
    }
}
