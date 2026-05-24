package com.eu.habbo;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import com.eu.habbo.core.CleanerThread;
import com.eu.habbo.core.ConfigurationManager;
import com.eu.habbo.core.CryptoConfig;
import com.eu.habbo.core.DatabaseLogger;
import com.eu.habbo.core.Logging;
import com.eu.habbo.core.TextsManager;
import com.eu.habbo.core.consolecommands.ConsoleCommand;
import com.eu.habbo.database.Database;
import com.eu.habbo.habbohotel.GameEnvironment;
import com.eu.habbo.networking.camera.CameraClient;
import com.eu.habbo.networking.gameserver.GameServer;
import com.eu.habbo.networking.rconserver.RCONServer;
import com.eu.habbo.plugin.PluginManager;
import com.eu.habbo.plugin.events.emulator.EmulatorConfigUpdatedEvent;
import com.eu.habbo.plugin.events.emulator.EmulatorLoadedEvent;
import com.eu.habbo.plugin.events.emulator.EmulatorStartShutdownEvent;
import com.eu.habbo.plugin.events.emulator.EmulatorStoppedEvent;
import com.eu.habbo.threading.ThreadPooling;
import com.eu.habbo.util.imager.badges.BadgeImager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Emulator {
   private static final Logger LOGGER = LoggerFactory.getLogger(Emulator.class);
   private static final String OS_NAME = System.getProperty("os.name") != null ? System.getProperty("os.name") : "Unknown";
   private static final String CLASS_PATH = System.getProperty("java.class.path") != null ? System.getProperty("java.class.path") : "Unknown";
   public static final int MAJOR = 3;
   public static final int MINOR = 5;
   public static final int BUILD = 3;
   public static final String PREVIEW = "";
   public static final String version = "Arcturus Morningstar 3.5.3 ";
   private static final String logo = "\n███╗   ███╗ ██████╗ ██████╗ ███╗   ██╗██╗███╗   ██╗ ██████╗ ███████╗████████╗ █████╗ ██████╗ \n████╗ ████║██╔═══██╗██╔══██╗████╗  ██║██║████╗  ██║██╔════╝ ██╔════╝╚══██╔══╝██╔══██╗██╔══██╗\n██╔████╔██║██║   ██║██████╔╝██╔██╗ ██║██║██╔██╗ ██║██║  ███╗███████╗   ██║   ███████║██████╔╝\n██║╚██╔╝██║██║   ██║██╔══██╗██║╚██╗██║██║██║╚██╗██║██║   ██║╚════██║   ██║   ██╔══██║██╔══██╗\n██║ ╚═╝ ██║╚██████╔╝██║  ██║██║ ╚████║██║██║ ╚████║╚██████╔╝███████║   ██║   ██║  ██║██║  ██║\n╚═╝     ╚═╝ ╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═══╝╚═╝╚═╝  ╚═══╝ ╚═════╝ ╚══════╝   ╚═╝   ╚═╝  ╚═╝╚═╝  ╚═╝\nStill Rocking in 2023.\n";
   public static String build = "";
   public static boolean isReady = false;
   public static boolean isShuttingDown = false;
   public static boolean stopped = false;
   public static boolean debugging = false;
   private static int timeStarted = 0;
   private static Runtime runtime;
   private static ConfigurationManager config;
   private static CryptoConfig crypto;
   private static TextsManager texts;
   private static GameServer gameServer;
   private static RCONServer rconServer;
   private static CameraClient cameraClient;
   private static Logging logging;
   private static Database database;
   private static DatabaseLogger databaseLogger;
   private static ThreadPooling threading;
   private static GameEnvironment gameEnvironment;
   private static PluginManager pluginManager;
   private static BadgeImager badgeImager;

   public static void promptEnterKey() {
      System.out.println("\n");
      System.out.println("Press \"ENTER\" if you agree to the terms stated above...");
      Scanner scanner = new Scanner(System.in);
      scanner.nextLine();
   }

   public static void main(String[] args) throws Exception {
      try {
         if (OS_NAME.startsWith("Windows") && !CLASS_PATH.contains("idea_rt.jar")) {
            ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger("ROOT");
            ConsoleAppender<ILoggingEvent> appender = (ConsoleAppender<ILoggingEvent>)root.getAppender("Console");
            appender.stop();
            appender.setWithJansi(true);
            appender.start();
         }

         Locale.setDefault(new Locale("en"));
         setBuild();
         stopped = false;
         ConsoleCommand.load();
         logging = new Logging();
         System.out
            .println(
               "\n███╗   ███╗ ██████╗ ██████╗ ███╗   ██╗██╗███╗   ██╗ ██████╗ ███████╗████████╗ █████╗ ██████╗ \n████╗ ████║██╔═══██╗██╔══██╗████╗  ██║██║████╗  ██║██╔════╝ ██╔════╝╚══██╔══╝██╔══██╗██╔══██╗\n██╔████╔██║██║   ██║██████╔╝██╔██╗ ██║██║██╔██╗ ██║██║  ███╗███████╗   ██║   ███████║██████╔╝\n██║╚██╔╝██║██║   ██║██╔══██╗██║╚██╗██║██║██║╚██╗██║██║   ██║╚════██║   ██║   ██╔══██║██╔══██╗\n██║ ╚═╝ ██║╚██████╔╝██║  ██║██║ ╚████║██║██║ ╚████║╚██████╔╝███████║   ██║   ██║  ██║██║  ██║\n╚═╝     ╚═╝ ╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═══╝╚═╝╚═╝  ╚═══╝ ╚═════╝ ╚══════╝   ╚═╝   ╚═╝  ╚═╝╚═╝  ╚═╝\nStill Rocking in 2023.\n"
            );
         if ("".toLowerCase().contains("beta")) {
            System.out
               .println(
                  "Warning, this is a beta build, this means that there may be unintended consequences so make sure you take regular backups while using this build. If you notice any issues you should make an issue on the Krews Git."
               );
            promptEnterKey();
         }

         System.out.println("");
         LOGGER.warn("Arcturus Morningstar 3.x is no longer accepting merge requests. Please target MS4 branches if you wish to contribute.");
         LOGGER.info("Follow our development at https://git.krews.org/morningstar/Arcturus-Community, ");
         System.out.println("");
         LOGGER.info("This project is for educational purposes only. This Emulator is an open-source fork of Arcturus created by TheGeneral.");
         LOGGER.info("Version: {}", "Arcturus Morningstar 3.5.3 ");
         LOGGER.info("Build: {}", build);
         long startTime = System.nanoTime();
         runtime = Runtime.getRuntime();
         config = new ConfigurationManager("config.ini");
         crypto = new CryptoConfig(
            getConfig().getBoolean("enc.enabled", false), getConfig().getValue("enc.e"), getConfig().getValue("enc.n"), getConfig().getValue("enc.d")
         );
         database = new Database(getConfig());
         databaseLogger = new DatabaseLogger();
         config.loaded = true;
         config.loadFromDatabase();
         threading = new ThreadPooling(getConfig().getInt("runtime.threads"));
         getDatabase().getDataSource().setMaximumPoolSize(getConfig().getInt("runtime.threads") * 2);
         getDatabase().getDataSource().setMinimumIdle(10);
         pluginManager = new PluginManager();
         pluginManager.reload();
         getPluginManager().fireEvent(new EmulatorConfigUpdatedEvent());
         texts = new TextsManager();
         new CleanerThread();
         gameServer = new GameServer(getConfig().getValue("game.host", "127.0.0.1"), getConfig().getInt("game.port", 30000));
         rconServer = new RCONServer(getConfig().getValue("rcon.host", "127.0.0.1"), getConfig().getInt("rcon.port", 30001));
         gameEnvironment = new GameEnvironment();
         gameEnvironment.load();
         gameServer.initializePipeline();
         gameServer.connect();
         rconServer.initializePipeline();
         rconServer.connect();
         badgeImager = new BadgeImager();
         LOGGER.info("Arcturus Morningstar has successfully loaded.");
         LOGGER.info("System launched in: {}ms. Using {} threads!", (System.nanoTime() - startTime) / 1000000.0, Runtime.getRuntime().availableProcessors() * 2);
         LOGGER.info("Memory: {}/{}MB", (runtime.totalMemory() - runtime.freeMemory()) / 1048576L, runtime.freeMemory() / 1048576L);
         debugging = getConfig().getBoolean("debug.mode");
         if (debugging) {
            ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger("ROOT");
            root.setLevel(Level.DEBUG);
            LOGGER.debug("Debugging enabled.");
         }

         getPluginManager().fireEvent(new EmulatorLoadedEvent());
         isReady = true;
         timeStarted = getIntUnixTimestamp();
         if (getConfig().getInt("runtime.threads") < Runtime.getRuntime().availableProcessors() * 2) {
            LOGGER.warn(
               "Emulator settings runtime.threads ({}) can be increased to ({}) to possibly increase performance.",
               getConfig().getInt("runtime.threads"),
               Runtime.getRuntime().availableProcessors() * 2
            );
         }

         getThreading().run(() -> {}, 1500L);
         if (getConfig().getBoolean("console.mode", true)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            while (!isShuttingDown && isReady) {
               try {
                  String line = reader.readLine();
                  if (line != null) {
                     ConsoleCommand.handle(line);
                  }

                  System.out.println("Waiting for command: ");
               } catch (Exception e) {
                  if (!(e instanceof IOException) || !e.getMessage().equals("Bad file descriptor")) {
                     LOGGER.error("Error while reading command", e);
                  }
               }
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   private static void setBuild() {
      if (Emulator.class.getProtectionDomain().getCodeSource() == null) {
         build = "UNKNOWN";
      } else {
         StringBuilder sb = new StringBuilder();

         try {
            String filepath = new File(Emulator.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getAbsolutePath();
            MessageDigest md = MessageDigest.getInstance("MD5");
            FileInputStream fis = new FileInputStream(filepath);
            byte[] dataBytes = new byte[1024];
            int nread = 0;

            while ((nread = fis.read(dataBytes)) != -1) {
               md.update(dataBytes, 0, nread);
            }

            byte[] mdbytes = md.digest();

            for (int i = 0; i < mdbytes.length; i++) {
               sb.append(Integer.toString((mdbytes[i] & 255) + 256, 16).substring(1));
            }
         } catch (Exception e) {
            build = "UNKNOWN";
            return;
         }

         build = sb.toString();
      }
   }

   private static void dispose() {
      getThreading().setCanAdd(false);
      isShuttingDown = true;
      isReady = false;
      LOGGER.info("Stopping Arcturus Morningstar {}", "Arcturus Morningstar 3.5.3 ");

      try {
         if (getPluginManager() != null) {
            getPluginManager().fireEvent(new EmulatorStartShutdownEvent());
         }
      } catch (Exception var9) {
      }

      try {
         if (cameraClient != null) {
            cameraClient.disconnect();
         }
      } catch (Exception var8) {
      }

      try {
         if (rconServer != null) {
            rconServer.stop();
         }
      } catch (Exception var7) {
      }

      try {
         if (gameEnvironment != null) {
            gameEnvironment.dispose();
         }
      } catch (Exception var6) {
      }

      try {
         if (getPluginManager() != null) {
            getPluginManager().fireEvent(new EmulatorStoppedEvent());
         }
      } catch (Exception var5) {
      }

      try {
         if (pluginManager != null) {
            pluginManager.dispose();
         }
      } catch (Exception var4) {
      }

      try {
         if (config != null) {
            config.saveToDatabase();
         }
      } catch (Exception var3) {
      }

      try {
         if (gameServer != null) {
            gameServer.stop();
         }
      } catch (Exception var2) {
      }

      LOGGER.info("Stopped Arcturus Morningstar {}", "Arcturus Morningstar 3.5.3 ");
      if (database != null) {
         getDatabase().dispose();
      }

      stopped = true;

      try {
         if (threading != null) {
            threading.shutDown();
         }
      } catch (Exception var1) {
      }
   }

   public static ConfigurationManager getConfig() {
      return config;
   }

   public static CryptoConfig getCrypto() {
      return crypto;
   }

   public static TextsManager getTexts() {
      return texts;
   }

   public static Database getDatabase() {
      return database;
   }

   public static DatabaseLogger getDatabaseLogger() {
      return databaseLogger;
   }

   public static Runtime getRuntime() {
      return runtime;
   }

   public static GameServer getGameServer() {
      return gameServer;
   }

   public static RCONServer getRconServer() {
      return rconServer;
   }

   @Deprecated
   public static Logging getLogging() {
      return logging;
   }

   public static ThreadPooling getThreading() {
      return threading;
   }

   public static GameEnvironment getGameEnvironment() {
      return gameEnvironment;
   }

   public static PluginManager getPluginManager() {
      return pluginManager;
   }

   public static Random getRandom() {
      return ThreadLocalRandom.current();
   }

   public static BadgeImager getBadgeImager() {
      return badgeImager;
   }

   public static CameraClient getCameraClient() {
      return cameraClient;
   }

   public static synchronized void setCameraClient(CameraClient client) {
      cameraClient = client;
   }

   public static int getTimeStarted() {
      return timeStarted;
   }

   public static int getOnlineTime() {
      return getIntUnixTimestamp() - timeStarted;
   }

   public static void prepareShutdown() {
      System.exit(0);
   }

   public static int timeStringToSeconds(String timeString) {
      int totalSeconds = 0;
      Matcher m = Pattern.compile("(([0-9]*) (second|minute|hour|day|week|month|year))").matcher(timeString);
      Map<String, Integer> map = new HashMap<String, Integer>() {
         {
            this.put("second", 1);
            this.put("minute", 60);
            this.put("hour", 3600);
            this.put("day", 86400);
            this.put("week", 604800);
            this.put("month", 2628000);
            this.put("year", 31536000);
         }
      };

      while (m.find()) {
         try {
            int amount = Integer.parseInt(m.group(2));
            String what = m.group(3);
            totalSeconds += amount * map.get(what);
         } catch (Exception var6) {
         }
      }

      return totalSeconds;
   }

   public static Date modifyDate(Date date, String timeString) {
      int totalSeconds = 0;
      Calendar c = Calendar.getInstance();
      c.setTime(date);
      Matcher m = Pattern.compile("(([0-9]*) (second|minute|hour|day|week|month|year))").matcher(timeString);
      Map<String, Integer> map = new HashMap<String, Integer>() {
         {
            this.put("second", 13);
            this.put("minute", 12);
            this.put("hour", 10);
            this.put("day", 5);
            this.put("week", 4);
            this.put("month", 2);
            this.put("year", 1);
         }
      };

      while (m.find()) {
         try {
            int amount = Integer.parseInt(m.group(2));
            String what = m.group(3);
            c.add(map.get(what), amount);
         } catch (Exception var8) {
         }
      }

      return c.getTime();
   }

   private static String dateToUnixTimestamp(Date date) {
      String res = "";
      Date aux = stringToDate("1970-01-01 00:00:00");
      Timestamp aux1 = dateToTimeStamp(aux);
      Timestamp aux2 = dateToTimeStamp(date);
      long difference = aux2.getTime() - aux1.getTime();
      long seconds = difference / 1000L;
      return res + seconds;
   }

   public static Date stringToDate(String date) {
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      Date res = null;

      try {
         res = format.parse(date);
      } catch (Exception e) {
         LOGGER.error("Error parsing date", e);
      }

      return res;
   }

   public static Timestamp dateToTimeStamp(Date date) {
      return new Timestamp(date.getTime());
   }

   public static Date getDate() {
      return new Date(System.currentTimeMillis());
   }

   public static String getUnixTimestamp() {
      return dateToUnixTimestamp(getDate());
   }

   public static int getIntUnixTimestamp() {
      return (int)(System.currentTimeMillis() / 1000L);
   }

   public static boolean isNumeric(String string) throws IllegalArgumentException {
      boolean isnumeric = false;
      if (string != null && !string.equals("")) {
         isnumeric = true;
         char[] chars = string.toCharArray();

         for (char aChar : chars) {
            isnumeric = Character.isDigit(aChar);
            if (!isnumeric) {
               break;
            }
         }
      }

      return isnumeric;
   }

   public int getUserCount() {
      return gameEnvironment.getHabboManager().getOnlineCount();
   }

   public int getRoomCount() {
      return gameEnvironment.getRoomManager().getActiveRooms().size();
   }

   static {
      Thread hook = new Thread(new Runnable() {
         @Override
         public synchronized void run() {
            Emulator.dispose();
         }
      });
      hook.setPriority(10);
      Runtime.getRuntime().addShutdownHook(hook);
   }
}
