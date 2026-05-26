package com.eu.habbo.messages.incoming.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.messages.incoming.MessageHandler;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Safety-quiz completion notice. Progresses the "SafetyQuizGraduate" achievement.
 *
 * The packet has no server-side validation that the quiz was really completed,
 * so any client could trivially spam-grind the achievement without anti-replay.
 * We mitigate with both a handler-level ratelimit (60s minimum spacing) and a
 * per-user one-shot-per-day guard, so the achievement still tops out within a
 * normal usage envelope but a malicious loop can't farm it.
 */
public class StartSafetyQuizEvent extends MessageHandler {

    /** user_id -> unix day (epochDay) of the last accepted submission. */
    private static final ConcurrentHashMap<Integer, Long> LAST_CLAIM_DAY = new ConcurrentHashMap<>();

    @Override
    public int getRatelimit() {
        return 60_000;
    }

    @Override
    public void handle() throws Exception {
        if (this.client == null || this.client.getHabbo() == null
                || this.client.getHabbo().getHabboInfo() == null) return;

        // Consume and discard the (unused) quiz-name payload; bound it just in case.
        String quizName = this.packet.readString();
        if (quizName != null && quizName.length() > 64) return;

        int userId = this.client.getHabbo().getHabboInfo().getId();
        long today = java.time.LocalDate.now(java.time.ZoneOffset.UTC).toEpochDay();
        Long prev = LAST_CLAIM_DAY.get(userId);
        if (prev != null && prev == today) {
            // Already claimed today.
            return;
        }
        LAST_CLAIM_DAY.put(userId, today);

        AchievementManager.progressAchievement(
                this.client.getHabbo(),
                Emulator.getGameEnvironment().getAchievementManager().getAchievement("SafetyQuizGraduate"));
    }
}
