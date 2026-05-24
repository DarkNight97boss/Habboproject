package com.eu.habbo.habbohotel.pets;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomChatMessage;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ISerialize;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.rooms.RoomEditSettingsErrorComposer;
import com.eu.habbo.messages.outgoing.rooms.pets.PetLevelUpdatedComposer;
import com.eu.habbo.messages.outgoing.rooms.pets.RoomPetExperienceComposer;
import com.eu.habbo.messages.outgoing.rooms.pets.RoomPetRespectComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserRemoveComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserTalkComposer;
import com.eu.habbo.plugin.events.pets.PetTalkEvent;
import gnu.trove.map.hash.THashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/pets/Pet.class */
public class Pet implements ISerialize, Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Pet.class);
    public int levelThirst;
    public int levelHunger;
    public boolean needsUpdate;
    public boolean packetUpdate;
    protected int id;
    protected int userId;
    protected Room room;
    protected String name;
    protected PetData petData;
    protected int race;
    protected String color;
    protected int happyness;
    protected int experience;
    protected int energy;
    protected int respect;
    protected int created;
    protected int level;
    RoomUnit roomUnit;
    private int chatTimeout;
    private int tickTimeout;
    private int happynessDelay;
    private int gestureTickTimeout;
    private int randomActionTickTimeout;
    private int postureTimeout;
    private int stayStartedAt;
    private int idleCommandTicks;
    private int freeCommandTicks;
    private PetTasks task;
    private boolean muted;

    /* JADX INFO: renamed from: com.eu.habbo.habbohotel.pets.Pet$1, reason: invalid class name */
    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/pets/Pet$1.class */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$eu$habbo$habbohotel$pets$PetTasks = new int[PetTasks.values().length];

        static {
            try {
                $SwitchMap$com$eu$habbo$habbohotel$pets$PetTasks[PetTasks.DOWN.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$pets$PetTasks[PetTasks.FLAT.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$pets$PetTasks[PetTasks.HERE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$pets$PetTasks[PetTasks.SIT.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$pets$PetTasks[PetTasks.BEG.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$pets$PetTasks[PetTasks.PLAY.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$pets$PetTasks[PetTasks.PLAY_FOOTBALL.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$pets$PetTasks[PetTasks.PLAY_DEAD.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$pets$PetTasks[PetTasks.FOLLOW.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$pets$PetTasks[PetTasks.JUMP.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$pets$PetTasks[PetTasks.STAND.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$pets$PetTasks[PetTasks.NEST.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$com$eu$habbo$habbohotel$pets$PetTasks[PetTasks.RIDE.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
        }
    }

    public Pet(ResultSet resultSet) throws SQLException {
        this.needsUpdate = false;
        this.packetUpdate = false;
        this.tickTimeout = Emulator.getIntUnixTimestamp();
        this.happynessDelay = Emulator.getIntUnixTimestamp();
        this.gestureTickTimeout = Emulator.getIntUnixTimestamp();
        this.randomActionTickTimeout = Emulator.getIntUnixTimestamp();
        this.postureTimeout = Emulator.getIntUnixTimestamp();
        this.stayStartedAt = 0;
        this.idleCommandTicks = 0;
        this.freeCommandTicks = -1;
        this.task = PetTasks.FREE;
        this.muted = false;
        this.id = resultSet.getInt("id");
        this.userId = resultSet.getInt("user_id");
        this.room = null;
        this.name = resultSet.getString("name");
        this.petData = Emulator.getGameEnvironment().getPetManager().getPetData(resultSet.getInt("type"));
        if (this.petData == null) {
            LOGGER.error("WARNING! Missing pet data for type: " + resultSet.getInt("type") + "! Insert a new entry into the pet_actions table for this type!");
            this.petData = Emulator.getGameEnvironment().getPetManager().getPetData(0);
        }
        this.race = resultSet.getInt("race");
        this.experience = resultSet.getInt("experience");
        this.happyness = resultSet.getInt("happyness");
        this.energy = resultSet.getInt("energy");
        this.respect = resultSet.getInt("respect");
        this.created = resultSet.getInt("created");
        this.color = resultSet.getString("color");
        this.levelThirst = resultSet.getInt("thirst");
        this.levelHunger = resultSet.getInt("hunger");
        this.level = PetManager.getLevel(this.experience);
    }

    public Pet(int i, int i2, String str, String str2, int i3) {
        this.needsUpdate = false;
        this.packetUpdate = false;
        this.tickTimeout = Emulator.getIntUnixTimestamp();
        this.happynessDelay = Emulator.getIntUnixTimestamp();
        this.gestureTickTimeout = Emulator.getIntUnixTimestamp();
        this.randomActionTickTimeout = Emulator.getIntUnixTimestamp();
        this.postureTimeout = Emulator.getIntUnixTimestamp();
        this.stayStartedAt = 0;
        this.idleCommandTicks = 0;
        this.freeCommandTicks = -1;
        this.task = PetTasks.FREE;
        this.muted = false;
        this.id = 0;
        this.userId = i3;
        this.room = null;
        this.name = str2;
        this.petData = Emulator.getGameEnvironment().getPetManager().getPetData(i);
        if (this.petData == null) {
            LOGGER.warn("Missing pet data for type: " + i + "! Insert a new entry into the pet_actions table for this type!");
        }
        this.race = i2;
        this.color = str;
        this.experience = 0;
        this.happyness = 100;
        this.energy = 100;
        this.respect = 0;
        this.levelThirst = 0;
        this.levelHunger = 0;
        this.created = Emulator.getIntUnixTimestamp();
        this.level = 1;
    }

    protected void say(String str) {
        if (this.roomUnit == null || this.room == null || str.isEmpty()) {
            return;
        }
        RoomChatMessage roomChatMessage = new RoomChatMessage(str, this.roomUnit, RoomChatMessageBubbles.NORMAL);
        if (((PetTalkEvent) Emulator.getPluginManager().fireEvent(new PetTalkEvent(this, roomChatMessage))).isCancelled()) {
            return;
        }
        this.room.petChat(new RoomUserTalkComposer(roomChatMessage).compose());
    }

    public void say(PetVocal petVocal) {
        if (petVocal != null) {
            say(petVocal.message);
        }
    }

    public void addEnergy(int i) {
        this.energy += i;
        if (this.energy > PetManager.maxEnergy(this.level)) {
            this.energy = PetManager.maxEnergy(this.level);
        }
        if (this.energy < 0) {
            this.energy = 0;
        }
    }

    public void addHappyness(int i) {
        this.happyness += i;
        if (this.happyness > 100) {
            this.happyness = 100;
        }
        if (this.happyness < 0) {
            this.happyness = 0;
        }
    }

    public int getRespect() {
        return this.respect;
    }

    public void addRespect() {
        this.respect++;
    }

    public int daysAlive() {
        return (Emulator.getIntUnixTimestamp() - this.created) / 86400;
    }

    public String bornDate() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.setTime(new Date(this.created));
        return calendar.get(5) + "/" + calendar.get(2) + "/" + calendar.get(1);
    }

    public void run() {
        Connection connection;
        if (this.needsUpdate) {
            try {
                connection = Emulator.getDatabase().getDataSource().getConnection();
            } catch (SQLException e) {
                LOGGER.error("Caught SQL exception", e);
            }
            try {
                if (this.id > 0) {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE users_pets SET room_id = ?, experience = ?, energy = ?, respect = ?, x = ?, y = ?, z = ?, rot = ?, hunger = ?, thirst = ?, happyness = ?, created = ? WHERE id = ?");
                    try {
                        preparedStatementPrepareStatement.setInt(1, this.room == null ? 0 : this.room.getId());
                        preparedStatementPrepareStatement.setInt(2, this.experience);
                        preparedStatementPrepareStatement.setInt(3, this.energy);
                        preparedStatementPrepareStatement.setInt(4, this.respect);
                        preparedStatementPrepareStatement.setInt(5, this.roomUnit != null ? this.roomUnit.getX() : (short) 0);
                        preparedStatementPrepareStatement.setInt(6, this.roomUnit != null ? this.roomUnit.getY() : (short) 0);
                        preparedStatementPrepareStatement.setDouble(7, this.roomUnit != null ? this.roomUnit.getZ() : 0.0d);
                        preparedStatementPrepareStatement.setInt(8, this.roomUnit != null ? this.roomUnit.getBodyRotation().getValue() : 0);
                        preparedStatementPrepareStatement.setInt(9, this.levelHunger);
                        preparedStatementPrepareStatement.setInt(10, this.levelThirst);
                        preparedStatementPrepareStatement.setInt(11, this.happyness);
                        preparedStatementPrepareStatement.setInt(12, this.created);
                        preparedStatementPrepareStatement.setInt(13, this.id);
                        preparedStatementPrepareStatement.execute();
                        if (preparedStatementPrepareStatement != null) {
                            preparedStatementPrepareStatement.close();
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
                } else if (this.id == 0) {
                    PreparedStatement preparedStatementPrepareStatement2 = connection.prepareStatement("INSERT INTO users_pets (user_id, room_id, name, race, type, color, experience, energy, respect, created) VALUES (?, 0, ?, ?, ?, ?, 0, 0, 0, ?)", 1);
                    try {
                        preparedStatementPrepareStatement2.setInt(1, this.userId);
                        preparedStatementPrepareStatement2.setString(2, this.name);
                        preparedStatementPrepareStatement2.setInt(3, this.race);
                        preparedStatementPrepareStatement2.setInt(4, 0);
                        if (this.petData != null) {
                            preparedStatementPrepareStatement2.setInt(4, this.petData.getType());
                        }
                        preparedStatementPrepareStatement2.setString(5, this.color);
                        preparedStatementPrepareStatement2.setInt(6, this.created);
                        preparedStatementPrepareStatement2.execute();
                        ResultSet generatedKeys = preparedStatementPrepareStatement2.getGeneratedKeys();
                        try {
                            if (generatedKeys.next()) {
                                this.id = generatedKeys.getInt(1);
                            }
                            if (generatedKeys != null) {
                                generatedKeys.close();
                            }
                            if (preparedStatementPrepareStatement2 != null) {
                                preparedStatementPrepareStatement2.close();
                            }
                        } catch (Throwable th3) {
                            if (generatedKeys != null) {
                                try {
                                    generatedKeys.close();
                                } catch (Throwable th4) {
                                    th3.addSuppressed(th4);
                                }
                            }
                            throw th3;
                        }
                    } catch (Throwable th5) {
                        if (preparedStatementPrepareStatement2 != null) {
                            try {
                                preparedStatementPrepareStatement2.close();
                            } catch (Throwable th6) {
                                th5.addSuppressed(th6);
                            }
                        }
                        throw th5;
                    }
                }
                if (connection != null) {
                    connection.close();
                }
                this.needsUpdate = false;
            } finally {
            }
        }
    }

    public void cycle() {
        RoomTile randomWalkableTile;
        this.idleCommandTicks++;
        int intUnixTimestamp = Emulator.getIntUnixTimestamp();
        if (this.roomUnit == null || this.task == PetTasks.RIDE) {
            return;
        }
        if (intUnixTimestamp - this.gestureTickTimeout > 5 && this.roomUnit.hasStatus(RoomUnitStatus.GESTURE)) {
            this.roomUnit.removeStatus(RoomUnitStatus.GESTURE);
            this.packetUpdate = true;
        }
        if (intUnixTimestamp - this.postureTimeout > 1 && this.task == null) {
            clearPosture();
            this.postureTimeout = intUnixTimestamp + 120;
        }
        if (this.freeCommandTicks > 0) {
            this.freeCommandTicks--;
            if (this.freeCommandTicks == 0) {
                freeCommand();
            }
        }
        if (this.roomUnit.isWalking()) {
            int iNextInt = Emulator.getRandom().nextInt(10) * 2;
            this.roomUnit.setWalkTimeOut(iNextInt < 20 ? 20 + intUnixTimestamp : iNextInt + intUnixTimestamp);
            if (this.energy >= 2) {
                addEnergy(-1);
            }
            if (this.levelHunger < 100) {
                this.levelHunger++;
            }
            if (this.levelThirst < 100) {
                this.levelThirst++;
            }
            if (this.happyness > 0 && intUnixTimestamp - this.happynessDelay >= 30) {
                this.happyness--;
                this.happynessDelay = intUnixTimestamp;
            }
        } else {
            if (this.roomUnit.getWalkTimeOut() < intUnixTimestamp && canWalk() && (randomWalkableTile = this.room.getRandomWalkableTile()) != null) {
                this.roomUnit.setGoalLocation(randomWalkableTile);
            }
            if (this.task == PetTasks.NEST || this.task == PetTasks.DOWN) {
                if (this.levelHunger > 0) {
                    this.levelHunger--;
                }
                if (this.levelThirst > 0) {
                    this.levelThirst--;
                }
                addEnergy(5);
                addHappyness(1);
                if (this.energy == PetManager.maxEnergy(this.level)) {
                    this.roomUnit.removeStatus(RoomUnitStatus.LAY);
                    this.roomUnit.setCanWalk(true);
                    this.roomUnit.setGoalLocation(this.room.getRandomWalkableTile());
                    this.task = null;
                    this.roomUnit.setStatus(RoomUnitStatus.GESTURE, PetGestures.ENERGY.getKey());
                    this.gestureTickTimeout = intUnixTimestamp;
                }
            } else if (this.tickTimeout >= 5) {
                if (this.levelHunger < 100) {
                    this.levelHunger++;
                }
                if (this.levelThirst < 100) {
                    this.levelThirst++;
                }
                if (this.energy < PetManager.maxEnergy(this.level)) {
                    this.energy++;
                }
                this.tickTimeout = intUnixTimestamp;
            }
            if (this.task == PetTasks.STAY && Emulator.getIntUnixTimestamp() - this.stayStartedAt >= 120) {
                this.task = null;
                getRoomUnit().setCanWalk(true);
            }
        }
        if (intUnixTimestamp - this.gestureTickTimeout > 15) {
            updateGesture(intUnixTimestamp);
        } else if (intUnixTimestamp - this.randomActionTickTimeout > 30) {
            randomAction();
            this.randomActionTickTimeout = intUnixTimestamp + (10 * Emulator.getRandom().nextInt(60));
        }
        if (this.muted || this.chatTimeout > intUnixTimestamp) {
            return;
        }
        if (this.energy <= 30) {
            say(this.petData.randomVocal(PetVocalsType.TIRED));
            if (this.energy <= 10) {
                findNest();
            }
        } else if (this.happyness > 85) {
            say(this.petData.randomVocal(PetVocalsType.GENERIC_HAPPY));
        } else if (this.happyness < 15) {
            say(this.petData.randomVocal(PetVocalsType.GENERIC_SAD));
        } else if (this.levelHunger > 50) {
            say(this.petData.randomVocal(PetVocalsType.HUNGRY));
            eat();
        } else if (this.levelThirst > 50) {
            say(this.petData.randomVocal(PetVocalsType.THIRSTY));
            drink();
        }
        int iNextInt2 = Emulator.getRandom().nextInt(30);
        this.chatTimeout = intUnixTimestamp + (iNextInt2 < 3 ? 30 : iNextInt2);
    }

    public void handleCommand(PetCommand petCommand, Habbo habbo, String[] strArr) {
        this.idleCommandTicks = 0;
        if (this.task == PetTasks.STAY) {
            this.stayStartedAt = 0;
            this.task = null;
            getRoomUnit().setCanWalk(true);
        }
        petCommand.handle(this, habbo, strArr);
    }

    public boolean canWalk() {
        if (this.task == null) {
            return true;
        }
        switch (AnonymousClass1.$SwitchMap$com$eu$habbo$habbohotel$pets$PetTasks[this.task.ordinal()]) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case RoomEditSettingsErrorComposer.RESTRICTED_TAGS /* 12 */:
            case RoomEditSettingsErrorComposer.TAGS_TOO_LONG /* 13 */:
                return false;
            default:
                return true;
        }
    }

    public void clearPosture() {
        THashMap tHashMap = new THashMap();
        if (this.roomUnit.hasStatus(RoomUnitStatus.MOVE)) {
            tHashMap.put(RoomUnitStatus.MOVE, this.roomUnit.getStatus(RoomUnitStatus.MOVE));
        }
        if (this.roomUnit.hasStatus(RoomUnitStatus.SIT)) {
            tHashMap.put(RoomUnitStatus.SIT, this.roomUnit.getStatus(RoomUnitStatus.SIT));
        }
        if (this.roomUnit.hasStatus(RoomUnitStatus.LAY)) {
            tHashMap.put(RoomUnitStatus.LAY, this.roomUnit.getStatus(RoomUnitStatus.LAY));
        }
        if (this.roomUnit.hasStatus(RoomUnitStatus.GESTURE)) {
            tHashMap.put(RoomUnitStatus.GESTURE, this.roomUnit.getStatus(RoomUnitStatus.GESTURE));
        }
        if (this.task == null) {
            boolean z = this.roomUnit.hasStatus(RoomUnitStatus.RIP);
            this.roomUnit.clearStatus();
            if (z) {
                this.roomUnit.setStatus(RoomUnitStatus.RIP, Emulator.PREVIEW);
            }
            for (Map.Entry entry : tHashMap.entrySet()) {
                this.roomUnit.setStatus((RoomUnitStatus) entry.getKey(), (String) entry.getValue());
            }
            if (tHashMap.isEmpty()) {
                return;
            }
            this.packetUpdate = true;
        }
    }

    public void updateGesture(int i) {
        this.gestureTickTimeout = i;
        if (this.energy < 30) {
            this.roomUnit.setStatus(RoomUnitStatus.GESTURE, PetGestures.TIRED.getKey());
            findNest();
            return;
        }
        if (this.happyness == 100) {
            this.roomUnit.setStatus(RoomUnitStatus.GESTURE, PetGestures.LOVE.getKey());
            return;
        }
        if (this.happyness >= 90) {
            randomHappyAction();
            this.roomUnit.setStatus(RoomUnitStatus.GESTURE, PetGestures.HAPPY.getKey());
            return;
        }
        if (this.happyness <= 5) {
            randomSadAction();
            this.roomUnit.setStatus(RoomUnitStatus.GESTURE, PetGestures.SAD.getKey());
            return;
        }
        if (this.levelHunger > 80) {
            this.roomUnit.setStatus(RoomUnitStatus.GESTURE, PetGestures.HUNGRY.getKey());
            eat();
        } else if (this.levelThirst > 80) {
            this.roomUnit.setStatus(RoomUnitStatus.GESTURE, PetGestures.THIRSTY.getKey());
            drink();
        } else if (this.idleCommandTicks > 240) {
            this.idleCommandTicks = 0;
            this.roomUnit.setStatus(RoomUnitStatus.GESTURE, PetGestures.QUESTION.getKey());
        }
    }

    @Override // com.eu.habbo.messages.ISerialize
    public void serialize(ServerMessage serverMessage) {
        serverMessage.appendInt(Integer.valueOf(this.id));
        serverMessage.appendString(this.name);
        if (this.petData != null) {
            serverMessage.appendInt(Integer.valueOf(this.petData.getType()));
        } else {
            serverMessage.appendInt((Integer) (-1));
        }
        serverMessage.appendInt(Integer.valueOf(this.race));
        serverMessage.appendString(this.color);
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt((Integer) 0);
    }

    public void findNest() {
        HabboItem habboItemRandomNest = this.petData.randomNest(this.room.getRoomSpecialTypes().getNests());
        this.roomUnit.setCanWalk(true);
        if (habboItemRandomNest != null) {
            this.roomUnit.setGoalLocation(this.room.getLayout().getTile(habboItemRandomNest.getX(), habboItemRandomNest.getY()));
            return;
        }
        this.roomUnit.setStatus(RoomUnitStatus.LAY, this.room.getStackHeight(this.roomUnit.getX(), this.roomUnit.getY(), false) + Emulator.PREVIEW);
        say(this.petData.randomVocal(PetVocalsType.SLEEPING));
        this.task = PetTasks.DOWN;
    }

    public void drink() {
        HabboItem habboItemRandomDrinkItem = this.petData.randomDrinkItem(this.room.getRoomSpecialTypes().getPetDrinks());
        if (habboItemRandomDrinkItem != null) {
            this.roomUnit.setCanWalk(true);
            this.roomUnit.setGoalLocation(this.room.getLayout().getTile(habboItemRandomDrinkItem.getX(), habboItemRandomDrinkItem.getY()));
        }
    }

    public void eat() {
        HabboItem habboItemRandomFoodItem = this.petData.randomFoodItem(this.room.getRoomSpecialTypes().getPetFoods());
        if (habboItemRandomFoodItem != null) {
            this.roomUnit.setCanWalk(true);
            this.roomUnit.setGoalLocation(this.room.getLayout().getTile(habboItemRandomFoodItem.getX(), habboItemRandomFoodItem.getY()));
        }
    }

    public void findToy() {
        HabboItem habboItemRandomToyItem = this.petData.randomToyItem(this.room.getRoomSpecialTypes().getPetToys());
        if (habboItemRandomToyItem != null) {
            this.roomUnit.setCanWalk(true);
            this.roomUnit.setGoalLocation(this.room.getLayout().getTile(habboItemRandomToyItem.getX(), habboItemRandomToyItem.getY()));
        }
    }

    public void randomHappyAction() {
        if (this.petData.actionsHappy.length > 0) {
            this.roomUnit.setStatus(RoomUnitStatus.fromString(this.petData.actionsHappy[Emulator.getRandom().nextInt(this.petData.actionsHappy.length)]), Emulator.PREVIEW);
        }
    }

    public void randomSadAction() {
        if (this.petData.actionsTired.length > 0) {
            this.roomUnit.setStatus(RoomUnitStatus.fromString(this.petData.actionsTired[Emulator.getRandom().nextInt(this.petData.actionsTired.length)]), Emulator.PREVIEW);
        }
    }

    public void randomAction() {
        if (this.petData.actionsRandom.length > 0) {
            this.roomUnit.setStatus(RoomUnitStatus.fromString(this.petData.actionsRandom[Emulator.getRandom().nextInt(this.petData.actionsRandom.length)]), Emulator.PREVIEW);
        }
    }

    public void addExperience(int i) {
        this.experience += i;
        if (this.room != null) {
            this.room.sendComposer(new RoomPetExperienceComposer(this, i).compose());
            if (this.level >= PetManager.experiences.length + 1 || this.experience < PetManager.experiences[this.level - 1]) {
                return;
            }
            levelUp();
        }
    }

    protected void levelUp() {
        if (this.level >= PetManager.experiences.length + 1) {
            return;
        }
        if (this.experience > PetManager.experiences[this.level - 1]) {
            this.experience = PetManager.experiences[this.level - 1];
        }
        this.level++;
        say(this.petData.randomVocal(PetVocalsType.LEVEL_UP));
        addHappyness(100);
        this.roomUnit.setStatus(RoomUnitStatus.GESTURE, "exp");
        this.gestureTickTimeout = Emulator.getIntUnixTimestamp();
        AchievementManager.progressAchievement(Emulator.getGameEnvironment().getHabboManager().getHabbo(this.userId), Emulator.getGameEnvironment().getAchievementManager().getAchievement("PetLevelUp"));
        this.room.sendComposer(new PetLevelUpdatedComposer(this).compose());
    }

    public void addThirst(int i) {
        this.levelThirst += i;
        if (this.levelThirst > 100) {
            this.levelThirst = 100;
        }
        if (this.levelThirst < 0) {
            this.levelThirst = 0;
        }
    }

    public void addHunger(int i) {
        this.levelHunger += i;
        if (this.levelHunger > 100) {
            this.levelHunger = 100;
        }
        if (this.levelHunger < 0) {
            this.levelHunger = 0;
        }
    }

    public void freeCommand() {
        this.task = null;
        this.roomUnit.setGoalLocation(getRoomUnit().getCurrentLocation());
        this.roomUnit.clearStatus();
        this.roomUnit.setCanWalk(true);
        say(this.petData.randomVocal(PetVocalsType.GENERIC_NEUTRAL));
    }

    public void scratched(Habbo habbo) {
        addHappyness(10);
        addExperience(10);
        addRespect();
        this.needsUpdate = true;
        if (habbo != null) {
            habbo.getHabboStats().petRespectPointsToGive--;
            habbo.getHabboInfo().getCurrentRoom().sendComposer(new RoomPetRespectComposer(this).compose());
            AchievementManager.progressAchievement(habbo, Emulator.getGameEnvironment().getAchievementManager().getAchievement("PetRespectGiver"));
        }
        AchievementManager.progressAchievement(Emulator.getGameEnvironment().getHabboManager().getHabbo(this.userId), Emulator.getGameEnvironment().getAchievementManager().getAchievement("PetRespectReceiver"));
    }

    public int getId() {
        return this.id;
    }

    public int getUserId() {
        return this.userId;
    }

    public void setUserId(int i) {
        this.userId = i;
    }

    public Room getRoom() {
        return this.room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String str) {
        this.name = str;
    }

    public PetData getPetData() {
        return this.petData;
    }

    public void setPetData(PetData petData) {
        this.petData = petData;
    }

    public int getRace() {
        return this.race;
    }

    public void setRace(int i) {
        this.race = i;
    }

    public String getColor() {
        return this.color;
    }

    public void setColor(String str) {
        this.color = str;
    }

    public int getHappyness() {
        return this.happyness;
    }

    public void setHappyness(int i) {
        this.happyness = i;
    }

    public int getExperience() {
        return this.experience;
    }

    public void setExperience(int i) {
        this.experience = i;
    }

    public int getEnergy() {
        return this.energy;
    }

    public void setEnergy(int i) {
        this.energy = i;
    }

    public int getMaxEnergy() {
        return this.level * 100;
    }

    public int getCreated() {
        return this.created;
    }

    public void setCreated(int i) {
        this.created = i;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int i) {
        this.level = i;
    }

    public RoomUnit getRoomUnit() {
        return this.roomUnit;
    }

    public void setRoomUnit(RoomUnit roomUnit) {
        this.roomUnit = roomUnit;
    }

    public PetTasks getTask() {
        return this.task;
    }

    public void setTask(PetTasks petTasks) {
        this.task = petTasks;
    }

    public boolean isMuted() {
        return this.muted;
    }

    public void setMuted(boolean z) {
        this.muted = z;
    }

    public int getLevelThirst() {
        return this.levelThirst;
    }

    public void setLevelThirst(int i) {
        this.levelThirst = i;
    }

    public int getLevelHunger() {
        return this.levelHunger;
    }

    public void setLevelHunger(int i) {
        this.levelHunger = i;
    }

    public void removeFromRoom() {
        removeFromRoom(false);
    }

    public void removeFromRoom(boolean z) {
        if (this.roomUnit != null && this.roomUnit.getCurrentLocation() != null) {
            this.roomUnit.getCurrentLocation().removeUnit(this.roomUnit);
        }
        if (!z) {
            this.room.sendComposer(new RoomUserRemoveComposer(this.roomUnit).compose());
            this.room.removePet(this.id);
        }
        this.roomUnit = null;
        this.room = null;
        this.needsUpdate = true;
    }

    public int getStayStartedAt() {
        return this.stayStartedAt;
    }

    public void setStayStartedAt(int i) {
        this.stayStartedAt = i;
    }
}
