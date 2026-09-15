package com.eu.habbo.util;

/**
 * Neutralizza il markup nei testi mostrati al client dentro alert/notifiche che
 * il client renderizza come HTML ristretto (&lt;b&gt;, &lt;br/&gt;, link).
 * Stesso approccio gia' usato per il motto in UserInfoCommand: le parentesi
 * angolari diventano quadre, il testo resta leggibile ma non e' piu' markup.
 */
public final class TextSanitizer {
    private TextSanitizer() {
    }

    public static String noMarkup(String text) {
        if (text == null) return "";
        return text.replace("<", "[").replace(">", "]");
    }
}
