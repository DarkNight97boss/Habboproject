package com.eu.habbo;

import ch.qos.logback.classic.Level;
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
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/Emulator.class */
public final class Emulator {
    private static final Logger LOGGER = LoggerFactory.getLogger(Emulator.class);
    private static final String OS_NAME;
    private static final String CLASS_PATH;
    public static final int MAJOR = 3;
    public static final int MINOR = 5;
    public static final int BUILD = 3;
    public static final String PREVIEW = "";
    public static final String version = "Arcturus Morningstar 3.5.3 ";
    private static final String logo = "\n███╗   ███╗ ██████╗ ██████╗ ███╗   ██╗██╗███╗   ██╗ ██████╗ ███████╗████████╗ █████╗ ██████╗ \n████╗ ████║██╔═══██╗██╔══██╗████╗  ██║██║████╗  ██║██╔════╝ ██╔════╝╚══██╔══╝██╔══██╗██╔══██╗\n██╔████╔██║██║   ██║██████╔╝██╔██╗ ██║██║██╔██╗ ██║██║  ███╗███████╗   ██║   ███████║██████╔╝\n██║╚██╔╝██║██║   ██║██╔══██╗██║╚██╗██║██║██║╚██╗██║██║   ██║╚════██║   ██║   ██╔══██║██╔══██╗\n██║ ╚═╝ ██║╚██████╔╝██║  ██║██║ ╚████║██║██║ ╚████║╚██████╔╝███████║   ██║   ██║  ██║██║  ██║\n╚═╝     ╚═╝ ╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═══╝╚═╝╚═╝  ╚═══╝ ╚═════╝ ╚══════╝   ╚═╝   ╚═╝  ╚═╝╚═╝  ╚═╝\nStill Rocking in 2023.\n";
    public static String build;
    public static boolean isReady;
    public static boolean isShuttingDown;
    public static boolean stopped;
    public static boolean debugging;
    private static int timeStarted;
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
        new Scanner(System.in).nextLine();
    }

    public static void main(String[] strArr) throws Exception {
        try {
            if (OS_NAME.startsWith("Windows") && !CLASS_PATH.contains("idea_rt.jar")) {
                ConsoleAppender appender = LoggerFactory.getLogger("ROOT").getAppender("Console");
                appender.stop();
                appender.setWithJansi(true);
                appender.start();
            }
            Locale.setDefault(new Locale("en"));
            setBuild();
            stopped = false;
            ConsoleCommand.load();
            logging = new Logging();
            System.out.println(logo);
            if (PREVIEW.toLowerCase().contains("beta")) {
                System.out.println("Warning, this is a beta build, this means that there may be unintended consequences so make sure you take regular backups while using this build. If you notice any issues you should make an issue on the Krews Git.");
                promptEnterKey();
            }
            System.out.println(PREVIEW);
            LOGGER.warn("Arcturus Morningstar 3.x is no longer accepting merge requests. Please target MS4 branches if you wish to contribute.");
            LOGGER.info("Follow our development at https://git.krews.org/morningstar/Arcturus-Community, ");
            System.out.println(PREVIEW);
            LOGGER.info("This project is for educational purposes only. This Emulator is an open-source fork of Arcturus created by TheGeneral.");
            LOGGER.info("Version: {}", version);
            LOGGER.info("Build: {}", build);
            long jNanoTime = System.nanoTime();
            runtime = Runtime.getRuntime();
            config = new ConfigurationManager("config.ini");
            crypto = new CryptoConfig(getConfig().getBoolean("enc.enabled", false), getConfig().getValue("enc.e"), getConfig().getValue("enc.n"), getConfig().getValue("enc.d"));
            database = new Database(getConfig());
            databaseLogger = new DatabaseLogger();
            config.loaded = true;
            config.loadFromDatabase();
            threading = new ThreadPooling(Integer.valueOf(getConfig().getInt("runtime.threads")));
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
            LOGGER.info("System launched in: {}ms. Using {} threads!", Double.valueOf((System.nanoTime() - jNanoTime) / 1000000.0d), Integer.valueOf(Runtime.getRuntime().availableProcessors() * 2));
            LOGGER.info("Memory: {}/{}MB", Long.valueOf((runtime.totalMemory() - runtime.freeMemory()) / 1048576), Long.valueOf(runtime.freeMemory() / 1048576));
            debugging = getConfig().getBoolean("debug.mode");
            if (debugging) {
                LoggerFactory.getLogger("ROOT").setLevel(Level.DEBUG);
                LOGGER.debug("Debugging enabled.");
            }
            getPluginManager().fireEvent(new EmulatorLoadedEvent());
            isReady = true;
            timeStarted = getIntUnixTimestamp();
            if (getConfig().getInt("runtime.threads") < Runtime.getRuntime().availableProcessors() * 2) {
                LOGGER.warn("Emulator settings runtime.threads ({}) can be increased to ({}) to possibly increase performance.", Integer.valueOf(getConfig().getInt("runtime.threads")), Integer.valueOf(Runtime.getRuntime().availableProcessors() * 2));
            }
            getThreading().run(() -> {
            }, 1500L);
            if (getConfig().getBoolean("console.mode", true)) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
                while (!isShuttingDown && isReady) {
                    try {
                        String line = bufferedReader.readLine();
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
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    private static void setBuild() {
        if (Emulator.class.getProtectionDomain().getCodeSource() == null) {
            build = "UNKNOWN";
            return;
        }
        StringBuilder sb = new StringBuilder();
        try {
            String absolutePath = new File(Emulator.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getAbsolutePath();
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            FileInputStream fileInputStream = new FileInputStream(absolutePath);
            byte[] bArr = new byte[1024];
            while (true) {
                int i = fileInputStream.read(bArr);
                if (i == -1) {
                    break;
                } else {
                    messageDigest.update(bArr, 0, i);
                }
            }
            for (byte b : messageDigest.digest()) {
                sb.append(Integer.toString((b & 255) + 256, 16).substring(1));
            }
            build = sb.toString();
        } catch (Exception e) {
            build = "UNKNOWN";
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void dispose() {
        getThreading().setCanAdd(false);
        isShuttingDown = true;
        isReady = false;
        LOGGER.info("Stopping Arcturus Morningstar {}", version);
        try {
            if (getPluginManager() != null) {
                getPluginManager().fireEvent(new EmulatorStartShutdownEvent());
            }
        } catch (Exception e) {
        }
        try {
            if (cameraClient != null) {
                cameraClient.disconnect();
            }
        } catch (Exception e2) {
        }
        try {
            if (rconServer != null) {
                rconServer.stop();
            }
        } catch (Exception e3) {
        }
        try {
            if (gameEnvironment != null) {
                gameEnvironment.dispose();
            }
        } catch (Exception e4) {
        }
        try {
            if (getPluginManager() != null) {
                getPluginManager().fireEvent(new EmulatorStoppedEvent());
            }
        } catch (Exception e5) {
        }
        try {
            if (pluginManager != null) {
                pluginManager.dispose();
            }
        } catch (Exception e6) {
        }
        try {
            if (config != null) {
                config.saveToDatabase();
            }
        } catch (Exception e7) {
        }
        try {
            if (gameServer != null) {
                gameServer.stop();
            }
        } catch (Exception e8) {
        }
        LOGGER.info("Stopped Arcturus Morningstar {}", version);
        if (database != null) {
            getDatabase().dispose();
        }
        stopped = true;
        try {
            if (threading != null) {
                threading.shutDown();
            }
        } catch (Exception e9) {
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

    public static synchronized void setCameraClient(CameraClient cameraClient2) {
        cameraClient = cameraClient2;
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

    public static int timeStringToSeconds(String str) {
        int i = 0;
        Matcher matcher = Pattern.compile("(([0-9]*) (second|minute|hour|day|week|month|year))").matcher(str);
        HashMap<String, Integer> map = new HashMap<String, Integer>() { // from class: com.eu.habbo.Emulator.2
            {
                put("second", 1);
                put("minute", 60);
                put("hour", 3600);
                put("day", 86400);
                put("week", 604800);
                put("month", 2628000);
                put("year", 31536000);
            }
        };
        while (matcher.find()) {
            try {
                i += Integer.parseInt(matcher.group(2)) * map.get(matcher.group(3)).intValue();
            } catch (Exception e) {
            }
        }
        return i;
    }

    public static Date modifyDate(Date date, String str) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Matcher matcher = Pattern.compile("(([0-9]*) (second|minute|hour|day|week|month|year))").matcher(str);
        HashMap<String, Integer> map = new HashMap<String, Integer>() { // from class: com.eu.habbo.Emulator.3
            {
                put("second", 13);
                put("minute", 12);
                put("hour", 10);
                put("day", 5);
                put("week", 4);
                put("month", 2);
                put("year", 1);
            }
        };
        while (matcher.find()) {
            try {
                calendar.add(map.get(matcher.group(3)).intValue(), Integer.parseInt(matcher.group(2)));
            } catch (Exception e) {
            }
        }
        return calendar.getTime();
    }

    private static String dateToUnixTimestamp(Date date) {
        return PREVIEW + ((dateToTimeStamp(date).getTime() - dateToTimeStamp(stringToDate("1970-01-01 00:00:00")).getTime()) / 1000);
    }

    public static Date stringToDate(String str) {
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(str);
        } catch (Exception e) {
            LOGGER.error("Error parsing date", e);
        }
        return date;
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
        return (int) (System.currentTimeMillis() / 1000);
    }

    public static boolean isNumeric(String str) throws IllegalArgumentException {
        boolean zIsDigit = false;
        if (str != null && !str.equals(PREVIEW)) {
            zIsDigit = true;
            for (char c : str.toCharArray()) {
                zIsDigit = Character.isDigit(c);
                if (!zIsDigit) {
                    break;
                }
            }
        }
        return zIsDigit;
    }

    public int getUserCount() {
        return gameEnvironment.getHabboManager().getOnlineCount();
    }

    public int getRoomCount() {
        return gameEnvironment.getRoomManager().getActiveRooms().size();
    }

    static {
        OS_NAME = System.getProperty("os.name") != null ? System.getProperty("os.name") : "Unknown";
        CLASS_PATH = System.getProperty("java.class.path") != null ? System.getProperty("java.class.path") : "Unknown";
        build = PREVIEW;
        isReady = false;
        isShuttingDown = false;
        stopped = false;
        debugging = false;
        timeStarted = 0;
        Thread thread = new Thread(new Runnable() { // from class: com.eu.habbo.Emulator.1
            @Override // java.lang.Runnable
            public synchronized void run() {
                Emulator.dispose();
            }
        });
        thread.setPriority(10);
        Runtime.getRuntime().addShutdownHook(thread);
    }
}
