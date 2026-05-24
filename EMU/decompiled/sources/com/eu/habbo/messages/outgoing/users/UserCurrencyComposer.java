package com.eu.habbo.messages.outgoing.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/users/UserCurrencyComposer.class */
public class UserCurrencyComposer extends MessageComposer {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserCurrencyComposer.class);
    private final Habbo habbo;

    public UserCurrencyComposer(Habbo habbo) {
        this.habbo = habbo;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.UserCurrencyComposer);
        String[] strArrSplit = Emulator.getConfig().getValue("seasonal.types").split(";");
        this.response.appendInt(Integer.valueOf(strArrSplit.length));
        for (String str : strArrSplit) {
            try {
                int iIntValue = Integer.valueOf(str).intValue();
                this.response.appendInt(Integer.valueOf(iIntValue));
                this.response.appendInt(Integer.valueOf(this.habbo.getHabboInfo().getCurrencyAmount(iIntValue)));
            } catch (Exception e) {
                LOGGER.error("Caught exception", e);
                return null;
            }
        }
        return this.response;
    }
}
