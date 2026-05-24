package com.eu.habbo.messages;

import com.eu.habbo.messages.incoming.Incoming;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/PacketNames.class */
public class PacketNames {
    private static final Logger LOGGER = LoggerFactory.getLogger(PacketNames.class);
    private final HashMap<Integer, String> incoming = new HashMap<>();
    private final HashMap<Integer, String> outgoing = new HashMap<>();

    public void initialize() {
        getNames(Incoming.class, this.incoming);
        getNames(Outgoing.class, this.outgoing);
    }

    public String getIncomingName(int i) {
        return this.incoming.getOrDefault(Integer.valueOf(i), "Unknown");
    }

    public String getOutgoingName(int i) {
        return this.outgoing.getOrDefault(Integer.valueOf(i), "Unknown");
    }

    private static void getNames(Class<?> cls, HashMap<Integer, String> map) {
        for (Field field : cls.getFields()) {
            int modifiers = field.getModifiers();
            if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers) && field.getType() == Integer.TYPE) {
                try {
                    int i = field.getInt(null);
                    if (i > 0) {
                        if (map.containsKey(Integer.valueOf(i))) {
                            LOGGER.warn("Duplicate packet id found {} for {}.", Integer.valueOf(i), cls.getSimpleName());
                        } else {
                            map.put(Integer.valueOf(i), field.getName());
                        }
                    }
                } catch (IllegalAccessException e) {
                    LOGGER.error("Failed to read field integer.", e);
                }
            }
        }
    }
}
