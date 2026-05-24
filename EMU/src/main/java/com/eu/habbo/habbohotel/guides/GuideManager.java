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
import java.util.Map.Entry;

public class GuideManager {
   private final THashSet<GuideTour> activeTours = new THashSet();
   private final THashSet<GuardianTicket> activeTickets = new THashSet();
   private final THashSet<GuardianTicket> closedTickets = new THashSet();
   private final THashMap<Habbo, Boolean> activeHelpers = new THashMap();
   private final THashMap<Habbo, GuardianTicket> activeGuardians = new THashMap();
   private final THashMap<Integer, Integer> tourRequestTiming = new THashMap();

   public void userLogsOut(Habbo habbo) {
      GuideTour tour = this.getGuideTourByHabbo(habbo);
      if (tour != null) {
         this.endSession(tour);
      }

      this.activeHelpers.remove(habbo);
      GuardianTicket ticket = this.getTicketForGuardian(habbo);
      if (ticket != null) {
         ticket.removeGuardian(habbo);
      }

      this.activeGuardians.remove(habbo);
   }

   public void setOnGuide(Habbo habbo, boolean onDuty) {
      if (onDuty) {
         this.activeHelpers.put(habbo, false);
      } else {
         GuideTour tour = this.getGuideTourByHabbo(habbo);
         if (tour != null) {
            return;
         }

         this.activeHelpers.remove(habbo);
      }
   }

   public boolean findHelper(GuideTour tour) {
      synchronized (this.activeHelpers) {
         for (Entry<Habbo, Boolean> set : this.activeHelpers.entrySet()) {
            if (!set.getValue() && !tour.hasDeclined(set.getKey().getHabboInfo().getId())) {
               tour.checkSum++;
               tour.setHelper(set.getKey());
               set.getKey().getClient().sendResponse(new GuideSessionAttachedComposer(tour, true));
               tour.getNoob().getClient().sendResponse(new GuideSessionAttachedComposer(tour, false));
               Emulator.getThreading().run(new GuideFindNewHelper(tour, set.getKey()), 60000L);
               this.activeTours.add(tour);
               return true;
            }
         }
      }

      this.endSession(tour);
      tour.getNoob().getClient().sendResponse(new GuideSessionErrorComposer(1));
      return false;
   }

   public void declineTour(GuideTour tour) {
      Habbo helper = tour.getHelper();
      tour.addDeclinedHelper(tour.getHelper().getHabboInfo().getId());
      tour.setHelper(null);
      helper.getClient().sendResponse(new GuideSessionEndedComposer(1));
      helper.getClient().sendResponse(new GuideSessionDetachedComposer());
      if (!this.findHelper(tour)) {
         this.endSession(tour);
         tour.getNoob().getClient().sendResponse(new GuideSessionErrorComposer(1));
      }
   }

   public void startSession(GuideTour tour, Habbo helper) {
      synchronized (this.activeTours) {
         synchronized (this.activeHelpers) {
            this.activeHelpers.put(helper, true);
            ServerMessage message = new GuideSessionStartedComposer(tour).compose();
            tour.getNoob().getClient().sendResponse(message);
            tour.getHelper().getClient().sendResponse(message);
            tour.checkSum++;
            this.tourRequestTiming.put(tour.getStartTime(), Emulator.getIntUnixTimestamp());
         }
      }
   }

   public void endSession(GuideTour tour) {
      synchronized (this.activeTours) {
         synchronized (this.activeHelpers) {
            tour.getNoob().getClient().sendResponse(new GuideSessionEndedComposer(1));
            tour.end();
            if (tour.getHelper() != null) {
               this.activeHelpers.put(tour.getHelper(), false);
               tour.getHelper().getClient().sendResponse(new GuideSessionEndedComposer(1));
               tour.getHelper().getClient().sendResponse(new GuideSessionDetachedComposer());
               tour.getHelper().getClient().sendResponse(new GuideToolsComposer(true));
            }
         }
      }
   }

   public void recommend(GuideTour tour, boolean recommend) {
      synchronized (this.activeTours) {
         tour.setWouldRecommend(recommend ? GuideRecommendStatus.YES : GuideRecommendStatus.NO);
         tour.getNoob().getClient().sendResponse(new GuideSessionDetachedComposer());
         AchievementManager.progressAchievement(tour.getNoob(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("GuideFeedbackGiver"));
         this.activeTours.remove(tour);
      }
   }

   public GuideTour getGuideTourByHelper(Habbo helper) {
      synchronized (this.activeTours) {
         TObjectHashIterator var3 = this.activeTours.iterator();

         while (var3.hasNext()) {
            GuideTour tour = (GuideTour)var3.next();
            if (!tour.isEnded() && tour.getHelper() == helper) {
               return tour;
            }
         }

         return null;
      }
   }

   public GuideTour getGuideTourByNoob(Habbo noob) {
      synchronized (this.activeTours) {
         TObjectHashIterator var3 = this.activeTours.iterator();

         while (var3.hasNext()) {
            GuideTour tour = (GuideTour)var3.next();
            if (tour.getNoob() == noob) {
               return tour;
            }
         }

         return null;
      }
   }

   public GuideTour getGuideTourByHabbo(Habbo habbo) {
      synchronized (this.activeTours) {
         TObjectHashIterator var3 = this.activeTours.iterator();

         while (var3.hasNext()) {
            GuideTour tour = (GuideTour)var3.next();
            if (tour.getHelper() == habbo || tour.getNoob() == habbo) {
               return tour;
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
         int total = 0;
         if (this.tourRequestTiming.isEmpty()) {
            return 5;
         }

         for (Entry<Integer, Integer> set : this.tourRequestTiming.entrySet()) {
            total += set.getValue() - set.getKey();
         }

         return total / this.tourRequestTiming.size();
      }
   }

   public void addGuardianTicket(GuardianTicket ticket) {
      synchronized (this.activeTickets) {
         this.activeTickets.add(ticket);
         this.findGuardians(ticket);
      }
   }

   public void findGuardians(GuardianTicket ticket) {
      synchronized (this.activeGuardians) {
         int count = ticket.getVotedCount();
         THashSet<Habbo> selectedGuardians = new THashSet();

         for (Entry<Habbo, GuardianTicket> set : this.activeGuardians.entrySet()) {
            if (count == 5) {
               break;
            }

            if (set.getKey() != ticket.getReporter() && set.getKey() != ticket.getReported()) {
               if (set.getValue() == null && ticket.getVoteForGuardian(set.getKey()) == null) {
                  ticket.requestToVote(set.getKey());
                  selectedGuardians.add(set.getKey());
               }

               count++;
            }
         }

         TObjectHashIterator var9 = selectedGuardians.iterator();

         while (var9.hasNext()) {
            Habbo habbo = (Habbo)var9.next();
            this.activeGuardians.put(habbo, ticket);
         }

         if (count < 5) {
            Emulator.getThreading().run(new GuardianTicketFindMoreSlaves(ticket), 3000L);
         }
      }
   }

   public void acceptTicket(Habbo guardian, boolean accepted) {
      GuardianTicket ticket = this.getTicketForGuardian(guardian);
      if (ticket != null) {
         if (!accepted) {
            ticket.removeGuardian(guardian);
            this.findGuardians(ticket);
            this.activeGuardians.put(guardian, null);
         } else {
            ticket.addGuardian(guardian);
            this.activeGuardians.put(guardian, ticket);
         }
      }
   }

   public GuardianTicket getTicketForGuardian(Habbo guardian) {
      synchronized (this.activeGuardians) {
         return (GuardianTicket)this.activeGuardians.get(guardian);
      }
   }

   public GuardianTicket getRecentTicket(Habbo reporter) {
      GuardianTicket ticket = null;
      synchronized (this.activeTickets) {
         TObjectHashIterator var4 = this.activeTickets.iterator();

         while (var4.hasNext()) {
            GuardianTicket t = (GuardianTicket)var4.next();
            if (t.getReporter() == reporter) {
               return t;
            }
         }
      }

      synchronized (this.closedTickets) {
         TObjectHashIterator var11 = this.closedTickets.iterator();

         while (var11.hasNext()) {
            GuardianTicket t = (GuardianTicket)var11.next();
            if (t.getReporter() == reporter
               && (
                  ticket == null
                     || Emulator.getIntUnixTimestamp() - t.getDate().getTime() / 1000L < Emulator.getIntUnixTimestamp() - ticket.getDate().getTime() / 1000L
               )) {
               ticket = t;
            }
         }

         return ticket;
      }
   }

   public GuardianTicket getOpenReportedHabboTicket(Habbo reported) {
      synchronized (this.activeTickets) {
         TObjectHashIterator var3 = this.activeTickets.iterator();

         while (var3.hasNext()) {
            GuardianTicket t = (GuardianTicket)var3.next();
            if (t.getReported() == reported) {
               return t;
            }
         }

         return null;
      }
   }

   public void closeTicket(GuardianTicket ticket) {
      synchronized (this.activeTickets) {
         this.activeTickets.remove(ticket);
      }

      synchronized (this.closedTickets) {
         this.closedTickets.add(ticket);
      }

      THashSet<Habbo> toUpdate = new THashSet();
      synchronized (this.activeGuardians) {
         for (Entry<Habbo, GuardianTicket> set : this.activeGuardians.entrySet()) {
            if (set.getValue() == ticket) {
               toUpdate.add(set.getKey());
            }
         }

         TObjectHashIterator var12 = toUpdate.iterator();

         while (var12.hasNext()) {
            Habbo habbo = (Habbo)var12.next();
            this.activeGuardians.put(habbo, null);
         }
      }
   }

   public void setOnGuardian(Habbo habbo, boolean onDuty) {
      if (onDuty) {
         this.activeGuardians.put(habbo, null);
      } else {
         GuardianTicket ticket = this.getTicketForGuardian(habbo);
         if (ticket != null) {
            ticket.removeGuardian(habbo);
         }

         this.activeGuardians.remove(habbo);
      }
   }

   public void cleanup() {
      synchronized (this.activeTours) {
         THashSet<GuideTour> tours = new THashSet();
         TObjectHashIterator var3 = this.activeTours.iterator();

         while (var3.hasNext()) {
            GuideTour tour = (GuideTour)var3.next();
            if (tour.isEnded() && Emulator.getIntUnixTimestamp() - tour.getEndTime() > 300) {
               tours.add(tour);
            }
         }

         var3 = tours.iterator();

         while (var3.hasNext()) {
            GuideTour tour = (GuideTour)var3.next();
            this.activeTours.remove(tour);
         }
      }

      synchronized (this.activeTickets) {
         THashSet<GuardianTicket> tickets = new THashSet();
         TObjectHashIterator var12 = this.closedTickets.iterator();

         while (var12.hasNext()) {
            GuardianTicket ticket = (GuardianTicket)var12.next();
            if (Emulator.getIntUnixTimestamp() - ticket.getDate().getTime() / 1000L > 900L) {
               tickets.add(ticket);
            }
         }

         var12 = tickets.iterator();

         while (var12.hasNext()) {
            GuardianTicket ticket = (GuardianTicket)var12.next();
            this.closedTickets.remove(ticket);
         }
      }
   }
}
