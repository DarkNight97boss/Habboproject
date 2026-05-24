package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.permissions.PermissionSetting;
import gnu.trove.map.hash.THashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/CommandHandler.class */
public class CommandHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandHandler.class);
    private static final THashMap<String, Command> commands = new THashMap<>(5);
    private static final Comparator<Command> ALPHABETICAL_ORDER = new Comparator<Command>() { // from class: com.eu.habbo.habbohotel.commands.CommandHandler.1
        @Override // java.util.Comparator
        public int compare(Command command, Command command2) {
            int iCompare = String.CASE_INSENSITIVE_ORDER.compare(command.permission, command2.permission);
            return iCompare != 0 ? iCompare : command.permission.compareTo(command2.permission);
        }
    };

    public CommandHandler() {
        long jCurrentTimeMillis = System.currentTimeMillis();
        reloadCommands();
        LOGGER.info("Command Handler -> Loaded! (" + (System.currentTimeMillis() - jCurrentTimeMillis) + " MS)");
    }

    public static void addCommand(Command command) {
        if (command == null) {
            return;
        }
        commands.put(command.getClass().getName(), command);
    }

    public static void addCommand(Class<? extends Command> cls) {
        try {
            addCommand(cls.newInstance());
            LOGGER.debug("Added command: {}", cls.getName());
        } catch (Exception e) {
            LOGGER.error("Caught exception", e);
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:96:0x00f5 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static boolean handleCommand(com.eu.habbo.habbohotel.gameclients.GameClient r8, java.lang.String r9) {
        /*
            Method dump skipped, instruction units count: 803
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.eu.habbo.habbohotel.commands.CommandHandler.handleCommand(com.eu.habbo.habbohotel.gameclients.GameClient, java.lang.String):boolean");
    }

    public static Command getCommand(String str) {
        for (Command command : commands.values()) {
            for (String str2 : command.keys) {
                if (str.equalsIgnoreCase(str2)) {
                    return command;
                }
            }
        }
        return null;
    }

    public void reloadCommands() {
        addCommand(new AboutCommand());
        addCommand(new AlertCommand());
        addCommand(new AllowTradingCommand());
        addCommand(new ArcturusCommand());
        addCommand(new BadgeCommand());
        addCommand(new BanCommand());
        addCommand(new BlockAlertCommand());
        addCommand(new BotsCommand());
        addCommand(new CalendarCommand());
        addCommand(new ChangeNameCommand());
        addCommand(new ChatTypeCommand());
        addCommand(new CommandsCommand());
        addCommand(new ConnectCameraCommand());
        addCommand(new ControlCommand());
        addCommand(new CoordsCommand());
        addCommand(new CreditsCommand());
        addCommand(new DiagonalCommand());
        addCommand(new DisconnectCommand());
        addCommand(new EjectAllCommand());
        addCommand(new EmptyInventoryCommand());
        addCommand(new EmptyBotsInventoryCommand());
        addCommand(new EmptyPetsInventoryCommand());
        addCommand(new EnableCommand());
        addCommand(new EventCommand());
        addCommand(new FacelessCommand());
        addCommand(new FastwalkCommand());
        addCommand(new FilterWordCommand());
        addCommand(new FreezeBotsCommand());
        addCommand(new FreezeCommand());
        addCommand(new GiftCommand());
        addCommand(new GiveRankCommand());
        addCommand(new HabnamCommand());
        addCommand(new HandItemCommand());
        addCommand(new HappyHourCommand());
        addCommand(new HideWiredCommand());
        addCommand(new HotelAlertCommand());
        addCommand(new HotelAlertLinkCommand());
        addCommand(new InvisibleCommand());
        addCommand(new IPBanCommand());
        addCommand(new LayCommand());
        addCommand(new MachineBanCommand());
        addCommand(new MassBadgeCommand());
        addCommand(new RoomBadgeCommand());
        addCommand(new MassCreditsCommand());
        addCommand(new MassGiftCommand());
        addCommand(new MassPixelsCommand());
        addCommand(new MassPointsCommand());
        addCommand(new MimicCommand());
        addCommand(new MoonwalkCommand());
        addCommand(new MultiCommand());
        addCommand(new MuteBotsCommand());
        addCommand(new MuteCommand());
        addCommand(new MutePetsCommand());
        addCommand(new PetInfoCommand());
        addCommand(new PickallCommand());
        addCommand(new PixelCommand());
        addCommand(new PluginsCommand());
        addCommand(new PointsCommand());
        addCommand(new PromoteTargetOfferCommand());
        addCommand(new PullCommand());
        addCommand(new PushCommand());
        addCommand(new RedeemCommand());
        addCommand(new ReloadRoomCommand());
        addCommand(new RoomAlertCommand());
        addCommand(new RoomBundleCommand());
        addCommand(new RoomCreditsCommand());
        addCommand(new RoomDanceCommand());
        addCommand(new RoomEffectCommand());
        addCommand(new RoomItemCommand());
        addCommand(new RoomKickCommand());
        addCommand(new RoomMuteCommand());
        addCommand(new RoomPixelsCommand());
        addCommand(new RoomPointsCommand());
        addCommand(new SayAllCommand());
        addCommand(new SayCommand());
        addCommand(new SetMaxCommand());
        addCommand(new SetPollCommand());
        addCommand(new SetSpeedCommand());
        addCommand(new ShoutAllCommand());
        addCommand(new ShoutCommand());
        addCommand(new ShutdownCommand());
        addCommand(new SitCommand());
        addCommand(new StandCommand());
        addCommand(new SitDownCommand());
        addCommand(new StaffAlertCommand());
        addCommand(new StaffOnlineCommand());
        addCommand(new StalkCommand());
        addCommand(new SummonCommand());
        addCommand(new SummonRankCommand());
        addCommand(new SuperbanCommand());
        addCommand(new SuperPullCommand());
        addCommand(new TakeBadgeCommand());
        addCommand(new TeleportCommand());
        addCommand(new TransformCommand());
        addCommand(new TrashCommand());
        addCommand(new UnbanCommand());
        addCommand(new UnloadRoomCommand());
        addCommand(new UnmuteCommand());
        addCommand(new UpdateAchievements());
        addCommand(new UpdateBotsCommand());
        addCommand(new UpdateCalendarCommand());
        addCommand(new UpdateCatalogCommand());
        addCommand(new UpdateConfigCommand());
        addCommand(new UpdateGuildPartsCommand());
        addCommand(new UpdateHotelViewCommand());
        addCommand(new UpdateItemsCommand());
        addCommand(new UpdateNavigatorCommand());
        addCommand(new UpdatePermissionsCommand());
        addCommand(new UpdatePetDataCommand());
        addCommand(new UpdatePluginsCommand());
        addCommand(new UpdatePollsCommand());
        addCommand(new UpdateTextsCommand());
        addCommand(new UpdateWordFilterCommand());
        addCommand(new UserInfoCommand());
        addCommand(new WordQuizCommand());
        addCommand(new UpdateYoutubePlaylistsCommand());
        addCommand(new AddYoutubePlaylistCommand());
        addCommand(new SoftKickCommand());
        addCommand(new SubscriptionCommand());
        addCommand(new TestCommand());
    }

    public List<Command> getCommandsForRank(int i) {
        ArrayList arrayList = new ArrayList();
        if (Emulator.getGameEnvironment().getPermissionsManager().rankExists(i)) {
            THashMap<String, Permission> permissions = Emulator.getGameEnvironment().getPermissionsManager().getRank(i).getPermissions();
            for (Command command : commands.values()) {
                if (!arrayList.contains(command) && permissions.contains(command.permission) && ((Permission) permissions.get(command.permission)).setting != PermissionSetting.DISALLOWED) {
                    arrayList.add(command);
                }
            }
        }
        arrayList.sort(ALPHABETICAL_ORDER);
        return arrayList;
    }

    public void dispose() {
        commands.clear();
        LOGGER.info("Command Handler -> Disposed!");
    }
}
