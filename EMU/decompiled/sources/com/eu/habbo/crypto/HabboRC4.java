package com.eu.habbo.crypto;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/crypto/HabboRC4.class */
public class HabboRC4 {
    private int i;
    private int j;
    private final int[] table = new int[256];

    public HabboRC4(byte[] bArr) {
        int length = bArr.length;
        while (this.i < 256) {
            this.table[this.i] = this.i;
            this.i++;
        }
        this.i = 0;
        this.j = 0;
        while (this.i < 256) {
            this.j = ((this.j + this.table[this.i]) + (bArr[this.i % length] & 255)) % 256;
            swap(this.i, this.j);
            this.i++;
        }
        this.i = 0;
        this.j = 0;
    }

    private void swap(int i, int i2) {
        int i3 = this.table[i];
        this.table[i] = this.table[i2];
        this.table[i2] = i3;
    }

    public void parse(byte[] bArr) {
        for (int i = 0; i < bArr.length; i++) {
            this.i = (this.i + 1) % 256;
            this.j = (this.j + this.table[this.i]) % 256;
            swap(this.i, this.j);
            bArr[i] = (byte) ((bArr[i] & 255) ^ this.table[(this.table[this.i] + this.table[this.j]) % 256]);
        }
    }
}
