package com.eu.habbo.messages.incoming.wired;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.interactions.InteractionWired;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.generic.alerts.UpdateFailedComposer;
import com.eu.habbo.messages.outgoing.wired.WiredSavedComposer;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/wired/WiredEffectSaveDataEvent.class */
public class WiredEffectSaveDataEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom != null) {
            if (currentRoom.hasRights(this.client.getHabbo()) || currentRoom.getOwnerId() == this.client.getHabbo().getHabboInfo().getId() || this.client.getHabbo().hasPermission(Permission.ACC_ANYROOMOWNER) || this.client.getHabbo().hasPermission(Permission.ACC_MOVEROTATE)) {
                InteractionWiredEffect effect = currentRoom.getRoomSpecialTypes().getEffect(iIntValue);
                try {
                    if (effect == null) {
                        throw new WiredSaveException(String.format("Wired effect with item id %s not found in room", Integer.valueOf(iIntValue)));
                    }
                    Optional optionalFindFirst = Arrays.stream(effect.getClass().getMethods()).filter(method -> {
                        return method.getName().equals("saveData");
                    }).findFirst();
                    if (!optionalFindFirst.isPresent()) {
                        this.client.sendResponse(new UpdateFailedComposer("Save method was not found"));
                    } else if (((Method) optionalFindFirst.get()).getParameterTypes()[0] == WiredSettings.class) {
                        if (effect.saveData(InteractionWired.readSettings(this.packet, true), this.client)) {
                            this.client.sendResponse(new WiredSavedComposer());
                            effect.needsUpdate(true);
                            Emulator.getThreading().run(effect);
                        }
                    } else if (((Boolean) ((Method) optionalFindFirst.get()).invoke(effect, this.packet, this.client)).booleanValue()) {
                        this.client.sendResponse(new WiredSavedComposer());
                        effect.needsUpdate(true);
                        Emulator.getThreading().run(effect);
                    }
                } catch (WiredSaveException e) {
                    this.client.sendResponse(new UpdateFailedComposer(e.getMessage()));
                }
            }
        }
    }
}
