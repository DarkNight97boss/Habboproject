package com.eu.habbo.habbohotel.users.inventory;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.inventory.EffectsListAddComposer;
import com.eu.habbo.messages.outgoing.inventory.EffectsListEffectEnableComposer;
import com.eu.habbo.messages.outgoing.inventory.EffectsListRemoveComposer;
import gnu.trove.map.hash.THashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/users/inventory/EffectsComponent.class */
public class EffectsComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(EffectsComponent.class);
    public final Habbo habbo;
    public final THashMap<Integer, HabboEffect> effects = new THashMap<>();
    public int activatedEffect = 0;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/users/inventory/EffectsComponent$HabboEffect.class */
    public static class HabboEffect {
        public int effect;
        public int userId;
        public int duration;
        public int activationTimestamp;
        public int total;
        public boolean enabled;
        public boolean isRankEnable;

        public HabboEffect(ResultSet resultSet) throws SQLException {
            this.duration = 86400;
            this.activationTimestamp = -1;
            this.total = 1;
            this.enabled = false;
            this.isRankEnable = false;
            this.effect = resultSet.getInt("effect");
            this.userId = resultSet.getInt("user_id");
            this.duration = resultSet.getInt("duration");
            this.activationTimestamp = resultSet.getInt("activation_timestamp");
            this.total = resultSet.getInt("total");
        }

        public HabboEffect(int i, int i2) {
            this.duration = 86400;
            this.activationTimestamp = -1;
            this.total = 1;
            this.enabled = false;
            this.isRankEnable = false;
            this.effect = i;
            this.userId = i2;
        }

        public boolean isActivated() {
            return this.activationTimestamp >= 0;
        }

        public boolean isRemaining() {
            if (this.duration <= 0) {
                return true;
            }
            if (this.total > 0 && this.activationTimestamp >= 0 && Emulator.getIntUnixTimestamp() - this.activationTimestamp >= this.duration) {
                this.activationTimestamp = -1;
                this.total--;
            }
            return this.total > 0;
        }

        public int remainingTime() {
            if (this.duration <= 0) {
                return Integer.MAX_VALUE;
            }
            return (Emulator.getIntUnixTimestamp() - this.activationTimestamp) + this.duration;
        }

        public void insert() {
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO users_effects (user_id, effect, total, duration) VALUES (?, ?, ?, ?)");
                    try {
                        preparedStatementPrepareStatement.setInt(1, this.userId);
                        preparedStatementPrepareStatement.setInt(2, this.effect);
                        preparedStatementPrepareStatement.setInt(3, this.total);
                        preparedStatementPrepareStatement.setInt(4, this.duration);
                        preparedStatementPrepareStatement.execute();
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
                EffectsComponent.LOGGER.error("Caught SQL exception", e);
            }
        }

        public void delete() {
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("DELETE FROM users_effects WHERE user_id = ? AND effect = ?");
                    try {
                        preparedStatementPrepareStatement.setInt(1, this.userId);
                        preparedStatementPrepareStatement.setInt(2, this.effect);
                        preparedStatementPrepareStatement.execute();
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
                EffectsComponent.LOGGER.error("Caught SQL exception", e);
            }
        }
    }

    public EffectsComponent(Habbo habbo) {
        this.habbo = habbo;
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM users_effects WHERE user_id = ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, habbo.getHabboInfo().getId());
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        try {
                            this.effects.put(Integer.valueOf(resultSetExecuteQuery.getInt("effect")), new HabboEffect(resultSetExecuteQuery));
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
        if (habbo.getHabboInfo().getRank().getRoomEffect() > 0) {
            createRankEffect(habbo.getHabboInfo().getRank().getRoomEffect());
        }
    }

    public HabboEffect createEffect(int i) {
        return createEffect(i, 86400);
    }

    public HabboEffect createEffect(int i, int i2) {
        HabboEffect habboEffect;
        synchronized (this.effects) {
            if (this.effects.containsKey(Integer.valueOf(i))) {
                habboEffect = (HabboEffect) this.effects.get(Integer.valueOf(i));
                if (habboEffect.total <= 99) {
                    habboEffect.total++;
                }
            } else {
                habboEffect = new HabboEffect(i, this.habbo.getHabboInfo().getId());
                habboEffect.duration = i2;
                habboEffect.insert();
            }
            addEffect(habboEffect);
        }
        return habboEffect;
    }

    public HabboEffect createRankEffect(int i) {
        HabboEffect habboEffect = new HabboEffect(i, this.habbo.getHabboInfo().getId());
        habboEffect.duration = 0;
        habboEffect.isRankEnable = true;
        habboEffect.activationTimestamp = Emulator.getIntUnixTimestamp();
        habboEffect.enabled = true;
        this.effects.put(Integer.valueOf(i), habboEffect);
        this.activatedEffect = i;
        return habboEffect;
    }

    public void addEffect(HabboEffect habboEffect) {
        this.effects.put(Integer.valueOf(habboEffect.effect), habboEffect);
        this.habbo.getClient().sendResponse(new EffectsListAddComposer(habboEffect));
    }

    public void dispose() {
        Connection connection;
        synchronized (this.effects) {
            try {
                connection = Emulator.getDatabase().getDataSource().getConnection();
            } catch (SQLException e) {
                LOGGER.error("Caught SQL exception", e);
            }
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE users_effects SET duration = ?, activation_timestamp = ?, total = ? WHERE user_id = ? AND effect = ?");
                try {
                    this.effects.forEachValue(habboEffect -> {
                        if (habboEffect.isRankEnable) {
                            return true;
                        }
                        try {
                            preparedStatementPrepareStatement.setInt(1, habboEffect.duration);
                            preparedStatementPrepareStatement.setInt(2, habboEffect.activationTimestamp);
                            preparedStatementPrepareStatement.setInt(3, habboEffect.total);
                            preparedStatementPrepareStatement.setInt(4, habboEffect.userId);
                            preparedStatementPrepareStatement.setInt(5, habboEffect.effect);
                            preparedStatementPrepareStatement.addBatch();
                            return true;
                        } catch (SQLException e2) {
                            LOGGER.error("Caught SQL exception", e2);
                            return true;
                        }
                    });
                    preparedStatementPrepareStatement.executeBatch();
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                    this.effects.clear();
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
            } catch (Throwable th3) {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (Throwable th4) {
                        th3.addSuppressed(th4);
                    }
                }
                throw th3;
            }
        }
    }

    public boolean ownsEffect(int i) {
        return this.effects.containsKey(Integer.valueOf(i));
    }

    public void activateEffect(int i) {
        HabboEffect habboEffect = (HabboEffect) this.effects.get(Integer.valueOf(i));
        if (habboEffect != null) {
            if (habboEffect.isRemaining()) {
                habboEffect.activationTimestamp = Emulator.getIntUnixTimestamp();
            } else {
                this.habbo.getClient().sendResponse(new EffectsListRemoveComposer(habboEffect));
            }
        }
    }

    public void enableEffect(int i) {
        HabboEffect habboEffect = (HabboEffect) this.effects.get(Integer.valueOf(i));
        if (habboEffect != null) {
            if (!habboEffect.isActivated()) {
                activateEffect(habboEffect.effect);
            }
            this.activatedEffect = i;
            if (this.habbo.getHabboInfo().getCurrentRoom() != null) {
                this.habbo.getHabboInfo().getCurrentRoom().giveEffect(this.habbo, i, habboEffect.remainingTime());
            }
            this.habbo.getClient().sendResponse(new EffectsListEffectEnableComposer(habboEffect));
        }
    }

    public boolean hasActivatedEffect(int i) {
        HabboEffect habboEffect = (HabboEffect) this.effects.get(Integer.valueOf(i));
        if (habboEffect != null) {
            return habboEffect.isActivated();
        }
        return false;
    }
}
