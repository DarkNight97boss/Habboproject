package com.eu.habbo.messages.incoming.wired;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/wired/WiredSaveException.class */
public class WiredSaveException extends Exception {
    private final String message;

    public WiredSaveException(String str) {
        this.message = str;
    }

    @Override // java.lang.Throwable
    public String getMessage() {
        return this.message;
    }
}
