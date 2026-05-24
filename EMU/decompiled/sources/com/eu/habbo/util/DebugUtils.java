package com.eu.habbo.util;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/util/DebugUtils.class */
public class DebugUtils {
    public static StackTraceElement getCallerCallerStacktrace() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String className = null;
        for (int i = 1; i < stackTrace.length; i++) {
            StackTraceElement stackTraceElement = stackTrace[i];
            if (!stackTraceElement.getClassName().equals(DebugUtils.class.getName()) && stackTraceElement.getClassName().indexOf("java.lang.Thread") != 0) {
                if (className == null) {
                    className = stackTraceElement.getClassName();
                } else if (!className.equals(stackTraceElement.getClassName())) {
                    return stackTraceElement;
                }
            }
        }
        return null;
    }
}
