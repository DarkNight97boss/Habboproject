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

public class PetsComponent {
   private static final Logger LOGGER = LoggerFactory.getLogger(PetsComponent.class);
   private final TIntObjectMap<Pet> pets = TCollections.synchronizedMap(new TIntObjectHashMap());

   public PetsComponent(Habbo habbo) {
      this.loadPets(habbo);
   }

   private void loadPets(Habbo habbo) {
      synchronized (this.pets) {
         try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            try {
               PreparedStatement statement = connection.prepareStatement("SELECT * FROM users_pets WHERE user_id = ? AND room_id = 0");

               try {
                  statement.setInt(1, habbo.getHabboInfo().getId());
                  ResultSet set = statement.executeQuery();

                  try {
                     while (set.next()) {
                        this.pets.put(set.getInt("id"), PetManager.loadPet(set));
                     }
                  } catch (Throwable var12) {
                     if (set != null) {
                        try {
                           set.close();
                        } catch (Throwable var11) {
                           var12.addSuppressed(var11);
                        }
                     }

                     throw var12;
                  }

                  if (set != null) {
                     set.close();
                  }
               } catch (Throwable var13) {
                  if (statement != null) {
                     try {
                        statement.close();
                     } catch (Throwable var10) {
                        var13.addSuppressed(var10);
                     }
                  }

                  throw var13;
               }

               if (statement != null) {
                  statement.close();
               }
            } catch (Throwable var14) {
               if (connection != null) {
                  try {
                     connection.close();
                  } catch (Throwable var9) {
                     var14.addSuppressed(var9);
                  }
               }

               throw var14;
            }

            if (connection != null) {
               connection.close();
            }
         } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
         }
      }
   }

   public Pet getPet(int id) {
      return (Pet)this.pets.get(id);
   }

   public void addPet(Pet pet) {
      synchronized (this.pets) {
         this.pets.put(pet.getId(), pet);
      }
   }

   public void addPets(Set<Pet> pets) {
      synchronized (this.pets) {
         for (Pet p : pets) {
            this.pets.put(p.getId(), p);
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
         TIntObjectIterator<Pet> petIterator = this.pets.iterator();
         int i = this.pets.size();

         while (i-- > 0) {
            try {
               petIterator.advance();
            } catch (NoSuchElementException e) {
               break;
            }

            if (((Pet)petIterator.value()).needsUpdate) {
               Emulator.getThreading().run((Runnable)petIterator.value());
            }
         }
      }
   }
}
