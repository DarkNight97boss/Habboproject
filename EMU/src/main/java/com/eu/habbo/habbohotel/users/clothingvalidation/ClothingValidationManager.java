package com.eu.habbo.habbohotel.users.clothingvalidation;

import com.eu.habbo.habbohotel.users.Habbo;
import gnu.trove.TIntCollection;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.TIntHashSet;
import java.util.ArrayList;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClothingValidationManager {
   private static final Logger LOGGER = LoggerFactory.getLogger(ClothingValidationManager.class);
   public static String FIGUREDATA_URL = "";
   public static boolean VALIDATE_ON_HC_EXPIRE = false;
   public static boolean VALIDATE_ON_LOGIN = false;
   public static boolean VALIDATE_ON_CHANGE_LOOKS = false;
   public static boolean VALIDATE_ON_MIMIC = false;
   public static boolean VALIDATE_ON_MANNEQUIN = false;
   public static boolean VALIDATE_ON_FBALLGATE = false;
   private static final Figuredata FIGUREDATA = new Figuredata();

   public static void reloadFiguredata(String newUrl) {
      try {
         FIGUREDATA.parseXML(newUrl);
      } catch (Exception e) {
         VALIDATE_ON_HC_EXPIRE = false;
         VALIDATE_ON_LOGIN = false;
         VALIDATE_ON_CHANGE_LOOKS = false;
         VALIDATE_ON_MIMIC = false;
         VALIDATE_ON_MANNEQUIN = false;
         VALIDATE_ON_FBALLGATE = false;
         LOGGER.error("Caught exception", e);
      }
   }

   public static String validateLook(Habbo habbo) {
      return validateLook(
         habbo.getHabboInfo().getLook(),
         habbo.getHabboInfo().getGender().name(),
         habbo.getHabboStats().hasActiveClub(),
         habbo.getInventory().getWardrobeComponent().getClothingSets()
      );
   }

   public static String validateLook(Habbo habbo, String look, String gender) {
      return validateLook(look, gender, habbo.getHabboStats().hasActiveClub(), habbo.getInventory().getWardrobeComponent().getClothingSets());
   }

   public static String validateLook(String look, String gender) {
      return validateLook(look, gender, false, new TIntHashSet());
   }

   public static String validateLook(String look, String gender, boolean isHC) {
      return validateLook(look, gender, isHC, new TIntHashSet());
   }

   public static String validateLook(String look, String gender, boolean isHC, TIntCollection ownedClothing) {
      if (FIGUREDATA.palettes.size() != 0 && FIGUREDATA.settypes.size() != 0) {
         String[] newLookParts = look.split(Pattern.quote("."));
         ArrayList<String> lookParts = new ArrayList<>();
         THashMap<String, String[]> parts = new THashMap();

         for (String lookpart : newLookParts) {
            if (lookpart.contains("-")) {
               String[] data = lookpart.split(Pattern.quote("-"));
               FiguredataSettype settype = FIGUREDATA.settypes.get(data[0]);
               if (settype != null) {
                  parts.put(data[0], data);
               }
            }
         }

         FIGUREDATA.settypes.entrySet().stream().filter(x -> !parts.containsKey(x.getKey())).forEach(x -> {
            FiguredataSettype settypex = x.getValue();
            if (!gender.equalsIgnoreCase("M") || isHC || settypex.mandatoryMale0) {
               if (!gender.equalsIgnoreCase("F") || isHC || settypex.mandatoryFemale0) {
                  if (!gender.equalsIgnoreCase("M") || !isHC || settypex.mandatoryMale1) {
                     if (!gender.equalsIgnoreCase("F") || !isHC || settypex.mandatoryFemale1) {
                        parts.put(x.getKey(), new String[]{x.getKey()});
                     }
                  }
               }
            }
         });
         parts.forEach(
            (key, datax) -> {
               try {
                  if (datax.length >= 1) {
                     FiguredataSettype settypex = FIGUREDATA.settypes.get(datax[0]);
                     if (settypex == null) {
                        return;
                     }

                     FiguredataPalette palette = FIGUREDATA.palettes.get(settypex.paletteId);
                     if (palette == null) {
                        throw new Exception("Palette " + settypex.paletteId + " does not exist");
                     }

                     int setId = Integer.parseInt(datax.length >= 2 ? datax[1] : "-1");
                     FiguredataSettypeSet set = settypex.getSet(setId);
                     if (set == null
                        || set.club && !isHC
                        || !set.selectable
                        || set.sellable && !ownedClothing.contains(set.id)
                        || !set.gender.equalsIgnoreCase("U") && !set.gender.equalsIgnoreCase(gender)) {
                        if (gender.equalsIgnoreCase("M") && !isHC && !settypex.mandatoryMale0) {
                           return;
                        }

                        if (gender.equalsIgnoreCase("F") && !isHC && !settypex.mandatoryFemale0) {
                           return;
                        }

                        if (gender.equalsIgnoreCase("M") && isHC && !settypex.mandatoryMale1) {
                           return;
                        }

                        if (gender.equalsIgnoreCase("F") && isHC && !settypex.mandatoryFemale1) {
                           return;
                        }

                        set = settypex.getFirstNonHCSetForGender(gender);
                        setId = set.id;
                     }

                     ArrayList<String> dataParts = new ArrayList<>();
                     int color1 = -1;
                     int color2 = -1;
                     if (set.colorable) {
                        color1 = datax.length >= 3 ? Integer.parseInt(datax[2]) : -1;
                        FiguredataPaletteColor color = palette.getColor(color1);
                        if (color == null || color.club && !isHC) {
                           color1 = palette.getFirstNonHCColor().id;
                        }
                     }

                     if (datax.length >= 4 && set.colorable) {
                        color2 = Integer.parseInt(datax[3]);
                        FiguredataPaletteColor color = palette.getColor(color2);
                        if (color == null || color.club && !isHC) {
                           color2 = palette.getFirstNonHCColor().id;
                        }
                     }

                     dataParts.add(settypex.type);
                     dataParts.add("" + setId);
                     if (color1 > -1) {
                        dataParts.add("" + color1);
                     }

                     if (color2 > -1) {
                        dataParts.add("" + color2);
                     }

                     lookParts.add(String.join("-", dataParts));
                  }
               } catch (Exception e) {
                  LOGGER.error("Error in clothing validation", e);
               }
            }
         );
         return String.join(".", lookParts);
      } else {
         return look;
      }
   }
}
