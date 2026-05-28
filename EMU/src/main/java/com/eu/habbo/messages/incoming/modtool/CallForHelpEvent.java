package com.eu.habbo.messages.incoming.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.CfhTopic;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboManager;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.modtool.ModToolIssueResponseAlertComposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Player-side "Call For Help" — apre un ticket di supporto che finisce
 * nel mod tool dello staff.
 *
 * Arcturus 3.5.5 carica le tabelle support_cfh_topics/categories e ha
 * tutto il backend (ModToolManager.addTicket / updateTicketToMods,
 * ModToolIssueInfoComposer, InsertModToolIssue) MA non includeva nessun
 * handler player-side -> il client mandava il pacchetto e l'EMU lo
 * ignorava. Questo handler chiude il gap.
 *
 * Wire layout (Habbo PROD-2015+, vedi {@code CallForHelpMessageComposer}
 * del client Nitro):
 *
 *   int    category    -- support_cfh_topics.id selezionata dal player
 *   int    reportedId  -- user_id segnalato (0 se non specifico)
 *   string message     -- descrizione libera del reporter
 *
 * Se il tuo client manda un ordine diverso (alcune build invertono
 * message/reportedId), adatta l'ordine delle read sotto.
 *
 * Difese:
 *   - {@link MessageHandler#getRatelimit()} = 15s (cap dal framework).
 *   - {@code ModToolManager.canOpenCfh()} con cooldown server-side
 *     configurabile (default 60s) -> anti-spam ticket anche via reconnect.
 *   - Topic deve esistere nel DB -> rifiuta input fuzz.
 *   - Message capped a 512 char prima di toccare il DB.
 */
public class CallForHelpEvent extends MessageHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CallForHelpEvent.class);

    @Override
    public int getRatelimit() {
        return 15000;
    }

    @Override
    public void handle() throws Exception {
        if (this.client == null || this.client.getHabbo() == null) return;
        Habbo reporter = this.client.getHabbo();
        int reporterId = reporter.getHabboInfo().getId();

        int category   = this.packet.readInt();
        int reportedId = this.packet.readInt();
        String message = this.packet.readString();

        if (message == null) message = "";
        if (message.length() > 512) message = message.substring(0, 512);

        // Validation: la topic DEVE esistere nel DB. Un client malformato che manda
        // category=99999 va rifiutato senza creare ticket fantasma.
        CfhTopic topic = Emulator.getGameEnvironment().getModToolManager().getCfhTopic(category);
        if (topic == null) {
            LOGGER.warn("CallForHelp refused: unknown topic id {} from user {}", category, reporterId);
            return;
        }

        // Server-side cooldown per anti-spam (oltre al ratelimit del framework).
        if (!Emulator.getGameEnvironment().getModToolManager().canOpenCfh(reporterId)) {
            this.client.sendResponse(new ModToolIssueResponseAlertComposer(
                    "Hai gia' inviato una segnalazione di recente. Attendi prima di inviarne un'altra."));
            return;
        }

        // Resolve reported user (puo' essere offline).
        String reportedUsername = "";
        int resolvedReportedId = 0;
        int roomId = 0;
        if (reportedId > 0) {
            Habbo onlineTarget = Emulator.getGameEnvironment().getHabboManager().getHabbo(reportedId);
            if (onlineTarget != null && onlineTarget.getHabboInfo() != null) {
                resolvedReportedId = onlineTarget.getHabboInfo().getId();
                reportedUsername = onlineTarget.getHabboInfo().getUsername();
                if (onlineTarget.getHabboInfo().getCurrentRoom() != null) {
                    roomId = onlineTarget.getHabboInfo().getCurrentRoom().getId();
                }
            } else {
                HabboInfo offlineTarget = HabboManager.getOfflineHabboInfo(reportedId);
                if (offlineTarget != null) {
                    resolvedReportedId = offlineTarget.getId();
                    reportedUsername = offlineTarget.getUsername();
                }
            }
        }

        Emulator.getGameEnvironment().getModToolManager().openCfhTicket(
                reporter, resolvedReportedId, reportedUsername, roomId, message, category);

        // Acknowledge to the player. Topic ha un auto-reply configurabile in DB
        // (support_cfh_topics.auto_reply); se vuoto, mandiamo un default.
        String ack = (topic.reply != null && !topic.reply.isEmpty())
                ? topic.reply
                : "La tua segnalazione e' stata inviata allo staff. Grazie per averci aiutato a mantenere l'hotel sicuro.";
        this.client.sendResponse(new ModToolIssueResponseAlertComposer(ack));
    }
}
