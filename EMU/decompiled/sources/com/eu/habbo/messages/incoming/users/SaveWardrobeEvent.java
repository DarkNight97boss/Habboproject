package com.eu.habbo.messages.incoming.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.HabboGender;
import com.eu.habbo.habbohotel.users.inventory.WardrobeComponent;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.plugin.events.users.UserSavedWardrobeEvent;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/users/SaveWardrobeEvent.class */
public class SaveWardrobeEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        WardrobeComponent.WardrobeItem wardrobeItemCreateLook;
        int iIntValue = this.packet.readInt().intValue();
        String string = this.packet.readString();
        String string2 = this.packet.readString();
        if (this.client.getHabbo().getInventory().getWardrobeComponent().getLooks().containsKey(Integer.valueOf(iIntValue))) {
            wardrobeItemCreateLook = (WardrobeComponent.WardrobeItem) this.client.getHabbo().getInventory().getWardrobeComponent().getLooks().get(Integer.valueOf(iIntValue));
            wardrobeItemCreateLook.setGender(HabboGender.valueOf(string2));
            wardrobeItemCreateLook.setLook(string);
            wardrobeItemCreateLook.setNeedsUpdate(true);
        } else {
            wardrobeItemCreateLook = this.client.getHabbo().getInventory().getWardrobeComponent().createLook(this.client.getHabbo(), iIntValue, string);
            wardrobeItemCreateLook.setGender(HabboGender.valueOf(string2));
            wardrobeItemCreateLook.setNeedsInsert(true);
            this.client.getHabbo().getInventory().getWardrobeComponent().getLooks().put(Integer.valueOf(iIntValue), wardrobeItemCreateLook);
        }
        Emulator.getPluginManager().fireEvent(new UserSavedWardrobeEvent(this.client.getHabbo(), wardrobeItemCreateLook));
        Emulator.getThreading().run(wardrobeItemCreateLook);
    }
}
