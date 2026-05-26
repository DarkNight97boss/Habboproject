-- =============================================================
-- Asteria Core — Localizzazione italiana per emulator_texts
-- =============================================================
--
-- Popola la tabella `emulator_texts` con le traduzioni italiane delle
-- chiavi piu' visibili agli utenti. Idempotente via INSERT ... ON
-- DUPLICATE KEY UPDATE: se la chiave esiste gia' (con un'altra lingua)
-- viene aggiornata; se manca, viene creata.
--
-- Strategia: tradotte ~80 chiavi di alta visibilita' (errori comandi,
-- messaggi auth/MFA/anti-flood, permessi, stanze). Le ~680 chiavi
-- restanti (descrizioni comandi staff dettagliate, achievement labels,
-- ecc.) restano in inglese in attesa di un seed esteso (Fase 6).
--
-- Schema (gia' presente in BaseDB):
--   CREATE TABLE emulator_texts (
--     `key` varchar(100) NOT NULL PRIMARY KEY,
--     `value` varchar(4096) NOT NULL DEFAULT ''
--   );
--
-- Per applicarla:
--   mysql -u root ms < EMU/sqlupdates/i18n_italian_seed.sql
--   poi nel server: ":reloadtexts" (comando staff)

START TRANSACTION;

-- ============= COMANDI: errori comuni =============
INSERT INTO `emulator_texts` (`key`, `value`) VALUES
  ('commands.error.cmd_userinfo.forgot_username', 'Specifica un nome utente.'),
  ('commands.error.cmd_shout_all.forgot_message', 'Specifica un messaggio.'),
  ('commands.error.cmd_control.not_self', 'Non puoi controllare te stesso.'),
  ('commands.error.cmd_control.not_found', 'Utente %user% non trovato.'),
  ('commands.error.cmd_alert.forgot_username', 'Specifica un nome utente.'),
  ('commands.error.cmd_alert.forgot_message', 'Specifica un messaggio.'),
  ('commands.error.cmd_alert.user_offline', '%user% non risulta online.'),
  ('commands.error.cmd_ban.forgot_user', 'Specifica un nome utente.'),
  ('commands.error.cmd_ban.forgot_time', 'Specifica una durata in secondi.'),
  ('commands.error.cmd_ban.invalid_time', 'Usa un numero valido.'),
  ('commands.error.cmd_ban.time_to_short', 'La durata del ban deve essere almeno 600 secondi (10 minuti).'),
  ('commands.error.cmd_ban.ban_self', 'Non puoi bannare te stesso.'),
  ('commands.error.cmd_ban.user_offline', 'Utente target non trovato.'),
  ('commands.error.cmd_credits.invalid_amount', 'Usa un numero valido.'),
  ('commands.error.cmd_credits.user_offline', 'Utente %user% non trovato.'),
  ('commands.error.cmd_disconnect.forgot_username', 'Specifica un nome utente.'),
  ('commands.error.cmd_disconnect.disconnect_self', 'Non puoi disconnettere te stesso.'),
  ('commands.error.cmd_disconnect.user_offline', 'Utente non trovato.'),
  ('commands.error.cmd_ha.forgot_message', 'Specifica un messaggio.'),
  ('commands.error.cmd_duckets.invalid_amount', 'Usa un numero valido.'),
  ('commands.error.cmd_duckets.user_offline', 'Utente %user% non trovato.'),
  ('commands.error.cmd_mute.forgot_username', 'Specifica un nome utente.'),
  ('commands.error.cmd_mute.user_offline', 'Utente %user% non trovato.'),
  ('commands.error.cmd_mute.invalid_time', 'Usa un numero di minuti valido.'),
  ('commands.error.cmd_mute.mute_self', 'Non puoi silenziare te stesso.'),
  ('commands.error.cmd_kick.forgot_username', 'Specifica un nome utente.'),
  ('commands.error.cmd_kick.user_offline', 'Utente %user% non trovato.'),
  ('commands.error.cmd_kick.kick_self', 'Non puoi espellere te stesso.'),
  ('commands.error.not_room_owner', 'Non sei il proprietario di questa stanza.'),
  ('commands.error.not_enough_credits', 'Crediti insufficienti.'),
  ('commands.error.not_enough_points', 'Valuta insufficiente.'),
  ('commands.error.in_use', 'Stanza in uso, riprova fra qualche istante.')
ON DUPLICATE KEY UPDATE `value` = VALUES(`value`);

-- ============= COMANDI: conferme/success =============
INSERT INTO `emulator_texts` (`key`, `value`) VALUES
  ('commands.generic.cmd_disconnect.success', 'L''utente %user% e'' stato disconnesso.'),
  ('commands.generic.cmd_freeze.frozen', 'Sei stato bloccato dallo staff.'),
  ('commands.generic.cmd_unfreeze.unfrozen', 'Lo staff ti ha sbloccato.'),
  ('commands.generic.cmd_credits.received', 'Hai ricevuto %amount% Crediti.'),
  ('commands.generic.cmd_duckets.received', 'Hai ricevuto %amount% Duckets.'),
  ('commands.generic.cmd_pixels.received', 'Hai ricevuto %amount% Pixel.'),
  ('commands.generic.cmd_points.received', 'Hai ricevuto %amount% Punti.'),
  ('commands.generic.cmd_setspeed.changed', 'Velocita'' della stanza impostata a %speed%.')
ON DUPLICATE KEY UPDATE `value` = VALUES(`value`);

-- ============= AUTH / SESSIONE =============
INSERT INTO `emulator_texts` (`key`, `value`) VALUES
  ('hotel.access.denied.no_username', 'Inserisci un nome utente.'),
  ('hotel.welcome.alert', 'Benvenuto su Asteria Core, %username%!'),
  ('hotel.alert.scripter', 'Attivita'' sospetta rilevata sul tuo account.'),
  ('connection.lost', 'Connessione interrotta.'),
  ('access.denied', 'Accesso negato.'),
  ('permission.insufficient', 'Permessi insufficienti.'),
  ('user.not_found', 'Utente non trovato.'),
  ('room.not_found', 'Stanza non trovata.'),
  ('request.invalid', 'Richiesta non valida.'),
  ('maintenance.enabled', 'Modalita'' manutenzione attiva. Riprova fra qualche minuto.')
ON DUPLICATE KEY UPDATE `value` = VALUES(`value`);

-- ============= STAFF MFA (Wave hardening) =============
INSERT INTO `emulator_texts` (`key`, `value`) VALUES
  ('mfa.staff.locked', 'Verifica il codice del tuo autenticatore per sbloccare i poteri staff.'),
  ('mfa.staff.challenge.title', 'Verifica MFA Staff'),
  ('mfa.staff.challenge.body', 'Inserisci il codice a 6 cifre dal tuo autenticatore (Google/FreeOTP).'),
  ('mfa.staff.verify.ok', 'Verifica MFA completata. Permessi staff attivi.'),
  ('mfa.staff.verify.invalid', 'Codice non valido o scaduto.'),
  ('mfa.staff.verify.locked', 'Troppi tentativi falliti. Account bloccato temporaneamente.')
ON DUPLICATE KEY UPDATE `value` = VALUES(`value`);

-- ============= ANTI-FLOOD / SICUREZZA =============
INSERT INTO `emulator_texts` (`key`, `value`) VALUES
  ('scripter.warning.modtools.kick', 'L''utente %username% ha tentato un''azione sospetta.'),
  ('commands.staff.quota.exceeded', 'Stai eseguendo comandi staff troppo velocemente. Attendi qualche istante.'),
  ('flood.chat.muted', 'Sei stato silenziato per spam. Riprova fra %seconds% secondi.'),
  ('flood.chat.warning', 'Stai scrivendo troppi messaggi consecutivi. Rallenta.'),
  ('rate.limit.too_fast', 'Stai effettuando troppe azioni in poco tempo.')
ON DUPLICATE KEY UPDATE `value` = VALUES(`value`);

-- ============= TRADE / ECONOMY =============
INSERT INTO `emulator_texts` (`key`, `value`) VALUES
  ('trade.error.locked', 'Lo scambio e'' stato bloccato per motivi di sicurezza.'),
  ('trade.error.not_allowed', 'Non puoi effettuare scambi in questo ambiente virtuale.'),
  ('trade.cancelled', 'Scambio annullato.'),
  ('trade.completed', 'Scambio completato con successo.'),
  ('catalog.error.unavailable', 'Articolo non disponibile al momento.'),
  ('catalog.error.not_enough_credits', 'Crediti insufficienti per acquistare questo articolo.'),
  ('marketplace.error.max_offers', 'Hai raggiunto il limite massimo di offerte attive.'),
  ('marketplace.offer.placed', 'La tua offerta e'' stata pubblicata.'),
  ('marketplace.offer.expired', 'La tua offerta e'' scaduta.'),
  ('marketplace.offer.sold', 'Il tuo articolo e'' stato venduto.')
ON DUPLICATE KEY UPDATE `value` = VALUES(`value`);

-- ============= STANZE / AMBIENTI =============
INSERT INTO `emulator_texts` (`key`, `value`) VALUES
  ('room.error.too_many_users', 'Questa stanza ha raggiunto il limite di utenti.'),
  ('room.error.banned', 'Sei stato bandito da questa stanza.'),
  ('room.error.password_wrong', 'Password della stanza errata.'),
  ('room.error.locked', 'Questa stanza e'' chiusa.'),
  ('room.kick.staff', 'Sei stato espulso dalla stanza dallo staff.'),
  ('room.kick.owner', 'Sei stato espulso dalla stanza dal proprietario.'),
  ('room.message.kicked', 'L''utente %username% e'' stato espulso dalla stanza.'),
  ('room.created', 'Stanza creata con successo.'),
  ('room.deleted', 'Stanza eliminata.')
ON DUPLICATE KEY UPDATE `value` = VALUES(`value`);

-- ============= MESSENGER / AMICIZIA =============
INSERT INTO `emulator_texts` (`key`, `value`) VALUES
  ('friend.request.sent', 'Richiesta di amicizia inviata.'),
  ('friend.request.received', '%username% ti ha inviato una richiesta di amicizia.'),
  ('friend.request.accepted', 'Sei diventato amico di %username%.'),
  ('friend.request.declined', 'Richiesta rifiutata.'),
  ('friend.error.list_full', 'La tua lista amici e'' piena.'),
  ('friend.error.target_list_full', '%username% ha la lista amici piena.')
ON DUPLICATE KEY UPDATE `value` = VALUES(`value`);

-- ============= CONNESSIONE / SISTEMA =============
INSERT INTO `emulator_texts` (`key`, `value`) VALUES
  ('system.error.generic', 'Si e'' verificato un errore. Riprova fra qualche istante.'),
  ('system.success.generic', 'Operazione completata.'),
  ('system.warning.generic', 'Avviso: l''operazione potrebbe non essere riuscita completamente.'),
  ('system.shutdown.notice', 'Il server si fermera'' fra %minutes% minuti per manutenzione.'),
  ('system.shutdown.imminent', 'Il server si fermera'' a breve.')
ON DUPLICATE KEY UPDATE `value` = VALUES(`value`);

COMMIT;

-- Verifica:
--   SELECT COUNT(*) AS chiavi_italiane FROM emulator_texts
--     WHERE `value` LIKE '%non%' OR `value` LIKE '%Specifica%' OR `value` LIKE '%Utente%';
-- Dovrebbe restituire >= 80.
