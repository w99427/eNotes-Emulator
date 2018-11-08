package io.enotes.emulator.utils;

import java.util.HashMap;
import java.util.Map;



public class TLVBox {
    private Map<Integer,byte[]> mObjects;
    private int mTotalBytes = 0;

    public TLVBox() {
        mObjects = new HashMap<>();
    }

    public static TLVBox parse(byte[] buffer, int offset, int length) throws Exception {
        TLVBox box = new TLVBox();
        int parsed = 0;
            while (parsed < length) {
                int type = buffer[parsed] & 0xff;
                parsed += 1;
                int size = buffer[parsed] & 0xff;
                parsed += 1;
                if (size == 0xff) {
                    byte[] sizeByte = new byte[2];
                    System.arraycopy(buffer, offset + parsed, sizeByte, 0, 2);
                    size = ByteUtil.byteArrayToInt(sizeByte);
                    parsed += 2;
                }
                byte[] value = new byte[size];
                System.arraycopy(buffer, offset + parsed, value, 0, size);
                box.putBytesValue(type, value);
                parsed += size;
            }
        return box;
    }

    public byte[] serialize() {
        int offset = 0;
        byte[] result = new byte[mTotalBytes];
        for (int key:mObjects.keySet()) {
            byte[] bytes = mObjects.get(key);
            byte[] type = new byte[]{(byte) key};
            System.arraycopy(type, 0, result, offset, type.length);
            offset += 1;
            int size = bytes.length;
            if (size < 0xff) {
                byte[] length = new byte[]{(byte) size};
                System.arraycopy(length, 0, result, offset, length.length);
                offset += 1;
            } else {
                byte[] length1 = new byte[3];
                byte[] bytes1 = ByteUtil.intToBytes(size);
                length1[0] = (byte) 0xff;
                length1[1] = bytes1[2];
                length1[2] = bytes1[3];
                System.arraycopy(length1, 0, result, offset, length1.length);
                offset += 3;
            }
            System.arraycopy(bytes, 0, result, offset, bytes.length);
            offset += bytes.length;
        }
        return result;
    }

    public void putByteValue(int type, byte value) {
        byte[] bytes = new byte[1];
        bytes[0] = value;
        putBytesValue(type, bytes);
    }

    public void putStringValue(int type, String value) {
        putBytesValue(type, value.getBytes());
    }

    public void putObjectValue(int type, TLVBox value) {
        putBytesValue(type, value.serialize());
    }

    public void putBytesValue(int type, byte[] value) {
        mObjects.put(type, value);
        if (value.length < 0xff) {
            mTotalBytes += value.length + 2;
        } else {
            mTotalBytes += value.length + 4;
        }
    }

    public String getStringValue(int type) {
        byte[] bytes = mObjects.get(type);
        if (bytes == null) {
            return null;
        }
        return ByteUtil.toHexString(bytes);
    }

    public byte[] getBytesValue(int type) {
        byte[] bytes = mObjects.get(type);
        return bytes;
    }
}
