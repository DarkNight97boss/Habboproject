import { beforeEach, describe, expect, it } from 'vitest';
import { BinaryReader } from '../BinaryReader';
import { BinaryWriter } from '../BinaryWriter';

const concatBuffers = (...parts: ArrayBuffer[]): ArrayBuffer =>
{
    const total = parts.reduce((sum, part) => sum + part.byteLength, 0);
    const out = new Uint8Array(total);
    let offset = 0;

    for(const part of parts)
    {
        out.set(new Uint8Array(part), offset);
        offset += part.byteLength;
    }

    return out.buffer;
};

describe('BinaryReader / BinaryWriter', () =>
{
    let writer: BinaryWriter;

    beforeEach(() =>
    {
        writer = new BinaryWriter();
    });

    describe('byte round-trip', () =>
    {
        it('writes and reads a single byte', () =>
        {
            writer.writeByte(0x42);

            const reader = new BinaryReader(writer.getBuffer());

            expect(reader.readByte()).toBe(0x42);
            expect(reader.remaining()).toBe(0);
        });

        it('readByte returns a signed int8 (values above 127 wrap negative)', () =>
        {
            writer.writeByte(0xFF);

            const reader = new BinaryReader(writer.getBuffer());

            expect(reader.readByte()).toBe(-1);
        });

        it('writeByte chains', () =>
        {
            writer.writeByte(1).writeByte(2).writeByte(3);

            const reader = new BinaryReader(writer.getBuffer());

            expect(reader.readByte()).toBe(1);
            expect(reader.readByte()).toBe(2);
            expect(reader.readByte()).toBe(3);
        });
    });

    describe('short round-trip (16-bit big-endian)', () =>
    {
        it('writes and reads a positive short', () =>
        {
            writer.writeShort(0x1234);

            const reader = new BinaryReader(writer.getBuffer());

            expect(reader.readShort()).toBe(0x1234);
        });

        it('round-trips the int16 boundary values', () =>
        {
            writer.writeShort(32767).writeShort(-1);

            const reader = new BinaryReader(writer.getBuffer());

            expect(reader.readShort()).toBe(32767);
            expect(reader.readShort()).toBe(-1);
        });

        it('emits big-endian byte order', () =>
        {
            writer.writeShort(0x0102);

            const bytes = new Uint8Array(writer.getBuffer());

            expect(bytes[0]).toBe(0x01);
            expect(bytes[1]).toBe(0x02);
        });
    });

    describe('int round-trip (32-bit big-endian)', () =>
    {
        it('writes and reads a positive int', () =>
        {
            writer.writeInt(123456789);

            const reader = new BinaryReader(writer.getBuffer());

            expect(reader.readInt()).toBe(123456789);
        });

        it('round-trips the int32 boundaries (max / min / -1)', () =>
        {
            writer.writeInt(2147483647).writeInt(-2147483648).writeInt(-1);

            const reader = new BinaryReader(writer.getBuffer());

            expect(reader.readInt()).toBe(2147483647);
            expect(reader.readInt()).toBe(-2147483648);
            expect(reader.readInt()).toBe(-1);
        });

        it('emits big-endian byte order', () =>
        {
            writer.writeInt(0x01020304);

            const bytes = new Uint8Array(writer.getBuffer());

            expect(bytes[0]).toBe(0x01);
            expect(bytes[1]).toBe(0x02);
            expect(bytes[2]).toBe(0x03);
            expect(bytes[3]).toBe(0x04);
        });
    });

    describe('string round-trip', () =>
    {
        it('writes a length-prefixed string and decodes it back via readShort + readBytes', () =>
        {
            writer.writeString('hello');

            const reader = new BinaryReader(writer.getBuffer());
            const length = reader.readShort();

            expect(length).toBe(5);
            expect(reader.readBytes(length).toString()).toBe('hello');
        });

        it('round-trips UTF-8 multibyte characters with correct byte length', () =>
        {
            // 'café' = 5 bytes UTF-8 (c, a, 0xC3 0xA9, ASCII finale)
            writer.writeString('café');

            const reader = new BinaryReader(writer.getBuffer());
            const length = reader.readShort();

            expect(length).toBe(5);
            expect(reader.readBytes(length).toString()).toBe('café');
        });

        it('writeString with includeLength=false omits the length prefix', () =>
        {
            writer.writeString('xy', false);

            const buf = writer.getBuffer();

            expect(buf.byteLength).toBe(2);
            expect(new Uint8Array(buf)[0]).toBe(0x78); // 'x'
            expect(new Uint8Array(buf)[1]).toBe(0x79); // 'y'
        });

        it('round-trips the empty string', () =>
        {
            writer.writeString('');

            const reader = new BinaryReader(writer.getBuffer());

            expect(reader.readShort()).toBe(0);
            expect(reader.remaining()).toBe(0);
        });
    });

    describe('writeBytes', () =>
    {
        it('appends a number[] payload', () =>
        {
            writer.writeBytes([ 0x10, 0x20, 0x30 ]);

            const reader = new BinaryReader(writer.getBuffer());

            expect(reader.readByte()).toBe(0x10);
            expect(reader.readByte()).toBe(0x20);
            expect(reader.readByte()).toBe(0x30);
        });

        it('appends an ArrayBuffer payload', () =>
        {
            const payload = new Uint8Array([ 0xAA, 0xBB ]).buffer;

            writer.writeBytes(payload);

            const reader = new BinaryReader(writer.getBuffer());

            expect(reader.readByte()).toBe(-86); // 0xAA as int8
            expect(reader.readByte()).toBe(-69); // 0xBB as int8
        });
    });

    describe('readBytes slice', () =>
    {
        it('returns an independent reader over the requested slice', () =>
        {
            writer.writeInt(0xCAFEBABE | 0).writeInt(0xDEADBEEF | 0);

            const reader = new BinaryReader(writer.getBuffer());
            const sliced = reader.readBytes(4);

            // The slice's position is independent of the outer reader.
            expect(sliced.readInt()).toBe(0xCAFEBABE | 0);
            // The outer reader advanced by 4 and can still read the second int.
            expect(reader.readInt()).toBe(0xDEADBEEF | 0);
        });
    });

    describe('remaining accounting', () =>
    {
        it('decrements by the read size and reaches 0 at the end of the buffer', () =>
        {
            writer.writeByte(1).writeShort(2).writeInt(3);

            const reader = new BinaryReader(writer.getBuffer());

            expect(reader.remaining()).toBe(7);

            reader.readByte();
            expect(reader.remaining()).toBe(6);

            reader.readShort();
            expect(reader.remaining()).toBe(4);

            reader.readInt();
            expect(reader.remaining()).toBe(0);
        });
    });

    describe('float / double read', () =>
    {
        // BinaryWriter has no write counterparts for float/double — build the
        // buffer by hand via DataView and check the reader decodes correctly.

        it('readFloat decodes an IEEE-754 single-precision big-endian value', () =>
        {
            const buf = new ArrayBuffer(4);
            new DataView(buf).setFloat32(0, 3.5, false);

            const reader = new BinaryReader(buf);

            expect(reader.readFloat()).toBeCloseTo(3.5, 5);
            expect(reader.remaining()).toBe(0);
        });

        it('readDouble decodes an IEEE-754 double-precision big-endian value', () =>
        {
            const buf = new ArrayBuffer(8);
            new DataView(buf).setFloat64(0, Math.PI, false);

            const reader = new BinaryReader(buf);

            expect(reader.readDouble()).toBeCloseTo(Math.PI, 12);
            expect(reader.remaining()).toBe(0);
        });
    });

    describe('writer position getter/setter', () =>
    {
        it('reports the position after writes', () =>
        {
            writer.writeInt(0).writeShort(0);

            expect(writer.position).toBe(6);
        });

        it('position can be set explicitly (caller-managed reposition)', () =>
        {
            writer.writeInt(0);
            writer.position = 0;

            expect(writer.position).toBe(0);
        });
    });

    describe('typical packet round-trip (header + payload)', () =>
    {
        it('encodes and decodes a header + mixed payload (short + int + string)', () =>
        {
            const header = 1234;
            const userId = 99999;
            const username = 'simoleo';

            writer
                .writeShort(header)
                .writeInt(userId)
                .writeString(username);

            const reader = new BinaryReader(writer.getBuffer());

            expect(reader.readShort()).toBe(header);
            expect(reader.readInt()).toBe(userId);

            const nameLength = reader.readShort();
            const name = reader.readBytes(nameLength).toString();

            expect(name).toBe(username);
            expect(reader.remaining()).toBe(0);
        });

        it('concatenated buffers round-trip across independent writer instances', () =>
        {
            const a = new BinaryWriter();
            const b = new BinaryWriter();

            a.writeInt(11);
            b.writeInt(22);

            const reader = new BinaryReader(concatBuffers(a.getBuffer(), b.getBuffer()));

            expect(reader.readInt()).toBe(11);
            expect(reader.readInt()).toBe(22);
            expect(reader.remaining()).toBe(0);
        });
    });
});
