package com.eu.habbo.habbohotel.modtool;

public enum CfhActionType {
   MODS(0),
   AUTO_REPLY(1),
   AUTO_IGNORE(2),
   GUARDIANS(3);

   public final int type;

   CfhActionType(int type) {
      this.type = type;
   }

   public static CfhActionType get(String name) {
      switch (name) {
         case "auto_reply":
            return AUTO_REPLY;
         case "auto_ignore":
            return AUTO_IGNORE;
         case "guardians":
            return GUARDIANS;
         default:
            return MODS;
      }
   }

   @Override
   public String toString() {
      return this.name().toLowerCase();
   }
}
