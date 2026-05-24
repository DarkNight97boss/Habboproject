package com.eu.habbo.habbohotel.users.inventory;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.PetManager;
import com.eu.habbo.habbohotel.users.Habbo;
import gnu.trove.TCollections;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/users/inventory/PetsComponent.class */
public class PetsComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(PetsComponent.class);
    private final TIntObjectMap<Pet> pets = TCollections.synchronizedMap(new TIntObjectHashMap());

    public PetsComponent(Habbo habbo) {
        loadPets(habbo);
    }

    private void loadPets(Habbo habbo) {
        Connection connection;
        synchronized (this.pets) {
            try {
                connection = Emulator.getDatabase().getDataSource().getConnection();
            } catch (SQLException e) {
                LOGGER.error("Caught SQL exception", e);
            }
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM users_pets WHERE user_id = ? AND room_id = 0");
                try {
                    preparedStatementPrepareStatement.setInt(1, habbo.getHabboInfo().getId());
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        try {
                            this.pets.put(resultSetExecuteQuery.getInt("id"), PetManager.loadPet(resultSetExecuteQuery));
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

    public Pet getPet(int i) {
        return (Pet) this.pets.get(i);
    }

    public void addPet(Pet pet) {
        synchronized (this.pets) {
            this.pets.put(pet.getId(), pet);
        }
    }

    public void addPets(Set<Pet> set) {
        synchronized (this.pets) {
            for (Pet pet : set) {
                this.pets.put(pet.getId(), pet);
            }
        }
    }

    public void removePet(Pet pet) {
        synchronized (this.pets) {
            this.pets.remove(pet.getId());
        }
    }

    public TIntObjectMap<Pet> getPets() {
        return this.pets;
    }

    public int getPetsCount() {
        return this.pets.size();
    }

    public void dispose() {
        synchronized (this.pets) {
            TIntObjectIterator it = this.pets.iterator();
            int size = this.pets.size();
            while (true) {
                int i = size;
                size--;
                if (i <= 0) {
                    break;
                }
                try {
                    it.advance();
                    if (((Pet) it.value()).needsUpdate) {
                        Emulator.getThreading().run((Runnable) it.value());
                    }
                } catch (NoSuchElementException e) {
                }
            }
        }
    }
}
