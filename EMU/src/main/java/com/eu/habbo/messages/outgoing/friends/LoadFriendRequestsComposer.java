package com.eu.habbo.messages.outgoing.friends;

import com.eu.habbo.habbohotel.messenger.FriendRequest;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import gnu.trove.iterator.hash.TObjectHashIterator;

public class LoadFriendRequestsComposer extends MessageComposer {
   private final Habbo habbo;

   public LoadFriendRequestsComposer(Habbo habbo) {
      this.habbo = habbo;
   }

   @Override
   protected ServerMessage composeInternal() {
      this.response.init(280);
      synchronized (this.habbo.getMessenger().getFriendRequests()) {
         this.response.appendInt(this.habbo.getMessenger().getFriendRequests().size());
         this.response.appendInt(this.habbo.getMessenger().getFriendRequests().size());
         TObjectHashIterator var2 = this.habbo.getMessenger().getFriendRequests().iterator();

         while (var2.hasNext()) {
            FriendRequest friendRequest = (FriendRequest)var2.next();
            this.response.appendInt(friendRequest.getId());
            this.response.appendString(friendRequest.getUsername());
            this.response.appendString(friendRequest.getLook());
         }
      }

      return this.response;
   }
}
