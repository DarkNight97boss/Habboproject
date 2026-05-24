package com.eu.habbo.habbohotel.pets;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.Outgoing;
import com.eu.habbo.messages.outgoing.inventory.AddHabboItemComposer;
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer;
import com.eu.habbo.messages.outgoing.rooms.pets.PetStatusUpdateComposer;
import com.eu.habbo.messages.outgoing.rooms.pets.RoomPetRespectComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserStatusComposer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/pets/MonsterplantPet.class */
public class MonsterplantPet extends Pet implements IPetLook {
    private static final Logger LOGGER = LoggerFactory.getLogger(MonsterplantPet.class);
    public static final Map<Integer, Pair<String, Integer>> bodyRarity = new LinkedHashMap<Integer, Pair<String, Integer>>() { // from class: com.eu.habbo.habbohotel.pets.MonsterplantPet.1
        {
            put(1, new Pair("Blungon", 0));
            put(2, new Pair("Wailzor", 1));
            put(3, new Pair("Stumpy", 1));
            put(4, new Pair("Sunspike", 2));
            put(5, new Pair("Squarg", 0));
            put(6, new Pair("Shroomer", 3));
            put(7, new Pair("Zuchinu", 3));
            put(8, new Pair("Abysswirl", 5));
            put(9, new Pair("Weggylum", 2));
            put(10, new Pair("Wystique", 4));
            put(11, new Pair("Hairbullis", 4));
            put(12, new Pair("Snozzle", 5));
        }
    };
    public static final Map<Integer, Pair<String, Integer>> colorRarity = new LinkedHashMap<Integer, Pair<String, Integer>>() { // from class: com.eu.habbo.habbohotel.pets.MonsterplantPet.2
        {
            put(0, new Pair("Aenueus", 0));
            put(1, new Pair("Griseus", 1));
            put(2, new Pair("Phoenicus", 2));
            put(3, new Pair("Viridulus", 1));
            put(4, new Pair("Cyaneus", 5));
            put(5, new Pair("Incarnatus", 2));
            put(6, new Pair("Azureus", 4));
            put(7, new Pair("Atamasc", 4));
            put(8, new Pair("Amethyst", 3));
            put(9, new Pair("Fulvus", 0));
            put(10, new Pair("Cinereus", 3));
        }
    };
    public static final ArrayList<Pair<String, Integer>> indexedBody = new ArrayList<>(bodyRarity.values());
    public static final ArrayList<Pair<String, Integer>> indexedColors = new ArrayList<>(colorRarity.values());
    public static int growTime = 1800;
    public static int timeToLive = 259200;
    private final int nose;
    private final int noseColor;
    private final int eyes;
    private final int eyesColor;
    private final int mouth;
    private final int mouthColor;
    public String look;
    private int type;
    private int hue;
    private int deathTimestamp;
    private boolean canBreed;
    private boolean publiclyBreedable;
    private int growthStage;
    private boolean hasDied;

    public MonsterplantPet(ResultSet resultSet) throws SQLException {
        super(resultSet);
        this.deathTimestamp = Emulator.getIntUnixTimestamp() + timeToLive;
        this.canBreed = true;
        this.publiclyBreedable = false;
        this.growthStage = 0;
        this.hasDied = false;
        this.type = resultSet.getInt("mp_type");
        this.hue = resultSet.getInt("mp_color");
        this.nose = resultSet.getInt("mp_nose");
        this.noseColor = resultSet.getInt("mp_nose_color");
        this.eyes = resultSet.getInt("mp_eyes");
        this.eyesColor = resultSet.getInt("mp_eyes_color");
        this.mouth = resultSet.getInt("mp_mouth");
        this.mouthColor = resultSet.getInt("mp_mouth_color");
        this.deathTimestamp = resultSet.getInt("mp_death_timestamp");
        this.publiclyBreedable = resultSet.getString("mp_allow_breed").equals("1");
        this.canBreed = resultSet.getString("mp_breedable").equals("1");
        this.hasDied = resultSet.getInt("mp_is_dead") == 1;
    }

    public MonsterplantPet(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9) {
        super(16, 0, Emulator.PREVIEW, Emulator.PREVIEW, i);
        this.deathTimestamp = Emulator.getIntUnixTimestamp() + timeToLive;
        this.canBreed = true;
        this.publiclyBreedable = false;
        this.growthStage = 0;
        this.hasDied = false;
        this.type = i2;
        this.hue = i3;
        this.nose = i4;
        this.noseColor = i5;
        this.mouth = i6;
        this.mouthColor = i7;
        this.eyes = i8;
        this.eyesColor = i9;
    }

    @Override // com.eu.habbo.habbohotel.pets.Pet
    public String getName() {
        String str = colorRarity.containsKey(Integer.valueOf(this.hue)) ? (String) colorRarity.get(Integer.valueOf(this.hue)).getKey() : "Unknownis";
        if (bodyRarity.containsKey(Integer.valueOf(this.type))) {
            str = str + " " + ((String) bodyRarity.get(Integer.valueOf(this.type)).getKey());
        }
        return str;
    }

    @Override // com.eu.habbo.habbohotel.pets.Pet, java.lang.Runnable
    public void run() {
        if (this.needsUpdate) {
            super.run();
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE users_pets SET mp_type = ?, mp_color = ?, mp_nose = ?, mp_eyes = ?, mp_mouth = ?, mp_nose_color = ?, mp_eyes_color = ?, mp_mouth_color = ?, mp_death_timestamp = ?, mp_breedable = ?, mp_allow_breed = ?, mp_is_dead = ? WHERE id = ?");
                    try {
                        preparedStatementPrepareStatement.setInt(1, this.type);
                        preparedStatementPrepareStatement.setInt(2, this.hue);
                        preparedStatementPrepareStatement.setInt(3, this.nose);
                        preparedStatementPrepareStatement.setInt(4, this.eyes);
                        preparedStatementPrepareStatement.setInt(5, this.mouth);
                        preparedStatementPrepareStatement.setInt(6, this.noseColor);
                        preparedStatementPrepareStatement.setInt(7, this.eyesColor);
                        preparedStatementPrepareStatement.setInt(8, this.mouthColor);
                        preparedStatementPrepareStatement.setInt(9, this.deathTimestamp);
                        preparedStatementPrepareStatement.setString(10, this.canBreed ? "1" : "0");
                        preparedStatementPrepareStatement.setString(11, this.publiclyBreedable ? "1" : "0");
                        preparedStatementPrepareStatement.setInt(12, this.hasDied ? 1 : 0);
                        preparedStatementPrepareStatement.setInt(13, this.id);
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
    }

    @Override // com.eu.habbo.habbohotel.pets.Pet
    public void cycle() {
        if (this.room != null && this.roomUnit != null) {
            if (isDead()) {
                this.roomUnit.removeStatus(RoomUnitStatus.GESTURE);
                if (!this.hasDied) {
                    AchievementManager.progressAchievement(Emulator.getGameEnvironment().getHabboManager().getHabbo(this.userId), Emulator.getGameEnvironment().getAchievementManager().getAchievement("MonsterPlantGardenOfDeath"));
                    this.hasDied = true;
                    this.needsUpdate = true;
                }
                this.roomUnit.clearStatus();
                this.roomUnit.setStatus(RoomUnitStatus.RIP, Emulator.PREVIEW);
                this.packetUpdate = true;
            } else {
                int intUnixTimestamp = (Emulator.getIntUnixTimestamp() - this.created) + 1;
                if (intUnixTimestamp >= growTime) {
                    this.growthStage = 7;
                    boolean z = false;
                    Iterator it = this.roomUnit.getStatusMap().keySet().iterator();
                    while (it.hasNext()) {
                        if (((RoomUnitStatus) it.next()).equals(RoomUnitStatus.GROW)) {
                            z = true;
                        }
                    }
                    if (z) {
                        this.roomUnit.clearStatus();
                        this.packetUpdate = true;
                    }
                } else {
                    int iCeil = (int) Math.ceil(((double) intUnixTimestamp) / (((double) growTime) / 7.0d));
                    if (iCeil > this.growthStage) {
                        this.growthStage = iCeil;
                        this.roomUnit.clearStatus();
                        this.roomUnit.setStatus(RoomUnitStatus.fromString("grw" + this.growthStage), Emulator.PREVIEW);
                        this.packetUpdate = true;
                    }
                }
                if (Emulator.getRandom().nextInt(Outgoing.CraftableProductsComposer) < 10) {
                    super.updateGesture(Emulator.getIntUnixTimestamp());
                    this.packetUpdate = true;
                }
            }
        }
        super.cycle();
    }

    public int getType() {
        return this.type;
    }

    public int getRarity() {
        if (bodyRarity.containsKey(Integer.valueOf(this.type)) && colorRarity.containsKey(Integer.valueOf(this.hue))) {
            return ((Integer) bodyRarity.get(Integer.valueOf(this.type)).getValue()).intValue() + ((Integer) colorRarity.get(Integer.valueOf(this.hue)).getValue()).intValue();
        }
        return 0;
    }

    @Override // com.eu.habbo.habbohotel.pets.IPetLook
    public String getLook() {
        return "16 0 FFFFFF 5 0 -1 10 1 " + this.type + " " + this.hue + " 2 " + this.mouth + " " + this.mouthColor + " 3 " + this.nose + " " + this.noseColor + " 4 " + this.eyes + " " + this.eyesColor;
    }

    @Override // com.eu.habbo.habbohotel.pets.Pet, com.eu.habbo.messages.ISerialize
    public void serialize(ServerMessage serverMessage) {
        serverMessage.appendInt(Integer.valueOf(getId()));
        serverMessage.appendString(getName());
        serverMessage.appendInt(Integer.valueOf(this.petData.getType()));
        serverMessage.appendInt(Integer.valueOf(this.race));
        serverMessage.appendString(getLook().substring(5));
        serverMessage.appendInt(Integer.valueOf(getRarity()));
        serverMessage.appendInt((Integer) 5);
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt((Integer) (-1));
        serverMessage.appendInt((Integer) 10);
        serverMessage.appendInt((Integer) 1);
        serverMessage.appendInt(Integer.valueOf(this.type));
        serverMessage.appendInt(Integer.valueOf(this.hue));
        serverMessage.appendInt((Integer) 2);
        serverMessage.appendInt(Integer.valueOf(this.mouth));
        serverMessage.appendInt(Integer.valueOf(this.mouthColor));
        serverMessage.appendInt((Integer) 3);
        serverMessage.appendInt(Integer.valueOf(this.nose));
        serverMessage.appendInt(Integer.valueOf(this.noseColor));
        serverMessage.appendInt((Integer) 4);
        serverMessage.appendInt(Integer.valueOf(this.eyes));
        serverMessage.appendInt(Integer.valueOf(this.eyesColor));
        serverMessage.appendInt(Integer.valueOf(this.growthStage));
    }

    public int remainingTimeToLive() {
        return Math.max(0, this.deathTimestamp - Emulator.getIntUnixTimestamp());
    }

    public boolean isDead() {
        return Emulator.getIntUnixTimestamp() >= this.deathTimestamp;
    }

    public void setDeathTimestamp(int i) {
        this.deathTimestamp = i;
    }

    public int getGrowthStage() {
        return this.growthStage;
    }

    public int remainingGrowTime() {
        if (this.growthStage == 7) {
            return 0;
        }
        return Math.max(0, growTime - (Emulator.getIntUnixTimestamp() - this.created));
    }

    public boolean isFullyGrown() {
        return this.growthStage == 7;
    }

    public boolean canBreed() {
        return this.canBreed;
    }

    public void setCanBreed(boolean z) {
        this.canBreed = z;
    }

    public boolean breedable() {
        return isFullyGrown() && this.canBreed && !isDead();
    }

    public boolean isPubliclyBreedable() {
        return this.publiclyBreedable;
    }

    public void setPubliclyBreedable(boolean z) {
        this.publiclyBreedable = z;
    }

    public void breed(MonsterplantPet monsterplantPet) {
        if (this.canBreed && monsterplantPet.canBreed) {
            this.canBreed = false;
            this.publiclyBreedable = false;
            monsterplantPet.setCanBreed(false);
            monsterplantPet.setPubliclyBreedable(false);
            this.room.sendComposer(new PetStatusUpdateComposer(monsterplantPet).compose());
            this.room.sendComposer(new PetStatusUpdateComposer(this).compose());
            getRoomUnit().setStatus(RoomUnitStatus.GESTURE, "reb");
            monsterplantPet.getRoomUnit().setStatus(RoomUnitStatus.GESTURE, "reb");
            this.room.sendComposer(new RoomUserStatusComposer(getRoomUnit()).compose());
            this.room.sendComposer(new RoomUserStatusComposer(monsterplantPet.getRoomUnit()).compose());
            getRoomUnit().removeStatus(RoomUnitStatus.GESTURE);
            monsterplantPet.getRoomUnit().removeStatus(RoomUnitStatus.GESTURE);
            Habbo habbo = this.room.getHabbo(getUserId());
            Habbo habbo2 = null;
            if (getUserId() != monsterplantPet.getUserId()) {
                habbo2 = this.room.getHabbo(monsterplantPet.getUserId());
            }
            Item item = (getRarity() < 8 || monsterplantPet.getRarity() < 8 || Emulator.getRandom().nextInt(100) > getRarity() + monsterplantPet.getRarity()) ? Emulator.getGameEnvironment().getItemManager().getItem(Emulator.getConfig().getInt("monsterplant.seed.item_id")) : Emulator.getGameEnvironment().getItemManager().getItem(Emulator.getConfig().getInt("monsterplant.seed_rare.item_id"));
            if (item != null) {
                if (habbo != null) {
                    AchievementManager.progressAchievement(habbo, Emulator.getGameEnvironment().getAchievementManager().getAchievement("MonsterPlantBreeder"), 5);
                    HabboItem habboItemCreateItem = Emulator.getGameEnvironment().getItemManager().createItem(habbo.getHabboInfo().getId(), item, 0, 0, Emulator.PREVIEW);
                    habbo.getInventory().getItemsComponent().addItem(habboItemCreateItem);
                    habbo.getClient().sendResponse(new AddHabboItemComposer(habboItemCreateItem));
                    habbo.getClient().sendResponse(new InventoryRefreshComposer());
                }
                if (habbo2 != null) {
                    AchievementManager.progressAchievement(habbo2, Emulator.getGameEnvironment().getAchievementManager().getAchievement("MonsterPlantBreeder"), 5);
                    HabboItem habboItemCreateItem2 = Emulator.getGameEnvironment().getItemManager().createItem(habbo2.getHabboInfo().getId(), item, 0, 0, Emulator.PREVIEW);
                    habbo2.getInventory().getItemsComponent().addItem(habboItemCreateItem2);
                    habbo2.getClient().sendResponse(new AddHabboItemComposer(habboItemCreateItem2));
                    habbo2.getClient().sendResponse(new InventoryRefreshComposer());
                }
            }
        }
    }

    private boolean mayScratch() {
        return ((double) (((float) getEnergy()) / ((float) getMaxEnergy()))) < 0.98d;
    }

    @Override // com.eu.habbo.habbohotel.pets.Pet
    public int getMaxEnergy() {
        return timeToLive;
    }

    @Override // com.eu.habbo.habbohotel.pets.Pet
    public int getEnergy() {
        if (isDead()) {
            return 100;
        }
        return this.deathTimestamp - Emulator.getIntUnixTimestamp();
    }

    @Override // com.eu.habbo.habbohotel.pets.Pet
    public synchronized void scratched(Habbo habbo) {
        if (mayScratch()) {
            AchievementManager.progressAchievement(habbo, Emulator.getGameEnvironment().getAchievementManager().getAchievement("MonsterPlantTreater"), 5);
            setDeathTimestamp(Emulator.getIntUnixTimestamp() + timeToLive);
            addHappyness(10);
            addExperience(10);
            this.room.sendComposer(new PetStatusUpdateComposer(this).compose());
            this.room.sendComposer(new RoomPetRespectComposer(this, 2).compose());
        }
    }

    @Override // com.eu.habbo.habbohotel.pets.Pet
    public boolean canWalk() {
        return false;
    }
}
