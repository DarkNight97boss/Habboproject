package com.eu.habbo.core;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/core/CryptoConfig.class */
public class CryptoConfig {
    private final boolean enabled;
    private final String exponent;
    private final String modulus;
    private final String privateExponent;

    public CryptoConfig(boolean z, String str, String str2, String str3) {
        this.enabled = z;
        this.exponent = str;
        this.modulus = str2;
        this.privateExponent = str3;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public String getExponent() {
        return this.exponent;
    }

    public String getModulus() {
        return this.modulus;
    }

    public String getPrivateExponent() {
        return this.privateExponent;
    }
}
