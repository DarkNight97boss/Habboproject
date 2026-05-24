package com.eu.habbo.messages.outgoing.friends;

import com.eu.habbo.habbohotel.messenger.MessengerBuddy;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class UserSearchResultComposer extends MessageComposer {
   private final THashSet<MessengerBuddy> users;
   private final THashSet<MessengerBuddy> friends;
   private final Habbo habbo;
   private static Comparator COMPARATOR = Comparator.<MessengerBuddy, Integer>comparing(b -> b.getUsername().length())
      .thenComparing((b, b2) -> b.getUsername().compareToIgnoreCase(b2.getUsername()));

   public UserSearchResultComposer(THashSet<MessengerBuddy> users, THashSet<MessengerBuddy> friends, Habbo habbo) {
      this.users = users;
      this.friends = friends;
      this.habbo = habbo;
   }

   @Override
   protected ServerMessage composeInternal() {
      this.response.init(973);
      List<MessengerBuddy> u = new ArrayList<>();
      TObjectHashIterator friends = this.users.iterator();

      while (friends.hasNext()) {
         MessengerBuddy buddy = (MessengerBuddy)friends.next();
         if (!this.inFriendList(buddy)) {
            u.add(buddy);
         }
      }

      List<MessengerBuddy> friendsx = new ArrayList<>(this.friends);
      u.sort(COMPARATOR);
      friendsx.sort(COMPARATOR);
      this.response.appendInt(this.friends.size());
      TObjectHashIterator var6 = this.friends.iterator();

      while (var6.hasNext()) {
         MessengerBuddy buddy = (MessengerBuddy)var6.next();
         this.response.appendInt(buddy.getId());
         this.response.appendString(buddy.getUsername());
         this.response.appendString(buddy.getMotto());
         this.response.appendBoolean(false);
         this.response.appendBoolean(false);
         this.response.appendString("");
         this.response.appendInt(1);
         this.response.appendString(buddy.getLook());
         this.response.appendString("");
      }

      this.response.appendInt(u.size());

      for (MessengerBuddy buddy : u) {
         this.response.appendInt(buddy.getId());
         this.response.appendString(buddy.getUsername());
         this.response.appendString(buddy.getMotto());
         this.response.appendBoolean(false);
         this.response.appendBoolean(false);
         this.response.appendString("");
         this.response.appendInt(1);
         this.response.appendString(buddy.getOnline() == 1 ? buddy.getLook() : "");
         this.response.appendString("");
      }

      return this.response;
   }

   private boolean inFriendList(MessengerBuddy buddy) {
      TObjectHashIterator var2 = this.friends.iterator();

      while (var2.hasNext()) {
         MessengerBuddy friend = (MessengerBuddy)var2.next();
         if (friend.getUsername().equals(buddy.getUsername())) {
            return true;
         }
      }

      return false;
   }
}
