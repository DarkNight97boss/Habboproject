package com.eu.habbo.habbohotel.items;

import com.eu.habbo.Emulator;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import gnu.trove.map.hash.THashMap;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
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

public class YoutubeManager {
   private static final Logger LOGGER = LoggerFactory.getLogger(YoutubeManager.class);
   private final THashMap<Integer, ArrayList<YoutubeManager.YoutubePlaylist>> playlists = new THashMap();
   private final THashMap<String, YoutubeManager.YoutubePlaylist> playlistCache = new THashMap();
   private final String apiKey = Emulator.getConfig().getValue("youtube.apikey");

   public void load() {
      this.playlists.clear();
      this.playlistCache.clear();
      long millis = System.currentTimeMillis();
      Emulator.getThreading().run(() -> {
         ExecutorService youtubeDataLoaderPool = Executors.newFixedThreadPool(10);
         LOGGER.info("YouTube Manager -> Loading...");

         try {
            Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            try {
               PreparedStatement statement = connection.prepareStatement("SELECT * FROM youtube_playlists");

               try {
                  ResultSet set = statement.executeQuery();

                  try {
                     while (set.next()) {
                        int itemId = set.getInt("item_id");
                        String playlistId = set.getString("playlist_id");
                        youtubeDataLoaderPool.submit(() -> {
                           try {
                              YoutubeManager.YoutubePlaylist playlist = this.getPlaylistDataById(playlistId);
                              if (playlist != null) {
                                 this.addPlaylistToItem(itemId, playlist);
                              }
                           } catch (IOException e) {
                              LOGGER.error("Failed to load YouTube playlist {} ERROR: {}", playlistId, e);
                           }
                        });
                     }
                  } catch (Throwable var13) {
                     if (set != null) {
                        try {
                           set.close();
                        } catch (Throwable var12) {
                           var13.addSuppressed(var12);
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
                     } catch (Throwable var11) {
                        var14.addSuppressed(var11);
                     }
                  }

                  throw var14;
               }

               if (statement != null) {
                  statement.close();
               }
            } catch (Throwable var15) {
               if (connection != null) {
                  try {
                     connection.close();
                  } catch (Throwable var10) {
                     var15.addSuppressed(var10);
                  }
               }

               throw var15;
            }

            if (connection != null) {
               connection.close();
            }
         } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
         }

         youtubeDataLoaderPool.shutdown();

         try {
            youtubeDataLoaderPool.awaitTermination(60L, TimeUnit.SECONDS);
         } catch (InterruptedException e) {
            e.printStackTrace();
         }

         LOGGER.info("YouTube Manager -> Loaded! (" + (System.currentTimeMillis() - millis) + " MS)");
      });
   }

   public YoutubeManager.YoutubePlaylist getPlaylistDataById(String playlistId) throws IOException {
      if (this.playlistCache.containsKey(playlistId)) {
         return (YoutubeManager.YoutubePlaylist)this.playlistCache.get(playlistId);
      }

      if (this.apiKey.isEmpty()) {
         return null;
      }

      URL playlistInfo = new URL("https://youtube.googleapis.com/youtube/v3/playlists?part=snippet&id=" + playlistId + "&maxResults=1&key=" + this.apiKey);
      HttpsURLConnection playlistCon = (HttpsURLConnection)playlistInfo.openConnection();
      if (playlistCon.getResponseCode() != 200) {
         InputStream errorInputStream = playlistCon.getErrorStream();
         InputStreamReader playlistISR = new InputStreamReader(errorInputStream);
         BufferedReader playlistBR = new BufferedReader(playlistISR);
         JsonObject errorObj = JsonParser.parseReader(playlistBR).getAsJsonObject();
         String message = errorObj.get("error").getAsJsonObject().get("message").getAsString();
         LOGGER.error("Failed to load YouTube playlist {} ERROR: {}", playlistId, message);
         return null;
      }

      InputStream playlistInputStream = playlistCon.getInputStream();
      InputStreamReader playlistISR = new InputStreamReader(playlistInputStream);
      BufferedReader playlistBR = new BufferedReader(playlistISR);
      JsonObject playlistData = JsonParser.parseReader(playlistBR).getAsJsonObject();
      JsonArray playlists = playlistData.get("items").getAsJsonArray();
      if (playlists.size() == 0) {
         LOGGER.error("Playlist {} not found!", playlistId);
         return null;
      }

      JsonObject playlistItem = playlists.get(0).getAsJsonObject().get("snippet").getAsJsonObject();
      String name = playlistItem.get("title").getAsString();
      String description = playlistItem.get("description").getAsString();
      ArrayList<YoutubeManager.YoutubeVideo> videos = new ArrayList<>();
      String nextPageToken = "";

      do {
         ArrayList<String> videoIds = new ArrayList<>();
         URL playlistItems;
         if (nextPageToken.isEmpty()) {
            playlistItems = new URL(
               "https://youtube.googleapis.com/youtube/v3/playlistItems?part=snippet%2Cstatus&playlistId=" + playlistId + "&maxResults=50&key=" + this.apiKey
            );
         } else {
            playlistItems = new URL(
               "https://youtube.googleapis.com/youtube/v3/playlistItems?part=snippet%2Cstatus&playlistId="
                  + playlistId
                  + "&pageToken="
                  + nextPageToken
                  + "&maxResults=50&key="
                  + this.apiKey
            );
         }

         HttpsURLConnection con = (HttpsURLConnection)playlistItems.openConnection();
         InputStream is = con.getInputStream();
         InputStreamReader isr = new InputStreamReader(is);
         BufferedReader br = new BufferedReader(isr);
         JsonObject object = JsonParser.parseReader(br).getAsJsonObject();

         for (JsonElement rawVideo : object.get("items").getAsJsonArray()) {
            JsonObject videoData = rawVideo.getAsJsonObject().get("snippet").getAsJsonObject();
            JsonObject videoStatus = rawVideo.getAsJsonObject().get("status").getAsJsonObject();
            if (videoStatus.get("privacyStatus").getAsString().equals("public")) {
               videoIds.add(videoData.get("resourceId").getAsJsonObject().get("videoId").getAsString());
            }
         }

         if (!videoIds.isEmpty()) {
            String commaSeparatedVideos = String.join(",", videoIds);
            URL VideoItems = new URL(
               "https://youtube.googleapis.com/youtube/v3/videos?part=contentDetails&id=" + commaSeparatedVideos + "&maxResults=50&key=" + this.apiKey
            );
            HttpsURLConnection con1 = (HttpsURLConnection)VideoItems.openConnection();
            InputStream is1 = con1.getInputStream();
            InputStreamReader isr1 = new InputStreamReader(is1);
            BufferedReader br1 = new BufferedReader(isr1);
            JsonObject object1 = JsonParser.parseReader(br1).getAsJsonObject();

            for (JsonElement rawVideo : object1.get("items").getAsJsonArray()) {
               JsonObject contentDetails = rawVideo.getAsJsonObject().get("contentDetails").getAsJsonObject();
               int duration = (int)Duration.parse(contentDetails.get("duration").getAsString()).getSeconds();
               if (duration >= 1) {
                  videos.add(new YoutubeManager.YoutubeVideo(rawVideo.getAsJsonObject().get("id").getAsString(), duration));
               }
            }
         }

         if (object.has("nextPageToken")) {
            nextPageToken = object.get("nextPageToken").getAsString();
         } else {
            nextPageToken = null;
         }
      } while (nextPageToken != null);

      if (videos.isEmpty()) {
         LOGGER.warn("Playlist {} has no videos!", playlistId);
         return null;
      } else {
         YoutubeManager.YoutubePlaylist playlist = new YoutubeManager.YoutubePlaylist(playlistId, name, description, videos);
         this.playlistCache.put(playlistId, playlist);
         return playlist;
      }
   }

   public ArrayList<YoutubeManager.YoutubePlaylist> getPlaylistsForItemId(int itemId) {
      return (ArrayList<YoutubeManager.YoutubePlaylist>)this.playlists.get(itemId);
   }

   public void addPlaylistToItem(int itemId, YoutubeManager.YoutubePlaylist playlist) {
      ((ArrayList)this.playlists.computeIfAbsent(itemId, k -> new ArrayList())).add(playlist);
   }

   public static class YoutubePlaylist {
      private final String id;
      private final String name;
      private final String description;
      private final ArrayList<YoutubeManager.YoutubeVideo> videos;

      YoutubePlaylist(String id, String name, String description, ArrayList<YoutubeManager.YoutubeVideo> videos) {
         this.id = id;
         this.name = name;
         this.description = description;
         this.videos = videos;
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

      public ArrayList<YoutubeManager.YoutubeVideo> getVideos() {
         return this.videos;
      }
   }

   public static class YoutubeVideo {
      private final String id;
      private final int duration;

      YoutubeVideo(String id, int duration) {
         this.id = id;
         this.duration = duration;
      }

      public String getId() {
         return this.id;
      }

      public int getDuration() {
         return this.duration;
      }
   }
}
