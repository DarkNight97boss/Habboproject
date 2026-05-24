package com.eu.habbo.habbohotel.users.clothingvalidation;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import gnu.trove.TIntCollection;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.TIntHashSet;
import java.util.ArrayList;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/users/clothingvalidation/ClothingValidationManager.class */
public class ClothingValidationManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClothingValidationManager.class);
    public static String FIGUREDATA_URL = Emulator.PREVIEW;
    public static boolean VALIDATE_ON_HC_EXPIRE = false;
    public static boolean VALIDATE_ON_LOGIN = false;
    public static boolean VALIDATE_ON_CHANGE_LOOKS = false;
    public static boolean VALIDATE_ON_MIMIC = false;
    public static boolean VALIDATE_ON_MANNEQUIN = false;
    public static boolean VALIDATE_ON_FBALLGATE = false;
    private static final Figuredata FIGUREDATA = new Figuredata();

    public static void reloadFiguredata(String str) {
        try {
            FIGUREDATA.parseXML(str);
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
        return validateLook(habbo.getHabboInfo().getLook(), habbo.getHabboInfo().getGender().name(), habbo.getHabboStats().hasActiveClub(), habbo.getInventory().getWardrobeComponent().getClothingSets());
    }

    public static String validateLook(Habbo habbo, String str, String str2) {
        return validateLook(str, str2, habbo.getHabboStats().hasActiveClub(), habbo.getInventory().getWardrobeComponent().getClothingSets());
    }

    public static String validateLook(String str, String str2) {
        return validateLook(str, str2, false, new TIntHashSet());
    }

    public static String validateLook(String str, String str2, boolean z) {
        return validateLook(str, str2, z, new TIntHashSet());
    }

    public static String validateLook(String str, String str2, boolean z, TIntCollection tIntCollection) {
        if (FIGUREDATA.palettes.size() == 0 || FIGUREDATA.settypes.size() == 0) {
            return str;
        }
        String[] strArrSplit = str.split(Pattern.quote("."));
        ArrayList arrayList = new ArrayList();
        THashMap tHashMap = new THashMap();
        for (String str3 : strArrSplit) {
            if (str3.contains("-")) {
                String[] strArrSplit2 = str3.split(Pattern.quote("-"));
                if (FIGUREDATA.settypes.get(strArrSplit2[0]) != null) {
                    tHashMap.put(strArrSplit2[0], strArrSplit2);
                }
            }
        }
        FIGUREDATA.settypes.entrySet().stream().filter(entry -> {
            return !tHashMap.containsKey(entry.getKey());
        }).forEach(entry2 -> {
            FiguredataSettype figuredataSettype = (FiguredataSettype) entry2.getValue();
            if (!str2.equalsIgnoreCase("M") || z || figuredataSettype.mandatoryMale0) {
                if (!str2.equalsIgnoreCase("F") || z || figuredataSettype.mandatoryFemale0) {
                    if (str2.equalsIgnoreCase("M") && z && !figuredataSettype.mandatoryMale1) {
                        return;
                    }
                    if (str2.equalsIgnoreCase("F") && z && !figuredataSettype.mandatoryFemale1) {
                        return;
                    }
                    tHashMap.put((String) entry2.getKey(), new String[]{(String) entry2.getKey()});
                }
            }
        });
        tHashMap.forEach((str4, strArr) -> {
            try {
                if (strArr.length >= 1) {
                    FiguredataSettype figuredataSettype = FIGUREDATA.settypes.get(strArr[0]);
                    if (figuredataSettype == null) {
                        return;
                    }
                    FiguredataPalette figuredataPalette = FIGUREDATA.palettes.get(Integer.valueOf(figuredataSettype.paletteId));
                    if (figuredataPalette == null) {
                        throw new Exception("Palette " + figuredataSettype.paletteId + " does not exist");
                    }
                    int i = Integer.parseInt(strArr.length >= 2 ? strArr[1] : "-1");
                    FiguredataSettypeSet set = figuredataSettype.getSet(i);
                    if (set == null || ((set.club && !z) || !set.selectable || ((set.sellable && !tIntCollection.contains(set.id)) || (!set.gender.equalsIgnoreCase("U") && !set.gender.equalsIgnoreCase(str2))))) {
                        if (str2.equalsIgnoreCase("M") && !z && !figuredataSettype.mandatoryMale0) {
                            return;
                        }
                        if (str2.equalsIgnoreCase("F") && !z && !figuredataSettype.mandatoryFemale0) {
                            return;
                        }
                        if (str2.equalsIgnoreCase("M") && z && !figuredataSettype.mandatoryMale1) {
                            return;
                        }
                        if (str2.equalsIgnoreCase("F") && z && !figuredataSettype.mandatoryFemale1) {
                            return;
                        }
                        set = figuredataSettype.getFirstNonHCSetForGender(str2);
                        i = set.id;
                    }
                    ArrayList arrayList2 = new ArrayList();
                    int i2 = -1;
                    int i3 = -1;
                    if (set.colorable) {
                        i2 = strArr.length >= 3 ? Integer.parseInt(strArr[2]) : -1;
                        FiguredataPaletteColor color = figuredataPalette.getColor(i2);
                        if (color == null || (color.club && !z)) {
                            i2 = figuredataPalette.getFirstNonHCColor().id;
                        }
                    }
                    if (strArr.length >= 4 && set.colorable) {
                        i3 = Integer.parseInt(strArr[3]);
                        FiguredataPaletteColor color2 = figuredataPalette.getColor(i3);
                        if (color2 == null || (color2.club && !z)) {
                            i3 = figuredataPalette.getFirstNonHCColor().id;
                        }
                    }
                    arrayList2.add(figuredataSettype.type);
                    arrayList2.add(Emulator.PREVIEW + i);
                    if (i2 > -1) {
                        arrayList2.add(Emulator.PREVIEW + i2);
                    }
                    if (i3 > -1) {
                        arrayList2.add(Emulator.PREVIEW + i3);
                    }
                    arrayList.add(String.join("-", arrayList2));
                }
            } catch (Exception e) {
                LOGGER.error("Error in clothing validation", e);
            }
        });
        return String.join(".", arrayList);
    }
}
