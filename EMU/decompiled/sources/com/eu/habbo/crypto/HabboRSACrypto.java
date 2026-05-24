package com.eu.habbo.crypto;

import com.eu.habbo.crypto.exceptions.HabboCryptoException;
import com.eu.habbo.crypto.utils.BigIntegerUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.lang3.mutable.MutableInt;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/crypto/HabboRSACrypto.class */
public class HabboRSACrypto {
    private final BigInteger e;
    private final BigInteger n;
    private final BigInteger d;
    private final int blockSize;

    public HabboRSACrypto(String str, String str2) {
        this.e = new BigInteger(str, 16);
        this.n = new BigInteger(str2, 16);
        this.d = null;
        this.blockSize = (this.n.bitLength() + 7) / 8;
    }

    public HabboRSACrypto(String str, String str2, String str3) {
        this.e = new BigInteger(str, 16);
        this.n = new BigInteger(str2, 16);
        this.d = new BigInteger(str3, 16);
        this.blockSize = (this.n.bitLength() + 7) / 8;
    }

    public byte[] Encrypt(byte[] bArr) throws HabboCryptoException {
        return DoEncrypt(bArr, true, 2);
    }

    public byte[] Decrypt(byte[] bArr) throws HabboCryptoException {
        return DoDecrypt(bArr, false, 2);
    }

    public byte[] Sign(byte[] bArr) throws HabboCryptoException {
        return DoEncrypt(bArr, false, 1);
    }

    public byte[] Verify(byte[] bArr) throws HabboCryptoException {
        return DoDecrypt(bArr, true, 1);
    }

    private BigInteger DoPublic(BigInteger bigInteger) {
        return bigInteger.modPow(this.e, this.n);
    }

    private BigInteger DoPrivate(BigInteger bigInteger) {
        return bigInteger.modPow(this.d, this.n);
    }

    private byte[] DoEncrypt(byte[] bArr, boolean z, int i) throws HabboCryptoException {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                int i2 = this.blockSize;
                int length = bArr.length;
                MutableInt mutableInt = new MutableInt(0);
                while (mutableInt.intValue() < length) {
                    BigInteger bigInteger = new BigInteger(Pkcs1Pad(bArr, mutableInt, length, i2, i));
                    BigInteger bigIntegerDoPublic = z ? DoPublic(bigInteger) : DoPrivate(bigInteger);
                    for (int iCeil = (int) (((double) i2) - Math.ceil(((double) bigIntegerDoPublic.bitLength()) / 8.0d)); iCeil > 0; iCeil--) {
                        byteArrayOutputStream.write(0);
                    }
                    byteArrayOutputStream.write(BigIntegerUtils.toUnsignedByteArray(bigIntegerDoPublic));
                }
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                byteArrayOutputStream.close();
                return byteArray;
            } finally {
            }
        } catch (IOException e) {
            throw new HabboCryptoException(e);
        }
    }

    private byte[] DoDecrypt(byte[] bArr, boolean z, int i) throws HabboCryptoException {
        if (bArr.length % this.blockSize != 0) {
            throw new HabboCryptoException("Decryption data was not in blocks of " + this.blockSize + " bytes, total " + bArr.length + ".");
        }
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                int length = bArr.length;
                int i2 = 0;
                while (i2 < length) {
                    byte[] bArr2 = new byte[this.blockSize];
                    System.arraycopy(bArr, i2, bArr2, 0, this.blockSize);
                    BigInteger bigInteger = new BigInteger(1, bArr2);
                    byte[] bArrPkcs1Unpad = Pkcs1Unpad((z ? DoPublic(bigInteger) : DoPrivate(bigInteger)).toByteArray(), this.blockSize, i);
                    i2 += this.blockSize;
                    byteArrayOutputStream.write(bArrPkcs1Unpad);
                }
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                byteArrayOutputStream.close();
                return byteArray;
            } finally {
            }
        } catch (IOException e) {
            throw new HabboCryptoException(e);
        }
    }

    private static byte[] Pkcs1Pad(byte[] bArr, MutableInt mutableInt, int i, int i2, int i3) {
        byte[] bArr2 = new byte[i2];
        int iIntValue = mutableInt.intValue();
        int iMin = Math.min(i, Math.min(bArr.length, (iIntValue + i2) - 11));
        mutableInt.setValue(iMin);
        int i4 = iMin - 1;
        while (i4 >= iIntValue && i2 > 11) {
            i2--;
            int i5 = i4;
            i4--;
            bArr2[i2] = bArr[i5];
        }
        int i6 = i2 - 1;
        bArr2[i6] = 0;
        if (i3 == 2) {
            while (i6 > 2) {
                i6--;
                bArr2[i6] = (byte) ThreadLocalRandom.current().nextInt(1, 256);
            }
        } else {
            while (i6 > 2) {
                i6--;
                bArr2[i6] = -1;
            }
        }
        int i7 = i6 - 1;
        bArr2[i7] = (byte) i3;
        bArr2[i7 - 1] = 0;
        return bArr2;
    }

    private static byte[] Pkcs1Unpad(byte[] bArr, int i, int i2) throws HabboCryptoException {
        byte[] bArr2 = new byte[i];
        int i3 = 0;
        int i4 = 0;
        while (i4 < bArr.length && bArr[i4] == 0) {
            i4++;
        }
        if (bArr.length - i4 != i - 1 || bArr[i4] != i2) {
            throw new HabboCryptoException("PKCS#1 unpad: i=" + i4 + ", expected b[i]==" + i2 + ", got b[i]=" + ((int) bArr[i4]));
        }
        int i5 = i4 + 1;
        while (bArr[i5] != 0) {
            i5++;
            if (i5 >= bArr.length) {
                throw new HabboCryptoException("PKCS#1 unpad: i=" + i5 + ", b[i-1]!=0 (=" + ((int) bArr[i5 - 1]) + ")");
            }
        }
        while (true) {
            i5++;
            if (i5 >= bArr.length) {
                byte[] bArr3 = new byte[i3];
                System.arraycopy(bArr2, 0, bArr3, 0, i3);
                return bArr3;
            }
            int i6 = i3;
            i3++;
            bArr2[i6] = bArr[i5];
        }
    }
}
