package com.eu.habbo.networking.camera.messages.incoming;

import com.eu.habbo.networking.camera.CameraClient;
import com.eu.habbo.networking.camera.CameraIncomingMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CameraLoginStatusEvent extends CameraIncomingMessage {

    private static final Logger LOGGER = LoggerFactory.getLogger(CameraLoginStatusEvent.class);

    public final static int LOGIN_OK = 0;
    public final static int LOGIN_ERROR = 1;
    public final static int NO_ACCOUNT = 2;
    public final static int ALREADY_LOGGED_IN = 3;
    public final static int BANNED = 4;
    public final static int OLD_BUILD = 5;
    public final static int NO_CAMERA_SUBSCRIPTION = 6;

    public CameraLoginStatusEvent(Short header, ByteBuf body) {
        super(header, body);
    }

    @Override
    public void handle(Channel client) throws Exception {
        int status = this.readInt();

        // Messaggi tradotti in italiano. Riferimenti al Camera Server upstream
        // (servizio fornito da Arcturus) preservati per chiarezza operativa:
        // se l'operatore non usa quel servizio, questi error non vengono mai
        // emessi (lo Status arriva solo se il client si connette al server).
        if (status == LOGIN_ERROR) {
            LOGGER.error("Login Camera Server fallito: credenziali errate.");
        } else if (status == NO_ACCOUNT) {
            LOGGER.error("Login Camera Server fallito: nessun account trovato. Registrazione gratuita sui forum upstream Arcturus (http://arcturus.pw/).");
        } else if (status == BANNED) {
            LOGGER.error("Accesso al Camera Server negato: account bannato sui forum upstream Arcturus.");
        } else if (status == ALREADY_LOGGED_IN) {
            LOGGER.error("Risulti gia' connesso al Camera Server.");
        } else if (status == OLD_BUILD) {
            LOGGER.error("Questa versione del motore Arcturus non e' piu' supportata dal Camera Server. Aggiorna.");
        } else if (status == NO_CAMERA_SUBSCRIPTION) {
            LOGGER.error("Sottoscrizione Camera Server assente: il servizio non e' utilizzabile.");
            LOGGER.error("Il motore (Arcturus Morningstar / Asteria Core) e' gratis: la sottoscrizione finanzia il Camera Server upstream.");
            LOGGER.error("Trial $2.50 / annuale $10 / lifetime $25 — vedi http://arcturus.pw/mysubscriptions.php");
        }

        if (status == LOGIN_OK) {
            CameraClient.isLoggedIn = true;
            LOGGER.info("Connesso al Camera Server (Arcturus upstream).");
        } else {
            CameraClient.attemptReconnect = false;
        }
    }
}