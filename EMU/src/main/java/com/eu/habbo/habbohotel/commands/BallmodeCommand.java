package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.interactions.games.football.FootballBallModes;
import com.eu.habbo.habbohotel.items.interactions.games.football.InteractionFootball;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.rooms.RoomLayout;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.HabboItem;

/**
 * {@code :ballmode <long|short|default>}
 *
 * <p>Cambia al volo la modalita' di calcio della palla calcio adiacente al
 * giocatore. Persistito in {@code football_ball_modes}.
 *
 * <ul>
 *   <li>{@code long} - tiro lungo upstream Arcturus (velocity 6, bounce 8-dir)</li>
 *   <li>{@code short} - dribble 1-casella (velocity 1, bounce 180, canWalkOn blocca)</li>
 *   <li>{@code default} - rimuove l'override, usa il default per item_name</li>
 * </ul>
 */
public class BallmodeCommand extends Command {

    public BallmodeCommand() {
        super("cmd_ballmode", Emulator.getTexts().getValue("commands.keys.cmd_ballmode", "ballmode").split(";"));
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) throws Exception {
        if (gameClient == null || gameClient.getHabbo() == null) return true;
        Room room = gameClient.getHabbo().getHabboInfo().getCurrentRoom();
        if (room == null) {
            return true;
        }
        if (params.length < 2) {
            gameClient.getHabbo().whisper(
                    Emulator.getTexts().getValue("commands.usage.ballmode",
                            "Usage: :ballmode <long|short|default>"),
                    RoomChatMessageBubbles.ALERT);
            return true;
        }
        String arg = params[1].trim().toLowerCase();
        FootballBallModes.BallMode mode;
        switch (arg) {
            case "long":
            case "l":
                mode = FootballBallModes.BallMode.LONG;
                break;
            case "short":
            case "s":
                mode = FootballBallModes.BallMode.SHORT;
                break;
            case "default":
            case "reset":
            case "d":
                mode = FootballBallModes.BallMode.DEFAULT;
                break;
            default:
                gameClient.getHabbo().whisper(
                        Emulator.getTexts().getValue("commands.usage.ballmode",
                                "Usage: :ballmode <long|short|default>"),
                        RoomChatMessageBubbles.ALERT);
                return true;
        }

        // Find the closest adjacent InteractionFootball.
        InteractionFootball target = findAdjacentBall(gameClient, room);
        if (target == null) {
            gameClient.getHabbo().whisper(
                    Emulator.getTexts().getValue("commands.ballmode.no_target",
                            "No football found in an adjacent tile."),
                    RoomChatMessageBubbles.ALERT);
            return true;
        }

        String adminName = gameClient.getHabbo().getHabboInfo().getUsername();
        boolean ok = FootballBallModes.setMode(target.getId(), mode, adminName);
        if (ok) {
            gameClient.getHabbo().whisper(
                    Emulator.getTexts().getValue("commands.ballmode.ok",
                            "Ball mode set to " + mode.name() + " (item " + target.getId() + ").")
                            .replace("%mode%", mode.name())
                            .replace("%id%", String.valueOf(target.getId())),
                    RoomChatMessageBubbles.GREEN);
        } else {
            gameClient.getHabbo().whisper(
                    Emulator.getTexts().getValue("commands.ballmode.fail",
                            "Failed to persist ball mode change (see server log)."),
                    RoomChatMessageBubbles.ALERT);
        }
        return true;
    }

    private InteractionFootball findAdjacentBall(GameClient gameClient, Room room) {
        RoomTile myTile = gameClient.getHabbo().getRoomUnit().getCurrentLocation();
        if (myTile == null) return null;
        InteractionFootball best = null;
        int bestDist = Integer.MAX_VALUE;
        // Scan items in the 3x3 around the user (centre + 8 adjacent).
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int tx = myTile.x + dx;
                int ty = myTile.y + dy;
                RoomTile t = room.getLayout().getTile((short) tx, (short) ty);
                if (t == null) continue;
                for (HabboItem item : room.getItemsAt(t)) {
                    if (item instanceof InteractionFootball) {
                        int d = Math.abs(dx) + Math.abs(dy);
                        if (d < bestDist
                                && RoomLayout.tilesAdjecent(myTile, t)) {
                            bestDist = d;
                            best = (InteractionFootball) item;
                        }
                    }
                }
            }
        }
        return best;
    }
}
