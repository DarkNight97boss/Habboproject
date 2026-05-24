package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomChatMessage;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.rooms.RoomChatType;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/YouAreAPirate.class */
public class YouAreAPirate implements Runnable {
    public static String[] iamapirate = {"Do what you want, 'cause a pirate is free,", "You are a pirate!", Emulator.PREVIEW, "Yar har, fiddle di dee,", "Being a pirate is all right with me,", "Do what you want 'cause a pirate is free, ", "You are a pirate!", "Yo Ho, ahoy and avast,", "Being a pirate is really badass!", "Hang the black flag at the end of the mast!", "You are a pirate!", Emulator.PREVIEW, "You are a pirate! - Yay!", Emulator.PREVIEW, "We've got us a map, (a map! )", "To lead us to a hidden box,", "That's all locked up with locks! (with locks! )", "And buried deep away!", Emulator.PREVIEW, "We'll dig up the box, (the box! )", "We know it's full of precious booty! ", "Burst open the locks!", "And then we'll say hooray! ", Emulator.PREVIEW, "Yar har, fiddle di dee,", "Being a pirate is all right with me!", "Do what you want 'cause a pirate is free, ", Emulator.PREVIEW, "You are a pirate!", "Yo Ho, ahoy and avast,", "Being a Pirate is really badass!", "Hang the black flag", "At the end of the mast!", "You are a pirate!", Emulator.PREVIEW, "Hahaha!", Emulator.PREVIEW, Emulator.PREVIEW, "We're sailing away (set sail! ), ", "Adventure awaits on every shore!", "We set sail and explore (ya-har! )", "And run and jump all day (Yay! )", "We float on our boat (the boat! )", "Until it's time to drop the anchor, ", "Then hang up our coats (aye-aye! )", "Until we sail again!", Emulator.PREVIEW, "Yar har, fiddle di dee,", "Being a pirate is all right with me!", "Do what you want 'cause a pirate is free, ", "You are a pirate!", Emulator.PREVIEW, "Yar har, wind at your back, lads,", "Wherever you go!", Emulator.PREVIEW, Emulator.PREVIEW, "Blue sky above and blue ocean below,", "You are a pirate!", Emulator.PREVIEW, "You are a pirate!"};
    public final Habbo habbo;
    public final Room room;
    private int index = 0;
    private int oldEffect;

    public YouAreAPirate(Habbo habbo, Room room) {
        this.habbo = habbo;
        this.room = room;
        this.oldEffect = this.habbo.getRoomUnit().getEffectId();
        this.room.giveEffect(this.habbo, 161, -1);
    }

    @Override // java.lang.Runnable
    public void run() {
        if (this.room == this.habbo.getHabboInfo().getCurrentRoom()) {
            if (!iamapirate[this.index].isEmpty()) {
                this.room.talk(this.habbo, new RoomChatMessage(iamapirate[this.index], this.habbo, RoomChatMessageBubbles.PIRATE), RoomChatType.SHOUT);
            }
            this.index++;
            if (this.index == iamapirate.length) {
                this.room.giveEffect(this.habbo, this.oldEffect, -1);
            } else {
                Emulator.getThreading().run(this, iamapirate[this.index - 1].length() * 100);
            }
        }
    }
}
