package com.eu.habbo.messages.incoming.wired;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.interactions.InteractionWired;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.generic.alerts.UpdateFailedComposer;
import com.eu.habbo.messages.outgoing.wired.WiredSavedComposer;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/wired/WiredTriggerSaveDataEvent.class */
public class WiredTriggerSaveDataEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        InteractionWiredTrigger trigger;
        int iIntValue = this.packet.readInt().intValue();
        Room currentRoom = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (currentRoom != null) {
            if ((currentRoom.hasRights(this.client.getHabbo()) || currentRoom.getOwnerId() == this.client.getHabbo().getHabboInfo().getId() || this.client.getHabbo().hasPermission(Permission.ACC_ANYROOMOWNER) || this.client.getHabbo().hasPermission(Permission.ACC_MOVEROTATE)) && (trigger = currentRoom.getRoomSpecialTypes().getTrigger(iIntValue)) != null) {
                Optional optionalFindFirst = Arrays.stream(trigger.getClass().getMethods()).filter(method -> {
                    return method.getName().equals("saveData");
                }).findFirst();
                if (!optionalFindFirst.isPresent()) {
                    this.client.sendResponse(new UpdateFailedComposer("Save method was not found"));
                    return;
                }
                if (((Method) optionalFindFirst.get()).getParameterTypes()[0] == WiredSettings.class) {
                    if (!trigger.saveData(InteractionWired.readSettings(this.packet, false))) {
                        this.client.sendResponse(new UpdateFailedComposer("There was an error while saving that trigger"));
                        return;
                    }
                    this.client.sendResponse(new WiredSavedComposer());
                    trigger.needsUpdate(true);
                    Emulator.getThreading().run(trigger);
                    return;
                }
                if (!((Boolean) ((Method) optionalFindFirst.get()).invoke(trigger, this.packet)).booleanValue()) {
                    this.client.sendResponse(new UpdateFailedComposer("There was an error while saving that trigger"));
                    return;
                }
                this.client.sendResponse(new WiredSavedComposer());
                trigger.needsUpdate(true);
                Emulator.getThreading().run(trigger);
            }
        }
    }
}
