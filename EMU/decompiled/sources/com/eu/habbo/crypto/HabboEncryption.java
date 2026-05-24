package com.eu.habbo.crypto;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/crypto/HabboEncryption.class */
public class HabboEncryption {
    private final HabboRSACrypto crypto;
    private final HabboDiffieHellman diffie;

    public HabboEncryption(String str, String str2, String str3) {
        this.crypto = new HabboRSACrypto(str, str2, str3);
        this.diffie = new HabboDiffieHellman(this.crypto);
    }

    public HabboRSACrypto getCrypto() {
        return this.crypto;
    }

    public HabboDiffieHellman getDiffie() {
        return this.diffie;
    }
}
