package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer;
import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/SendGift.class */
public class SendGift extends RCONMessage<SendGiftJSON> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SendGift.class);

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/SendGift$SendGiftJSON.class */
    static class SendGiftJSON {
        public int user_id = -1;
        public int itemid = -1;
        public String message = Emulator.PREVIEW;

        SendGiftJSON() {
        }
    }

    public SendGift() {
        super(SendGiftJSON.class);
    }

    @Override // com.eu.habbo.messages.rcon.RCONMessage
    public void handle(Gson gson, SendGiftJSON sendGiftJSON) {
        if (sendGiftJSON.user_id < 0) {
            this.status = 1;
            this.message = Emulator.getTexts().getValue("commands.error.cmd_gift.user_not_found").replace("%username%", sendGiftJSON.user_id + Emulator.PREVIEW);
            return;
        }
        if (sendGiftJSON.itemid < 0) {
            this.status = 1;
            this.message = Emulator.getTexts().getValue("commands.error.cmd_gift.not_a_number");
            return;
        }
        Item item = Emulator.getGameEnvironment().getItemManager().getItem(sendGiftJSON.itemid);
        if (item == null) {
            this.status = 1;
            this.message = Emulator.getTexts().getValue("commands.error.cmd_gift.not_found").replace("%itemid%", sendGiftJSON.itemid + Emulator.PREVIEW);
            return;
        }
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(sendGiftJSON.user_id);
        boolean z = habbo != null;
        String string = Emulator.PREVIEW;
        if (z) {
            string = habbo.getHabboInfo().getUsername();
        } else {
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM users WHERE id = ? LIMIT 1");
                    try {
                        preparedStatementPrepareStatement.setInt(1, sendGiftJSON.user_id);
                        ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                        try {
                            if (resultSetExecuteQuery.next()) {
                                string = resultSetExecuteQuery.getString("username");
                                z = true;
                            }
                            if (resultSetExecuteQuery != null) {
                                resultSetExecuteQuery.close();
                            }
                            if (preparedStatementPrepareStatement != null) {
                                preparedStatementPrepareStatement.close();
                            }
                            if (connection != null) {
                                connection.close();
                            }
                        } catch (Throwable th) {
                            if (resultSetExecuteQuery != null) {
                                try {
                                    resultSetExecuteQuery.close();
                                } catch (Throwable th2) {
                                    th.addSuppressed(th2);
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        if (preparedStatementPrepareStatement != null) {
                            try {
                                preparedStatementPrepareStatement.close();
                            } catch (Throwable th4) {
                                th3.addSuppressed(th4);
                            }
                        }
                        throw th3;
                    }
                } finally {
                }
            } catch (SQLException e) {
                LOGGER.error("Caught SQL exception", e);
            }
        }
        if (!z) {
            this.status = 1;
            this.message = Emulator.getTexts().getValue("commands.error.cmd_gift.user_not_found").replace("%username%", string);
            return;
        }
        HabboItem habboItemCreateItem = Emulator.getGameEnvironment().getItemManager().createItem(0, item, 0, 0, Emulator.PREVIEW);
        Emulator.getGameEnvironment().getItemManager().createGift(string, Emulator.getGameEnvironment().getItemManager().getItem(((Integer) Emulator.getGameEnvironment().getCatalogManager().giftFurnis.values().toArray()[Emulator.getRandom().nextInt(Emulator.getGameEnvironment().getCatalogManager().giftFurnis.size())]).intValue()), ("1\t" + habboItemCreateItem.getId()) + "\t0\t0\t0\t" + sendGiftJSON.message + "\t0\t0", 0, 0);
        this.message = Emulator.getTexts().getValue("commands.succes.cmd_gift").replace("%username%", string).replace("%itemname%", habboItemCreateItem.getBaseItem().getName());
        if (habbo != null) {
            habbo.getClient().sendResponse(new InventoryRefreshComposer());
        }
    }
}
