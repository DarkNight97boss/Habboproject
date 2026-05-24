package com.eu.habbo.habbohotel.users.subscriptions;

import com.eu.habbo.Emulator;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/users/subscriptions/SubscriptionManager.class */
public class SubscriptionManager {
    public static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionManager.class);
    public THashMap<String, Class<? extends Subscription>> types = new THashMap<>();

    public void init() {
        this.types.put(Subscription.HABBO_CLUB, SubscriptionHabboClub.class);
    }

    public void addSubscriptionType(String str, Class<? extends Subscription> cls) {
        if (this.types.containsKey(str) || this.types.containsValue(cls)) {
            throw new RuntimeException("Subscription Type must be unique. An class with type: " + cls.getName() + " was already added OR the key: " + str + " is already in use.");
        }
        this.types.put(str, cls);
    }

    public void removeSubscriptionType(String str) {
        this.types.remove(str);
    }

    public Class<? extends Subscription> getSubscriptionClass(String str) {
        if (this.types.containsKey(str)) {
            return (Class) this.types.get(str);
        }
        LOGGER.debug("Can't find subscription class: {}", str);
        return Subscription.class;
    }

    public void dispose() {
        this.types.clear();
    }

    public THashSet<Subscription> getSubscriptionsForUser(int i) {
        THashSet<Subscription> tHashSet = new THashSet<>();
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM users_subscriptions WHERE user_id = ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, i);
                    try {
                        ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                        while (resultSetExecuteQuery.next()) {
                            try {
                                Constructor<? extends Subscription> constructor = Emulator.getGameEnvironment().getSubscriptionManager().getSubscriptionClass(resultSetExecuteQuery.getString("subscription_type")).getConstructor(Integer.class, Integer.class, String.class, Integer.class, Integer.class, Boolean.class);
                                constructor.setAccessible(true);
                                Object[] objArr = new Object[6];
                                objArr[0] = Integer.valueOf(resultSetExecuteQuery.getInt("id"));
                                objArr[1] = Integer.valueOf(resultSetExecuteQuery.getInt("user_id"));
                                objArr[2] = resultSetExecuteQuery.getString("subscription_type");
                                objArr[3] = Integer.valueOf(resultSetExecuteQuery.getInt("timestamp_start"));
                                objArr[4] = Integer.valueOf(resultSetExecuteQuery.getInt("duration"));
                                objArr[5] = Boolean.valueOf(resultSetExecuteQuery.getInt("active") == 1);
                                tHashSet.add(constructor.newInstance(objArr));
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
                    } catch (IllegalAccessException e) {
                        LOGGER.error("IllegalAccessException", e);
                    } catch (InstantiationException e2) {
                        LOGGER.error("InstantiationException", e2);
                    } catch (InvocationTargetException e3) {
                        LOGGER.error("InvocationTargetException", e3);
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
        } catch (NoSuchMethodException e4) {
            LOGGER.error("Caught NoSuchMethodException", e4);
        } catch (SQLException e5) {
            LOGGER.error("Caught SQL exception", e5);
        }
        return tHashSet;
    }
}
