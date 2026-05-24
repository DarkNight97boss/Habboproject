package com.eu.habbo.habbohotel.users.inventory;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.permissions.Rank;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboBadge;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/users/inventory/BadgesComponent.class */
public class BadgesComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(BadgesComponent.class);
    private final THashSet<HabboBadge> badges = new THashSet<>();

    public BadgesComponent(Habbo habbo) {
        this.badges.addAll(loadBadges(habbo));
    }

    private static THashSet<HabboBadge> loadBadges(Habbo habbo) {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        THashSet<HabboBadge> tHashSet = new THashSet<>();
        Set<String> staffBadges = Emulator.getGameEnvironment().getPermissionsManager().getStaffBadges();
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM users_badges WHERE user_id = ?");
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            preparedStatementPrepareStatement.setInt(1, habbo.getHabboInfo().getId());
            ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
            while (resultSetExecuteQuery.next()) {
                try {
                    HabboBadge habboBadge = new HabboBadge(resultSetExecuteQuery, habbo);
                    if (staffBadges.contains(habboBadge.getCode())) {
                        boolean z = true;
                        Iterator<Rank> it = Emulator.getGameEnvironment().getPermissionsManager().getRanksByBadgeCode(habboBadge.getCode()).iterator();
                        while (true) {
                            if (!it.hasNext()) {
                                break;
                            }
                            if (it.next().getId() == habbo.getHabboInfo().getRank().getId()) {
                                z = false;
                                break;
                            }
                        }
                        if (z) {
                            deleteBadge(habbo.getHabboInfo().getId(), habboBadge.getCode());
                        }
                    }
                    tHashSet.add(habboBadge);
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
            return tHashSet;
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
    }

    public static void resetSlots(Habbo habbo) {
        TObjectHashIterator it = habbo.getInventory().getBadgesComponent().getBadges().iterator();
        while (it.hasNext()) {
            HabboBadge habboBadge = (HabboBadge) it.next();
            if (habboBadge.getSlot() != 0) {
                habboBadge.setSlot(0);
                habboBadge.needsUpdate(true);
                Emulator.getThreading().run(habboBadge);
            }
        }
    }

    public static ArrayList<HabboBadge> getBadgesOfflineHabbo(int i) {
        Connection connection;
        ArrayList<HabboBadge> arrayList = new ArrayList<>();
        try {
            connection = Emulator.getDatabase().getDataSource().getConnection();
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM users_badges WHERE slot_id > 0 AND user_id = ? ORDER BY slot_id ASC");
            try {
                preparedStatementPrepareStatement.setInt(1, i);
                ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                while (resultSetExecuteQuery.next()) {
                    try {
                        arrayList.add(new HabboBadge(resultSetExecuteQuery, null));
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
                return arrayList;
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
    }

    public static HabboBadge createBadge(String str, Habbo habbo) {
        HabboBadge habboBadge = new HabboBadge(0, str, 0, habbo);
        habboBadge.run();
        habbo.getInventory().getBadgesComponent().addBadge(habboBadge);
        return habboBadge;
    }

    public static void deleteBadge(int i, String str) {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("DELETE users_badges FROM users_badges WHERE user_id = ? AND badge_code LIKE ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, i);
                    preparedStatementPrepareStatement.setString(2, str);
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
            LOGGER.error("Caught SQL exception", e);
        }
    }

    public ArrayList<HabboBadge> getWearingBadges() {
        ArrayList<HabboBadge> arrayList;
        synchronized (this.badges) {
            arrayList = new ArrayList<>();
            TObjectHashIterator it = this.badges.iterator();
            while (it.hasNext()) {
                HabboBadge habboBadge = (HabboBadge) it.next();
                if (habboBadge.getSlot() != 0) {
                    arrayList.add(habboBadge);
                }
            }
            arrayList.sort(new Comparator<HabboBadge>() { // from class: com.eu.habbo.habbohotel.users.inventory.BadgesComponent.1
                @Override // java.util.Comparator
                public int compare(HabboBadge habboBadge2, HabboBadge habboBadge3) {
                    return habboBadge2.getSlot() - habboBadge3.getSlot();
                }
            });
        }
        return arrayList;
    }

    public THashSet<HabboBadge> getBadges() {
        return this.badges;
    }

    public boolean hasBadge(String str) {
        return getBadge(str) != null;
    }

    public HabboBadge getBadge(String str) {
        synchronized (this.badges) {
            TObjectHashIterator it = this.badges.iterator();
            while (it.hasNext()) {
                HabboBadge habboBadge = (HabboBadge) it.next();
                if (habboBadge.getCode().equalsIgnoreCase(str)) {
                    return habboBadge;
                }
            }
            return null;
        }
    }

    public void addBadge(HabboBadge habboBadge) {
        synchronized (this.badges) {
            this.badges.add(habboBadge);
        }
    }

    public HabboBadge removeBadge(String str) {
        synchronized (this.badges) {
            TObjectHashIterator it = this.badges.iterator();
            while (it.hasNext()) {
                HabboBadge habboBadge = (HabboBadge) it.next();
                if (habboBadge.getCode().equalsIgnoreCase(str)) {
                    this.badges.remove(habboBadge);
                    return habboBadge;
                }
            }
            return null;
        }
    }

    public void removeBadge(HabboBadge habboBadge) {
        synchronized (this.badges) {
            this.badges.remove(habboBadge);
        }
    }

    public void dispose() {
        synchronized (this.badges) {
            this.badges.clear();
        }
    }
}
