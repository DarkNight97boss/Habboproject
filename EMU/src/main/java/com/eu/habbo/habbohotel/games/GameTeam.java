package com.eu.habbo.habbohotel.games;

import com.eu.habbo.habbohotel.users.Habbo;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;

public class GameTeam {
   public final GameTeamColors teamColor;
   private final THashSet<GamePlayer> members;
   private int teamScore;

   public GameTeam(GameTeamColors teamColor) {
      this.teamColor = teamColor;
      this.members = new THashSet();
   }

   public void initialise() {
      TObjectHashIterator var1 = this.members.iterator();

      while (var1.hasNext()) {
         GamePlayer player = (GamePlayer)var1.next();
         player.reset();
      }

      this.teamScore = 0;
   }

   public void reset() {
      this.members.clear();
   }

   public void addTeamScore(int teamScore) {
      this.teamScore += teamScore;
   }

   public int getTeamScore() {
      return this.teamScore;
   }

   public synchronized int getTotalScore() {
      int score = this.teamScore;
      TObjectHashIterator var2 = this.members.iterator();

      while (var2.hasNext()) {
         GamePlayer player = (GamePlayer)var2.next();
         score += player.getScore();
      }

      return score;
   }

   public void addMember(GamePlayer gamePlayer) {
      synchronized (this.members) {
         this.members.add(gamePlayer);
      }
   }

   public void removeMember(GamePlayer gamePlayer) {
      synchronized (this.members) {
         this.members.remove(gamePlayer);
      }
   }

   public void clearMembers() {
      TObjectHashIterator var1 = this.members.iterator();

      while (var1.hasNext()) {
         GamePlayer player = (GamePlayer)var1.next();
         if (player != null && player.getHabbo() != null) {
            if (player.getHabbo().getHabboInfo().getGamePlayer() != null) {
               player.getHabbo().getHabboInfo().getGamePlayer().reset();
            }

            player.getHabbo().getHabboInfo().setCurrentGame(null);
            player.getHabbo().getHabboInfo().setGamePlayer(null);
         }
      }

      this.members.clear();
   }

   public void resetScores() {
      TObjectHashIterator var1 = this.members.iterator();

      while (var1.hasNext()) {
         GamePlayer player = (GamePlayer)var1.next();
         if (player != null) {
            player.reset();
         }
      }

      this.teamScore = 0;
   }

   public THashSet<GamePlayer> getMembers() {
      return this.members;
   }

   public boolean isMember(Habbo habbo) {
      TObjectHashIterator var2 = this.members.iterator();

      while (var2.hasNext()) {
         GamePlayer p = (GamePlayer)var2.next();
         if (p.getHabbo().equals(habbo)) {
            return true;
         }
      }

      return false;
   }

   @Deprecated
   public GamePlayer getPlayerForHabbo(Habbo habbo) {
      TObjectHashIterator var2 = this.members.iterator();

      while (var2.hasNext()) {
         GamePlayer p = (GamePlayer)var2.next();
         if (p.getHabbo().equals(habbo)) {
            return p;
         }
      }

      return null;
   }
}
