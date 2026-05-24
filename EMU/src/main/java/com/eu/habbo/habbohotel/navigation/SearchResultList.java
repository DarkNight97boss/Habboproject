package com.eu.habbo.habbohotel.navigation;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomState;
import com.eu.habbo.messages.ISerialize;
import com.eu.habbo.messages.ServerMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchResultList implements ISerialize, Comparable<SearchResultList> {
   public final int order;
   public final String code;
   public final String query;
   public final SearchAction action;
   public final ListMode mode;
   public final DisplayMode hidden;
   public final List<Room> rooms;
   public final boolean filter;
   public final boolean showInvisible;
   public final DisplayOrder displayOrder;
   public final int categoryOrder;

   public SearchResultList(
      int order,
      String code,
      String query,
      SearchAction action,
      ListMode mode,
      DisplayMode hidden,
      List<Room> rooms,
      boolean filter,
      boolean showInvisible,
      DisplayOrder displayOrder,
      int categoryOrder
   ) {
      this.order = order;
      this.code = code;
      this.query = query;
      this.action = action;
      this.mode = mode;
      this.rooms = rooms;
      this.hidden = hidden;
      this.filter = filter;
      this.showInvisible = showInvisible;
      this.displayOrder = displayOrder;
      this.categoryOrder = categoryOrder;
   }

   @Override
   public void serialize(ServerMessage message) {
      message.appendString(this.code);
      message.appendString(this.query);
      message.appendInt(this.action.type);
      message.appendBoolean(this.hidden.equals(DisplayMode.COLLAPSED));
      message.appendInt(this.mode.type);
      synchronized (this.rooms) {
         if (!this.showInvisible) {
            List<Room> toRemove = new ArrayList<>();

            for (Room room : this.rooms) {
               if (room.getState() == RoomState.INVISIBLE) {
                  toRemove.add(room);
               }
            }

            this.rooms.removeAll(toRemove);
         }

         message.appendInt(this.rooms.size());
         Collections.sort(this.rooms);

         for (Room room : this.rooms) {
            room.serialize(message);
         }
      }
   }

   public int compareTo(SearchResultList o) {
      if (this.displayOrder == DisplayOrder.ACTIVITY) {
         return this.code.equalsIgnoreCase("popular") ? -1 : this.rooms.size() - o.rooms.size();
      } else {
         return this.categoryOrder - o.categoryOrder;
      }
   }
}
