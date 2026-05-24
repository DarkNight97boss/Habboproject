package com.eu.habbo.messages.incoming.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.HabboManager;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.users.ChangeNameCheckResultComposer;
import java.util.ArrayList;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/users/ChangeNameCheckUsernameEvent.class */
public class ChangeNameCheckUsernameEvent extends MessageHandler {
    public static String VALID_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_-=!?@:,.";

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        if (this.client.getHabbo().getHabboStats().allowNameChange) {
            String string = this.packet.readString();
            int i = 0;
            ArrayList arrayList = new ArrayList(4);
            if (string.length() < 3) {
                i = 2;
            } else if (string.length() > 15) {
                i = 3;
            } else if (string.equalsIgnoreCase(this.client.getHabbo().getHabboInfo().getUsername()) || HabboManager.getOfflineHabboInfo(string) != null || ConfirmChangeNameEvent.changingUsernames.contains(string.toLowerCase())) {
                i = 5;
                arrayList.add(string + Emulator.getRandom().nextInt(9999));
                arrayList.add(string + Emulator.getRandom().nextInt(9999));
                arrayList.add(string + Emulator.getRandom().nextInt(9999));
                arrayList.add(string + Emulator.getRandom().nextInt(9999));
            } else if (Emulator.getGameEnvironment().getWordFilter().filter(string, this.client.getHabbo()).equalsIgnoreCase(string)) {
                String strReplace = string;
                for (char c : VALID_CHARACTERS.toCharArray()) {
                    strReplace = strReplace.replace(c + Emulator.PREVIEW, Emulator.PREVIEW);
                }
                if (strReplace.isEmpty()) {
                    this.client.getHabbo().getHabboStats().changeNameChecked = string;
                } else {
                    i = 4;
                }
            } else {
                i = 4;
            }
            this.client.sendResponse(new ChangeNameCheckResultComposer(i, string, arrayList));
        }
    }
}
