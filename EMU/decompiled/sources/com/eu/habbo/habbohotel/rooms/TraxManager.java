package com.eu.habbo.habbohotel.rooms;

import com.eu.habbo.Emulator;
import com.eu.habbo.core.Disposable;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.SoundTrack;
import com.eu.habbo.habbohotel.items.interactions.InteractionJukeBox;
import com.eu.habbo.habbohotel.items.interactions.InteractionMusicDisc;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.Outgoing;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.messages.outgoing.inventory.AddHabboItemComposer;
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer;
import com.eu.habbo.messages.outgoing.inventory.RemoveHabboItemComposer;
import com.eu.habbo.messages.outgoing.rooms.items.jukebox.JukeBoxMySongsComposer;
import com.eu.habbo.messages.outgoing.rooms.items.jukebox.JukeBoxNowPlayingMessageComposer;
import com.eu.habbo.messages.outgoing.rooms.items.jukebox.JukeBoxPlayListComposer;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.hash.THashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/rooms/TraxManager.class */
public class TraxManager implements Disposable {
    public static int NORMAL_JUKEBOX_LIMIT = 10;
    public static int LARGE_JUKEBOX_LIMIT = 20;
    private static final Logger LOGGER = LoggerFactory.getLogger(TraxManager.class);
    private final Room room;
    private InteractionJukeBox jukeBox;
    private int songsLimit;
    private final List<InteractionMusicDisc> songs = new ArrayList(0);
    private int totalLength = 0;
    private int startedTimestamp = 0;
    private InteractionMusicDisc currentlyPlaying = null;
    private int playingIndex = 0;
    private int cycleStartedTimestamp = 0;
    private Habbo starter = null;
    private boolean disposed = false;

    public TraxManager(Room room) {
        this.songsLimit = 0;
        this.room = room;
        this.jukeBox = loadRoomJukebox();
        if (this.jukeBox != null) {
            loadPlaylist();
            this.songsLimit = getSongsLimit(this.jukeBox);
            return;
        }
        TObjectHashIterator it = room.getRoomSpecialTypes().getItemsOfType(InteractionJukeBox.class).iterator();
        while (it.hasNext()) {
            this.jukeBox = (InteractionJukeBox) ((HabboItem) it.next());
        }
        if (this.jukeBox != null) {
            loadPlaylist();
            this.songsLimit = getSongsLimit(this.jukeBox);
        }
    }

    public InteractionJukeBox loadRoomJukebox() {
        HabboItem habboItemLoadHabboItem;
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM room_trax WHERE room_id = ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, this.room.getId());
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    try {
                        if (!resultSetExecuteQuery.next() || (habboItemLoadHabboItem = Emulator.getGameEnvironment().getItemManager().loadHabboItem(resultSetExecuteQuery.getInt("trax_item_id"))) == null) {
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
                        if (!(habboItemLoadHabboItem instanceof InteractionJukeBox)) {
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
                        InteractionJukeBox interactionJukeBox = (InteractionJukeBox) habboItemLoadHabboItem;
                        if (resultSetExecuteQuery != null) {
                            resultSetExecuteQuery.close();
                        }
                        if (preparedStatementPrepareStatement != null) {
                            preparedStatementPrepareStatement.close();
                        }
                        if (connection != null) {
                            connection.close();
                        }
                        return interactionJukeBox;
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
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
            return null;
        }
    }

    public void loadPlaylist() {
        if (this.jukeBox == null) {
            return;
        }
        this.songs.clear();
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM trax_playlist WHERE trax_item_id = ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, this.jukeBox.getId());
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        try {
                            HabboItem habboItemLoadHabboItem = Emulator.getGameEnvironment().getItemManager().loadHabboItem(resultSetExecuteQuery.getInt("item_id"));
                            if (habboItemLoadHabboItem != null) {
                                if ((habboItemLoadHabboItem instanceof InteractionMusicDisc) && habboItemLoadHabboItem.getRoomId() == -1) {
                                    SoundTrack soundTrack = Emulator.getGameEnvironment().getItemManager().getSoundTrack(((InteractionMusicDisc) habboItemLoadHabboItem).getSongId());
                                    if (soundTrack != null) {
                                        this.songs.add((InteractionMusicDisc) habboItemLoadHabboItem);
                                        this.totalLength += soundTrack.getLength();
                                    }
                                } else {
                                    deleteSongFromPlaylist(this.jukeBox.getId(), habboItemLoadHabboItem.getId());
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
    }

    public static void deleteSongFromPlaylist(int i, int i2) {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("DELETE FROM trax_playlist WHERE trax_item_id = ? AND item_id = ? LIMIT 1");
                try {
                    preparedStatementPrepareStatement.setInt(1, i);
                    preparedStatementPrepareStatement.setInt(2, i2);
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

    public void addTraxOnRoom(InteractionJukeBox interactionJukeBox) {
        if (this.jukeBox != null) {
            return;
        }
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO room_trax (room_id, trax_item_id) VALUES (?, ?)");
                try {
                    preparedStatementPrepareStatement.setInt(1, this.room.getId());
                    preparedStatementPrepareStatement.setInt(2, interactionJukeBox.getId());
                    preparedStatementPrepareStatement.execute();
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                    this.jukeBox = interactionJukeBox;
                    loadPlaylist();
                    this.songsLimit = getSongsLimit(this.jukeBox);
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

    public void removeTraxOnRoom(InteractionJukeBox interactionJukeBox) {
        if (this.jukeBox.getId() != interactionJukeBox.getId()) {
            return;
        }
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("DELETE FROM room_trax WHERE room_id = ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, this.room.getId());
                    preparedStatementPrepareStatement.execute();
                    if (preparedStatementPrepareStatement != null) {
                        preparedStatementPrepareStatement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                    stop();
                    this.jukeBox = null;
                    this.songs.clear();
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

    public void cycle() {
        if (isPlaying()) {
            if (timePlaying() >= this.totalLength) {
                play(0);
            }
            if (currentSong() == null || Emulator.getIntUnixTimestamp() < this.startedTimestamp + currentSong().getLength()) {
                return;
            }
            play((this.playingIndex + 1) % this.songs.size());
        }
    }

    public void play(int i) {
        play(i, null);
    }

    public void play(int i, Habbo habbo) {
        if (this.currentlyPlaying == null) {
            this.jukeBox.setExtradata("1");
            this.room.updateItem(this.jukeBox);
        }
        if (this.songs.isEmpty()) {
            stop();
            return;
        }
        int size = i % this.songs.size();
        this.currentlyPlaying = this.songs.get(size);
        if (this.currentlyPlaying != null) {
            this.room.setJukeBoxActive(true);
            this.startedTimestamp = Emulator.getIntUnixTimestamp();
            this.playingIndex = size;
            if (habbo != null) {
                this.starter = habbo;
                this.cycleStartedTimestamp = Emulator.getIntUnixTimestamp();
            }
        }
        this.room.sendComposer(new JukeBoxNowPlayingMessageComposer(Emulator.getGameEnvironment().getItemManager().getSoundTrack(this.currentlyPlaying.getSongId()), this.playingIndex, 0).compose());
    }

    public void stop() {
        if (this.starter != null && this.cycleStartedTimestamp > 0) {
            AchievementManager.progressAchievement(this.starter, Emulator.getGameEnvironment().getAchievementManager().getAchievement("MusicPlayer"), (Emulator.getIntUnixTimestamp() - this.cycleStartedTimestamp) / 60);
        }
        this.room.setJukeBoxActive(false);
        this.currentlyPlaying = null;
        this.startedTimestamp = 0;
        this.cycleStartedTimestamp = 0;
        this.starter = null;
        this.playingIndex = 0;
        this.jukeBox.setExtradata("0");
        this.room.updateItem(this.jukeBox);
        this.room.sendComposer(new JukeBoxNowPlayingMessageComposer(null, -1, 0).compose());
    }

    public SoundTrack currentSong() {
        if (this.songs.isEmpty() || this.playingIndex >= this.songs.size()) {
            return null;
        }
        return Emulator.getGameEnvironment().getItemManager().getSoundTrack(this.songs.get(this.playingIndex).getSongId());
    }

    public void addSong(InteractionMusicDisc interactionMusicDisc, Habbo habbo) {
        if (this.jukeBox == null) {
            return;
        }
        if (this.songsLimit < this.songs.size() + 1) {
            new THashMap();
            habbo.getClient().sendResponse(new BubbleAlertComposer("${playlist.editor.alert.playlist.full.title}", "${playlist.editor.alert.playlist.full}").compose());
            return;
        }
        SoundTrack soundTrack = Emulator.getGameEnvironment().getItemManager().getSoundTrack(interactionMusicDisc.getSongId());
        if (soundTrack != null) {
            this.totalLength += soundTrack.getLength();
            this.songs.add(interactionMusicDisc);
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO trax_playlist (trax_item_id, item_id) VALUES (?, ?)");
                    try {
                        preparedStatementPrepareStatement.setInt(1, this.jukeBox.getId());
                        preparedStatementPrepareStatement.setInt(2, interactionMusicDisc.getId());
                        preparedStatementPrepareStatement.execute();
                        if (preparedStatementPrepareStatement != null) {
                            preparedStatementPrepareStatement.close();
                        }
                        if (connection != null) {
                            connection.close();
                        }
                        this.room.sendComposer(new JukeBoxPlayListComposer(this.songs, this.totalLength).compose());
                        interactionMusicDisc.setRoomId(-1);
                        interactionMusicDisc.needsUpdate(true);
                        Emulator.getThreading().run(interactionMusicDisc);
                        habbo.getInventory().getItemsComponent().removeHabboItem(interactionMusicDisc);
                        habbo.getClient().sendResponse(new RemoveHabboItemComposer(interactionMusicDisc.getGiftAdjustedId()));
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
                return;
            }
        }
        sendUpdatedSongList();
    }

    public void removeSong(int i) {
        if (this.songs.isEmpty()) {
            return;
        }
        InteractionMusicDisc song = getSong(i);
        if (song != null) {
            this.songs.remove(song);
            deleteSongFromPlaylist(this.jukeBox.getId(), i);
            this.totalLength -= Emulator.getGameEnvironment().getItemManager().getSoundTrack(song.getSongId()).getLength();
            if (this.currentlyPlaying == song) {
                play(this.playingIndex);
            }
            this.room.sendComposer(new JukeBoxPlayListComposer(this.songs, this.totalLength).compose());
            song.setRoomId(0);
            song.needsUpdate(true);
            Emulator.getThreading().run(song);
            Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(song.getUserId());
            if (habbo != null) {
                habbo.getInventory().getItemsComponent().addItem(song);
                GameClient client = habbo.getClient();
                if (client != null) {
                    client.sendResponse(new AddHabboItemComposer(song));
                    client.sendResponse(new InventoryRefreshComposer());
                }
            }
        }
        sendUpdatedSongList();
    }

    public static void removeAllSongs(InteractionJukeBox interactionJukeBox) {
        try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM trax_playlist WHERE trax_item_id = ?");
                try {
                    preparedStatementPrepareStatement.setInt(1, interactionJukeBox.getId());
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        try {
                            HabboItem habboItemLoadHabboItem = Emulator.getGameEnvironment().getItemManager().loadHabboItem(resultSetExecuteQuery.getInt("item_id"));
                            deleteSongFromPlaylist(interactionJukeBox.getId(), resultSetExecuteQuery.getInt("item_id"));
                            if (habboItemLoadHabboItem != null && (habboItemLoadHabboItem instanceof InteractionMusicDisc) && habboItemLoadHabboItem.getRoomId() == -1) {
                                habboItemLoadHabboItem.setRoomId(0);
                                habboItemLoadHabboItem.needsUpdate(true);
                                Emulator.getThreading().run(habboItemLoadHabboItem);
                                Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(habboItemLoadHabboItem.getUserId());
                                if (habbo != null) {
                                    habbo.getInventory().getItemsComponent().addItem(habboItemLoadHabboItem);
                                    GameClient client = habbo.getClient();
                                    if (client != null) {
                                        client.sendResponse(new AddHabboItemComposer(habboItemLoadHabboItem));
                                        client.sendResponse(new InventoryRefreshComposer());
                                    }
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
    }

    public List<SoundTrack> soundTrackList() {
        ArrayList arrayList = new ArrayList(this.songs.size());
        Iterator<InteractionMusicDisc> it = this.songs.iterator();
        while (it.hasNext()) {
            SoundTrack soundTrack = Emulator.getGameEnvironment().getItemManager().getSoundTrack(it.next().getSongId());
            if (soundTrack != null) {
                arrayList.add(soundTrack);
            }
        }
        return arrayList;
    }

    public List<InteractionMusicDisc> myList(Habbo habbo) {
        return (List) habbo.getInventory().getItemsComponent().getItems().valueCollection().stream().filter(habboItem -> {
            return (habboItem instanceof InteractionMusicDisc) && habboItem.getRoomId() == 0;
        }).map(habboItem2 -> {
            return (InteractionMusicDisc) habboItem2;
        }).collect(Collectors.toList());
    }

    public InteractionMusicDisc getSong(int i) {
        for (InteractionMusicDisc interactionMusicDisc : this.songs) {
            if (interactionMusicDisc != null && interactionMusicDisc.getId() == i) {
                return interactionMusicDisc;
            }
        }
        return null;
    }

    public int getSongsLimit(InteractionJukeBox interactionJukeBox) {
        return "jukebox_big".equals(interactionJukeBox.getBaseItem().getName()) ? LARGE_JUKEBOX_LIMIT : NORMAL_JUKEBOX_LIMIT;
    }

    public void updateCurrentPlayingSong(Habbo habbo) {
        if (isPlaying()) {
            habbo.getClient().sendResponse(new JukeBoxNowPlayingMessageComposer(Emulator.getGameEnvironment().getItemManager().getSoundTrack(this.currentlyPlaying.getSongId()), this.playingIndex, Outgoing.CraftableProductsComposer * (Emulator.getIntUnixTimestamp() - this.startedTimestamp)));
        } else {
            habbo.getClient().sendResponse(new JukeBoxNowPlayingMessageComposer(null, -1, 0));
        }
    }

    public void sendUpdatedSongList() {
        this.room.getHabbos().forEach(habbo -> {
            GameClient client = habbo.getClient();
            if (client != null) {
                client.sendResponse(new JukeBoxMySongsComposer(myList(habbo)));
            }
        });
    }

    public int timePlaying() {
        return Emulator.getIntUnixTimestamp() - this.startedTimestamp;
    }

    public int totalLength() {
        return this.totalLength;
    }

    public List<InteractionMusicDisc> getSongs() {
        return this.songs;
    }

    public boolean isPlaying() {
        return this.currentlyPlaying != null;
    }

    public int getSongsLimit() {
        return this.songsLimit;
    }

    public InteractionJukeBox getJukeBox() {
        return this.jukeBox;
    }

    @Override // com.eu.habbo.core.Disposable
    public void dispose() {
        this.disposed = true;
    }

    @Override // com.eu.habbo.core.Disposable
    public boolean disposed() {
        return this.disposed;
    }
}
