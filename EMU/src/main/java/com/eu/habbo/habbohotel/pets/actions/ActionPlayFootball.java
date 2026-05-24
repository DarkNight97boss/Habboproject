package com.eu.habbo.habbohotel.pets.actions;

import com.eu.habbo.habbohotel.items.interactions.InteractionPushable;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.PetAction;
import com.eu.habbo.habbohotel.pets.PetVocalsType;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import gnu.trove.iterator.hash.TObjectHashIterator;

public class ActionPlayFootball extends PetAction {
   public ActionPlayFootball() {
      super(null, false);
   }

   @Override
   public boolean apply(Pet pet, Habbo habbo, String[] data) {
      Room room = pet.getRoom();
      if (room != null && room.getLayout() != null) {
         HabboItem foundBall = null;
         TObjectHashIterator var6 = room.getFloorItems().iterator();

         while (var6.hasNext()) {
            HabboItem item = (HabboItem)var6.next();
            if (item instanceof InteractionPushable) {
               foundBall = item;
            }
         }

         if (foundBall == null) {
            return false;
         }

         pet.getRoomUnit().setGoalLocation(room.getLayout().getTile(foundBall.getX(), foundBall.getY()));
         if (pet.getHappyness() > 75) {
            pet.say(pet.getPetData().randomVocal(PetVocalsType.PLAYFUL));
         } else {
            pet.say(pet.getPetData().randomVocal(PetVocalsType.GENERIC_NEUTRAL));
         }

         return true;
      } else {
         return false;
      }
   }
}
