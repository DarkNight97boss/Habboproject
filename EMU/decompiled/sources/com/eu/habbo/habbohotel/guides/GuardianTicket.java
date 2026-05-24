package com.eu.habbo.habbohotel.guides;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.modtool.ModToolChatLog;
import com.eu.habbo.habbohotel.modtool.ModToolIssue;
import com.eu.habbo.habbohotel.modtool.ModToolTicketType;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.Outgoing;
import com.eu.habbo.messages.outgoing.guardians.GuardianNewReportReceivedComposer;
import com.eu.habbo.messages.outgoing.guardians.GuardianVotingRequestedComposer;
import com.eu.habbo.messages.outgoing.guardians.GuardianVotingResultComposer;
import com.eu.habbo.messages.outgoing.guardians.GuardianVotingTimeEnded;
import com.eu.habbo.messages.outgoing.guardians.GuardianVotingVotesComposer;
import com.eu.habbo.messages.outgoing.guides.BullyReportClosedComposer;
import com.eu.habbo.threading.runnables.GuardianNotAccepted;
import com.eu.habbo.threading.runnables.GuardianVotingFinish;
import gnu.trove.map.hash.THashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/guides/GuardianTicket.class */
public class GuardianTicket {
    private final Habbo reporter;
    private final Habbo reported;
    private final Date date;
    private ArrayList<ModToolChatLog> chatLogs;
    private GuardianVoteType verdict;
    private final THashMap<Habbo, GuardianVote> votes = new THashMap<>();
    private int timeLeft = 120;
    private int resendCount = 0;
    private int checkSum = 0;
    private int guardianCount = 0;

    public GuardianTicket(Habbo habbo, Habbo habbo2, ArrayList<ModToolChatLog> arrayList) {
        this.chatLogs = arrayList;
        Collections.sort(arrayList);
        Emulator.getThreading().run(new GuardianVotingFinish(this), 120000L);
        this.reported = habbo2;
        this.reporter = habbo;
        this.date = new Date();
    }

    public void requestToVote(Habbo habbo) {
        habbo.getClient().sendResponse(new GuardianNewReportReceivedComposer());
        this.votes.put(habbo, new GuardianVote(this.guardianCount, habbo));
        Emulator.getThreading().run(new GuardianNotAccepted(this, habbo), Emulator.getConfig().getInt("guardians.accept.timer") * Outgoing.CraftableProductsComposer);
    }

    public void addGuardian(Habbo habbo) {
        GuardianVote guardianVote = (GuardianVote) this.votes.get(habbo);
        if (guardianVote == null || guardianVote.type != GuardianVoteType.SEARCHING) {
            return;
        }
        habbo.getClient().sendResponse(new GuardianVotingRequestedComposer(this));
        guardianVote.type = GuardianVoteType.WAITING;
        updateVotes();
    }

    public void removeGuardian(Habbo habbo) {
        GuardianVote voteForGuardian = getVoteForGuardian(habbo);
        if (voteForGuardian == null) {
            return;
        }
        if (voteForGuardian.type == GuardianVoteType.SEARCHING || voteForGuardian.type == GuardianVoteType.WAITING) {
            getVoteForGuardian(habbo).type = GuardianVoteType.NOT_VOTED;
        }
        getVoteForGuardian(habbo).ignore = true;
        habbo.getClient().sendResponse(new GuardianVotingTimeEnded());
        updateVotes();
    }

    public void vote(Habbo habbo, GuardianVoteType guardianVoteType) {
        ((GuardianVote) this.votes.get(habbo)).type = guardianVoteType;
        updateVotes();
        AchievementManager.progressAchievement(habbo, Emulator.getGameEnvironment().getAchievementManager().getAchievement("GuideChatReviewer"));
        finish();
    }

    public void updateVotes() {
        synchronized (this.votes) {
            for (Map.Entry entry : this.votes.entrySet()) {
                if (((GuardianVote) entry.getValue()).type != GuardianVoteType.WAITING && ((GuardianVote) entry.getValue()).type != GuardianVoteType.NOT_VOTED && !((GuardianVote) entry.getValue()).ignore && ((GuardianVote) entry.getValue()).type != GuardianVoteType.SEARCHING) {
                    ((Habbo) entry.getKey()).getClient().sendResponse(new GuardianVotingVotesComposer(this, (Habbo) entry.getKey()));
                }
            }
        }
    }

    public void finish() {
        if (getVotedCount() < Emulator.getConfig().getInt("guardians.minimum.votes")) {
            if (this.votes.size() < Emulator.getConfig().getInt("guardians.maximum.guardians.total") && this.resendCount != Emulator.getConfig().getInt("guardians.maximum.resends")) {
                this.timeLeft = 30;
                Emulator.getThreading().run(new GuardianVotingFinish(this), 10000L);
                this.resendCount++;
                Emulator.getGameEnvironment().getGuideManager().findGuardians(this);
                return;
            }
            this.verdict = GuardianVoteType.FORWARDED;
            Emulator.getGameEnvironment().getGuideManager().closeTicket(this);
            ModToolIssue modToolIssue = new ModToolIssue(this.reporter.getHabboInfo().getId(), this.reporter.getHabboInfo().getUsername(), this.reported.getHabboInfo().getId(), this.reported.getHabboInfo().getUsername(), 0, Emulator.PREVIEW, ModToolTicketType.GUARDIAN);
            Emulator.getGameEnvironment().getModToolManager().addTicket(modToolIssue);
            Emulator.getGameEnvironment().getModToolManager().updateTicketToMods(modToolIssue);
            this.reporter.getClient().sendResponse(new BullyReportClosedComposer(1));
            return;
        }
        this.verdict = calculateVerdict();
        for (Map.Entry entry : this.votes.entrySet()) {
            if (((GuardianVote) entry.getValue()).type == GuardianVoteType.ACCEPTABLY || ((GuardianVote) entry.getValue()).type == GuardianVoteType.BADLY || ((GuardianVote) entry.getValue()).type == GuardianVoteType.AWFULLY) {
                ((Habbo) entry.getKey()).getClient().sendResponse(new GuardianVotingResultComposer(this, (GuardianVote) entry.getValue()));
            }
        }
        Emulator.getGameEnvironment().getGuideManager().closeTicket(this);
        if (this.verdict == GuardianVoteType.ACCEPTABLY) {
            this.reporter.getClient().sendResponse(new BullyReportClosedComposer(2));
        } else {
            this.reporter.getClient().sendResponse(new BullyReportClosedComposer(1));
        }
    }

    public boolean inProgress() {
        return this.verdict == null;
    }

    public GuardianVoteType calculateVerdict() {
        int i = 0;
        int i2 = 0;
        int i3 = 0;
        synchronized (this.votes) {
            Iterator it = this.votes.entrySet().iterator();
            while (it.hasNext()) {
                GuardianVote guardianVote = (GuardianVote) ((Map.Entry) it.next()).getValue();
                if (guardianVote.type == GuardianVoteType.ACCEPTABLY) {
                    i++;
                } else if (guardianVote.type == GuardianVoteType.BADLY) {
                    i2++;
                } else if (guardianVote.type == GuardianVoteType.AWFULLY) {
                    i3++;
                }
            }
        }
        int i4 = 0 + i + i2;
        return GuardianVoteType.BADLY;
    }

    public GuardianVote getVoteForGuardian(Habbo habbo) {
        return (GuardianVote) this.votes.get(habbo);
    }

    public THashMap<Habbo, GuardianVote> getVotes() {
        return this.votes;
    }

    public int getTimeLeft() {
        return this.timeLeft;
    }

    public GuardianVoteType getVerdict() {
        return this.verdict;
    }

    public ArrayList<ModToolChatLog> getChatLogs() {
        return this.chatLogs;
    }

    public int getResendCount() {
        return this.resendCount;
    }

    public int getCheckSum() {
        return this.checkSum;
    }

    public Habbo getReporter() {
        return this.reporter;
    }

    public Habbo getReported() {
        return this.reported;
    }

    public Date getDate() {
        return this.date;
    }

    public int getGuardianCount() {
        return this.guardianCount;
    }

    public ArrayList<GuardianVote> getSortedVotes(Habbo habbo) {
        ArrayList<GuardianVote> arrayList;
        synchronized (this.votes) {
            arrayList = new ArrayList<>((Collection<? extends GuardianVote>) this.votes.values());
            Collections.sort(arrayList);
            GuardianVote guardianVote = null;
            Iterator<GuardianVote> it = arrayList.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                GuardianVote next = it.next();
                if (next.guardian == habbo) {
                    guardianVote = next;
                    break;
                }
            }
            arrayList.remove(guardianVote);
        }
        return arrayList;
    }

    public int getVotedCount() {
        int i = 0;
        synchronized (this.votes) {
            for (Map.Entry entry : this.votes.entrySet()) {
                if (((GuardianVote) entry.getValue()).type == GuardianVoteType.ACCEPTABLY || ((GuardianVote) entry.getValue()).type == GuardianVoteType.BADLY || ((GuardianVote) entry.getValue()).type == GuardianVoteType.AWFULLY) {
                    i++;
                }
            }
        }
        return i;
    }
}
