package com.eu.habbo.messages.outgoing.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ModToolSanctionItem;
import com.eu.habbo.habbohotel.modtool.ModToolSanctions;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/modtool/ModToolUserInfoComposer.class */
public class ModToolUserInfoComposer extends MessageComposer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModToolUserInfoComposer.class);
    private final ResultSet set;

    public ModToolUserInfoComposer(ResultSet resultSet) {
        this.set = resultSet;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        ArrayList arrayList;
        Connection connection;
        this.response.init(Outgoing.ModToolUserInfoComposer);
        int i = 0;
        try {
            try {
                connection = Emulator.getDatabase().getDataSource().getConnection();
            } catch (SQLException e) {
                LOGGER.error("Caught SQL exception", e);
            }
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT COUNT(*) AS amount FROM bans WHERE user_id = ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, this.set.getInt("user_id"));
                    try {
                        ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                        try {
                            if (resultSetExecuteQuery.next()) {
                                i = resultSetExecuteQuery.getInt("amount");
                            }
                            if (resultSetExecuteQuery != null) {
                                resultSetExecuteQuery.close();
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
                    } catch (SQLException e2) {
                        LOGGER.error("Caught SQL exception", e2);
                    }
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                    this.response.appendInt(Integer.valueOf(this.set.getInt("user_id")));
                    this.response.appendString(this.set.getString("username"));
                    this.response.appendString(this.set.getString("look"));
                    this.response.appendInt(Integer.valueOf((Emulator.getIntUnixTimestamp() - this.set.getInt("account_created")) / 60));
                    this.response.appendInt(Integer.valueOf((this.set.getInt("online") == 1 ? 0 : Emulator.getIntUnixTimestamp() - this.set.getInt("last_online")) / 60));
                    this.response.appendBoolean(Boolean.valueOf(this.set.getInt("online") == 1));
                    this.response.appendInt(Integer.valueOf(this.set.getInt("cfh_send")));
                    this.response.appendInt(Integer.valueOf(this.set.getInt("cfh_abusive")));
                    this.response.appendInt(Integer.valueOf(this.set.getInt("cfh_warnings")));
                    this.response.appendInt(Integer.valueOf(i));
                    this.response.appendInt(Integer.valueOf(this.set.getInt("tradelock_amount")));
                    this.response.appendString(Emulator.PREVIEW);
                    this.response.appendString(Emulator.PREVIEW);
                    this.response.appendInt(Integer.valueOf(this.set.getInt("user_id")));
                    this.response.appendInt((Integer) 0);
                    this.response.appendString(this.set.getBoolean("hide_mail") ? Emulator.PREVIEW : this.set.getString("mail"));
                    this.response.appendString("Rank (" + this.set.getInt("rank_id") + "): " + this.set.getString("rank_name"));
                    ModToolSanctions modToolSanctions = Emulator.getGameEnvironment().getModToolSanctions();
                    if (Emulator.getConfig().getBoolean("hotel.sanctions.enabled") && (arrayList = (ArrayList) Emulator.getGameEnvironment().getModToolSanctions().getSanctions(this.set.getInt("user_id")).get(Integer.valueOf(this.set.getInt("user_id")))) != null && arrayList.size() > 0) {
                        this.response.appendString(modToolSanctions.getSanctionType(modToolSanctions.getSanctionLevelItem(((ModToolSanctionItem) arrayList.get(arrayList.size() - 1)).sanctionLevel)));
                        this.response.appendInt((Integer) 31);
                    }
                    return this.response;
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
            } catch (Throwable th5) {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (Throwable th6) {
                        th5.addSuppressed(th6);
                    }
                }
                throw th5;
            }
        } catch (SQLException e3) {
            LOGGER.error("Caught SQL exception", e3);
            return null;
        }
    }
}
