package com.eu.habbo.habbohotel.guides;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.guides.GuideSessionAttachedComposer;
import com.eu.habbo.messages.outgoing.guides.GuideSessionDetachedComposer;
import com.eu.habbo.messages.outgoing.guides.GuideSessionEndedComposer;
import com.eu.habbo.messages.outgoing.guides.GuideSessionErrorComposer;
import com.eu.habbo.messages.outgoing.guides.GuideSessionStartedComposer;
import com.eu.habbo.messages.outgoing.guides.GuideToolsComposer;
import com.eu.habbo.threading.runnables.GuardianTicketFindMoreSlaves;
import com.eu.habbo.threading.runnables.GuideFindNewHelper;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/guides/GuideManager.class */
public class GuideManager {
    private final THashSet<GuideTour> activeTours = new THashSet<>();
    private final THashSet<GuardianTicket> activeTickets = new THashSet<>();
    private final THashSet<GuardianTicket> closedTickets = new THashSet<>();
    private final THashMap<Habbo, Boolean> activeHelpers = new THashMap<>();
    private final THashMap<Habbo, GuardianTicket> activeGuardians = new THashMap<>();
    private final THashMap<Integer, Integer> tourRequestTiming = new THashMap<>();

    public void userLogsOut(Habbo habbo) {
        GuideTour guideTourByHabbo = getGuideTourByHabbo(habbo);
        if (guideTourByHabbo != null) {
            endSession(guideTourByHabbo);
        }
        this.activeHelpers.remove(habbo);
        GuardianTicket ticketForGuardian = getTicketForGuardian(habbo);
        if (ticketForGuardian != null) {
            ticketForGuardian.removeGuardian(habbo);
        }
        this.activeGuardians.remove(habbo);
    }

    public void setOnGuide(Habbo habbo, boolean z) {
        if (z) {
            this.activeHelpers.put(habbo, false);
        } else {
            if (getGuideTourByHabbo(habbo) != null) {
                return;
            }
            this.activeHelpers.remove(habbo);
        }
    }

    public boolean findHelper(GuideTour guideTour) {
        synchronized (this.activeHelpers) {
            for (Map.Entry entry : this.activeHelpers.entrySet()) {
                if (!((Boolean) entry.getValue()).booleanValue() && !guideTour.hasDeclined(((Habbo) entry.getKey()).getHabboInfo().getId())) {
                    guideTour.checkSum++;
                    guideTour.setHelper((Habbo) entry.getKey());
                    ((Habbo) entry.getKey()).getClient().sendResponse(new GuideSessionAttachedComposer(guideTour, true));
                    guideTour.getNoob().getClient().sendResponse(new GuideSessionAttachedComposer(guideTour, false));
                    Emulator.getThreading().run(new GuideFindNewHelper(guideTour, (Habbo) entry.getKey()), 60000L);
                    this.activeTours.add(guideTour);
                    return true;
                }
            }
            endSession(guideTour);
            guideTour.getNoob().getClient().sendResponse(new GuideSessionErrorComposer(1));
            return false;
        }
    }

    public void declineTour(GuideTour guideTour) {
        Habbo helper = guideTour.getHelper();
        guideTour.addDeclinedHelper(guideTour.getHelper().getHabboInfo().getId());
        guideTour.setHelper(null);
        helper.getClient().sendResponse(new GuideSessionEndedComposer(1));
        helper.getClient().sendResponse(new GuideSessionDetachedComposer());
        if (findHelper(guideTour)) {
            return;
        }
        endSession(guideTour);
        guideTour.getNoob().getClient().sendResponse(new GuideSessionErrorComposer(1));
    }

    public void startSession(GuideTour guideTour, Habbo habbo) {
        synchronized (this.activeTours) {
            synchronized (this.activeHelpers) {
                this.activeHelpers.put(habbo, true);
                ServerMessage serverMessageCompose = new GuideSessionStartedComposer(guideTour).compose();
                guideTour.getNoob().getClient().sendResponse(serverMessageCompose);
                guideTour.getHelper().getClient().sendResponse(serverMessageCompose);
                guideTour.checkSum++;
                this.tourRequestTiming.put(Integer.valueOf(guideTour.getStartTime()), Integer.valueOf(Emulator.getIntUnixTimestamp()));
            }
        }
    }

    public void endSession(GuideTour guideTour) {
        synchronized (this.activeTours) {
            synchronized (this.activeHelpers) {
                guideTour.getNoob().getClient().sendResponse(new GuideSessionEndedComposer(1));
                guideTour.end();
                if (guideTour.getHelper() != null) {
                    this.activeHelpers.put(guideTour.getHelper(), false);
                    guideTour.getHelper().getClient().sendResponse(new GuideSessionEndedComposer(1));
                    guideTour.getHelper().getClient().sendResponse(new GuideSessionDetachedComposer());
                    guideTour.getHelper().getClient().sendResponse(new GuideToolsComposer(true));
                }
            }
        }
    }

    public void recommend(GuideTour guideTour, boolean z) {
        synchronized (this.activeTours) {
            guideTour.setWouldRecommend(z ? GuideRecommendStatus.YES : GuideRecommendStatus.NO);
            guideTour.getNoob().getClient().sendResponse(new GuideSessionDetachedComposer());
            AchievementManager.progressAchievement(guideTour.getNoob(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("GuideFeedbackGiver"));
            this.activeTours.remove(guideTour);
        }
    }

    public GuideTour getGuideTourByHelper(Habbo habbo) {
        synchronized (this.activeTours) {
            TObjectHashIterator it = this.activeTours.iterator();
            while (it.hasNext()) {
                GuideTour guideTour = (GuideTour) it.next();
                if (!guideTour.isEnded() && guideTour.getHelper() == habbo) {
                    return guideTour;
                }
            }
            return null;
        }
    }

    public GuideTour getGuideTourByNoob(Habbo habbo) {
        synchronized (this.activeTours) {
            TObjectHashIterator it = this.activeTours.iterator();
            while (it.hasNext()) {
                GuideTour guideTour = (GuideTour) it.next();
                if (guideTour.getNoob() == habbo) {
                    return guideTour;
                }
            }
            return null;
        }
    }

    public GuideTour getGuideTourByHabbo(Habbo habbo) {
        synchronized (this.activeTours) {
            TObjectHashIterator it = this.activeTours.iterator();
            while (it.hasNext()) {
                GuideTour guideTour = (GuideTour) it.next();
                if (guideTour.getHelper() == habbo || guideTour.getNoob() == habbo) {
                    return guideTour;
                }
            }
            return null;
        }
    }

    public int getGuidesCount() {
        return this.activeHelpers.size();
    }

    public int getGuardiansCount() {
        return this.activeGuardians.size();
    }

    public boolean activeGuardians() {
        return this.activeGuardians.size() > 0;
    }

    public int getAverageWaitingTime() {
        synchronized (this.tourRequestTiming) {
            int iIntValue = 0;
            if (this.tourRequestTiming.isEmpty()) {
                return 5;
            }
            for (Map.Entry entry : this.tourRequestTiming.entrySet()) {
                iIntValue += ((Integer) entry.getValue()).intValue() - ((Integer) entry.getKey()).intValue();
            }
            return iIntValue / this.tourRequestTiming.size();
        }
    }

    public void addGuardianTicket(GuardianTicket guardianTicket) {
        synchronized (this.activeTickets) {
            this.activeTickets.add(guardianTicket);
            findGuardians(guardianTicket);
        }
    }

    public void findGuardians(GuardianTicket guardianTicket) {
        synchronized (this.activeGuardians) {
            int votedCount = guardianTicket.getVotedCount();
            THashSet tHashSet = new THashSet();
            for (Map.Entry entry : this.activeGuardians.entrySet()) {
                if (votedCount == 5) {
                    break;
                }
                if (entry.getKey() != guardianTicket.getReporter() && entry.getKey() != guardianTicket.getReported()) {
                    if (entry.getValue() == null && guardianTicket.getVoteForGuardian((Habbo) entry.getKey()) == null) {
                        guardianTicket.requestToVote((Habbo) entry.getKey());
                        tHashSet.add((Habbo) entry.getKey());
                    }
                    votedCount++;
                }
            }
            TObjectHashIterator it = tHashSet.iterator();
            while (it.hasNext()) {
                this.activeGuardians.put((Habbo) it.next(), guardianTicket);
            }
            if (votedCount < 5) {
                Emulator.getThreading().run(new GuardianTicketFindMoreSlaves(guardianTicket), 3000L);
            }
        }
    }

    public void acceptTicket(Habbo habbo, boolean z) {
        GuardianTicket ticketForGuardian = getTicketForGuardian(habbo);
        if (ticketForGuardian != null) {
            if (z) {
                ticketForGuardian.addGuardian(habbo);
                this.activeGuardians.put(habbo, ticketForGuardian);
            } else {
                ticketForGuardian.removeGuardian(habbo);
                findGuardians(ticketForGuardian);
                this.activeGuardians.put(habbo, (Object) null);
            }
        }
    }

    public GuardianTicket getTicketForGuardian(Habbo habbo) {
        GuardianTicket guardianTicket;
        synchronized (this.activeGuardians) {
            guardianTicket = (GuardianTicket) this.activeGuardians.get(habbo);
        }
        return guardianTicket;
    }

    public GuardianTicket getRecentTicket(Habbo habbo) {
        GuardianTicket guardianTicket = null;
        synchronized (this.activeTickets) {
            TObjectHashIterator it = this.activeTickets.iterator();
            while (it.hasNext()) {
                GuardianTicket guardianTicket2 = (GuardianTicket) it.next();
                if (guardianTicket2.getReporter() == habbo) {
                    return guardianTicket2;
                }
            }
            synchronized (this.closedTickets) {
                TObjectHashIterator it2 = this.closedTickets.iterator();
                while (it2.hasNext()) {
                    GuardianTicket guardianTicket3 = (GuardianTicket) it2.next();
                    if (guardianTicket3.getReporter() == habbo) {
                        if (guardianTicket == null || ((long) Emulator.getIntUnixTimestamp()) - (guardianTicket3.getDate().getTime() / 1000) < ((long) Emulator.getIntUnixTimestamp()) - (guardianTicket.getDate().getTime() / 1000)) {
                            guardianTicket = guardianTicket3;
                        }
                    }
                }
            }
            return guardianTicket;
        }
    }

    public GuardianTicket getOpenReportedHabboTicket(Habbo habbo) {
        synchronized (this.activeTickets) {
            TObjectHashIterator it = this.activeTickets.iterator();
            while (it.hasNext()) {
                GuardianTicket guardianTicket = (GuardianTicket) it.next();
                if (guardianTicket.getReported() == habbo) {
                    return guardianTicket;
                }
            }
            return null;
        }
    }

    public void closeTicket(GuardianTicket guardianTicket) {
        synchronized (this.activeTickets) {
            this.activeTickets.remove(guardianTicket);
        }
        synchronized (this.closedTickets) {
            this.closedTickets.add(guardianTicket);
        }
        THashSet tHashSet = new THashSet();
        synchronized (this.activeGuardians) {
            for (Map.Entry entry : this.activeGuardians.entrySet()) {
                if (entry.getValue() == guardianTicket) {
                    tHashSet.add((Habbo) entry.getKey());
                }
            }
            TObjectHashIterator it = tHashSet.iterator();
            while (it.hasNext()) {
                this.activeGuardians.put((Habbo) it.next(), (Object) null);
            }
        }
    }

    public void setOnGuardian(Habbo habbo, boolean z) {
        if (z) {
            this.activeGuardians.put(habbo, (Object) null);
            return;
        }
        GuardianTicket ticketForGuardian = getTicketForGuardian(habbo);
        if (ticketForGuardian != null) {
            ticketForGuardian.removeGuardian(habbo);
        }
        this.activeGuardians.remove(habbo);
    }

    public void cleanup() {
        synchronized (this.activeTours) {
            THashSet tHashSet = new THashSet();
            TObjectHashIterator it = this.activeTours.iterator();
            while (it.hasNext()) {
                GuideTour guideTour = (GuideTour) it.next();
                if (guideTour.isEnded() && Emulator.getIntUnixTimestamp() - guideTour.getEndTime() > 300) {
                    tHashSet.add(guideTour);
                }
            }
            TObjectHashIterator it2 = tHashSet.iterator();
            while (it2.hasNext()) {
                this.activeTours.remove((GuideTour) it2.next());
            }
        }
        synchronized (this.activeTickets) {
            THashSet tHashSet2 = new THashSet();
            TObjectHashIterator it3 = this.closedTickets.iterator();
            while (it3.hasNext()) {
                GuardianTicket guardianTicket = (GuardianTicket) it3.next();
                if (((long) Emulator.getIntUnixTimestamp()) - (guardianTicket.getDate().getTime() / 1000) > 900) {
                    tHashSet2.add(guardianTicket);
                }
            }
            TObjectHashIterator it4 = tHashSet2.iterator();
            while (it4.hasNext()) {
                this.closedTickets.remove((GuardianTicket) it4.next());
            }
        }
    }
}
