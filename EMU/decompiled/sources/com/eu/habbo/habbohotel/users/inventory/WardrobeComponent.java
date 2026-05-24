package com.eu.habbo.habbohotel.users.inventory;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboGender;
import gnu.trove.TIntCollection;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/users/inventory/WardrobeComponent.class */
public class WardrobeComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(WardrobeComponent.class);
    private final THashMap<Integer, WardrobeItem> looks = new THashMap<>();
    private final TIntSet clothing = new TIntHashSet();
    private final TIntSet clothingSets = new TIntHashSet();

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/users/inventory/WardrobeComponent$WardrobeItem.class */
    public class WardrobeItem implements Runnable {
        private int slotId;
        private HabboGender gender;
        private Habbo habbo;
        private String look;
        private boolean needsInsert;
        private boolean needsUpdate;

        private WardrobeItem(ResultSet resultSet, Habbo habbo) throws SQLException {
            this.needsInsert = false;
            this.needsUpdate = false;
            this.gender = HabboGender.valueOf(resultSet.getString("gender"));
            this.look = resultSet.getString("look");
            this.slotId = resultSet.getInt("slot_id");
            this.habbo = habbo;
        }

        private WardrobeItem(HabboGender habboGender, String str, int i, Habbo habbo) {
            this.needsInsert = false;
            this.needsUpdate = false;
            this.gender = habboGender;
            this.look = str;
            this.slotId = i;
            this.habbo = habbo;
        }

        public HabboGender getGender() {
            return this.gender;
        }

        public void setGender(HabboGender habboGender) {
            this.gender = habboGender;
        }

        public Habbo getHabbo() {
            return this.habbo;
        }

        public void setHabbo(Habbo habbo) {
            this.habbo = habbo;
        }

        public String getLook() {
            return this.look;
        }

        public void setLook(String str) {
            this.look = str;
        }

        public void setNeedsInsert(boolean z) {
            this.needsInsert = z;
        }

        public void setNeedsUpdate(boolean z) {
            this.needsUpdate = z;
        }

        public int getSlotId() {
            return this.slotId;
        }

        @Override // java.lang.Runnable
        public void run() {
            PreparedStatement preparedStatementPrepareStatement;
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    if (this.needsInsert) {
                        this.needsInsert = false;
                        this.needsUpdate = false;
                        preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO users_wardrobe (slot_id, look, user_id, gender) VALUES (?, ?, ?, ?)");
                        try {
                            preparedStatementPrepareStatement.setInt(1, this.slotId);
                            preparedStatementPrepareStatement.setString(2, this.look);
                            preparedStatementPrepareStatement.setInt(3, this.habbo.getHabboInfo().getId());
                            preparedStatementPrepareStatement.setString(4, this.gender.name());
                            preparedStatementPrepareStatement.execute();
                            if (preparedStatementPrepareStatement != null) {
                                preparedStatementPrepareStatement.close();
                            }
                        } finally {
                        }
                    }
                    if (this.needsUpdate) {
                        this.needsUpdate = false;
                        preparedStatementPrepareStatement = connection.prepareStatement("UPDATE users_wardrobe SET look = ? WHERE slot_id = ? AND user_id = ?");
                        try {
                            preparedStatementPrepareStatement.setString(1, this.look);
                            preparedStatementPrepareStatement.setInt(2, this.slotId);
                            preparedStatementPrepareStatement.setInt(3, this.habbo.getHabboInfo().getId());
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
            } catch (SQLException e) {
                WardrobeComponent.LOGGER.error("Caught SQL exception", e);
            }
        }
    }

    public WardrobeComponent(Habbo habbo) {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM users_wardrobe WHERE user_id = ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, habbo.getHabboInfo().getId());
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        try {
                            this.looks.put(Integer.valueOf(resultSetExecuteQuery.getInt("slot_id")), new WardrobeItem(resultSetExecuteQuery, habbo));
                        } finally {
                            if (resultSetExecuteQuery != null) {
                                try {
                                    resultSetExecuteQuery.close();
                                } catch (Throwable th) {
                                    th.addSuppressed(th);
                                }
                            }
                        }
                    }
                    if (resultSetExecuteQuery != null) {
                        resultSetExecuteQuery.close();
                    }
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    preparedStatementPrepareStatement = connection.prepareStatement("SELECT users_clothing.*, catalog_clothing.setid FROM users_clothing LEFT JOIN catalog_clothing ON catalog_clothing.id = users_clothing.clothing_id WHERE users_clothing.user_id = ?");
                    try {
                        preparedStatementPrepareStatement.setInt(1, habbo.getHabboInfo().getId());
                        resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                        while (resultSetExecuteQuery.next()) {
                            try {
                                this.clothing.add(resultSetExecuteQuery.getInt("clothing_id"));
                                for (String str : resultSetExecuteQuery.getString("setid").split(Pattern.quote(","))) {
                                    try {
                                        this.clothingSets.add(Integer.parseInt(str));
                                    } catch (Exception e) {
                                    }
                                }
                            } catch (Throwable th2) {
                                throw th2;
                            }
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
                    } finally {
                    }
                } finally {
                }
            } finally {
            }
        } catch (SQLException e2) {
            LOGGER.error("Caught SQL exception", e2);
        }
    }

    public WardrobeItem createLook(Habbo habbo, int i, String str) {
        return new WardrobeItem(habbo.getHabboInfo().getGender(), str, i, habbo);
    }

    public THashMap<Integer, WardrobeItem> getLooks() {
        return this.looks;
    }

    public TIntCollection getClothing() {
        return this.clothing;
    }

    public TIntCollection getClothingSets() {
        return this.clothingSets;
    }

    public void dispose() {
        this.looks.values().stream().filter(wardrobeItem -> {
            return wardrobeItem.needsInsert || wardrobeItem.needsUpdate;
        }).forEach(wardrobeItem2 -> {
            Emulator.getThreading().run(wardrobeItem2);
        });
        this.looks.clear();
    }
}
