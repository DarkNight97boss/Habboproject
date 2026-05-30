package com.eu.habbo.core;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bot Oracolo — Fase 1.
 *
 * Quando un utente parla nella stanza configurata come "Oracolo"
 * (default room_id 58), il messaggio viene interpretato come una
 * proposta di nuova feature.
 *
 * Flusso:
 *   1. Hook in RoomUserTalkEvent verifica se room.id == oracolo.room_id
 *   2. Verifica cooldown anti-spam per utente
 *   3. INSERT in feature_requests (status=PENDING)
 *   4. Whisper di conferma con l'ID alla persona che ha proposto
 *
 * Lo staff (acc_supporttool) può cambiare lo stato con :oracolo.
 *
 * Fase 2 (futura, quando oracolo.llm.api_key è impostata):
 *   metodo generateAiDraft(requestId) chiama LLM (Claude/OpenAI) e
 *   popola feature_requests.ai_draft con una bozza di spec markdown.
 */
public class OracoloManager
{
    public enum Status
    {
        PENDING, IN_REVIEW, ACCEPTED, REJECTED, IMPLEMENTED
    }

    public static class FeatureRequest
    {
        public int id;
        public int userId;
        public String userName;
        public int roomId;
        public String text;
        public Status status;
        public int votesUp;
        public int votesDown;
        public String aiDraft;
        public int createdAt;
        public int updatedAt;
    }

    // Cooldown per utente (anti-spam): userId → ultimo timestamp di submit.
    private static final Map<Integer, Integer> COOLDOWNS = new ConcurrentHashMap<>();

    private OracoloManager() {}

    public static boolean isEnabled()
    {
        return Emulator.getConfig().getBoolean("oracolo.enabled", true);
    }

    public static int getRoomId()
    {
        return Emulator.getConfig().getInt("oracolo.room_id", 58);
    }

    public static int getMaxTextLength()
    {
        return Emulator.getConfig().getInt("oracolo.max_text_length", 300);
    }

    public static int getCooldownSeconds()
    {
        return Emulator.getConfig().getInt("oracolo.cooldown_seconds", 30);
    }

    /**
     * Chiamato dal chat handler: se il messaggio è nella stanza Oracolo,
     * lo registra come proposta e ritorna true (chat continua normalmente
     * cosi gli altri vedono cosa è stato proposto).
     */
    public static boolean tryRecordRequest(Habbo habbo, String message)
    {
        if(habbo == null || message == null || message.isEmpty()) return false;
        if(!isEnabled()) return false;
        if(habbo.getHabboInfo().getCurrentRoom() == null) return false;
        if(habbo.getHabboInfo().getCurrentRoom().getId() != getRoomId()) return false;

        // Lo staff non spamma proposte usando la chat — usano i pannelli mod.
        // Possono comunque parlare normalmente nella stanza.
        try
        {
            if(habbo.hasPermission(Permission.ACC_SUPPORTTOOL)) return false;
        }
        catch(Exception ignored) {}

        String text = message.trim();
        if(text.length() < 10)
        {
            try
            {
                String msg = Emulator.getTexts().getValue(
                    "commands.error.cmd_oracolo.too_short",
                    "La proposta deve avere almeno 10 caratteri."
                );
                habbo.whisper(msg, RoomChatMessageBubbles.ALERT);
            }
            catch(Exception ignored) {}
            return false;
        }

        int maxLen = getMaxTextLength();
        if(text.length() > maxLen)
        {
            try
            {
                String msg = Emulator.getTexts().getValue(
                    "commands.error.cmd_oracolo.too_long",
                    "La proposta non può superare %max% caratteri."
                ).replace("%max%", String.valueOf(maxLen));
                habbo.whisper(msg, RoomChatMessageBubbles.ALERT);
            }
            catch(Exception ignored) {}
            return false;
        }

        int userId = habbo.getHabboInfo().getId();
        int now = Emulator.getIntUnixTimestamp();
        int cd = getCooldownSeconds();
        Integer last = COOLDOWNS.get(userId);
        if(last != null && (now - last) < cd)
        {
            try
            {
                String msg = Emulator.getTexts().getValue(
                    "commands.error.cmd_oracolo.cooldown",
                    "Hai già proposto una feature di recente. Riprova tra %seconds% secondi."
                ).replace("%seconds%", String.valueOf(cd - (now - last)));
                habbo.whisper(msg, RoomChatMessageBubbles.ALERT);
            }
            catch(Exception ignored) {}
            return false;
        }

        int newId = insert(userId, habbo.getHabboInfo().getUsername(), getRoomId(), text, now);
        if(newId <= 0) return false;

        COOLDOWNS.put(userId, now);

        try
        {
            String msg = Emulator.getTexts().getValue(
                "commands.success.cmd_oracolo.received",
                "✨ Grazie! La tua proposta è stata registrata come richiesta #%id%. Lo staff la valuterà presto."
            ).replace("%id%", String.valueOf(newId));
            habbo.whisper(msg, RoomChatMessageBubbles.BOT);
        }
        catch(Exception ignored) {}

        return true;
    }

    private static int insert(int userId, String userName, int roomId, String text, int now)
    {
        try(Connection conn = Emulator.getDatabase().getDataSource().getConnection();
            PreparedStatement st = conn.prepareStatement(
                "INSERT INTO feature_requests " +
                "(user_id, user_name, room_id, text, status, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, 'PENDING', ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            ))
        {
            st.setInt(1, userId);
            st.setString(2, userName);
            st.setInt(3, roomId);
            st.setString(4, text);
            st.setInt(5, now);
            st.setInt(6, now);
            st.executeUpdate();
            try(ResultSet rs = st.getGeneratedKeys())
            {
                if(rs.next()) return rs.getInt(1);
            }
        }
        catch(Exception ignored) {}
        return -1;
    }

    public static List<FeatureRequest> listTop(int limit, Status filterStatus)
    {
        List<FeatureRequest> out = new ArrayList<>();
        String sql = (filterStatus == null)
            ? "SELECT * FROM feature_requests ORDER BY (votes_up - votes_down) DESC, id DESC LIMIT ?"
            : "SELECT * FROM feature_requests WHERE status = ? ORDER BY (votes_up - votes_down) DESC, id DESC LIMIT ?";

        try(Connection conn = Emulator.getDatabase().getDataSource().getConnection();
            PreparedStatement st = conn.prepareStatement(sql))
        {
            if(filterStatus == null)
            {
                st.setInt(1, limit);
            }
            else
            {
                st.setString(1, filterStatus.name());
                st.setInt(2, limit);
            }
            try(ResultSet rs = st.executeQuery())
            {
                while(rs.next()) out.add(read(rs));
            }
        }
        catch(Exception ignored) {}
        return out;
    }

    public static FeatureRequest findById(int id)
    {
        try(Connection conn = Emulator.getDatabase().getDataSource().getConnection();
            PreparedStatement st = conn.prepareStatement(
                "SELECT * FROM feature_requests WHERE id = ? LIMIT 1"
            ))
        {
            st.setInt(1, id);
            try(ResultSet rs = st.executeQuery())
            {
                if(rs.next()) return read(rs);
            }
        }
        catch(Exception ignored) {}
        return null;
    }

    /**
     * Voto utente. vote=+1 upvote, vote=-1 downvote. Idempotente per
     * (request_id, user_id): se cambia voto aggiorna, altrimenti rifiuta.
     * Ritorna codice: 0=ok, 1=not found, 2=own request, 3=already voted same.
     */
    public static int castVote(int requestId, Habbo voter, int vote)
    {
        if(voter == null) return 1;
        if(vote != 1 && vote != -1) return 1;
        FeatureRequest req = findById(requestId);
        if(req == null) return 1;
        if(req.userId == voter.getHabboInfo().getId()) return 2;

        int now = Emulator.getIntUnixTimestamp();
        try(Connection conn = Emulator.getDatabase().getDataSource().getConnection())
        {
            // Verifica voto esistente.
            int existing = 0;
            try(PreparedStatement st = conn.prepareStatement(
                "SELECT vote FROM feature_request_votes WHERE request_id = ? AND user_id = ?"
            ))
            {
                st.setInt(1, requestId);
                st.setInt(2, voter.getHabboInfo().getId());
                try(ResultSet rs = st.executeQuery())
                {
                    if(rs.next()) existing = rs.getInt(1);
                }
            }

            if(existing == vote) return 3;

            try(PreparedStatement st = conn.prepareStatement(
                "INSERT INTO feature_request_votes (request_id, user_id, vote, voted_at) " +
                "VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE vote = VALUES(vote), voted_at = VALUES(voted_at)"
            ))
            {
                st.setInt(1, requestId);
                st.setInt(2, voter.getHabboInfo().getId());
                st.setInt(3, vote);
                st.setInt(4, now);
                st.executeUpdate();
            }

            // Ricalcola aggregati.
            try(PreparedStatement st = conn.prepareStatement(
                "UPDATE feature_requests SET " +
                "votes_up = (SELECT COUNT(*) FROM feature_request_votes WHERE request_id = ? AND vote = 1), " +
                "votes_down = (SELECT COUNT(*) FROM feature_request_votes WHERE request_id = ? AND vote = -1), " +
                "updated_at = ? WHERE id = ?"
            ))
            {
                st.setInt(1, requestId);
                st.setInt(2, requestId);
                st.setInt(3, now);
                st.setInt(4, requestId);
                st.executeUpdate();
            }
        }
        catch(Exception ignored) {}
        return 0;
    }

    public static boolean changeStatus(int requestId, Status newStatus)
    {
        if(newStatus == null) return false;
        try(Connection conn = Emulator.getDatabase().getDataSource().getConnection();
            PreparedStatement st = conn.prepareStatement(
                "UPDATE feature_requests SET status = ?, updated_at = ? WHERE id = ?"
            ))
        {
            st.setString(1, newStatus.name());
            st.setInt(2, Emulator.getIntUnixTimestamp());
            st.setInt(3, requestId);
            return st.executeUpdate() > 0;
        }
        catch(Exception ignored) {}
        return false;
    }

    private static FeatureRequest read(ResultSet rs) throws Exception
    {
        FeatureRequest r = new FeatureRequest();
        r.id = rs.getInt("id");
        r.userId = rs.getInt("user_id");
        r.userName = rs.getString("user_name");
        r.roomId = rs.getInt("room_id");
        r.text = rs.getString("text");
        try { r.status = Status.valueOf(rs.getString("status")); }
        catch(Exception ex) { r.status = Status.PENDING; }
        r.votesUp = rs.getInt("votes_up");
        r.votesDown = rs.getInt("votes_down");
        r.aiDraft = rs.getString("ai_draft");
        r.createdAt = rs.getInt("created_at");
        r.updatedAt = rs.getInt("updated_at");
        return r;
    }
}
