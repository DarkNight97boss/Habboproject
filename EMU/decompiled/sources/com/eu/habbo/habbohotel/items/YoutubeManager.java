package com.eu.habbo.habbohotel.items;

import com.eu.habbo.Emulator;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import gnu.trove.map.hash.THashMap;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HttpsURLConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/YoutubeManager.class */
public class YoutubeManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(YoutubeManager.class);
    private final THashMap<Integer, ArrayList<YoutubePlaylist>> playlists = new THashMap<>();
    private final THashMap<String, YoutubePlaylist> playlistCache = new THashMap<>();
    private final String apiKey = Emulator.getConfig().getValue("youtube.apikey");

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/YoutubeManager$YoutubePlaylist.class */
    public static class YoutubePlaylist {
        private final String id;
        private final String name;
        private final String description;
        private final ArrayList<YoutubeVideo> videos;

        YoutubePlaylist(String str, String str2, String str3, ArrayList<YoutubeVideo> arrayList) {
            this.id = str;
            this.name = str2;
            this.description = str3;
            this.videos = arrayList;
        }

        public String getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public String getDescription() {
            return this.description;
        }

        public ArrayList<YoutubeVideo> getVideos() {
            return this.videos;
        }
    }

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/YoutubeManager$YoutubeVideo.class */
    public static class YoutubeVideo {
        private final String id;
        private final int duration;

        YoutubeVideo(String str, int i) {
            this.id = str;
            this.duration = i;
        }

        public String getId() {
            return this.id;
        }

        public int getDuration() {
            return this.duration;
        }
    }

    public void load() {
        this.playlists.clear();
        this.playlistCache.clear();
        long jCurrentTimeMillis = System.currentTimeMillis();
        Emulator.getThreading().run(() -> {
            Connection connection;
            ExecutorService executorServiceNewFixedThreadPool = Executors.newFixedThreadPool(10);
            LOGGER.info("YouTube Manager -> Loading...");
            try {
                connection = Emulator.getDatabase().getDataSource().getConnection();
            } catch (SQLException e) {
                LOGGER.error("Caught SQL exception", e);
            }
            try {
                PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("SELECT * FROM youtube_playlists");
                try {
                    ResultSet resultSetExecuteQuery = preparedStatementPrepareStatement.executeQuery();
                    while (resultSetExecuteQuery.next()) {
                        try {
                            int i = resultSetExecuteQuery.getInt("item_id");
                            String string = resultSetExecuteQuery.getString("playlist_id");
                            executorServiceNewFixedThreadPool.submit(() -> {
                                try {
                                    YoutubePlaylist playlistDataById = getPlaylistDataById(string);
                                    if (playlistDataById != null) {
                                        addPlaylistToItem(i, playlistDataById);
                                    }
                                } catch (IOException e2) {
                                    LOGGER.error("Failed to load YouTube playlist {} ERROR: {}", string, e2);
                                }
                            });
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
                    executorServiceNewFixedThreadPool.shutdown();
                    try {
                        executorServiceNewFixedThreadPool.awaitTermination(60L, TimeUnit.SECONDS);
                    } catch (InterruptedException e2) {
                        e2.printStackTrace();
                    }
                    LOGGER.info("YouTube Manager -> Loaded! (" + (System.currentTimeMillis() - jCurrentTimeMillis) + " MS)");
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
        });
    }

    public YoutubePlaylist getPlaylistDataById(String str) throws IOException {
        if (this.playlistCache.containsKey(str)) {
            return (YoutubePlaylist) this.playlistCache.get(str);
        }
        if (this.apiKey.isEmpty()) {
            return null;
        }
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) new URL("https://youtube.googleapis.com/youtube/v3/playlists?part=snippet&id=" + str + "&maxResults=1&key=" + this.apiKey).openConnection();
        if (httpsURLConnection.getResponseCode() != 200) {
            LOGGER.error("Failed to load YouTube playlist {} ERROR: {}", str, JsonParser.parseReader(new BufferedReader(new InputStreamReader(httpsURLConnection.getErrorStream()))).getAsJsonObject().get("error").getAsJsonObject().get("message").getAsString());
            return null;
        }
        JsonArray asJsonArray = JsonParser.parseReader(new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()))).getAsJsonObject().get("items").getAsJsonArray();
        if (asJsonArray.size() == 0) {
            LOGGER.error("Playlist {} not found!", str);
            return null;
        }
        JsonObject asJsonObject = asJsonArray.get(0).getAsJsonObject().get("snippet").getAsJsonObject();
        String asString = asJsonObject.get("title").getAsString();
        String asString2 = asJsonObject.get("description").getAsString();
        ArrayList arrayList = new ArrayList();
        String asString3 = Emulator.PREVIEW;
        do {
            ArrayList arrayList2 = new ArrayList();
            JsonObject asJsonObject2 = JsonParser.parseReader(new BufferedReader(new InputStreamReader(((HttpsURLConnection) (asString3.isEmpty() ? new URL("https://youtube.googleapis.com/youtube/v3/playlistItems?part=snippet%2Cstatus&playlistId=" + str + "&maxResults=50&key=" + this.apiKey) : new URL("https://youtube.googleapis.com/youtube/v3/playlistItems?part=snippet%2Cstatus&playlistId=" + str + "&pageToken=" + asString3 + "&maxResults=50&key=" + this.apiKey)).openConnection()).getInputStream()))).getAsJsonObject();
            for (JsonElement jsonElement : asJsonObject2.get("items").getAsJsonArray()) {
                JsonObject asJsonObject3 = jsonElement.getAsJsonObject().get("snippet").getAsJsonObject();
                if (jsonElement.getAsJsonObject().get("status").getAsJsonObject().get("privacyStatus").getAsString().equals("public")) {
                    arrayList2.add(asJsonObject3.get("resourceId").getAsJsonObject().get("videoId").getAsString());
                }
            }
            if (!arrayList2.isEmpty()) {
                for (JsonElement jsonElement2 : JsonParser.parseReader(new BufferedReader(new InputStreamReader(((HttpsURLConnection) new URL("https://youtube.googleapis.com/youtube/v3/videos?part=contentDetails&id=" + String.join(",", arrayList2) + "&maxResults=50&key=" + this.apiKey).openConnection()).getInputStream()))).getAsJsonObject().get("items").getAsJsonArray()) {
                    int seconds = (int) Duration.parse(jsonElement2.getAsJsonObject().get("contentDetails").getAsJsonObject().get("duration").getAsString()).getSeconds();
                    if (seconds >= 1) {
                        arrayList.add(new YoutubeVideo(jsonElement2.getAsJsonObject().get("id").getAsString(), seconds));
                    }
                }
            }
            asString3 = asJsonObject2.has("nextPageToken") ? asJsonObject2.get("nextPageToken").getAsString() : null;
        } while (asString3 != null);
        if (arrayList.isEmpty()) {
            LOGGER.warn("Playlist {} has no videos!", str);
            return null;
        }
        YoutubePlaylist youtubePlaylist = new YoutubePlaylist(str, asString, asString2, arrayList);
        this.playlistCache.put(str, youtubePlaylist);
        return youtubePlaylist;
    }

    public ArrayList<YoutubePlaylist> getPlaylistsForItemId(int i) {
        return (ArrayList) this.playlists.get(Integer.valueOf(i));
    }

    public void addPlaylistToItem(int i, YoutubePlaylist youtubePlaylist) {
        ((ArrayList) this.playlists.computeIfAbsent(Integer.valueOf(i), num -> {
            return new ArrayList();
        })).add(youtubePlaylist);
    }
}
