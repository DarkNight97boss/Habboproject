package com.eu.habbo.habbohotel.pets;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.pets.InteractionNest;
import com.eu.habbo.habbohotel.items.interactions.pets.InteractionPetDrink;
import com.eu.habbo.habbohotel.items.interactions.pets.InteractionPetFood;
import com.eu.habbo.habbohotel.items.interactions.pets.InteractionPetToy;
import com.eu.habbo.habbohotel.pets.actions.ActionBeg;
import com.eu.habbo.habbohotel.pets.actions.ActionBreatheFire;
import com.eu.habbo.habbohotel.pets.actions.ActionBreed;
import com.eu.habbo.habbohotel.pets.actions.ActionCroak;
import com.eu.habbo.habbohotel.pets.actions.ActionDip;
import com.eu.habbo.habbohotel.pets.actions.ActionDown;
import com.eu.habbo.habbohotel.pets.actions.ActionDrink;
import com.eu.habbo.habbohotel.pets.actions.ActionEat;
import com.eu.habbo.habbohotel.pets.actions.ActionFollow;
import com.eu.habbo.habbohotel.pets.actions.ActionFollowLeft;
import com.eu.habbo.habbohotel.pets.actions.ActionFollowRight;
import com.eu.habbo.habbohotel.pets.actions.ActionFree;
import com.eu.habbo.habbohotel.pets.actions.ActionHere;
import com.eu.habbo.habbohotel.pets.actions.ActionJump;
import com.eu.habbo.habbohotel.pets.actions.ActionMoveForward;
import com.eu.habbo.habbohotel.pets.actions.ActionNest;
import com.eu.habbo.habbohotel.pets.actions.ActionPlay;
import com.eu.habbo.habbohotel.pets.actions.ActionPlayDead;
import com.eu.habbo.habbohotel.pets.actions.ActionPlayFootball;
import com.eu.habbo.habbohotel.pets.actions.ActionRelax;
import com.eu.habbo.habbohotel.pets.actions.ActionSilent;
import com.eu.habbo.habbohotel.pets.actions.ActionSit;
import com.eu.habbo.habbohotel.pets.actions.ActionSpeak;
import com.eu.habbo.habbohotel.pets.actions.ActionStand;
import com.eu.habbo.habbohotel.pets.actions.ActionStay;
import com.eu.habbo.habbohotel.pets.actions.ActionTorch;
import com.eu.habbo.habbohotel.pets.actions.ActionTurnLeft;
import com.eu.habbo.habbohotel.pets.actions.ActionTurnRight;
import com.eu.habbo.habbohotel.pets.actions.ActionWave;
import com.eu.habbo.habbohotel.pets.actions.ActionWings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.set.hash.THashSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PetManager {
   public static int MAXIMUM_PET_INVENTORY_SIZE = 25;
   private static final Logger LOGGER = LoggerFactory.getLogger(PetManager.class);
   public static final int[] experiences = new int[]{
      100, 200, 400, 600, 900, 1300, 1800, 2400, 3200, 4300, 5700, 7600, 10100, 13300, 17500, 23000, 30200, 39600, 51900
   };
   static int[] skins = new int[]{0, 1, 6, 7};
   public final THashMap<Integer, PetAction> petActions = new THashMap<Integer, PetAction>() {
      {
         this.put(0, new ActionFree());
         this.put(1, new ActionSit());
         this.put(2, new ActionDown());
         this.put(3, new ActionHere());
         this.put(4, new ActionBeg());
         this.put(5, new ActionPlayDead());
         this.put(6, new ActionStay());
         this.put(7, new ActionFollow());
         this.put(8, new ActionStand());
         this.put(9, new ActionJump());
         this.put(10, new ActionSpeak());
         this.put(11, new ActionPlay());
         this.put(12, new ActionSilent());
         this.put(13, new ActionNest());
         this.put(14, new ActionDrink());
         this.put(15, new ActionFollowLeft());
         this.put(16, new ActionFollowRight());
         this.put(17, new ActionPlayFootball());
         this.put(24, new ActionMoveForward());
         this.put(25, new ActionTurnLeft());
         this.put(26, new ActionTurnRight());
         this.put(27, new ActionRelax());
         this.put(28, new ActionCroak());
         this.put(29, new ActionDip());
         this.put(30, new ActionWave());
         this.put(35, new ActionWings());
         this.put(36, new ActionBreatheFire());
         this.put(38, new ActionTorch());
         this.put(43, new ActionEat());
         this.put(46, new ActionBreed());
      }
   };
   private final THashMap<Integer, THashSet<PetRace>> petRaces;
   private final THashMap<Integer, PetData> petData;
   private final TIntIntMap breedingPetType;
   private final THashMap<Integer, TIntObjectHashMap<ArrayList<PetBreedingReward>>> breedingReward;

   public PetManager() {
      long millis = System.currentTimeMillis();
      this.petRaces = new THashMap();
      this.petData = new THashMap();
      this.breedingPetType = new TIntIntHashMap();
      this.breedingReward = new THashMap();
      this.reloadPetData();
      LOGGER.info("Pet Manager -> Loaded! (" + (System.currentTimeMillis() - millis) + " MS)");
   }

   public static int getLevel(int experience) {
      int index = -1;

      for (int i = 0; i < experiences.length; i++) {
         if (experiences[i] > experience) {
            index = i;
            break;
         }
      }

      if (index == -1) {
         index = experiences.length;
      }

      return index + 1;
   }

   public static int maxEnergy(int level) {
      return 100 * level;
   }

   public static int randomBody(int minimumRarity, boolean isRare) {
      int randomRarity = isRare
         ? random(Math.max(minimumRarity - 1, 0), MonsterplantPet.bodyRarity.size() - minimumRarity + (minimumRarity - 1), 2.0)
         : random(Math.max(minimumRarity - 1, 0), MonsterplantPet.bodyRarity.size(), 2.0);
      return (Integer)MonsterplantPet.bodyRarity.get(MonsterplantPet.bodyRarity.keySet().toArray()[randomRarity]).getValue();
   }

   public static int randomColor(int minimumRarity, boolean isRare) {
      int randomRarity = isRare
         ? random(Math.max(minimumRarity - 1, 0), MonsterplantPet.colorRarity.size() - minimumRarity + (minimumRarity - 1), 2.0)
         : random(Math.max(minimumRarity - 1, 0), MonsterplantPet.colorRarity.size(), 2.0);
      return (Integer)MonsterplantPet.colorRarity.get(MonsterplantPet.colorRarity.keySet().toArray()[randomRarity]).getValue();
   }

   public static int random(int low, int high, double bias) {
      double r = Math.random();
      r = Math.pow(r, bias);
      return (int)(low + (high - low) * r);
   }

   public static Pet loadPet(ResultSet set) throws SQLException {
      if (set.getInt("type") == 15) {
         return new HorsePet(set);
      } else if (set.getInt("type") == 16) {
         return new MonsterplantPet(set);
      } else {
         return set.getInt("type") != 26 && set.getInt("type") != 27 ? new Pet(set) : new GnomePet(set);
      }
   }

   public static NormalDistribution getNormalDistributionForBreeding(int levelOne, int levelTwo) {
      return getNormalDistributionForBreeding((levelOne + levelTwo) / 2);
   }

   public static NormalDistribution getNormalDistributionForBreeding(double avgLevel) {
      return new NormalDistribution(avgLevel, (20.0 - avgLevel / 2.0) / 2.0);
   }

   public void reloadPetData() {
      this.petRaces.clear();
      this.petData.clear();
      this.breedingPetType.clear();
      this.breedingReward.clear();

      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         try {
            this.loadRaces(connection);
            this.loadPetData(connection);
            this.loadPetCommands(connection);
            this.loadPetBreeding(connection);
         } catch (Throwable var5) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var4) {
                  var5.addSuppressed(var4);
               }
            }

            throw var5;
         }

         if (connection != null) {
            connection.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
         LOGGER.error("Pet Manager -> Failed to load!");
      }
   }

   private void loadRaces(Connection connection) {
      this.petRaces.clear();

      try {
         Statement statement = connection.createStatement();

         try {
            ResultSet set = statement.executeQuery("SELECT * FROM pet_breeds ORDER BY race, color_one, color_two ASC");

            try {
               for (; set.next(); ((THashSet)this.petRaces.get(set.getInt("race"))).add(new PetRace(set))) {
                  if (this.petRaces.get(set.getInt("race")) == null) {
                     this.petRaces.put(set.getInt("race"), new THashSet());
                  }
               }
            } catch (Throwable var8) {
               if (set != null) {
                  try {
                     set.close();
                  } catch (Throwable var7) {
                     var8.addSuppressed(var7);
                  }
               }

               throw var8;
            }

            if (set != null) {
               set.close();
            }
         } catch (Throwable var9) {
            if (statement != null) {
               try {
                  statement.close();
               } catch (Throwable var6) {
                  var9.addSuppressed(var6);
               }
            }

            throw var9;
         }

         if (statement != null) {
            statement.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }
   }

   private void loadPetData(Connection connection) {
      try {
         Statement statement = connection.createStatement();

         try {
            ResultSet set = statement.executeQuery("SELECT * FROM pet_actions ORDER BY pet_type ASC");

            try {
               while (set.next()) {
                  this.petData.put(set.getInt("pet_type"), new PetData(set));
               }
            } catch (Throwable var8) {
               if (set != null) {
                  try {
                     set.close();
                  } catch (Throwable var7) {
                     var8.addSuppressed(var7);
                  }
               }

               throw var8;
            }

            if (set != null) {
               set.close();
            }
         } catch (Throwable var9) {
            if (statement != null) {
               try {
                  statement.close();
               } catch (Throwable var6) {
                  var9.addSuppressed(var6);
               }
            }

            throw var9;
         }

         if (statement != null) {
            statement.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }

      this.loadPetItems(connection);
      this.loadPetVocals(connection);
   }

   private void loadPetItems(Connection connection) {
      try {
         Statement statement = connection.createStatement();

         try {
            ResultSet set = statement.executeQuery("SELECT * FROM pet_items");

            try {
               while (set.next()) {
                  Item baseItem = Emulator.getGameEnvironment().getItemManager().getItem(set.getInt("item_id"));
                  if (baseItem != null) {
                     if (set.getInt("pet_id") == -1) {
                        if (baseItem.getInteractionType().getType() == InteractionNest.class) {
                           PetData.generalNestItems.add(baseItem);
                        } else if (baseItem.getInteractionType().getType() == InteractionPetFood.class) {
                           PetData.generalFoodItems.add(baseItem);
                        } else if (baseItem.getInteractionType().getType() == InteractionPetDrink.class) {
                           PetData.generalDrinkItems.add(baseItem);
                        } else if (baseItem.getInteractionType().getType() == InteractionPetToy.class) {
                           PetData.generalToyItems.add(baseItem);
                        }
                     } else {
                        PetData data = this.getPetData(set.getInt("pet_id"));
                        if (data != null) {
                           if (baseItem.getInteractionType().getType() == InteractionNest.class) {
                              data.addNest(baseItem);
                           } else if (baseItem.getInteractionType().getType() == InteractionPetFood.class) {
                              data.addFoodItem(baseItem);
                           } else if (baseItem.getInteractionType().getType() == InteractionPetDrink.class) {
                              data.addDrinkItem(baseItem);
                           } else if (baseItem.getInteractionType().getType() == InteractionPetToy.class) {
                              data.addToyItem(baseItem);
                           }
                        }
                     }
                  }
               }
            } catch (Throwable var8) {
               if (set != null) {
                  try {
                     set.close();
                  } catch (Throwable var7) {
                     var8.addSuppressed(var7);
                  }
               }

               throw var8;
            }

            if (set != null) {
               set.close();
            }
         } catch (Throwable var9) {
            if (statement != null) {
               try {
                  statement.close();
               } catch (Throwable var6) {
                  var9.addSuppressed(var6);
               }
            }

            throw var9;
         }

         if (statement != null) {
            statement.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }
   }

   private void loadPetVocals(Connection connection) {
      try {
         Statement statement = connection.createStatement();

         try {
            ResultSet set = statement.executeQuery("SELECT * FROM pet_vocals");

            try {
               while (set.next()) {
                  if (set.getInt("pet_id") >= 0) {
                     if (this.petData.containsKey(set.getInt("pet_id"))) {
                        PetVocalsType petVocalsType = PetVocalsType.valueOf(set.getString("type").toUpperCase());
                        if (petVocalsType != null) {
                           ((THashSet)((PetData)this.petData.get(set.getInt("pet_id"))).petVocals.get(petVocalsType))
                              .add(new PetVocal(set.getString("message")));
                        } else {
                           LOGGER.error("Unknown pet vocal type " + set.getString("type"));
                        }
                     } else {
                        LOGGER.error("Missing pet_actions table entry for pet id " + set.getInt("pet_id"));
                     }
                  } else {
                     if (!PetData.generalPetVocals.containsKey(PetVocalsType.valueOf(set.getString("type").toUpperCase()))) {
                        PetData.generalPetVocals.put(PetVocalsType.valueOf(set.getString("type").toUpperCase()), new THashSet());
                     }

                     ((THashSet)PetData.generalPetVocals.get(PetVocalsType.valueOf(set.getString("type").toUpperCase())))
                        .add(new PetVocal(set.getString("message")));
                  }
               }
            } catch (Throwable var8) {
               if (set != null) {
                  try {
                     set.close();
                  } catch (Throwable var7) {
                     var8.addSuppressed(var7);
                  }
               }

               throw var8;
            }

            if (set != null) {
               set.close();
            }
         } catch (Throwable var9) {
            if (statement != null) {
               try {
                  statement.close();
               } catch (Throwable var6) {
                  var9.addSuppressed(var6);
               }
            }

            throw var9;
         }

         if (statement != null) {
            statement.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }
   }

   private void loadPetCommands(Connection connection) {
      THashMap<Integer, PetCommand> commandsList = new THashMap();

      try {
         Statement statement = connection.createStatement();

         try {
            ResultSet set = statement.executeQuery("SELECT * FROM pet_commands_data");

            try {
               while (set.next()) {
                  commandsList.put(set.getInt("command_id"), new PetCommand(set, (PetAction)this.petActions.get(set.getInt("command_id"))));
               }
            } catch (Throwable var14) {
               if (set != null) {
                  try {
                     set.close();
                  } catch (Throwable var10) {
                     var14.addSuppressed(var10);
                  }
               }

               throw var14;
            }

            if (set != null) {
               set.close();
            }
         } catch (Throwable var15) {
            if (statement != null) {
               try {
                  statement.close();
               } catch (Throwable var9) {
                  var15.addSuppressed(var9);
               }
            }

            throw var15;
         }

         if (statement != null) {
            statement.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }

      try {
         Statement statement = connection.createStatement();

         try {
            ResultSet set = statement.executeQuery("SELECT * FROM pet_commands ORDER BY pet_id ASC");

            try {
               while (set.next()) {
                  PetData data = (PetData)this.petData.get(set.getInt("pet_id"));
                  if (data != null) {
                     data.getPetCommands().add((PetCommand)commandsList.get(set.getInt("command_id")));
                  }
               }
            } catch (Throwable var11) {
               if (set != null) {
                  try {
                     set.close();
                  } catch (Throwable var8) {
                     var11.addSuppressed(var8);
                  }
               }

               throw var11;
            }

            if (set != null) {
               set.close();
            }
         } catch (Throwable var12) {
            if (statement != null) {
               try {
                  statement.close();
               } catch (Throwable var7) {
                  var12.addSuppressed(var7);
               }
            }

            throw var12;
         }

         if (statement != null) {
            statement.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }
   }

   private void loadPetBreeding(Connection connection) {
      try {
         Statement statement = connection.createStatement();

         try {
            ResultSet set = statement.executeQuery("SELECT * FROM pet_breeding");

            try {
               while (set.next()) {
                  this.breedingPetType.put(set.getInt("pet_id"), set.getInt("offspring_id"));
               }
            } catch (Throwable var13) {
               if (set != null) {
                  try {
                     set.close();
                  } catch (Throwable var9) {
                     var13.addSuppressed(var9);
                  }
               }

               throw var13;
            }

            if (set != null) {
               set.close();
            }
         } catch (Throwable var14) {
            if (statement != null) {
               try {
                  statement.close();
               } catch (Throwable var8) {
                  var14.addSuppressed(var8);
               }
            }

            throw var14;
         }

         if (statement != null) {
            statement.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }

      try {
         Statement statement = connection.createStatement();

         try {
            ResultSet set = statement.executeQuery("SELECT * FROM pet_breeding_races");

            try {
               while (set.next()) {
                  PetBreedingReward reward = new PetBreedingReward(set);
                  if (!this.breedingReward.containsKey(reward.petType)) {
                     this.breedingReward.put(reward.petType, new TIntObjectHashMap());
                  }

                  if (!((TIntObjectHashMap)this.breedingReward.get(reward.petType)).containsKey(reward.rarityLevel)) {
                     ((TIntObjectHashMap)this.breedingReward.get(reward.petType)).put(reward.rarityLevel, new ArrayList());
                  }

                  ((ArrayList)((TIntObjectHashMap)this.breedingReward.get(reward.petType)).get(reward.rarityLevel)).add(reward);
               }
            } catch (Throwable var10) {
               if (set != null) {
                  try {
                     set.close();
                  } catch (Throwable var7) {
                     var10.addSuppressed(var7);
                  }
               }

               throw var10;
            }

            if (set != null) {
               set.close();
            }
         } catch (Throwable var11) {
            if (statement != null) {
               try {
                  statement.close();
               } catch (Throwable var6) {
                  var11.addSuppressed(var6);
               }
            }

            throw var11;
         }

         if (statement != null) {
            statement.close();
         }
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
      }
   }

   public THashSet<PetRace> getBreeds(String petName) {
      if (!petName.startsWith("a0 pet")) {
         LOGGER.error("Pet " + petName + " not found. Make sure it matches the pattern \"a0 pet<pet_id>\"!");
         return null;
      }

      try {
         int petId = Integer.valueOf(petName.split("t")[1]);
         return (THashSet<PetRace>)this.petRaces.get(petId);
      } catch (Exception e) {
         LOGGER.error("Caught exception", e);
         return null;
      }
   }

   public TIntObjectHashMap<ArrayList<PetBreedingReward>> getBreedingRewards(int petType) {
      return (TIntObjectHashMap<ArrayList<PetBreedingReward>>)this.breedingReward.get(petType);
   }

   public int getRarityForOffspring(final Pet pet) {
      final int[] rarityLevel = new int[]{0};
      TIntObjectHashMap<ArrayList<PetBreedingReward>> offspringList = (TIntObjectHashMap<ArrayList<PetBreedingReward>>)this.breedingReward
         .get(pet.getPetData().getType());
      offspringList.forEachEntry(new TIntObjectProcedure<ArrayList<PetBreedingReward>>() {
         public boolean execute(int i, ArrayList<PetBreedingReward> petBreedingRewards) {
            for (PetBreedingReward reward : petBreedingRewards) {
               if (reward.breed == pet.getRace()) {
                  rarityLevel[0] = i;
                  return false;
               }
            }

            return true;
         }
      });
      return 4 - rarityLevel[0];
   }

   public PetData getPetData(int type) {
      synchronized (this.petData) {
         if (this.petData.containsKey(type)) {
            return (PetData)this.petData.get(type);
         }

         try {
            Connection connection;
            label153: {
               connection = Emulator.getDatabase().getDataSource().getConnection();

               PetData var7;
               try {
                  LOGGER.error("Missing petdata for type " + type + ". Adding this to the database...");
                  PreparedStatement statement = connection.prepareStatement("INSERT INTO pet_actions (pet_type) VALUES (?)");

                  try {
                     statement.setInt(1, type);
                     statement.execute();
                  } catch (Throwable var13) {
                     if (statement != null) {
                        try {
                           statement.close();
                        } catch (Throwable var12) {
                           var13.addSuppressed(var12);
                        }
                     }

                     throw var13;
                  }

                  if (statement != null) {
                     statement.close();
                  }

                  statement = connection.prepareStatement("SELECT * FROM pet_actions WHERE pet_type = ? LIMIT 1");

                  label141: {
                     try {
                        statement.setInt(1, type);
                        ResultSet set = statement.executeQuery();

                        label143: {
                           try {
                              if (!set.next()) {
                                 break label143;
                              }

                              PetData petData = new PetData(set);
                              this.petData.put(type, petData);
                              LOGGER.error("Missing petdata for type " + type + " added to the database!");
                              var7 = petData;
                           } catch (Throwable var14) {
                              if (set != null) {
                                 try {
                                    set.close();
                                 } catch (Throwable var11) {
                                    var14.addSuppressed(var11);
                                 }
                              }

                              throw var14;
                           }

                           if (set != null) {
                              set.close();
                           }
                           break label141;
                        }

                        if (set != null) {
                           set.close();
                        }
                     } catch (Throwable var15) {
                        if (statement != null) {
                           try {
                              statement.close();
                           } catch (Throwable var10) {
                              var15.addSuppressed(var10);
                           }
                        }

                        throw var15;
                     }

                     if (statement != null) {
                        statement.close();
                     }
                     break label153;
                  }

                  if (statement != null) {
                     statement.close();
                  }
               } catch (Throwable var16) {
                  if (connection != null) {
                     try {
                        connection.close();
                     } catch (Throwable var9) {
                        var16.addSuppressed(var9);
                     }
                  }

                  throw var16;
               }

               if (connection != null) {
                  connection.close();
               }

               return var7;
            }

            if (connection != null) {
               connection.close();
            }
         } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
         }

         return null;
      }
   }

   public PetData getPetData(String petName) {
      synchronized (this.petData) {
         for (Entry<Integer, PetData> entry : this.petData.entrySet()) {
            if (entry.getValue().getName().equalsIgnoreCase(petName)) {
               return entry.getValue();
            }
         }

         return null;
      }
   }

   public Collection<PetData> getPetData() {
      return this.petData.values();
   }

   public Pet createPet(Item item, String name, String race, String color, GameClient client) {
      int type = Integer.valueOf(item.getName().toLowerCase().replace("a0 pet", ""));
      if (this.petData.containsKey(type)) {
         Pet pet;
         if (type == 15) {
            pet = new HorsePet(type, Integer.valueOf(race), color, name, client.getHabbo().getHabboInfo().getId());
         } else if (type == 16) {
            pet = this.createMonsterplant(null, client.getHabbo(), false, null, 0);
         } else {
            pet = new Pet(type, Integer.valueOf(race), color, name, client.getHabbo().getHabboInfo().getId());
         }

         pet.needsUpdate = true;
         pet.run();
         return pet;
      } else {
         return null;
      }
   }

   public Pet createPet(int type, String name, GameClient client) {
      return this.createPet(type, Emulator.getRandom().nextInt(((THashSet)this.petRaces.get(type)).size() + 1), name, client);
   }

   public Pet createPet(int type, int race, String name, GameClient client) {
      if (this.petData.containsKey(type)) {
         Pet pet = new Pet(type, race, "FFFFFF", name, client.getHabbo().getHabboInfo().getId());
         pet.needsUpdate = true;
         pet.run();
         return pet;
      } else {
         return null;
      }
   }

   public MonsterplantPet createMonsterplant(Room room, Habbo habbo, boolean rare, RoomTile t, int minimumRarity) {
      MonsterplantPet pet = new MonsterplantPet(
         habbo.getHabboInfo().getId(),
         randomBody(minimumRarity, rare),
         randomColor(minimumRarity, rare),
         Emulator.getRandom().nextInt(12) + 1,
         Emulator.getRandom().nextInt(11),
         Emulator.getRandom().nextInt(12) + 1,
         Emulator.getRandom().nextInt(11),
         Emulator.getRandom().nextInt(12) + 1,
         Emulator.getRandom().nextInt(11)
      );
      pet.setUserId(habbo.getHabboInfo().getId());
      pet.setRoom(room);
      pet.setRoomUnit(new RoomUnit());
      pet.getRoomUnit().setPathFinderRoom(room);
      pet.needsUpdate = true;
      pet.run();
      return pet;
   }

   public Pet createGnome(String name, Room room, Habbo habbo) {
      Pet pet = new GnomePet(
         26,
         0,
         "FFFFFF",
         name,
         habbo.getHabboInfo().getId(),
         "5 0 -1 "
            + this.randomGnomeSkinColor()
            + " 1 10"
            + (1 + Emulator.getRandom().nextInt(2))
            + " "
            + this.randomGnomeColor()
            + " 2 201 "
            + this.randomGnomeColor()
            + " 3 30"
            + (1 + Emulator.getRandom().nextInt(2))
            + " "
            + this.randomGnomeColor()
            + " 4 40"
            + Emulator.getRandom().nextInt(2)
            + " "
            + this.randomGnomeColor()
      );
      pet.setUserId(habbo.getHabboInfo().getId());
      pet.setRoom(room);
      pet.setRoomUnit(new RoomUnit());
      pet.getRoomUnit().setPathFinderRoom(room);
      pet.needsUpdate = true;
      pet.run();
      return pet;
   }

   public Pet createLeprechaun(String name, Room room, Habbo habbo) {
      Pet pet = new GnomePet(27, 0, "FFFFFF", name, habbo.getHabboInfo().getId(), "5 0 -1 0 1 102 19 2 201 27 3 302 23 4 401 27");
      pet.setUserId(habbo.getHabboInfo().getId());
      pet.setRoom(room);
      pet.setRoomUnit(new RoomUnit());
      pet.getRoomUnit().setPathFinderRoom(room);
      pet.needsUpdate = true;
      pet.run();
      return pet;
   }

   private int randomGnomeColor() {
      int color = 19;

      while (color == 19 || color == 27) {
         color = Emulator.getRandom().nextInt(34);
      }

      return color;
   }

   private int randomLeprechaunColor() {
      return Emulator.getRandom().nextInt(2) == 1 ? 19 : 27;
   }

   private int randomGnomeSkinColor() {
      return skins[Emulator.getRandom().nextInt(skins.length)];
   }

   public boolean deletePet(Pet pet) {
      try {
         Connection connection = Emulator.getDatabase().getDataSource().getConnection();

         boolean var4;
         try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM users_pets WHERE id = ? LIMIT 1");

            try {
               statement.setInt(1, pet.getId());
               var4 = statement.execute();
            } catch (Throwable var8) {
               if (statement != null) {
                  try {
                     statement.close();
                  } catch (Throwable var7) {
                     var8.addSuppressed(var7);
                  }
               }

               throw var8;
            }

            if (statement != null) {
               statement.close();
            }
         } catch (Throwable var9) {
            if (connection != null) {
               try {
                  connection.close();
               } catch (Throwable var6) {
                  var9.addSuppressed(var6);
               }
            }

            throw var9;
         }

         if (connection != null) {
            connection.close();
         }

         return var4;
      } catch (SQLException e) {
         LOGGER.error("Caught SQL exception", e);
         return false;
      }
   }
}
