package com.eu.habbo.messages.outgoing.rooms.pets.breeding;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.PetBreedingReward;
import com.eu.habbo.habbohotel.pets.PetManager;
import com.eu.habbo.messages.ISerialize;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.ArrayList;
import org.apache.commons.math3.distribution.NormalDistribution;

public class PetBreedingResultComposer extends MessageComposer {
   private final int boxId;
   private final int petType;
   private final PetBreedingResultComposer.PetBreedingPet petOne;
   private final PetBreedingResultComposer.PetBreedingPet petTwo;

   public PetBreedingResultComposer(int boxId, int petType, Pet petOne, String ownerPetOne, Pet petTwo, String ownerPetTwo) {
      this.boxId = boxId;
      this.petType = petType;
      this.petOne = new PetBreedingResultComposer.PetBreedingPet(petOne, ownerPetOne);
      this.petTwo = new PetBreedingResultComposer.PetBreedingPet(petTwo, ownerPetTwo);
   }

   @Override
   protected ServerMessage composeInternal() {
      this.response.init(634);
      this.response.appendInt(this.boxId);
      this.petOne.serialize(this.response);
      this.petTwo.serialize(this.response);
      double avgLevel = ((float)this.petOne.pet.getLevel() + this.petTwo.pet.getLevel()) / 2.0F;
      NormalDistribution normalDistribution = PetManager.getNormalDistributionForBreeding(avgLevel);
      TIntObjectHashMap<ArrayList<PetBreedingReward>> rewardBreeds = Emulator.getGameEnvironment().getPetManager().getBreedingRewards(this.petType);
      this.response.appendInt(4);
      int percentage1 = (int)(normalDistribution.cumulativeProbability(10.0) * 100.0);
      int percentage2 = (int)(normalDistribution.cumulativeProbability(15.0) * 100.0) - percentage1;
      int percentage3 = (int)(normalDistribution.cumulativeProbability(18.0) * 100.0) - percentage1 - percentage2;
      int percentage4 = (int)(normalDistribution.cumulativeProbability(20.0) * 100.0) - percentage1 - percentage2 - percentage3;
      int dPercentage = 100 - (percentage1 + percentage2 + percentage3 + percentage4);
      if (dPercentage > 0) {
         percentage1 += dPercentage;
      } else {
         percentage4 -= dPercentage;
      }

      this.response.appendInt(percentage4);
      this.response.appendInt(((ArrayList<PetBreedingReward>)rewardBreeds.get(4)).size());

      for (PetBreedingReward reward : (ArrayList<PetBreedingReward>)rewardBreeds.get(4)) {
         this.response.appendInt(reward.breed);
      }

      this.response.appendInt(percentage3);
      this.response.appendInt(((ArrayList<PetBreedingReward>)rewardBreeds.get(3)).size());

      for (PetBreedingReward reward : (ArrayList<PetBreedingReward>)rewardBreeds.get(3)) {
         this.response.appendInt(reward.breed);
      }

      this.response.appendInt(percentage2);
      this.response.appendInt(((ArrayList<PetBreedingReward>)rewardBreeds.get(2)).size());

      for (PetBreedingReward reward : (ArrayList<PetBreedingReward>)rewardBreeds.get(2)) {
         this.response.appendInt(reward.breed);
      }

      this.response.appendInt(percentage1);
      this.response.appendInt(((ArrayList<PetBreedingReward>)rewardBreeds.get(1)).size());

      for (PetBreedingReward reward : (ArrayList<PetBreedingReward>)rewardBreeds.get(1)) {
         this.response.appendInt(reward.breed);
      }

      this.response.appendInt(this.petType);
      return this.response;
   }

   public class PetBreedingPet implements ISerialize {
      public final Pet pet;
      public final String ownerName;

      public PetBreedingPet(Pet pet, String ownerName) {
         this.pet = pet;
         this.ownerName = ownerName;
      }

      @Override
      public void serialize(ServerMessage message) {
         message.appendInt(this.pet.getId());
         message.appendString(this.pet.getName());
         message.appendInt(this.pet.getLevel());
         message.appendString(this.pet.getColor());
         message.appendString(this.ownerName);
      }
   }
}
