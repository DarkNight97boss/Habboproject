package com.eu.habbo.messages.incoming.camera;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.camera.CameraPublishWaitMessageComposer;
import com.eu.habbo.messages.outgoing.catalog.NotEnoughPointsTypeComposer;
import com.eu.habbo.plugin.events.users.UserPublishPictureEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CameraPublishToWebEvent extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CameraPublishToWebEvent.class);

    public static int CAMERA_PUBLISH_POINTS = 5;
    public static int CAMERA_PUBLISH_POINTS_TYPE = 0;

    @Override
    public int getRatelimit() {
        return 1000;
    }

    @Override
    public void handle() throws Exception {
        Habbo habbo = this.client.getHabbo();

        if (habbo == null) return;
        if (habbo.getHabboInfo().getPhotoTimestamp() == 0) return;
        if (habbo.getHabboInfo().getPhotoJSON().isEmpty()) return;
        if (!habbo.getHabboInfo().getPhotoJSON().contains(habbo.getHabboInfo().getPhotoTimestamp() + "")) return;

        if (habbo.getHabboInfo().getCurrencyAmount(CameraPublishToWebEvent.CAMERA_PUBLISH_POINTS_TYPE) < CameraPublishToWebEvent.CAMERA_PUBLISH_POINTS) {
            this.client.sendResponse(new NotEnoughPointsTypeComposer(false, true, CameraPublishToWebEvent.CAMERA_PUBLISH_POINTS));
            return;
        }

        // Lock on the HabboInfo so two concurrent publishes from the same account
        // can't both pass the cooldown check, both INSERT into camera_web, and
        // both spend the publish points (one charge would clamp to 0). Per-user
        // lock — never crosses accounts.
        synchronized (habbo.getHabboInfo()) {
            // Re-check spend inside the lock: a concurrent transaction may have
            // already drained the balance.
            if (habbo.getHabboInfo().getCurrencyAmount(CameraPublishToWebEvent.CAMERA_PUBLISH_POINTS_TYPE) < CameraPublishToWebEvent.CAMERA_PUBLISH_POINTS) {
                this.client.sendResponse(new NotEnoughPointsTypeComposer(false, true, CameraPublishToWebEvent.CAMERA_PUBLISH_POINTS));
                return;
            }

            int timestamp = Emulator.getIntUnixTimestamp();
            int cooldownLeft = Math.max(0, Emulator.getConfig().getInt("camera.publish.delay") - (timestamp - habbo.getHabboInfo().getWebPublishTimestamp()));

            boolean isOk = false;
            if (cooldownLeft == 0) {
                // Reserve the cooldown slot FIRST so a parallel attempt that
                // somehow bypasses the synchronized block (e.g. plugin event
                // re-entrancy) sees the bumped timestamp.
                habbo.getHabboInfo().setWebPublishTimestamp(timestamp);

                UserPublishPictureEvent publishPictureEvent = new UserPublishPictureEvent(habbo, habbo.getHabboInfo().getPhotoURL(), timestamp, habbo.getHabboInfo().getPhotoRoomId());
                if (!Emulator.getPluginManager().fireEvent(publishPictureEvent).isCancelled()) {
                    try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                         PreparedStatement statement = connection.prepareStatement("INSERT INTO camera_web (user_id, room_id, timestamp, url) VALUES (?, ?, ?, ?)")) {
                        statement.setInt(1, habbo.getHabboInfo().getId());
                        statement.setInt(2, publishPictureEvent.roomId);
                        statement.setInt(3, publishPictureEvent.timestamp);
                        statement.setString(4, publishPictureEvent.URL);
                        statement.execute();

                        habbo.givePoints(CameraPublishToWebEvent.CAMERA_PUBLISH_POINTS_TYPE, -CameraPublishToWebEvent.CAMERA_PUBLISH_POINTS);
                        isOk = true;
                    } catch (SQLException e) {
                        // DB insert failed -> roll back the cooldown reservation.
                        habbo.getHabboInfo().setWebPublishTimestamp(habbo.getHabboInfo().getWebPublishTimestamp());
                        LOGGER.error("Caught SQL exception", e);
                    }
                } else {
                    // Cancelled by a plugin -> roll back the cooldown reservation.
                    habbo.getHabboInfo().setWebPublishTimestamp(habbo.getHabboInfo().getWebPublishTimestamp());
                }
            }

            this.client.sendResponse(new CameraPublishWaitMessageComposer(isOk, cooldownLeft, isOk ? habbo.getHabboInfo().getPhotoURL() : ""));
        }
    }
}