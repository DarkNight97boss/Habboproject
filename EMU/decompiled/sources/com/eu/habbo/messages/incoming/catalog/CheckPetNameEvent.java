package com.eu.habbo.messages.incoming.catalog;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.catalog.PetNameErrorComposer;
import org.apache.commons.lang3.StringUtils;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/catalog/CheckPetNameEvent.class */
public class CheckPetNameEvent extends MessageHandler {
    public static int PET_NAME_LENGTH_MINIMUM = Emulator.getConfig().getInt("hotel.pets.name.length.min");
    public static int PET_NAME_LENGTH_MAXIMUM = Emulator.getConfig().getInt("hotel.pets.name.length.max");

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        String string = this.packet.readString();
        if (string.length() < PET_NAME_LENGTH_MINIMUM) {
            this.client.sendResponse(new PetNameErrorComposer(2, PET_NAME_LENGTH_MINIMUM + Emulator.PREVIEW));
            return;
        }
        if (string.length() > PET_NAME_LENGTH_MAXIMUM) {
            this.client.sendResponse(new PetNameErrorComposer(1, PET_NAME_LENGTH_MAXIMUM + Emulator.PREVIEW));
        } else if (StringUtils.isAlphanumeric(string)) {
            this.client.sendResponse(new PetNameErrorComposer(0, string));
        } else {
            this.client.sendResponse(new PetNameErrorComposer(3, string));
        }
    }
}
