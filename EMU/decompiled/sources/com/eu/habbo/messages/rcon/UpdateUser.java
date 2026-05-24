package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserDataComposer;
import com.eu.habbo.messages.outgoing.users.MeMenuSettingsComposer;
import com.eu.habbo.messages.outgoing.users.UpdateUserLookComposer;
import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/UpdateUser.class */
public class UpdateUser extends RCONMessage<JSON> {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateUser.class);

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/UpdateUser$JSON.class */
    static class JSON {
        public int user_id;
        public int achievement_score = 0;
        public int block_following = -1;
        public int block_friendrequests = -1;
        public int block_roominvites = -1;
        public int old_chat = -1;
        public int block_camera_follow = -1;
        public String look = Emulator.PREVIEW;
        public boolean strip_unredeemed_clothing = false;

        JSON() {
        }
    }

    public UpdateUser() {
        super(JSON.class);
    }

    @Override // com.eu.habbo.messages.rcon.RCONMessage
    public void handle(Gson gson, JSON json) {
        if (json.user_id > 0) {
            Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(json.user_id);
            if (habbo != null) {
                habbo.getHabboStats().addAchievementScore(json.achievement_score);
                if (json.block_following != -1) {
                    habbo.getHabboStats().blockFollowing = json.block_following == 1;
                }
                if (json.block_friendrequests != -1) {
                    habbo.getHabboStats().blockFriendRequests = json.block_friendrequests == 1;
                }
                if (json.block_roominvites != -1) {
                    habbo.getHabboStats().blockRoomInvites = json.block_roominvites == 1;
                }
                if (json.old_chat != -1) {
                    habbo.getHabboStats().preferOldChat = json.old_chat == 1;
                }
                if (json.block_camera_follow != -1) {
                    habbo.getHabboStats().blockCameraFollow = json.block_camera_follow == 1;
                }
                if (!json.look.isEmpty()) {
                    habbo.getHabboInfo().setLook(json.look);
                    if (habbo.getClient() != null) {
                        habbo.getClient().sendResponse(new UpdateUserLookComposer(habbo).compose());
                    }
                    if (habbo.getHabboInfo().getCurrentRoom() != null) {
                        habbo.getHabboInfo().getCurrentRoom().sendComposer(new RoomUserDataComposer(habbo).compose());
                    }
                }
                habbo.getHabboStats().run();
                habbo.getClient().sendResponse(new MeMenuSettingsComposer(habbo));
                return;
            }
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE users_settings SET achievement_score = achievement_score + ? " + (json.block_following != -1 ? ", block_following = ?" : Emulator.PREVIEW) + (json.block_friendrequests != -1 ? ", block_friendrequests = ?" : Emulator.PREVIEW) + (json.block_roominvites != -1 ? ", block_roominvites = ?" : Emulator.PREVIEW) + (json.old_chat != -1 ? ", old_chat = ?" : Emulator.PREVIEW) + (json.block_camera_follow != -1 ? ", block_camera_follow = ?" : Emulator.PREVIEW) + " WHERE user_id = ? LIMIT 1");
                    try {
                        preparedStatementPrepareStatement.setInt(1, json.achievement_score);
                        int i = 1 + 1;
                        if (json.block_following != -1) {
                            preparedStatementPrepareStatement.setString(i, json.block_following == 1 ? "1" : "0");
                            i++;
                        }
                        if (json.block_friendrequests != -1) {
                            preparedStatementPrepareStatement.setString(i, json.block_friendrequests == 1 ? "1" : "0");
                            i++;
                        }
                        if (json.block_roominvites != -1) {
                            preparedStatementPrepareStatement.setString(i, json.block_roominvites == 1 ? "1" : "0");
                            i++;
                        }
                        if (json.old_chat != -1) {
                            preparedStatementPrepareStatement.setString(i, json.old_chat == 1 ? "1" : "0");
                            i++;
                        }
                        if (json.block_camera_follow != -1) {
                            preparedStatementPrepareStatement.setString(i, json.block_camera_follow == 1 ? "1" : "0");
                            i++;
                        }
                        preparedStatementPrepareStatement.setInt(i, json.user_id);
                        preparedStatementPrepareStatement.execute();
                        if (preparedStatementPrepareStatement != null) {
                            preparedStatementPrepareStatement.close();
                        }
                        if (!json.look.isEmpty()) {
                            preparedStatementPrepareStatement = connection.prepareStatement("UPDATE users SET look = ? WHERE id = ? LIMIT 1");
                            try {
                                preparedStatementPrepareStatement.setString(1, json.look);
                                preparedStatementPrepareStatement.setInt(2, json.user_id);
                                preparedStatementPrepareStatement.execute();
                                if (preparedStatementPrepareStatement != null) {
                                    preparedStatementPrepareStatement.close();
                                }
                            } finally {
                            }
                        }
                        if (connection != null) {
                            connection.close();
                        }
                    } finally {
                    }
                } finally {
                }
            } catch (SQLException e) {
                LOGGER.error("Caught SQL exception", e);
            }
        }
    }
}
