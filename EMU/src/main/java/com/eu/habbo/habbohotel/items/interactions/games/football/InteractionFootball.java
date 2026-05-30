package com.eu.habbo.habbohotel.items.interactions.games.football;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.games.GameTeamColors;
import com.eu.habbo.habbohotel.games.football.FootballGame;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionPushable;
import com.eu.habbo.habbohotel.items.interactions.games.InteractionGameTeamItem;
import com.eu.habbo.habbohotel.items.interactions.games.football.goals.InteractionFootballGoal;
import com.eu.habbo.habbohotel.items.interactions.interfaces.ConditionalGate;
import com.eu.habbo.habbohotel.rooms.*;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.rooms.items.ItemStateComposer;
import com.eu.habbo.util.pathfinding.Rotation;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;


public class InteractionFootball extends InteractionPushable implements ConditionalGate {

    public InteractionFootball(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public InteractionFootball(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }


    //
    // ====================================================================
    // POLICY "1 CASELLA PER SPINTA" vs "TIRO LUNGO" — switchable per palla
    // ====================================================================
    // Default per item_name (vedi FootballBallModes.defaultModeForItemName):
    //   - palle colorate (fball_ball2/3/4 = red/blue/yellow) -> LONG (upstream)
    //   - tutte le altre (Game Ball, Grand Final, Habbo Football, Beach, Snow)
    //     -> SHORT (1 casella per spinta, bounce 180, canWalkOn blocca)
    // Override per item via comando admin :ballmode, persistito in tabella
    // football_ball_modes. Vedi FootballBallModes.
    //
    // Mode SHORT: per ogni contatto utente↔palla la palla avanza ESATTAMENTE
    // una casella nella direzione del passo dell'utente, poi si ferma. Bounce
    // = 180° (vedi getBounceDirection). canWalkOn dinamico (vedi sotto)
    // impedisce all'avatar di "calpestare" la palla contro un ostacolo.
    //
    // Mode LONG: ripristina i valori upstream Arcturus (velocity 6, drag 4,
    // tackle 4) + bounce 8-direzioni + canWalkOn libero. La palla viaggia
    // diversi tile in linea retta.
    //
    // L'eccezione "safety stop" quando extradata=="1" e tilesWalked==2
    // (ritorno 0) e' preservata in ENTRAMBE le mode: protegge i wired
    // complessi che pongono un lock sulla palla.
    //
    // Niente impatto sulla logica di goal/scoreboard/wired: quei componenti
    // vivono in classi separate (InteractionFootballGoal*, FootballGame,
    // ecc.) e non vengono toccati da questo file.
    //
    private boolean isLongKick() {
        return FootballBallModes.isLongKick(this.getId(),
                this.getBaseItem() != null ? this.getBaseItem().getName() : null);
    }

    @Override
    public int getWalkOnVelocity(RoomUnit roomUnit, Room room) {
        if (roomUnit.getPath().isEmpty() && roomUnit.tilesWalked() == 2 && this.getExtradata().equals("1"))
            return 0;
        if (isLongKick()) {
            // Upstream Arcturus: kick lungo se l'utente arriva sulla palla con
            // un solo passo residuo nel path.
            if (roomUnit.getPath().size() == 0 && roomUnit.tilesWalked() == 1)
                return 6;
            return 1;
        }
        return 1;
    }

    @Override
    public int getWalkOffVelocity(RoomUnit roomUnit, Room room) {
        if (isLongKick()) {
            // Upstream Arcturus: kick lungo all'uscita se il path e' finito subito.
            if (roomUnit.getPath().size() == 0 && roomUnit.tilesWalked() == 0)
                return 6;
            return 1;
        }
        return 1;
    }

    @Override
    public int getDragVelocity(RoomUnit roomUnit, Room room) {
        if (roomUnit.getPath().isEmpty() && roomUnit.tilesWalked() == 2)
            return 0;
        return 1;
    }

    @Override
    public int getTackleVelocity(RoomUnit roomUnit, Room room) {
        return isLongKick() ? 4 : 1;
    }


    @Override
    public RoomUserRotation getWalkOnDirection(RoomUnit roomUnit, Room room) {
        return roomUnit.getBodyRotation();
    }

    @Override
    public RoomUserRotation getWalkOffDirection(RoomUnit roomUnit, Room room) {
        RoomTile peek = roomUnit.getPath().peek();
        RoomTile nextWalkTile = peek != null ? room.getLayout().getTile(peek.x, peek.y) : roomUnit.getGoal();
        return RoomUserRotation.values()[(RoomUserRotation.values().length + Rotation.Calculate(roomUnit.getX(), roomUnit.getY(), nextWalkTile.x, nextWalkTile.y) + 4) % 8];
    }

    public RoomUserRotation getDragDirection(RoomUnit roomUnit, Room room) {
        return roomUnit.getBodyRotation();
    }

    public RoomUserRotation getTackleDirection(RoomUnit roomUnit, Room room) {
        return roomUnit.getBodyRotation();
    }


    @Override
    public int getNextRollDelay(int currentStep, int totalSteps) {

        if(totalSteps > 4) {
            if(currentStep <= 4) {
                return 125;
            }
        }

        return 500;

        /*int t = 2500;
        return (totalSteps == 1) ? 500 : 100 * ((t = t / t - 1) * t * t * t * t + 1) + (currentStep * 100);*/
    }

    @Override
    public RoomUserRotation getBounceDirection(Room room, RoomUserRotation currentDirection) {
        // SHORT mode: bounce 180 sempre. Mappa:
        //   N <-> S, NE <-> SW, E <-> W, SE <-> NW
        //
        // LONG mode: logica upstream Arcturus a 8 direzioni con fallback diagonale,
        // pensata per il "tiro lungo" che puo' rimbalzare in diagonale.
        if (!isLongKick()) {
            int idx = currentDirection.getValue();
            return RoomUserRotation.values()[(idx + 4) % 8];
        }
        switch (currentDirection) {
            default:
            case NORTH:
                return RoomUserRotation.SOUTH;
            case NORTH_EAST:
                if (this.validMove(room, room.getLayout().getTile(this.getX(), this.getY()), room.getLayout().getTileInFront(room.getLayout().getTile(this.getX(), this.getY()), RoomUserRotation.NORTH_WEST.getValue())))
                    return RoomUserRotation.NORTH_WEST;
                else if (this.validMove(room, room.getLayout().getTile(this.getX(), this.getY()), room.getLayout().getTileInFront(room.getLayout().getTile(this.getX(), this.getY()), RoomUserRotation.SOUTH_EAST.getValue())))
                    return RoomUserRotation.SOUTH_EAST;
                else
                    return RoomUserRotation.SOUTH_WEST;
            case EAST:
                return RoomUserRotation.WEST;
            case SOUTH_EAST:
                if (this.validMove(room, room.getLayout().getTile(this.getX(), this.getY()), room.getLayout().getTileInFront(room.getLayout().getTile(this.getX(), this.getY()), RoomUserRotation.SOUTH_WEST.getValue())))
                    return RoomUserRotation.SOUTH_WEST;
                else if (this.validMove(room, room.getLayout().getTile(this.getX(), this.getY()), room.getLayout().getTileInFront(room.getLayout().getTile(this.getX(), this.getY()), RoomUserRotation.NORTH_EAST.getValue())))
                    return RoomUserRotation.NORTH_EAST;
                else
                    return RoomUserRotation.NORTH_WEST;
            case SOUTH:
                return RoomUserRotation.NORTH;
            case SOUTH_WEST:
                if (this.validMove(room, room.getLayout().getTile(this.getX(), this.getY()), room.getLayout().getTileInFront(room.getLayout().getTile(this.getX(), this.getY()), RoomUserRotation.SOUTH_EAST.getValue())))
                    return RoomUserRotation.SOUTH_EAST;
                else if (this.validMove(room, room.getLayout().getTile(this.getX(), this.getY()), room.getLayout().getTileInFront(room.getLayout().getTile(this.getX(), this.getY()), RoomUserRotation.NORTH_WEST.getValue())))
                    return RoomUserRotation.NORTH_WEST;
                else
                    return RoomUserRotation.NORTH_EAST;
            case WEST:
                return RoomUserRotation.EAST;
            case NORTH_WEST:
                if (this.validMove(room, room.getLayout().getTile(this.getX(), this.getY()), room.getLayout().getTileInFront(room.getLayout().getTile(this.getX(), this.getY()), RoomUserRotation.NORTH_EAST.getValue())))
                    return RoomUserRotation.NORTH_EAST;
                else if (this.validMove(room, room.getLayout().getTile(this.getX(), this.getY()), room.getLayout().getTileInFront(room.getLayout().getTile(this.getX(), this.getY()), RoomUserRotation.SOUTH_WEST.getValue())))
                    return RoomUserRotation.SOUTH_WEST;
                else
                    return RoomUserRotation.SOUTH_EAST;
        }
    }


    @Override
    public boolean validMove(Room room, RoomTile from, RoomTile to) {
        if (to == null || to.state == RoomTileState.INVALID) return false;
        HabboItem topItem = room.getTopItemAt(to.x, to.y, this);

        // Move is valid if there isnt any furni yet
        if (topItem == null) {
            return true;
        }

        // If any furni on tile is not stackable, move is invalid (tested on 22-03-2022)
        if (room.getItemsAt(to).stream().anyMatch(x -> !x.getBaseItem().allowStack())) {
            return false;
        }

        // Ball can only go up by 1.65 according to Habbo (tested using stack tile on 22-03-2022)
        BigDecimal topItemHeight = BigDecimal.valueOf(topItem.getZ() + topItem.getBaseItem().getHeight());
        BigDecimal ballHeight = BigDecimal.valueOf(this.getZ());

        if (topItemHeight.subtract(ballHeight).compareTo(new BigDecimal(1.65)) > 0) {
            return false;
        }

        // If top item is a football goal, the move is only valid if ball is coming from the front side
        // Ball shouldn't come from the back or from the sides (tested on 22-03-2022)
        if (topItem instanceof InteractionFootballGoal) {
            int ballDirection = Rotation.Calculate(from.x, from.y, to.x, to.y);
            int goalRotation = topItem.getRotation();

            switch (goalRotation) {
                case 0:
                    return ballDirection > 2 && ballDirection < 6;
                case 2:
                    return ballDirection > 4;
                case 4:
                    return ballDirection > 6 || ballDirection < 2;
                case 6:
                    return ballDirection > 0 && ballDirection < 4;
            }
        }

        return topItem.getBaseItem().allowStack();
    }

    //Events

    @Override
    public void onDrag(Room room, RoomUnit roomUnit, int velocity, RoomUserRotation direction) {

    }

    @Override
    public void onKick(Room room, RoomUnit roomUnit, int velocity, RoomUserRotation direction) {

    }

    @Override
    public void onTackle(Room room, RoomUnit roomUnit, int velocity, RoomUserRotation direction) {

    }

    @Override
    public void onMove(Room room, RoomTile from, RoomTile to, RoomUserRotation direction, RoomUnit kicker, int nextRoll, int currentStep, int totalSteps) {
        FootballGame game = (FootballGame) room.getGame(FootballGame.class);
        if (game == null) {
            try {
                game = FootballGame.class.getDeclaredConstructor(new Class[]{Room.class}).newInstance(room);
                room.addGame(game);
            } catch (Exception e) {
                return;
            }
        }
        HabboItem currentTopItem = room.getTopItemAt(from.x, from.y, this);
        HabboItem topItem = room.getTopItemAt(to.x, to.y, this);
        if ((topItem != null) && ((currentTopItem == null) || (currentTopItem.getId() != topItem.getId())) && ((topItem instanceof InteractionFootballGoal))) {
            GameTeamColors color = ((InteractionGameTeamItem) topItem).teamColor;
            game.onScore(kicker, color);
        }

        this.setExtradata(Math.abs(currentStep - (totalSteps + 1)) + "");
        room.sendComposer(new ItemStateComposer(this).compose());

        /*this.setExtradata(nextRoll <= 200 ? "8" : (nextRoll <= 250 ? "7" : (nextRoll <= 300 ? "6" : (nextRoll <= 350 ? "5" : (nextRoll <= 400 ? "4" : (nextRoll <= 450 ? "3" : (nextRoll <= 500 ? "2" : "1")))))));
        room.sendComposer(new ItemStateComposer(this).compose());*/
    }

    @Override
    public void onBounce(Room room, RoomUserRotation oldDirection, RoomUserRotation newDirection, RoomUnit kicker) {

    }

    @Override
    public void onStop(Room room, RoomUnit kicker, int currentStep, int totalSteps) {
        this.setExtradata("0");
        room.sendComposer(new ItemStateComposer(this).compose());
    }

    @Override
    public boolean canStillMove(Room room, RoomTile from, RoomTile to, RoomUserRotation direction, RoomUnit kicker, int nextRoll, int currentStep, int totalSteps) {
        HabboItem topItem = room.getTopItemAt(from.x, from.y, this);

        // Goal-stop legacy: se sopra c'e' una porta da gol e non siamo al primo
        // step, la palla si ferma (era cosi' anche nell'upstream Arcturus).
        if (topItem != null && topItem.getBaseItem().getName().startsWith("fball_goal_")
                && currentStep != 1) {
            return false;
        }

        // POLICY "il kicker non si auto-blocca" (richiesta utente):
        // l'upstream Arcturus blocca la palla con probabilita' 70% se nel tile
        // di destinazione c'e' QUALSIASI habbo, incluso il giocatore stesso che
        // ha appena calciato. Con la nostra policy "1 casella per spinta" questo
        // si verificava costantemente perche' nel walkOn/drag il tile target e'
        // tipicamente quello dove il kicker sta gia' camminando -> sensazione
        // "la palla non mi segue, a volte si a volte no".
        //
        // Manteniamo la simulazione tackle (un ALTRO giocatore puo' intercettare
        // la palla con 70%) ma escludiamo il kicker stesso: la palla del MIO
        // kick non puo' essere bloccata da ME.
        if (room.hasHabbosAt(to.x, to.y)) {
            boolean otherHabboBlocking = false;
            for (Habbo h : room.getHabbosAt(to)) {
                if (h != null && h.getRoomUnit() != null && h.getRoomUnit() != kicker) {
                    otherHabboBlocking = true;
                    break;
                }
            }
            if (otherHabboBlocking && Emulator.getRandom().nextInt(10) >= 3) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onPickUp(Room room) {
        this.setExtradata("0");
    }

    /**
     * POLICY "palla sempre 1 casella davanti al kicker" (richiesta utente).
     *
     * Sovrascriviamo {@code canWalkOn} (ereditato da InteractionPushable che lo
     * teneva sempre = true) per RIFIUTARE l'ingresso dell'avatar sul tile della
     * palla quando la palla non potrebbe avanzare nella direzione di approccio.
     * Combinato con l'interfaccia {@link ConditionalGate}, questo causa il
     * "reject" del movimento dentro {@code RoomUnit} (vedi linee ~297-310):
     * setRotation rollback, tilesWalked--, goal = current, MOVE status removed.
     *
     * Effetti:
     *   - dribbling libero (palla puo' avanzare): canWalkOn ritorna true,
     *     onWalkOn parte come prima, palla 1 casella avanti -> esperienza
     *     fluida come adesso.
     *   - palla contro muro / furni non-stackable / bordo stanza /
     *     altro habbo davanti: canWalkOn ritorna false, l'avatar si ferma
     *     sul tile adiacente e la palla resta visibile 1 casella avanti.
     *     Per spingerla, l'utente deve cliccarla esplicitamente
     *     (onClick tackle) o cambiare angolo.
     *
     * Niente impatto su wired/goal:
     *   - wired calcia-palla, contatori goal, gate, scoreboard, FootballGame:
     *     reagiscono a onClick/onMove/onScore, NON a canWalkOn.
     *   - se la palla e' sopra una porta da gol (controlla validMove ->
     *     InteractionFootballGoal) il check di validMove gia' filtra le
     *     direzioni ammesse, quindi questo non rompe i tiri legittimi in
     *     porta dall'angolatura corretta.
     */
    @Override
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objects) {
        if (roomUnit == null || room == null) {
            return true;
        }

        // LONG mode (tiro lungo): comportamento upstream Arcturus = sempre
        // walkabile. Niente blocco preventivo: l'utente puo' calciare la
        // palla anche correndoci sopra, e la palla viaggia diversi tile.
        if (isLongKick()) {
            return true;
        }

        // ECCEZIONE "calcio esplicito" (solo mode SHORT): se l'utente ha
        // cliccato direttamente sulla palla (goal == ball.pos) lasciamo
        // passare il walkOn. Questo attiva onWalkOnVelocity -> onKick ->
        // KickBallAction(isDrag=false) e quindi anche il BOUNCE 180 contro
        // muro/superfici, che l'utente vuole espressamente conservare per
        // il "calcio diretto".
        if (roomUnit.getGoal() != null
                && roomUnit.getGoal().x == this.getX()
                && roomUnit.getGoal().y == this.getY()) {
            return true;
        }

        int dx = this.getX() - roomUnit.getX();
        int dy = this.getY() - roomUnit.getY();
        if (dx == 0 && dy == 0) {
            // L'avatar e' gia' sopra (spawn, teleport, edge case): non bloccare.
            return true;
        }
        // Tile dove la palla DOVREBBE andare se l'avatar mette piede sopra.
        int forwardX = this.getX() + dx;
        int forwardY = this.getY() + dy;
        RoomTile fromTile = room.getLayout().getTile(this.getX(), this.getY());
        RoomTile forwardTile = room.getLayout().getTile((short) forwardX, (short) forwardY);

        // Muro / bordo / furni non-stackable / direzione di gol non ammessa
        if (!this.validMove(room, fromTile, forwardTile)) {
            return false;
        }
        // Altro habbo davanti: blocca per mantenere la palla visibile.
        // (Il kicker stesso ovviamente non puo' essere su forwardTile, perche'
        // si trova alle spalle del tile della palla.)
        if (forwardTile != null && room.hasHabbosAt(forwardTile.x, forwardTile.y)) {
            for (Habbo h : room.getHabbosAt(forwardTile)) {
                if (h != null && h.getRoomUnit() != null && h.getRoomUnit() != roomUnit) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Callback richiesto da {@link ConditionalGate}: il pathfinder ha gia'
     * rollbackato la mossa (rotation, tilesWalked, goal) e lo status MOVE,
     * quindi qui non serve fare nulla. Niente chat-whisper per non spammare
     * l'utente: la sensazione di "avatar che si ferma davanti alla palla"
     * e' gia' un feedback visivo sufficiente.
     */
    @Override
    public void onRejected(RoomUnit roomUnit, Room room, Object[] objects) {
        // no-op
    }

}
