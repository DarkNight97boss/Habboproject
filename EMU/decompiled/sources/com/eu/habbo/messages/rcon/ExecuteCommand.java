package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.commands.CommandHandler;
import com.eu.habbo.habbohotel.users.Habbo;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/ExecuteCommand.class */
public class ExecuteCommand extends RCONMessage<JSONExecuteCommand> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecuteCommand.class);

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/ExecuteCommand$JSONExecuteCommand.class */
    static class JSONExecuteCommand {
        public int user_id;
        public String command;

        JSONExecuteCommand() {
        }
    }

    public ExecuteCommand() {
        super(JSONExecuteCommand.class);
    }

    @Override // com.eu.habbo.messages.rcon.RCONMessage
    public void handle(Gson gson, JSONExecuteCommand jSONExecuteCommand) {
        try {
            Habbo habbo = Emulator.getGameServer().getGameClientManager().getHabbo(jSONExecuteCommand.user_id);
            if (habbo == null) {
                this.status = 2;
            } else {
                CommandHandler.handleCommand(habbo.getClient(), jSONExecuteCommand.command);
            }
        } catch (Exception e) {
            this.status = 1;
            LOGGER.error("Caught exception", e);
        }
    }
}
