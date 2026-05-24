package com.eu.habbo.messages.outgoing.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.guilds.Guild;
import com.eu.habbo.habbohotel.messenger.Messenger;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/users/UserProfileComposer.class */
public class UserProfileComposer extends MessageComposer {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserProfileComposer.class);
    private final HabboInfo habboInfo;
    private Habbo habbo;
    private GameClient viewer;

    public UserProfileComposer(HabboInfo habboInfo, GameClient gameClient) {
        this.habboInfo = habboInfo;
        this.viewer = gameClient;
    }

    public UserProfileComposer(Habbo habbo, GameClient gameClient) {
        this.habbo = habbo;
        this.habboInfo = habbo.getHabboInfo();
        this.viewer = gameClient;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        if (this.habboInfo == null) {
            return null;
        }
        this.response.init(3898);
        this.response.appendInt(Integer.valueOf(this.habboInfo.getId()));
        this.response.appendString(this.habboInfo.getUsername());
        this.response.appendString(this.habboInfo.getLook());
        this.response.appendString(this.habboInfo.getMotto());
        this.response.appendString(new SimpleDateFormat("dd-MM-yyyy").format(new Date(((long) this.habboInfo.getAccountCreated()) * 1000)));
        if (this.habbo != null) {
            achievementScore = this.habbo.getHabboStats().getAchievementScore();
        } else {
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT achievement_score FROM users_settings WHERE user_id = ? LIMIT 1");
                    try {
                        preparedStatementPrepareStatement.setInt(1, this.habboInfo.getId());
                        ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                        try {
                            achievementScore = resultSetExecuteQuery.next() ? resultSetExecuteQuery.getInt("achievement_score") : 0;
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
        this.response.appendInt(Integer.valueOf(achievementScore));
        this.response.appendInt(Integer.valueOf(Messenger.getFriendCount(this.habboInfo.getId())));
        this.response.appendBoolean(Boolean.valueOf(this.viewer.getHabbo().getMessenger().getFriends().containsKey(Integer.valueOf(this.habboInfo.getId()))));
        this.response.appendBoolean(Boolean.valueOf(Messenger.friendRequested(this.viewer.getHabbo().getHabboInfo().getId(), this.habboInfo.getId())));
        this.response.appendBoolean(Boolean.valueOf(this.habboInfo.isOnline()));
        List<Guild> arrayList = new ArrayList();
        if (this.habbo != null) {
            ArrayList arrayList2 = new ArrayList();
            for (int size = this.habbo.getHabboStats().guilds.size(); size > 0; size--) {
                int iIntValue = this.habbo.getHabboStats().guilds.get(size - 1).intValue();
                if (iIntValue != 0) {
                    Guild guild = Emulator.getGameEnvironment().getGuildManager().getGuild(iIntValue);
                    if (guild != null) {
                        arrayList.add(guild);
                    } else {
                        arrayList2.add(Integer.valueOf(iIntValue));
                    }
                }
            }
            Iterator it = arrayList2.iterator();
            while (it.hasNext()) {
                this.habbo.getHabboStats().removeGuild(((Integer) it.next()).intValue());
            }
        } else {
            arrayList = Emulator.getGameEnvironment().getGuildManager().getGuilds(this.habboInfo.getId());
        }
        this.response.appendInt(Integer.valueOf(arrayList.size()));
        for (Guild guild2 : arrayList) {
            this.response.appendInt(Integer.valueOf(guild2.getId()));
            this.response.appendString(guild2.getName());
            this.response.appendString(guild2.getBadge());
            this.response.appendString(Emulator.getGameEnvironment().getGuildManager().getSymbolColor(guild2.getColorOne()).valueA);
            this.response.appendString(Emulator.getGameEnvironment().getGuildManager().getSymbolColor(guild2.getColorTwo()).valueA);
            this.response.appendBoolean(Boolean.valueOf(this.habbo != null && guild2.getId() == this.habbo.getHabboStats().guild));
            this.response.appendInt(Integer.valueOf(guild2.getOwnerId()));
            this.response.appendBoolean(Boolean.valueOf(guild2.getOwnerId() == this.habboInfo.getId()));
        }
        this.response.appendInt(Integer.valueOf(Emulator.getIntUnixTimestamp() - this.habboInfo.getLastOnline()));
        this.response.appendBoolean(true);
        return this.response;
    }
}
