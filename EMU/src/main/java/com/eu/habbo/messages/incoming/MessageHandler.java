package com.eu.habbo.messages.incoming;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.messages.ClientMessage;

public abstract class MessageHandler {
    public GameClient client;
    public ClientMessage packet;
    public boolean isCancelled = false;

    public abstract void handle() throws Exception;

    public int getRatelimit() {
        return 0;
    }

    /**
     * Convenience guard: returns the {@link Habbo} attached to this handler's
     * client, or {@code null} if any link in the chain is missing.
     *
     * Many handlers chain {@code this.client.getHabbo().getHabboInfo()...}
     * without null-checking. Under load it's normal for a packet to arrive
     * while the client is being disposed (race between disconnect and the
     * I/O thread draining the queue) and the chain NPEs. Each handler
     * crashing the dispatch loop with a stack trace was a hidden CPU tax;
     * with this guard a handler can early-out cleanly:
     *
     *   Habbo me = this.requireHabbo();
     *   if (me == null) return;
     */
    protected final Habbo requireHabbo() {
        if (this.client == null) return null;
        Habbo h = this.client.getHabbo();
        if (h == null) return null;
        HabboInfo info = h.getHabboInfo();
        if (info == null) return null;
        return h;
    }
}