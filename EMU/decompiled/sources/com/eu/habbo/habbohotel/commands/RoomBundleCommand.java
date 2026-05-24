package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.CatalogItem;
import com.eu.habbo.habbohotel.catalog.CatalogPage;
import com.eu.habbo.habbohotel.catalog.CatalogPageLayouts;
import com.eu.habbo.habbohotel.catalog.layouts.RoomBundleLayout;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/RoomBundleCommand.class */
public class RoomBundleCommand extends Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoomBundleCommand.class);

    public RoomBundleCommand() {
        super("cmd_bundle", Emulator.getTexts().getValue("commands.keys.cmd_bundle").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        if (strArr.length < 5) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_bundle.missing_params"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (Emulator.getGameEnvironment().getCatalogManager().getCatalogPage("room_bundle_" + gameClient.getHabbo().getHabboInfo().getCurrentRoom().getId()) != null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_bundle.duplicate"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        int iIntValue = Integer.valueOf(strArr[1]).intValue();
        int iIntValue2 = Integer.valueOf(strArr[2]).intValue();
        int iIntValue3 = Integer.valueOf(strArr[3]).intValue();
        int iIntValue4 = Integer.valueOf(strArr[4]).intValue();
        CatalogPage catalogPageCreateCatalogPage = Emulator.getGameEnvironment().getCatalogManager().createCatalogPage("Room Bundle: " + gameClient.getHabbo().getHabboInfo().getCurrentRoom().getName(), "room_bundle_" + gameClient.getHabbo().getHabboInfo().getCurrentRoom().getId(), gameClient.getHabbo().getHabboInfo().getCurrentRoom().getId(), 0, CatalogPageLayouts.room_bundle, gameClient.getHabbo().getHabboInfo().getRank().getId(), iIntValue);
        if (!(catalogPageCreateCatalogPage instanceof RoomBundleLayout)) {
            return true;
        }
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO catalog_items (page_id, item_ids, catalog_name, cost_credits, cost_points, points_type ) VALUES (?, ?, ?, ?, ?, ?)", 1);
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            preparedStatementPrepareStatement.setInt(1, catalogPageCreateCatalogPage.getId());
            preparedStatementPrepareStatement.setString(2, Emulator.PREVIEW);
            preparedStatementPrepareStatement.setString(3, "room_bundle");
            preparedStatementPrepareStatement.setInt(4, iIntValue2);
            preparedStatementPrepareStatement.setInt(5, iIntValue3);
            preparedStatementPrepareStatement.setInt(6, iIntValue4);
            preparedStatementPrepareStatement.execute();
            ResultSet generatedKeys = preparedStatementPrepareStatement.getGeneratedKeys();
            try {
                if (generatedKeys.next()) {
                    PreparedStatement preparedStatementPrepareStatement2 = connection.prepareStatement("SELECT * FROM catalog_items WHERE id = ?");
                    try {
                        preparedStatementPrepareStatement2.setInt(1, generatedKeys.getInt(1));
                        ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement2.executeQuery();
                        try {
                            if (resultSetExecuteQuery.next()) {
                                catalogPageCreateCatalogPage.addItem(new CatalogItem(resultSetExecuteQuery));
                            }
                            if (resultSetExecuteQuery != null) {
                                resultSetExecuteQuery.close();
                            }
                            if (preparedStatementPrepareStatement2 != null) {
                                preparedStatementPrepareStatement2.close();
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
                        if (preparedStatementPrepareStatement2 != null) {
                            try {
                                preparedStatementPrepareStatement2.close();
                            } catch (Throwable th4) {
                                th3.addSuppressed(th4);
                            }
                        }
                        throw th3;
                    }
                }
                if (generatedKeys != null) {
                    generatedKeys.close();
                }
                if (preparedStatementPrepareStatement != null) {
                    preparedStatementPrepareStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
                ((RoomBundleLayout) catalogPageCreateCatalogPage).loadItems(gameClient.getHabbo().getHabboInfo().getCurrentRoom());
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_bundle").replace("%id%", catalogPageCreateCatalogPage.getId() + Emulator.PREVIEW), RoomChatMessageBubbles.ALERT);
                return true;
            } catch (Throwable th5) {
                if (generatedKeys != null) {
                    try {
                        generatedKeys.close();
                    } catch (Throwable th6) {
                        th5.addSuppressed(th6);
                    }
                }
                throw th5;
            }
        } catch (Throwable th7) {
            if (preparedStatementPrepareStatement != null) {
                try {
                    preparedStatementPrepareStatement.close();
                } catch (Throwable th8) {
                    th7.addSuppressed(th8);
                }
            }
            throw th7;
        }
    }
}
