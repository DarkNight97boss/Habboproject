package com.eu.habbo.crypto;

import com.eu.habbo.crypto.exceptions.HabboCryptoException;
import com.eu.habbo.crypto.utils.BigIntegerUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.lang3.mutable.MutableInt;

public class HabboRSACrypto {
   private final BigInteger e;
   private final BigInteger n;
   private final BigInteger d;
   private final int blockSize;

   public HabboRSACrypto(String e, String n) {
      this.e = new BigInteger(e, 16);
      this.n = new BigInteger(n, 16);
      this.d = null;
      this.blockSize = (this.n.bitLength() + 7) / 8;
   }

   public HabboRSACrypto(String e, String n, String d) {
      this.e = new BigInteger(e, 16);
      this.n = new BigInteger(n, 16);
      this.d = new BigInteger(d, 16);
      this.blockSize = (this.n.bitLength() + 7) / 8;
   }

   public byte[] Encrypt(byte[] data) throws HabboCryptoException {
      return this.DoEncrypt(data, true, 2);
   }

   public byte[] Decrypt(byte[] data) throws HabboCryptoException {
      return this.DoDecrypt(data, false, 2);
   }

   public byte[] Sign(byte[] data) throws HabboCryptoException {
      return this.DoEncrypt(data, false, 1);
   }

   public byte[] Verify(byte[] data) throws HabboCryptoException {
      return this.DoDecrypt(data, true, 1);
   }

   private BigInteger DoPublic(BigInteger x) {
      return x.modPow(this.e, this.n);
   }

   private BigInteger DoPrivate(BigInteger x) {
      return x.modPow(this.d, this.n);
   }

   private byte[] DoEncrypt(byte[] data, boolean isPublic, int padType) throws HabboCryptoException {
      try {
         ByteArrayOutputStream dst = new ByteArrayOutputStream();

         byte[] padded;
         try {
            int bl = this.blockSize;
            int end = data.length;
            MutableInt pos = new MutableInt(0);

            while (pos.intValue() < end) {
               padded = Pkcs1Pad(data, pos, end, bl, padType);
               BigInteger block = new BigInteger(padded);
               BigInteger chunk = isPublic ? this.DoPublic(block) : this.DoPrivate(block);

               for (int b = (int)(bl - Math.ceil(chunk.bitLength() / 8.0)); b > 0; b--) {
                  dst.write(0);
               }

               dst.write(BigIntegerUtils.toUnsignedByteArray(chunk));
            }

            padded = dst.toByteArray();
         } catch (Throwable var13) {
            try {
               dst.close();
            } catch (Throwable var12) {
               var13.addSuppressed(var12);
            }

            throw var13;
         }

         dst.close();
         return padded;
      } catch (IOException e) {
         throw new HabboCryptoException(e);
      }
   }

   private byte[] DoDecrypt(byte[] data, boolean isPublic, int padType) throws HabboCryptoException {
      if (data.length % this.blockSize != 0) {
         throw new HabboCryptoException("Decryption data was not in blocks of " + this.blockSize + " bytes, total " + data.length + ".");
      }

      try {
         ByteArrayOutputStream dst = new ByteArrayOutputStream();

         byte[] blockData;
         try {
            int end = data.length;
            int pos = 0;

            while (pos < end) {
               blockData = new byte[this.blockSize];
               System.arraycopy(data, pos, blockData, 0, this.blockSize);
               BigInteger block = new BigInteger(1, blockData);
               BigInteger chunk = isPublic ? this.DoPublic(block) : this.DoPrivate(block);
               byte[] unpadded = Pkcs1Unpad(chunk.toByteArray(), this.blockSize, padType);
               pos += this.blockSize;
               dst.write(unpadded);
            }

            blockData = dst.toByteArray();
         } catch (Throwable var12) {
            try {
               dst.close();
            } catch (Throwable var11) {
               var12.addSuppressed(var11);
            }

            throw var12;
         }

         dst.close();
         return blockData;
      } catch (IOException e) {
         throw new HabboCryptoException(e);
      }
   }

   private static byte[] Pkcs1Pad(byte[] src, MutableInt pos, int end, int n, int padType) {
      byte[] result = new byte[n];
      int p = pos.intValue();
      end = Math.min(end, Math.min(src.length, p + n - 11));
      pos.setValue(end);

      for (int i = end - 1; i >= p && n > 11; result[n] = src[i--]) {
         n--;
      }

      result[--n] = 0;
      if (padType == 2) {
         while (n > 2) {
            result[--n] = (byte)ThreadLocalRandom.current().nextInt(1, 256);
         }
      } else {
         while (n > 2) {
            result[--n] = -1;
         }
      }

      result[--n] = (byte)padType;
      result[--n] = 0;
      return result;
   }

   private static byte[] Pkcs1Unpad(byte[] b, int n, int padType) throws HabboCryptoException {
      byte[] result = new byte[n];
      int resultPos = 0;
      int i = 0;

      while (i < b.length && b[i] == 0) {
         i++;
      }

      if (b.length - i == n - 1 && b[i] == padType) {
         i++;

         while (b[i] != 0) {
            if (++i >= b.length) {
               throw new HabboCryptoException("PKCS#1 unpad: i=" + i + ", b[i-1]!=0 (=" + b[i - 1] + ")");
            }
         }

         while (++i < b.length) {
            result[resultPos++] = b[i];
         }

         byte[] resultCopy = new byte[resultPos];
         System.arraycopy(result, 0, resultCopy, 0, resultPos);
         return resultCopy;
      } else {
         throw new HabboCryptoException("PKCS#1 unpad: i=" + i + ", expected b[i]==" + padType + ", got b[i]=" + b[i]);
      }
   }
}
