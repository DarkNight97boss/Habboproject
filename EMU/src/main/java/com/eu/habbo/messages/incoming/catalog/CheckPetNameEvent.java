package com.eu.habbo.messages.incoming.catalog;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.catalog.PetNameErrorComposer;
import org.apache.commons.lang3.StringUtils;

public class CheckPetNameEvent extends MessageHandler {
    public static int PET_NAME_LENGTH_MINIMUM = Emulator.getConfig().getInt("hotel.pets.name.length.min");
    public static int PET_NAME_LENGTH_MAXIMUM = Emulator.getConfig().getInt("hotel.pets.name.length.max");

    @Override
    public void handle() throws Exception {
        // Pet names are capped by `hotel.pets.name.length.max` (default 15)
        // but we still want a hard ceiling at the parser to keep oversize
        // input out of the StringUtils.isAlphanumeric scan.
        String petName = this.packet.readString(64);

        if (petName.length() < PET_NAME_LENGTH_MINIMUM) {
            this.client.sendResponse(new PetNameErrorComposer(PetNameErrorComposer.NAME_TO_SHORT, PET_NAME_LENGTH_MINIMUM + ""));
        } else if (petName.length() > PET_NAME_LENGTH_MAXIMUM) {
            this.client.sendResponse(new PetNameErrorComposer(PetNameErrorComposer.NAME_TO_LONG, PET_NAME_LENGTH_MAXIMUM + ""));
        } else if (!StringUtils.isAlphanumeric(petName)) {
            this.client.sendResponse(new PetNameErrorComposer(PetNameErrorComposer.FORBIDDEN_CHAR, petName));
        } else {
            this.client.sendResponse(new PetNameErrorComposer(PetNameErrorComposer.NAME_OK, petName));
        }
    }
}
