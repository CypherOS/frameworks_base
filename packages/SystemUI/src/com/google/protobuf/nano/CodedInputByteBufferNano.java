package com.google.protobuf.nano;

import java.io.IOException;

public final class CodedInputByteBufferNano {
    private final byte[] buffer;
    private int bufferPos;
    private int bufferSize;
    private int bufferSizeAfterLimit;
    private int bufferStart;
    private int currentLimit = Integer.MAX_VALUE;
    private int lastTag;
    private int recursionDepth;
    private int recursionLimit = 64;
    private int sizeLimit = 67108864;

    public static CodedInputByteBufferNano newInstance(byte[] buf, int off, int len) {
        return new CodedInputByteBufferNano(buf, off, len);
    }

    public int readTag() throws IOException {
        if (isAtEnd()) {
            this.lastTag = 0;
            return 0;
        }
        this.lastTag = readRawVarint32();
        if (this.lastTag != 0) {
            return this.lastTag;
        }
        throw InvalidProtocolBufferNanoException.invalidTag();
    }

    public void checkLastTagWas(int value) throws InvalidProtocolBufferNanoException {
        if (this.lastTag != value) {
            throw InvalidProtocolBufferNanoException.invalidEndTag();
        }
    }

    public boolean skipField(int tag) throws IOException {
        switch (WireFormatNano.getTagWireType(tag)) {
            case 0:
                readInt32();
                return true;
            case 1:
                readRawLittleEndian64();
                return true;
            case 2:
                skipRawBytes(readRawVarint32());
                return true;
            case 3:
                skipMessage();
                checkLastTagWas(WireFormatNano.makeTag(WireFormatNano.getTagFieldNumber(tag), 4));
                return true;
            case 4:
                return false;
            case 5:
                readRawLittleEndian32();
                return true;
            default:
                throw InvalidProtocolBufferNanoException.invalidWireType();
        }
    }

    public void skipMessage() throws IOException {
        while (true) {
            int tag = readTag();
            if (tag == 0 || !skipField(tag)) {
                return;
            }
        }
    }

    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readRawLittleEndian32());
    }

    public long readUInt64() throws IOException {
        return readRawVarint64();
    }

    public long readInt64() throws IOException {
        return readRawVarint64();
    }

    public int readInt32() throws IOException {
        return readRawVarint32();
    }

    public boolean readBool() throws IOException {
        return readRawVarint32() != 0;
    }

    public String readString() throws IOException {
        int size = readRawVarint32();
        if (size > this.bufferSize - this.bufferPos || size <= 0) {
            return new String(readRawBytes(size), InternalNano.UTF_8);
        }
        String result = new String(this.buffer, this.bufferPos, size, InternalNano.UTF_8);
        this.bufferPos += size;
        return result;
    }

    public void readMessage(MessageNano msg) throws IOException {
        int length = readRawVarint32();
        if (this.recursionDepth < this.recursionLimit) {
            int oldLimit = pushLimit(length);
            this.recursionDepth++;
            msg.mergeFrom(this);
            checkLastTagWas(0);
            this.recursionDepth--;
            popLimit(oldLimit);
            return;
        }
        throw InvalidProtocolBufferNanoException.recursionLimitExceeded();
    }

    public byte[] readBytes() throws IOException {
        int size = readRawVarint32();
        if (size <= this.bufferSize - this.bufferPos && size > 0) {
            byte[] result = new byte[size];
            System.arraycopy(this.buffer, this.bufferPos, result, 0, size);
            this.bufferPos += size;
            return result;
        } else if (size == 0) {
            return WireFormatNano.EMPTY_BYTES;
        } else {
            return readRawBytes(size);
        }
    }

    public int readUInt32() throws IOException {
        return readRawVarint32();
    }

    public int readEnum() throws IOException {
        return readRawVarint32();
    }

    public int readRawVarint32() throws IOException {
        byte tmp = readRawByte();
        if (tmp >= (byte) 0) {
            return tmp;
        }
        int result = tmp & 127;
        byte readRawByte = readRawByte();
        tmp = readRawByte;
        if (readRawByte >= (byte) 0) {
            result |= tmp << 7;
        } else {
            result |= (tmp & 127) << 7;
            readRawByte = readRawByte();
            tmp = readRawByte;
            if (readRawByte >= (byte) 0) {
                result |= tmp << 14;
            } else {
                result |= (tmp & 127) << 14;
                readRawByte = readRawByte();
                tmp = readRawByte;
                if (readRawByte >= (byte) 0) {
                    result |= tmp << 21;
                } else {
                    result |= (tmp & 127) << 21;
                    readRawByte = readRawByte();
                    result |= readRawByte << 28;
                    if (readRawByte < (byte) 0) {
                        for (int i = 0; i < 5; i++) {
                            if (readRawByte() >= (byte) 0) {
                                return result;
                            }
                        }
                        throw InvalidProtocolBufferNanoException.malformedVarint();
                    }
                }
            }
        }
        return result;
    }

    public long readRawVarint64() throws IOException {
        long result = 0;
        for (int shift = 0; shift < 64; shift += 7) {
            byte b = readRawByte();
            result |= ((long) (b & 127)) << shift;
            if ((b & 128) == 0) {
                return result;
            }
        }
        throw InvalidProtocolBufferNanoException.malformedVarint();
    }

    public int readRawLittleEndian32() throws IOException {
        return (((readRawByte() & 255) | ((readRawByte() & 255) << 8)) | ((readRawByte() & 255) << 16)) | ((readRawByte() & 255) << 24);
    }

    public long readRawLittleEndian64() throws IOException {
        return (((((((((long) readRawByte()) & 255) | ((((long) readRawByte()) & 255) << 8)) | ((((long) readRawByte()) & 255) << 16)) | ((((long) readRawByte()) & 255) << 24)) | ((((long) readRawByte()) & 255) << 32)) | ((((long) readRawByte()) & 255) << 40)) | ((((long) readRawByte()) & 255) << 48)) | ((255 & ((long) readRawByte())) << 56);
    }

    private CodedInputByteBufferNano(byte[] buffer, int off, int len) {
        this.buffer = buffer;
        this.bufferStart = off;
        this.bufferSize = off + len;
        this.bufferPos = off;
    }

    public int pushLimit(int byteLimit) throws InvalidProtocolBufferNanoException {
        if (byteLimit >= 0) {
            byteLimit += this.bufferPos;
            int oldLimit = this.currentLimit;
            if (byteLimit <= oldLimit) {
                this.currentLimit = byteLimit;
                recomputeBufferSizeAfterLimit();
                return oldLimit;
            }
            throw InvalidProtocolBufferNanoException.truncatedMessage();
        }
        throw InvalidProtocolBufferNanoException.negativeSize();
    }

    private void recomputeBufferSizeAfterLimit() {
        this.bufferSize += this.bufferSizeAfterLimit;
        int bufferEnd = this.bufferSize;
        if (bufferEnd > this.currentLimit) {
            this.bufferSizeAfterLimit = bufferEnd - this.currentLimit;
            this.bufferSize -= this.bufferSizeAfterLimit;
            return;
        }
        this.bufferSizeAfterLimit = 0;
    }

    public void popLimit(int oldLimit) {
        this.currentLimit = oldLimit;
        recomputeBufferSizeAfterLimit();
    }

    public boolean isAtEnd() {
        return this.bufferPos == this.bufferSize;
    }

    public int getPosition() {
        return this.bufferPos - this.bufferStart;
    }

    public void rewindToPosition(int position) {
        StringBuilder stringBuilder;
        if (position > this.bufferPos - this.bufferStart) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("Position ");
            stringBuilder.append(position);
            stringBuilder.append(" is beyond current ");
            stringBuilder.append(this.bufferPos - this.bufferStart);
            throw new IllegalArgumentException(stringBuilder.toString());
        } else if (position >= 0) {
            this.bufferPos = this.bufferStart + position;
        } else {
            stringBuilder = new StringBuilder();
            stringBuilder.append("Bad position ");
            stringBuilder.append(position);
            throw new IllegalArgumentException(stringBuilder.toString());
        }
    }

    public byte readRawByte() throws IOException {
        if (this.bufferPos != this.bufferSize) {
            byte[] bArr = this.buffer;
            int i = this.bufferPos;
            this.bufferPos = i + 1;
            return bArr[i];
        }
        throw InvalidProtocolBufferNanoException.truncatedMessage();
    }

    public byte[] readRawBytes(int size) throws IOException {
        if (size < 0) {
            throw InvalidProtocolBufferNanoException.negativeSize();
        } else if (this.bufferPos + size > this.currentLimit) {
            skipRawBytes(this.currentLimit - this.bufferPos);
            throw InvalidProtocolBufferNanoException.truncatedMessage();
        } else if (size <= this.bufferSize - this.bufferPos) {
            byte[] bytes = new byte[size];
            System.arraycopy(this.buffer, this.bufferPos, bytes, 0, size);
            this.bufferPos += size;
            return bytes;
        } else {
            throw InvalidProtocolBufferNanoException.truncatedMessage();
        }
    }

    public void skipRawBytes(int size) throws IOException {
        if (size < 0) {
            throw InvalidProtocolBufferNanoException.negativeSize();
        } else if (this.bufferPos + size > this.currentLimit) {
            skipRawBytes(this.currentLimit - this.bufferPos);
            throw InvalidProtocolBufferNanoException.truncatedMessage();
        } else if (size <= this.bufferSize - this.bufferPos) {
            this.bufferPos += size;
        } else {
            throw InvalidProtocolBufferNanoException.truncatedMessage();
        }
    }
}
