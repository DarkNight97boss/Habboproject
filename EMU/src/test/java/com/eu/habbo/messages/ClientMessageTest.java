package com.eu.habbo.messages;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Path critico: il parser dei pacchetti client e' la prima linea contro
 * l'amplificazione di memoria (un client puo' dichiarare stringhe enormi).
 * Questi test fissano il contratto di readString(maxBytes): tronca, consuma
 * comunque i byte dichiarati (il pacchetto resta allineato) e non lancia mai.
 */
class ClientMessageTest {

    private static ByteBuf stringThenInt(String s, int declaredLength, int next) {
        ByteBuf b = Unpooled.buffer();
        b.writeShort(declaredLength);
        b.writeBytes(s.getBytes(StandardCharsets.UTF_8));
        b.writeInt(next);
        return b;
    }

    @Test
    void readStringTroncaAlMaxMaConsumaTuttiIByteDichiarati() {
        ClientMessage m = new ClientMessage(1, stringThenInt("hello", 5, 42));
        assertEquals("he", m.readString(2), "deve restituire solo i primi maxBytes");
        assertEquals(42, m.readInt(), "il campo successivo deve restare allineato");
    }

    @Test
    void readStringClampaLaLunghezzaDichiarataAiByteDisponibili() {
        ByteBuf b = Unpooled.buffer();
        b.writeShort(500);                       // dichiara 500 byte...
        b.writeBytes("abc".getBytes(StandardCharsets.UTF_8)); // ...ne fornisce 3
        ClientMessage m = new ClientMessage(1, b);
        assertEquals("abc", m.readString(64), "niente over-allocation: legge solo cio' che c'e'");
    }

    @Test
    void readStringConMaxZeroRestituisceVuotoSenzaRompereIlPacchetto() {
        ClientMessage m = new ClientMessage(1, stringThenInt("junk", 4, 7));
        assertEquals("", m.readString(0));
        assertEquals(7, m.readInt());
    }

    @Test
    void readStringSuBufferVuotoNonLancia() {
        ClientMessage m = new ClientMessage(1, Unpooled.EMPTY_BUFFER);
        assertEquals("", m.readString(10));
        assertEquals(0, m.readInt());
    }
}
