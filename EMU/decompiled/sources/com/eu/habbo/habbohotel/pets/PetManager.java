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
import com.eu.habbo.messages.outgoing.rooms.items.rentablespaces.RentableSpaceInfoComposer;
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
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/pets/PetManager.class */
public class PetManager {
    public static int MAXIMUM_PET_INVENTORY_SIZE = 25;
    private static final Logger LOGGER = LoggerFactory.getLogger(PetManager.class);
    public static final int[] experiences = {100, RentableSpaceInfoComposer.NOT_ENOUGH_CREDITS, RentableSpaceInfoComposer.CANT_RENT_GENERIC, 600, 900, 1300, 1800, 2400, 3200, 4300, 5700, 7600, 10100, 13300, 17500, 23000, 30200, 39600, 51900};
    static int[] skins = {0, 1, 6, 7};
    public final THashMap<Integer, PetAction> petActions = new THashMap<Integer, PetAction>() { // from class: com.eu.habbo.habbohotel.pets.PetManager.1
        {
            put(0, new ActionFree());
            put(1, new ActionSit());
            put(2, new ActionDown());
            put(3, new ActionHere());
            put(4, new ActionBeg());
            put(5, new ActionPlayDead());
            put(6, new ActionStay());
            put(7, new ActionFollow());
            put(8, new ActionStand());
            put(9, new ActionJump());
            put(10, new ActionSpeak());
            put(11, new ActionPlay());
            put(12, new ActionSilent());
            put(13, new ActionNest());
            put(14, new ActionDrink());
            put(15, new ActionFollowLeft());
            put(16, new ActionFollowRight());
            put(17, new ActionPlayFootball());
            put(24, new ActionMoveForward());
            put(25, new ActionTurnLeft());
            put(26, new ActionTurnRight());
            put(27, new ActionRelax());
            put(28, new ActionCroak());
            put(29, new ActionDip());
            put(30, new ActionWave());
            put(35, new ActionWings());
            put(36, new ActionBreatheFire());
            put(38, new ActionTorch());
            put(43, new ActionEat());
            put(46, new ActionBreed());
        }
    };
    private final THashMap<Integer, THashSet<PetRace>> petRaces;
    private final THashMap<Integer, PetData> petData;
    private final TIntIntMap breedingPetType;
    private final THashMap<Integer, TIntObjectHashMap<ArrayList<PetBreedingReward>>> breedingReward;

    public PetManager() {
        long jCurrentTimeMillis = System.currentTimeMillis();
        this.petRaces = new THashMap<>();
        this.petData = new THashMap<>();
        this.breedingPetType = new TIntIntHashMap();
        this.breedingReward = new THashMap<>();
        reloadPetData();
        LOGGER.info("Pet Manager -> Loaded! (" + (System.currentTimeMillis() - jCurrentTimeMillis) + " MS)");
    }

    public static int getLevel(int i) {
        int length = -1;
        int i2 = 0;
        while (true) {
            if (i2 >= experiences.length) {
                break;
            }
            if (experiences[i2] > i) {
                length = i2;
                break;
            }
            i2++;
        }
        if (length == -1) {
            length = experiences.length;
        }
        return length + 1;
    }

    public static int maxEnergy(int i) {
        return 100 * i;
    }

    public static int randomBody(int i, boolean z) {
        return ((Integer) MonsterplantPet.bodyRarity.get(MonsterplantPet.bodyRarity.keySet().toArray()[z ? random(Math.max(i - 1, 0), (MonsterplantPet.bodyRarity.size() - i) + (i - 1), 2.0d) : random(Math.max(i - 1, 0), MonsterplantPet.bodyRarity.size(), 2.0d)]).getValue()).intValue();
    }

    public static int randomColor(int i, boolean z) {
        return ((Integer) MonsterplantPet.colorRarity.get(MonsterplantPet.colorRarity.keySet().toArray()[z ? random(Math.max(i - 1, 0), (MonsterplantPet.colorRarity.size() - i) + (i - 1), 2.0d) : random(Math.max(i - 1, 0), MonsterplantPet.colorRarity.size(), 2.0d)]).getValue()).intValue();
    }

    public static int random(int i, int i2, double d) {
        return (int) (((double) i) + (((double) (i2 - i)) * Math.pow(Math.random(), d)));
    }

    public static Pet loadPet(ResultSet resultSet) throws SQLException {
        return resultSet.getInt("type") == 15 ? new HorsePet(resultSet) : resultSet.getInt("type") == 16 ? new MonsterplantPet(resultSet) : (resultSet.getInt("type") == 26 || resultSet.getInt("type") == 27) ? new GnomePet(resultSet) : new Pet(resultSet);
    }

    public static NormalDistribution getNormalDistributionForBreeding(int i, int i2) {
        return getNormalDistributionForBreeding((i + i2) / 2);
    }

    public static NormalDistribution getNormalDistributionForBreeding(double d) {
        return new NormalDistribution(d, (20.0d - (d / 2.0d)) / 2.0d);
    }

    public void reloadPetData() {
        this.petRaces.clear();
        this.petData.clear();
        this.breedingPetType.clear();
        this.breedingReward.clear();
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                loadRaces(connection);
                loadPetData(connection);
                loadPetCommands(connection);
                loadPetBreeding(connection);
                if (connection != null) {
                    connection.close();
                }
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
            LOGGER.error("Pet Manager -> Failed to load!");
        }
    }

    private void loadRaces(Connection connection) {
        this.petRaces.clear();
        try {
            Statement statementCreateStatement = connection.createStatement();
            try {
                ResultSet resultSetExecuteQuery = statementCreateStatement.executeQuery("SELECT * FROM pet_breeds ORDER BY race, color_one, color_two ASC");
                while (resultSetExecuteQuery.next()) {
                    try {
                        if (this.petRaces.get(Integer.valueOf(resultSetExecuteQuery.getInt("race"))) == null) {
                            this.petRaces.put(Integer.valueOf(resultSetExecuteQuery.getInt("race")), new THashSet());
                        }
                        ((THashSet) this.petRaces.get(Integer.valueOf(resultSetExecuteQuery.getInt("race")))).add(new PetRace(resultSetExecuteQuery));
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
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
    }

    private void loadPetData(Connection connection) {
        try {
            Statement statementCreateStatement = connection.createStatement();
            try {
                ResultSet resultSetExecuteQuery = statementCreateStatement.executeQuery("SELECT * FROM pet_actions ORDER BY pet_type ASC");
                while (resultSetExecuteQuery.next()) {
                    try {
                        this.petData.put(Integer.valueOf(resultSetExecuteQuery.getInt("pet_type")), new PetData(resultSetExecuteQuery));
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
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        loadPetItems(connection);
        loadPetVocals(connection);
    }

    private void loadPetItems(Connection connection) {
        try {
            Statement statementCreateStatement = connection.createStatement();
            try {
                ResultSet resultSetExecuteQuery = statementCreateStatement.executeQuery("SELECT * FROM pet_items");
                while (resultSetExecuteQuery.next()) {
                    try {
                        Item item = Emulator.getGameEnvironment().getItemManager().getItem(resultSetExecuteQuery.getInt("item_id"));
                        if (item != null) {
                            if (resultSetExecuteQuery.getInt("pet_id") != -1) {
                                PetData petData = getPetData(resultSetExecuteQuery.getInt("pet_id"));
                                if (petData != null) {
                                    if (item.getInteractionType().getType() == InteractionNest.class) {
                                        petData.addNest(item);
                                    } else if (item.getInteractionType().getType() == InteractionPetFood.class) {
                                        petData.addFoodItem(item);
                                    } else if (item.getInteractionType().getType() == InteractionPetDrink.class) {
                                        petData.addDrinkItem(item);
                                    } else if (item.getInteractionType().getType() == InteractionPetToy.class) {
                                        petData.addToyItem(item);
                                    }
                                }
                            } else if (item.getInteractionType().getType() == InteractionNest.class) {
                                PetData.generalNestItems.add(item);
                            } else if (item.getInteractionType().getType() == InteractionPetFood.class) {
                                PetData.generalFoodItems.add(item);
                            } else if (item.getInteractionType().getType() == InteractionPetDrink.class) {
                                PetData.generalDrinkItems.add(item);
                            } else if (item.getInteractionType().getType() == InteractionPetToy.class) {
                                PetData.generalToyItems.add(item);
                            }
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
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
    }

    private void loadPetVocals(Connection connection) {
        try {
            Statement statementCreateStatement = connection.createStatement();
            try {
                ResultSet resultSetExecuteQuery = statementCreateStatement.executeQuery("SELECT * FROM pet_vocals");
                while (resultSetExecuteQuery.next()) {
                    try {
                        if (resultSetExecuteQuery.getInt("pet_id") < 0) {
                            if (!PetData.generalPetVocals.containsKey(PetVocalsType.valueOf(resultSetExecuteQuery.getString("type").toUpperCase()))) {
                                PetData.generalPetVocals.put(PetVocalsType.valueOf(resultSetExecuteQuery.getString("type").toUpperCase()), new THashSet());
                            }
                            ((THashSet) PetData.generalPetVocals.get(PetVocalsType.valueOf(resultSetExecuteQuery.getString("type").toUpperCase()))).add(new PetVocal(resultSetExecuteQuery.getString("message")));
                        } else if (this.petData.containsKey(Integer.valueOf(resultSetExecuteQuery.getInt("pet_id")))) {
                            PetVocalsType petVocalsTypeValueOf = PetVocalsType.valueOf(resultSetExecuteQuery.getString("type").toUpperCase());
                            if (petVocalsTypeValueOf != null) {
                                ((THashSet) ((PetData) this.petData.get(Integer.valueOf(resultSetExecuteQuery.getInt("pet_id")))).petVocals.get(petVocalsTypeValueOf)).add(new PetVocal(resultSetExecuteQuery.getString("message")));
                            } else {
                                LOGGER.error("Unknown pet vocal type " + resultSetExecuteQuery.getString("type"));
                            }
                        } else {
                            LOGGER.error("Missing pet_actions table entry for pet id " + resultSetExecuteQuery.getInt("pet_id"));
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
            } finally {
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
    }

    private void loadPetCommands(Connection connection) {
        Statement statementCreateStatement;
        THashMap tHashMap = new THashMap();
        try {
            statementCreateStatement = connection.createStatement();
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            ResultSet resultSetExecuteQuery = statementCreateStatement.executeQuery("SELECT * FROM pet_commands_data");
            while (resultSetExecuteQuery.next()) {
                try {
                    tHashMap.put(Integer.valueOf(resultSetExecuteQuery.getInt("command_id")), new PetCommand(resultSetExecuteQuery, (PetAction) this.petActions.get(Integer.valueOf(resultSetExecuteQuery.getInt("command_id")))));
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
            try {
                statementCreateStatement = connection.createStatement();
                try {
                    ResultSet resultSetExecuteQuery2 = statementCreateStatement.executeQuery("SELECT * FROM pet_commands ORDER BY pet_id ASC");
                    while (resultSetExecuteQuery2.next()) {
                        try {
                            PetData petData = (PetData) this.petData.get(Integer.valueOf(resultSetExecuteQuery2.getInt("pet_id")));
                            if (petData != null) {
                                petData.getPetCommands().add((PetCommand) tHashMap.get(Integer.valueOf(resultSetExecuteQuery2.getInt("command_id"))));
                            }
                        } catch (Throwable th3) {
                            if (resultSetExecuteQuery2 != null) {
                                try {
                                    resultSetExecuteQuery2.close();
                                } catch (Throwable th4) {
                                    th3.addSuppressed(th4);
                                }
                            }
                            throw th3;
                        }
                    }
                    if (resultSetExecuteQuery2 != null) {
                        resultSetExecuteQuery2.close();
                    }
                    if (statementCreateStatement != null) {
                        statementCreateStatement.close();
                    }
                } finally {
                }
            } catch (SQLException e2) {
                LOGGER.error("Caught SQL exception", e2);
            }
        } finally {
            if (statementCreateStatement != null) {
                try {
                    statementCreateStatement.close();
                } catch (Throwable th5) {
                    th.addSuppressed(th5);
                }
            }
        }
    }

    private void loadPetBreeding(Connection connection) {
        Statement statementCreateStatement;
        try {
            statementCreateStatement = connection.createStatement();
            try {
                ResultSet resultSetExecuteQuery = statementCreateStatement.executeQuery("SELECT * FROM pet_breeding");
                while (resultSetExecuteQuery.next()) {
                    try {
                        this.breedingPetType.put(resultSetExecuteQuery.getInt("pet_id"), resultSetExecuteQuery.getInt("offspring_id"));
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
            } finally {
                if (statementCreateStatement != null) {
                    try {
                        statementCreateStatement.close();
                    } catch (Throwable th3) {
                        th.addSuppressed(th3);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
        try {
            statementCreateStatement = connection.createStatement();
            try {
                ResultSet resultSetExecuteQuery2 = statementCreateStatement.executeQuery("SELECT * FROM pet_breeding_races");
                while (resultSetExecuteQuery2.next()) {
                    try {
                        PetBreedingReward petBreedingReward = new PetBreedingReward(resultSetExecuteQuery2);
                        if (!this.breedingReward.containsKey(Integer.valueOf(petBreedingReward.petType))) {
                            this.breedingReward.put(Integer.valueOf(petBreedingReward.petType), new TIntObjectHashMap());
                        }
                        if (!((TIntObjectHashMap) this.breedingReward.get(Integer.valueOf(petBreedingReward.petType))).containsKey(petBreedingReward.rarityLevel)) {
                            ((TIntObjectHashMap) this.breedingReward.get(Integer.valueOf(petBreedingReward.petType))).put(petBreedingReward.rarityLevel, new ArrayList());
                        }
                        ((ArrayList) ((TIntObjectHashMap) this.breedingReward.get(Integer.valueOf(petBreedingReward.petType))).get(petBreedingReward.rarityLevel)).add(petBreedingReward);
                    } catch (Throwable th4) {
                        if (resultSetExecuteQuery2 != null) {
                            try {
                                resultSetExecuteQuery2.close();
                            } catch (Throwable th5) {
                                th4.addSuppressed(th5);
                            }
                        }
                        throw th4;
                    }
                }
                if (resultSetExecuteQuery2 != null) {
                    resultSetExecuteQuery2.close();
                }
                if (statementCreateStatement != null) {
                    statementCreateStatement.close();
                }
            } finally {
            }
        } catch (SQLException e2) {
            LOGGER.error("Caught SQL exception", e2);
        }
    }

    public THashSet<PetRace> getBreeds(String str) {
        if (!str.startsWith("a0 pet")) {
            LOGGER.error("Pet " + str + " not found. Make sure it matches the pattern \"a0 pet<pet_id>\"!");
            return null;
        }
        try {
            return (THashSet) this.petRaces.get(Integer.valueOf(Integer.valueOf(str.split("t")[1]).intValue()));
        } catch (Exception e) {
            LOGGER.error("Caught exception", e);
            return null;
        }
    }

    public TIntObjectHashMap<ArrayList<PetBreedingReward>> getBreedingRewards(int i) {
        return (TIntObjectHashMap) this.breedingReward.get(Integer.valueOf(i));
    }

    public int getRarityForOffspring(final Pet pet) {
        final int[] iArr = {0};
        ((TIntObjectHashMap) this.breedingReward.get(Integer.valueOf(pet.getPetData().getType()))).forEachEntry(new TIntObjectProcedure<ArrayList<PetBreedingReward>>() { // from class: com.eu.habbo.habbohotel.pets.PetManager.2
            public boolean execute(int i, ArrayList<PetBreedingReward> arrayList) {
                Iterator<PetBreedingReward> it = arrayList.iterator();
                while (it.hasNext()) {
                    if (it.next().breed == pet.getRace()) {
                        iArr[0] = i;
                        return false;
                    }
                }
                return true;
            }
        });
        return 4 - iArr[0];
    }

    public PetData getPetData(int i) {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        ResultSet resultSetExecuteQuery;
        synchronized (this.petData) {
            if (this.petData.containsKey(Integer.valueOf(i))) {
                return (PetData) this.petData.get(Integer.valueOf(i));
            }
            try {
                connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    LOGGER.error("Missing petdata for type " + i + ". Adding this to the database...");
                    preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO pet_actions (pet_type) VALUES (?)");
                    try {
                        preparedStatementPrepareStatement.setInt(1, i);
                        preparedStatementPrepareStatement.execute();
                        if (preparedStatementPrepareStatement != null) {
                            preparedStatementPrepareStatement.close();
                        }
                        preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM pet_actions WHERE pet_type = ? LIMIT 1");
                        try {
                            preparedStatementPrepareStatement.setInt(1, i);
                            resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                            try {
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
                        } finally {
                            if (preparedStatementPrepareStatement != null) {
                                try {
                                    preparedStatementPrepareStatement.close();
                                } catch (Throwable th3) {
                                    th.addSuppressed(th3);
                                }
                            }
                        }
                    } catch (Throwable th4) {
                        throw th4;
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
            } catch (SQLException e) {
                LOGGER.error("Caught SQL exception", e);
            }
            if (!resultSetExecuteQuery.next()) {
                if (resultSetExecuteQuery != null) {
                    resultSetExecuteQuery.close();
                }
                if (preparedStatementPrepareStatement != null) {
                    preparedStatementPrepareStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
                return null;
            }
            PetData petData = new PetData(resultSetExecuteQuery);
            this.petData.put(Integer.valueOf(i), petData);
            LOGGER.error("Missing petdata for type " + i + " added to the database!");
            if (resultSetExecuteQuery != null) {
                resultSetExecuteQuery.close();
            }
            if (preparedStatementPrepareStatement != null) {
                preparedStatementPrepareStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
            return petData;
        }
    }

    public PetData getPetData(String str) {
        synchronized (this.petData) {
            for (Map.Entry entry : this.petData.entrySet()) {
                if (((PetData) entry.getValue()).getName().equalsIgnoreCase(str)) {
                    return (PetData) entry.getValue();
                }
            }
            return null;
        }
    }

    public Collection<PetData> getPetData() {
        return this.petData.values();
    }

    public Pet createPet(Item item, String str, String str2, String str3, GameClient gameClient) {
        int iIntValue = Integer.valueOf(item.getName().toLowerCase().replace("a0 pet", Emulator.PREVIEW)).intValue();
        if (!this.petData.containsKey(Integer.valueOf(iIntValue))) {
            return null;
        }
        Pet horsePet = iIntValue == 15 ? new HorsePet(iIntValue, Integer.valueOf(str2).intValue(), str3, str, gameClient.getHabbo().getHabboInfo().getId()) : iIntValue == 16 ? createMonsterplant(null, gameClient.getHabbo(), false, null, 0) : new Pet(iIntValue, Integer.valueOf(str2).intValue(), str3, str, gameClient.getHabbo().getHabboInfo().getId());
        horsePet.needsUpdate = true;
        horsePet.run();
        return horsePet;
    }

    public Pet createPet(int i, String str, GameClient gameClient) {
        return createPet(i, Emulator.getRandom().nextInt(((THashSet) this.petRaces.get(Integer.valueOf(i))).size() + 1), str, gameClient);
    }

    public Pet createPet(int i, int i2, String str, GameClient gameClient) {
        if (!this.petData.containsKey(Integer.valueOf(i))) {
            return null;
        }
        Pet pet = new Pet(i, i2, "FFFFFF", str, gameClient.getHabbo().getHabboInfo().getId());
        pet.needsUpdate = true;
        pet.run();
        return pet;
    }

    public MonsterplantPet createMonsterplant(Room room, Habbo habbo, boolean z, RoomTile roomTile, int i) {
        MonsterplantPet monsterplantPet = new MonsterplantPet(habbo.getHabboInfo().getId(), randomBody(i, z), randomColor(i, z), Emulator.getRandom().nextInt(12) + 1, Emulator.getRandom().nextInt(11), Emulator.getRandom().nextInt(12) + 1, Emulator.getRandom().nextInt(11), Emulator.getRandom().nextInt(12) + 1, Emulator.getRandom().nextInt(11));
        monsterplantPet.setUserId(habbo.getHabboInfo().getId());
        monsterplantPet.setRoom(room);
        monsterplantPet.setRoomUnit(new RoomUnit());
        monsterplantPet.getRoomUnit().setPathFinderRoom(room);
        monsterplantPet.needsUpdate = true;
        monsterplantPet.run();
        return monsterplantPet;
    }

    public Pet createGnome(String str, Room room, Habbo habbo) {
        GnomePet gnomePet = new GnomePet(26, 0, "FFFFFF", str, habbo.getHabboInfo().getId(), "5 0 -1 " + randomGnomeSkinColor() + " 1 10" + (1 + Emulator.getRandom().nextInt(2)) + " " + randomGnomeColor() + " 2 201 " + randomGnomeColor() + " 3 30" + (1 + Emulator.getRandom().nextInt(2)) + " " + randomGnomeColor() + " 4 40" + Emulator.getRandom().nextInt(2) + " " + randomGnomeColor());
        gnomePet.setUserId(habbo.getHabboInfo().getId());
        gnomePet.setRoom(room);
        gnomePet.setRoomUnit(new RoomUnit());
        gnomePet.getRoomUnit().setPathFinderRoom(room);
        gnomePet.needsUpdate = true;
        gnomePet.run();
        return gnomePet;
    }

    public Pet createLeprechaun(String str, Room room, Habbo habbo) {
        GnomePet gnomePet = new GnomePet(27, 0, "FFFFFF", str, habbo.getHabboInfo().getId(), "5 0 -1 0 1 102 19 2 201 27 3 302 23 4 401 27");
        gnomePet.setUserId(habbo.getHabboInfo().getId());
        gnomePet.setRoom(room);
        gnomePet.setRoomUnit(new RoomUnit());
        gnomePet.getRoomUnit().setPathFinderRoom(room);
        gnomePet.needsUpdate = true;
        gnomePet.run();
        return gnomePet;
    }

    private int randomGnomeColor() {
        int iNextInt = 19;
        while (true) {
            int i = iNextInt;
            if (i != 19 && i != 27) {
                return i;
            }
            iNextInt = Emulator.getRandom().nextInt(34);
        }
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
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("DELETE FROM users_pets WHERE id = ? LIMIT 1");
                try {
                    preparedStatementPrepareStatement.setInt(1, pet.getId());
                    boolean zExecute = preparedStatementPrepareStatement.execute();
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                    return zExecute;
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
            return false;
        }
    }
}
