package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.habbohotel.gameclients.GameClient;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/Command.class */
public abstract class Command {
    public final String permission;
    public final String[] keys;

    public Command(String str, String[] strArr) {
        this.permission = str;
        this.keys = strArr;
    }

    public abstract boolean handle(GameClient gameClient, String[] strArr) throws Exception;
}
