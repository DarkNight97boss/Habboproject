package com.eu.habbo.habbohotel.permissions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.plugin.HabboPlugin;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/permissions/PermissionsManager.class */
public class PermissionsManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionsManager.class);
    private final TIntObjectHashMap<Rank> ranks;
    private final TIntIntHashMap enables;
    private final THashMap<String, List<Rank>> badges;

    public PermissionsManager() {
        long jCurrentTimeMillis = System.currentTimeMillis();
        this.ranks = new TIntObjectHashMap<>();
        this.enables = new TIntIntHashMap();
        this.badges = new THashMap<>();
        reload();
        LOGGER.info("Permissions Manager -> Loaded! (" + (System.currentTimeMillis() - jCurrentTimeMillis) + " MS)");
    }

    public void reload() {
        loadPermissions();
        loadEnables();
    }

    private void loadPermissions() {
        Rank rank;
        this.badges.clear();
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                Statement statementCreateStatement = connection.createStatement();
                try {
                    ResultSet resultSetExecuteQuery = statementCreateStatement.executeQuery("SELECT * FROM permissions ORDER BY id ASC");
                    while (resultSetExecuteQuery.next()) {
                        try {
                            if (this.ranks.containsKey(resultSetExecuteQuery.getInt("id"))) {
                                rank = (Rank) this.ranks.get(resultSetExecuteQuery.getInt("id"));
                                rank.load(resultSetExecuteQuery);
                            } else {
                                rank = new Rank(resultSetExecuteQuery);
                                this.ranks.put(resultSetExecuteQuery.getInt("id"), rank);
                            }
                            if (rank != null && !rank.getBadge().isEmpty()) {
                                if (!this.badges.containsKey(rank.getBadge())) {
                                    this.badges.put(rank.getBadge(), new ArrayList());
                                }
                                ((List) this.badges.get(rank.getBadge())).add(rank);
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
                    }
                    if (resultSetExecuteQuery != null) {
                        resultSetExecuteQuery.close();
                    }
                    if (statementCreateStatement != null) {
                        statementCreateStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Throwable th3) {
                    if (statementCreateStatement != null) {
                        try {
                            statementCreateStatement.close();
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

    private void loadEnables() {
        Connection connection;
        synchronized (this.enables) {
            this.enables.clear();
            try {
                connection = Emulator.getDatabase().getDataSource().getConnection();
            } catch (SQLException e) {
                LOGGER.error("Caught SQL exception", e);
            }
            try {
                Statement statementCreateStatement = connection.createStatement();
                try {
                    ResultSet resultSetExecuteQuery = statementCreateStatement.executeQuery("SELECT * FROM special_enables");
                    while (resultSetExecuteQuery.next()) {
                        try {
                            this.enables.put(resultSetExecuteQuery.getInt("effect_id"), resultSetExecuteQuery.getInt("min_rank"));
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
                    if (statementCreateStatement != null) {
                        statementCreateStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Throwable th3) {
                    if (statementCreateStatement != null) {
                        try {
                            statementCreateStatement.close();
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
        }
    }

    public boolean rankExists(int i) {
        return this.ranks.containsKey(i);
    }

    public Rank getRank(int i) {
        return (Rank) this.ranks.get(i);
    }

    public Rank getRankByName(String str) {
        for (Rank rank : this.ranks.valueCollection()) {
            if (rank.getName().equalsIgnoreCase(str)) {
                return rank;
            }
        }
        return null;
    }

    public boolean isEffectBlocked(int i, int i2) {
        return this.enables.contains(i) && this.enables.get(i) > i2;
    }

    public boolean hasPermission(Habbo habbo, String str) {
        return hasPermission(habbo, str, false);
    }

    public boolean hasPermission(Habbo habbo, String str, boolean z) {
        if (hasPermission(habbo.getHabboInfo().getRank(), str, z)) {
            return true;
        }
        TObjectHashIterator it = Emulator.getPluginManager().getPlugins().iterator();
        while (it.hasNext()) {
            if (((HabboPlugin) it.next()).hasPermission(habbo, str)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasPermission(Rank rank, String str, boolean z) {
        return rank.hasPermission(str, z);
    }

    public Set<String> getStaffBadges() {
        return this.badges.keySet();
    }

    public List<Rank> getRanksByBadgeCode(String str) {
        return (List) this.badges.get(str);
    }

    public List<Rank> getAllRanks() {
        return new ArrayList(this.ranks.valueCollection());
    }
}
