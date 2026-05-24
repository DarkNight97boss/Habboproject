package com.eu.habbo.util.figure;

import com.eu.habbo.Emulator;
import gnu.trove.map.hash.THashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.ArrayUtils;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/util/figure/FigureUtil.class */
public class FigureUtil {
    public static THashMap<String, String> getFigureBits(String str) {
        THashMap<String, String> tHashMap = new THashMap<>();
        for (String str2 : str.split("\\.")) {
            String[] strArrSplit = str2.split("-", 2);
            tHashMap.put(strArrSplit[0], strArrSplit.length > 1 ? strArrSplit[1] : Emulator.PREVIEW);
        }
        return tHashMap;
    }

    public static String mergeFigures(String str, String str2) {
        return mergeFigures(str, str2, null, null);
    }

    public static String mergeFigures(String str, String str2, String[] strArr) {
        return mergeFigures(str, str2, strArr, null);
    }

    public static boolean hasBlacklistedClothing(String str, Set<Integer> set) {
        for (String str2 : str.split("\\.")) {
            String[] strArrSplit = str2.split("-");
            if (strArrSplit.length >= 2 && set.contains(Integer.valueOf(strArrSplit[1]))) {
                return true;
            }
        }
        return false;
    }

    public static String mergeFigures(String str, String str2, String[] strArr, String[] strArr2) {
        THashMap<String, String> figureBits = getFigureBits(str);
        THashMap<String, String> figureBits2 = getFigureBits(str2);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry entry : figureBits.entrySet()) {
            if (strArr == null || ArrayUtils.contains(strArr, entry.getKey())) {
                sb.append((String) entry.getKey()).append("-").append((String) entry.getValue()).append(".");
            }
        }
        for (Map.Entry entry2 : figureBits2.entrySet()) {
            if (strArr2 == null || ArrayUtils.contains(strArr2, entry2.getKey())) {
                sb.append((String) entry2.getKey()).append("-").append((String) entry2.getValue()).append(".");
            }
        }
        if (sb.toString().endsWith(".")) {
            sb = new StringBuilder(sb.substring(0, sb.length() - 1));
        }
        return sb.toString();
    }
}
