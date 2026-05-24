package com.eu.habbo.crypto;

import com.eu.habbo.crypto.exceptions.HabboCryptoException;
import com.eu.habbo.crypto.utils.BigIntegerUtils;
import com.eu.habbo.util.HexUtils;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/crypto/HabboDiffieHellman.class */
public class HabboDiffieHellman {
    private static final int DH_PRIMES_BIT_SIZE = 128;
    private static final int DH_KEY_BIT_SIZE = 128;
    private final HabboRSACrypto crypto;
    private BigInteger DHPrime;
    private BigInteger DHGenerator;
    private BigInteger DHPrivate;
    private BigInteger DHPublic;

    public HabboDiffieHellman(HabboRSACrypto habboRSACrypto) {
        this.crypto = habboRSACrypto;
        generateDHPrimes();
        generateDHKeys();
    }

    public BigInteger getDHPrime() {
        return this.DHPrime;
    }

    public BigInteger getDHGenerator() {
        return this.DHGenerator;
    }

    private void generateDHPrimes() {
        this.DHPrime = BigInteger.probablePrime(128, ThreadLocalRandom.current());
        this.DHGenerator = BigInteger.probablePrime(128, ThreadLocalRandom.current());
        if (this.DHGenerator.compareTo(this.DHPrime) > 0) {
            BigInteger bigInteger = this.DHPrime;
            this.DHPrime = this.DHGenerator;
            this.DHGenerator = bigInteger;
        }
    }

    private void generateDHKeys() {
        this.DHPrivate = BigInteger.probablePrime(128, ThreadLocalRandom.current());
        this.DHPublic = this.DHGenerator.modPow(this.DHPrivate, this.DHPrime);
    }

    private String encryptBigInteger(BigInteger bigInteger) throws HabboCryptoException {
        return HexUtils.toHex(this.crypto.Sign(bigInteger.toString(10).getBytes(StandardCharsets.UTF_8))).toLowerCase();
    }

    private BigInteger decryptBigInteger(String str) throws HabboCryptoException {
        return new BigInteger(new String(this.crypto.Decrypt(HexUtils.toBytes(str)), StandardCharsets.UTF_8), 10);
    }

    public String getPublicKey() throws HabboCryptoException {
        return encryptBigInteger(this.DHPublic);
    }

    public String getSignedPrime() throws HabboCryptoException {
        return encryptBigInteger(this.DHPrime);
    }

    public String getSignedGenerator() throws HabboCryptoException {
        return encryptBigInteger(this.DHGenerator);
    }

    public void doHandshake(String str, String str2) throws HabboCryptoException {
        this.DHPrime = decryptBigInteger(str);
        this.DHGenerator = decryptBigInteger(str2);
        if (this.DHPrime == null || this.DHGenerator == null) {
            throw new HabboCryptoException("DHPrime or DHGenerator was null.");
        }
        if (this.DHPrime.compareTo(BigInteger.valueOf(2L)) < 1) {
            throw new HabboCryptoException("Prime cannot be <= 2!\nPrime: " + this.DHPrime.toString());
        }
        if (this.DHGenerator.compareTo(this.DHPrime) > -1) {
            throw new HabboCryptoException("Generator cannot be >= Prime!\nPrime: " + this.DHPrime.toString() + "\nGenerator: " + this.DHGenerator.toString());
        }
        generateDHKeys();
    }

    public byte[] getSharedKey(String str) throws HabboCryptoException {
        return BigIntegerUtils.toUnsignedByteArray(decryptBigInteger(str).modPow(this.DHPrivate, this.DHPrime));
    }
}
