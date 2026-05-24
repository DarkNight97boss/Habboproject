package com.eu.habbo.habbohotel.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.messenger.Message;
import com.eu.habbo.habbohotel.rooms.RoomChatMessage;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.friends.FriendChatMessageComposer;
import com.eu.habbo.plugin.events.users.UserTriggerWordFilterEvent;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Normalizer;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/modtool/WordFilter.class */
public class WordFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(WordFilter.class);
    private static final Pattern DIACRITICS_AND_FRIENDS = Pattern.compile("[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+");
    public static boolean ENABLED_FRIENDCHAT = true;
    public static String DEFAULT_REPLACEMENT = "bobba";
    protected THashSet<WordFilterWord> autoReportWords = new THashSet<>();
    protected THashSet<WordFilterWord> hideMessageWords = new THashSet<>();
    protected THashSet<WordFilterWord> words = new THashSet<>();

    public WordFilter() {
        long jCurrentTimeMillis = System.currentTimeMillis();
        reload();
        LOGGER.info("WordFilter -> Loaded! (" + (System.currentTimeMillis() - jCurrentTimeMillis) + " MS)");
    }

    private static String stripDiacritics(String str) {
        return DIACRITICS_AND_FRIENDS.matcher(Normalizer.normalize(str, Normalizer.Form.NFD)).replaceAll(Emulator.PREVIEW);
    }

    public synchronized void reload() {
        if (Emulator.getConfig().getBoolean("hotel.wordfilter.enabled")) {
            this.autoReportWords.clear();
            this.hideMessageWords.clear();
            this.words.clear();
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    Statement statementCreateStatement = connection.createStatement();
                    try {
                        ResultSet resultSetExecuteQuery = statementCreateStatement.executeQuery("SELECT * FROM wordfilter");
                        while (resultSetExecuteQuery.next()) {
                            try {
                                try {
                                    WordFilterWord wordFilterWord = new WordFilterWord(resultSetExecuteQuery);
                                    if (wordFilterWord.autoReport) {
                                        this.autoReportWords.add(wordFilterWord);
                                    } else if (wordFilterWord.hideMessage) {
                                        this.hideMessageWords.add(wordFilterWord);
                                    }
                                    this.words.add(wordFilterWord);
                                } catch (SQLException e) {
                                    LOGGER.error("Caught SQL exception", e);
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
                        if (connection != null) {
                            connection.close();
                        }
                    } catch (Throwable th3) {
                        if (statementCreateStatement != null) {
                            try {
                                statementCreateStatement.close();
                            } catch (Throwable th4) {
                                th3.addSuppressed(th4);
                            }
                        }
                        throw th3;
                    }
                } finally {
                }
            } catch (SQLException e2) {
                LOGGER.error("Caught SQL exception", e2);
            }
        }
    }

    public String normalise(String str) {
        return DIACRITICS_AND_FRIENDS.matcher(Normalizer.normalize(StringUtils.stripAccents(str), Normalizer.Form.NFKD).replaceAll("[,.;:'\"]", " ").replace("I", "l").replaceAll("[^\\p{ASCII}*$]", Emulator.PREVIEW).replaceAll("\\p{M}", " ").replaceAll("^\\p{M}*$]", Emulator.PREVIEW).replaceAll("[1|]", "i").replace("2", "z").replace("3", "e").replace("4", "a").replace("5", "s").replace("8", "b").replace("0", "o").replace(" ", " ").replace("$", "s").replace("ß", "b").trim()).replaceAll(" ");
    }

    public boolean autoReportCheck(RoomChatMessage roomChatMessage) {
        String lowerCase = normalise(roomChatMessage.getMessage()).toLowerCase();
        TObjectHashIterator it = this.autoReportWords.iterator();
        while (it.hasNext()) {
            WordFilterWord wordFilterWord = (WordFilterWord) it.next();
            if (lowerCase.contains(wordFilterWord.key)) {
                Emulator.getGameEnvironment().getModToolManager().quickTicket(roomChatMessage.getHabbo(), "Automatic WordFilter", roomChatMessage.getMessage());
                if (!Emulator.getConfig().getBoolean("notify.staff.chat.auto.report")) {
                    return true;
                }
                Emulator.getGameEnvironment().getHabboManager().sendPacketToHabbosWithPermission(new FriendChatMessageComposer(new Message(roomChatMessage.getHabbo().getHabboInfo().getId(), 0, Emulator.getTexts().getValue("warning.auto.report").replace("%user%", roomChatMessage.getHabbo().getHabboInfo().getUsername()).replace("%word%", wordFilterWord.key))).compose(), "acc_staff_chat");
                return true;
            }
        }
        return false;
    }

    public boolean hideMessageCheck(String str) {
        String lowerCase = normalise(str).toLowerCase();
        TObjectHashIterator it = this.hideMessageWords.iterator();
        while (it.hasNext()) {
            if (lowerCase.contains(((WordFilterWord) it.next()).key)) {
                return true;
            }
        }
        return false;
    }

    public String[] filter(String[] strArr) {
        for (int i = 0; i < strArr.length; i++) {
            strArr[i] = filter(strArr[i], (Habbo) null);
        }
        return strArr;
    }

    public String filter(String str, Habbo habbo) {
        String strReplace = str;
        if (Emulator.getConfig().getBoolean("hotel.wordfilter.normalise")) {
            strReplace = normalise(strReplace);
        }
        TObjectHashIterator it = this.words.iterator();
        boolean z = false;
        while (it.hasNext()) {
            WordFilterWord wordFilterWord = (WordFilterWord) it.next();
            if (StringUtils.containsIgnoreCase(strReplace, wordFilterWord.key) && (habbo == null || !((UserTriggerWordFilterEvent) Emulator.getPluginManager().fireEvent(new UserTriggerWordFilterEvent(habbo, wordFilterWord))).isCancelled())) {
                strReplace = strReplace.replace("(?i)" + wordFilterWord.key, wordFilterWord.replacement);
                z = true;
                if (habbo != null && wordFilterWord.muteTime > 0) {
                    habbo.mute(wordFilterWord.muteTime, false);
                }
            }
        }
        return !z ? str : strReplace;
    }

    public void filter(RoomChatMessage roomChatMessage, Habbo habbo) {
        String lowerCase = roomChatMessage.getMessage().toLowerCase();
        if (Emulator.getConfig().getBoolean("hotel.wordfilter.normalise")) {
            lowerCase = normalise(lowerCase);
        }
        TObjectHashIterator it = this.words.iterator();
        while (it.hasNext()) {
            WordFilterWord wordFilterWord = (WordFilterWord) it.next();
            if (StringUtils.containsIgnoreCase(lowerCase, wordFilterWord.key) && (habbo == null || !((UserTriggerWordFilterEvent) Emulator.getPluginManager().fireEvent(new UserTriggerWordFilterEvent(habbo, wordFilterWord))).isCancelled())) {
                lowerCase = lowerCase.replace(wordFilterWord.key, wordFilterWord.replacement);
                roomChatMessage.filtered = true;
            }
        }
        if (roomChatMessage.filtered) {
            roomChatMessage.setMessage(lowerCase);
        }
    }

    public void addWord(WordFilterWord wordFilterWord) {
        this.words.add(wordFilterWord);
    }
}
