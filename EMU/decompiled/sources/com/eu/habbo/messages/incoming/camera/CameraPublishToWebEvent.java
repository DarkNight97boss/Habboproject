package com.eu.habbo.messages.incoming.camera;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.camera.CameraPublishWaitMessageComposer;
import com.eu.habbo.messages.outgoing.catalog.NotEnoughPointsTypeComposer;
import com.eu.habbo.plugin.events.users.UserPublishPictureEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/camera/CameraPublishToWebEvent.class */
public class CameraPublishToWebEvent extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CameraPublishToWebEvent.class);
    public static int CAMERA_PUBLISH_POINTS = 5;
    public static int CAMERA_PUBLISH_POINTS_TYPE = 0;

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Habbo habbo = this.client.getHabbo();
        if (habbo == null || habbo.getHabboInfo().getPhotoTimestamp() == 0 || habbo.getHabboInfo().getPhotoJSON().isEmpty() || !habbo.getHabboInfo().getPhotoJSON().contains(habbo.getHabboInfo().getPhotoTimestamp() + Emulator.PREVIEW)) {
            return;
        }
        if (habbo.getHabboInfo().getCurrencyAmount(CAMERA_PUBLISH_POINTS_TYPE) < CAMERA_PUBLISH_POINTS) {
            this.client.sendResponse(new NotEnoughPointsTypeComposer(false, true, CAMERA_PUBLISH_POINTS));
            return;
        }
        int intUnixTimestamp = Emulator.getIntUnixTimestamp();
        boolean z = false;
        int iMax = Math.max(0, Emulator.getConfig().getInt("camera.publish.delay") - (intUnixTimestamp - this.client.getHabbo().getHabboInfo().getWebPublishTimestamp()));
        if (iMax == 0) {
            UserPublishPictureEvent userPublishPictureEvent = new UserPublishPictureEvent(this.client.getHabbo(), this.client.getHabbo().getHabboInfo().getPhotoURL(), intUnixTimestamp, this.client.getHabbo().getHabboInfo().getPhotoRoomId());
            if (!((UserPublishPictureEvent) Emulator.getPluginManager().fireEvent(userPublishPictureEvent)).isCancelled()) {
                try {
                    Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                    try {
                        PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO camera_web (user_id, room_id, timestamp, url) VALUES (?, ?, ?, ?)");
                        try {
                            preparedStatementPrepareStatement.setInt(1, this.client.getHabbo().getHabboInfo().getId());
                            preparedStatementPrepareStatement.setInt(2, userPublishPictureEvent.roomId);
                            preparedStatementPrepareStatement.setInt(3, userPublishPictureEvent.timestamp);
                            preparedStatementPrepareStatement.setString(4, userPublishPictureEvent.URL);
                            preparedStatementPrepareStatement.execute();
                            this.client.getHabbo().getHabboInfo().setWebPublishTimestamp(intUnixTimestamp);
                            this.client.getHabbo().givePoints(CAMERA_PUBLISH_POINTS_TYPE, -CAMERA_PUBLISH_POINTS);
                            z = true;
                            if (preparedStatementPrepareStatement != null) {
                                preparedStatementPrepareStatement.close();
                            }
                            if (connection != null) {
                                connection.close();
                            }
                        } catch (Throwable th) {
                            if (preparedStatementPrepareStatement != null) {
                                try {
                                    preparedStatementPrepareStatement.close();
                                } catch (Throwable th2) {
                                    th.addSuppressed(th2);
                                }
                            }
                            throw th;
                        }
                    } finally {
                    }
                } catch (SQLException e) {
                    LOGGER.error("Caught SQL exception", e);
                }
            }
        }
        this.client.sendResponse(new CameraPublishWaitMessageComposer(z, iMax, z ? this.client.getHabbo().getHabboInfo().getPhotoURL() : Emulator.PREVIEW));
    }
}
