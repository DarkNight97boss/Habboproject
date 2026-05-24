package com.eu.habbo.messages.outgoing.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ModToolCategory;
import com.eu.habbo.habbohotel.modtool.ModToolIssue;
import com.eu.habbo.habbohotel.modtool.ModToolTicketState;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.hash.THashMap;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.hash.THashSet;
import java.util.Iterator;

public class ModToolComposer extends MessageComposer implements TObjectProcedure<ModToolCategory> {
   private final Habbo habbo;

   public ModToolComposer(Habbo habbo) {
      this.habbo = habbo;
   }

   @Override
   protected ServerMessage composeInternal() {
      this.response.init(2696);
      if (this.habbo.hasPermission(Permission.ACC_MODTOOL_TICKET_Q)) {
         THashSet<ModToolIssue> openTickets = new THashSet();
         THashMap<Integer, ModToolIssue> tickets = Emulator.getGameEnvironment().getModToolManager().getTickets();

         for (ModToolIssue t : tickets.values()) {
            if (t.state != ModToolTicketState.CLOSED) {
               openTickets.add(t);
            }
         }

         int ticketsCount = openTickets.size();
         if (ticketsCount > 100) {
            ticketsCount = 100;
         }

         this.response.appendInt(ticketsCount);
         Iterator<ModToolIssue> it = openTickets.iterator();

         for (int i = 0; i < ticketsCount; i++) {
            it.next().serialize(this.response);
         }
      } else {
         this.response.appendInt(0);
      }

      synchronized (Emulator.getGameEnvironment().getModToolManager().getPresets()) {
         this.response.appendInt(((THashSet)Emulator.getGameEnvironment().getModToolManager().getPresets().get("user")).size());
         TObjectHashIterator var10 = ((THashSet)Emulator.getGameEnvironment().getModToolManager().getPresets().get("user")).iterator();

         while (var10.hasNext()) {
            String s = (String)var10.next();
            this.response.appendString(s);
         }
      }

      this.response.appendInt(Emulator.getGameEnvironment().getModToolManager().getCategory().size());
      Emulator.getGameEnvironment().getModToolManager().getCategory().forEachValue(this);
      this.response.appendBoolean(this.habbo.hasPermission(Permission.ACC_MODTOOL_TICKET_Q));
      this.response.appendBoolean(this.habbo.hasPermission(Permission.ACC_MODTOOL_USER_LOGS));
      this.response.appendBoolean(this.habbo.hasPermission(Permission.ACC_MODTOOL_USER_ALERT));
      this.response.appendBoolean(this.habbo.hasPermission(Permission.ACC_MODTOOL_USER_KICK));
      this.response.appendBoolean(this.habbo.hasPermission(Permission.ACC_MODTOOL_USER_BAN));
      this.response.appendBoolean(this.habbo.hasPermission(Permission.ACC_MODTOOL_ROOM_INFO));
      this.response.appendBoolean(this.habbo.hasPermission(Permission.ACC_MODTOOL_ROOM_LOGS));
      synchronized (Emulator.getGameEnvironment().getModToolManager().getPresets()) {
         this.response.appendInt(((THashSet)Emulator.getGameEnvironment().getModToolManager().getPresets().get("room")).size());
         TObjectHashIterator var11 = ((THashSet)Emulator.getGameEnvironment().getModToolManager().getPresets().get("room")).iterator();

         while (var11.hasNext()) {
            String s = (String)var11.next();
            this.response.appendString(s);
         }
      }

      return this.response;
   }

   public boolean execute(ModToolCategory category) {
      this.response.appendString(category.getName());
      return true;
   }
}
