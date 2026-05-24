package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/RCONMessage.class */
public abstract class RCONMessage<T> {
    public static final int STATUS_OK = 0;
    public static final int STATUS_ERROR = 1;
    public static final int HABBO_NOT_FOUND = 2;
    public static final int ROOM_NOT_FOUND = 3;
    public static final int SYSTEM_ERROR = 4;
    public final Class<T> type;
    public int status = 0;
    public String message = Emulator.PREVIEW;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/RCONMessage$RCONMessageSerializer.class */
    public static class RCONMessageSerializer implements JsonSerializer<RCONMessage> {
        public JsonElement serialize(RCONMessage rCONMessage, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("status", new JsonPrimitive(Integer.valueOf(rCONMessage.status)));
            jsonObject.add("message", new JsonPrimitive(rCONMessage.message));
            return jsonObject;
        }
    }

    public RCONMessage(Class<T> cls) {
        this.type = cls;
    }

    public abstract void handle(Gson gson, T t);
}
