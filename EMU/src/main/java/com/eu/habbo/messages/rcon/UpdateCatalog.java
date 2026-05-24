package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.outgoing.catalog.CatalogModeComposer;
import com.eu.habbo.messages.outgoing.catalog.CatalogUpdatedComposer;
import com.eu.habbo.messages.outgoing.catalog.DiscountComposer;
import com.eu.habbo.messages.outgoing.catalog.GiftConfigurationComposer;
import com.eu.habbo.messages.outgoing.catalog.RecyclerLogicComposer;
import com.eu.habbo.messages.outgoing.catalog.marketplace.MarketplaceConfigComposer;
import com.google.gson.Gson;

public class UpdateCatalog extends RCONMessage<UpdateCatalog.JSONUpdateCatalog> {
   public UpdateCatalog() {
      super(UpdateCatalog.JSONUpdateCatalog.class);
   }

   public void handle(Gson gson, UpdateCatalog.JSONUpdateCatalog json) {
      Emulator.getGameEnvironment().getCatalogManager().initialize();
      Emulator.getGameServer().getGameClientManager().sendBroadcastResponse(new CatalogUpdatedComposer());
      Emulator.getGameServer().getGameClientManager().sendBroadcastResponse(new CatalogModeComposer(0));
      Emulator.getGameServer().getGameClientManager().sendBroadcastResponse(new DiscountComposer());
      Emulator.getGameServer().getGameClientManager().sendBroadcastResponse(new MarketplaceConfigComposer());
      Emulator.getGameServer().getGameClientManager().sendBroadcastResponse(new GiftConfigurationComposer());
      Emulator.getGameServer().getGameClientManager().sendBroadcastResponse(new RecyclerLogicComposer());
      Emulator.getGameEnvironment().getCraftingManager().reload();
   }

   static class JSONUpdateCatalog {
   }
}
