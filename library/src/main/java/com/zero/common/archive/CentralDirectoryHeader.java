package com.zero.common.archive;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class CentralDirectoryHeader {
    int madeVersion; //Version made by
    int extractVersion; //Version needed to extract (minimum)
    int flag;//General purpose bit flag
    int method;//Compression method
    long time;
    long crc; // crc-32 of entry data
    short diskNumberStart;
    short internalAttrs;
    int externalAttrs;
    byte[] name;
    byte[] extra;
    byte[] comment;
    long compressedSize;  // compressed count of entry data
    long size; // uncompressed count of entry data
    long offset;
    long actualCompressedSize;
    long actualSize;
    long actualOffset;
    boolean zip64;

    public CentralDirectoryHeader(ByteBuffer centralBuffer) {
        centralBuffer.getInt();
        madeVersion = centralBuffer.getShort() & 0xFFFF;
        extractVersion = centralBuffer.getShort() & 0xFFFF;
        flag = centralBuffer.getShort() & 0xFFFF;
        method = centralBuffer.getShort() & 0xFFFF;
        time = centralBuffer.getInt() & 0xFFFFFFFFL;
        crc = centralBuffer.getInt() & 0xFFFFFFFFL;
        compressedSize = centralBuffer.getInt() & 0xFFFFFFFFL;
        actualCompressedSize = compressedSize;
        size = centralBuffer.getInt() & 0xFFFFFFFFL;
        actualSize = size;
        name = new byte[centralBuffer.getShort() & 0xFFFF];
        extra = new byte[centralBuffer.getShort() & 0xFFFF];
        comment = new byte[centralBuffer.getShort() & 0xFFFF];
        diskNumberStart = centralBuffer.getShort();
        internalAttrs = centralBuffer.getShort();
        externalAttrs = centralBuffer.getInt();
        offset = centralBuffer.getInt() & 0xFFFFFFFFL;
        actualOffset = offset;
        centralBuffer.get(name);
        centralBuffer.get(extra);
        centralBuffer.get(comment);
        byte[] bytes = getValue(ZipConstants.EXTID_ZIP64, extra);
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        if (size >= ZipConstants.ZIP64_MAGICVAL) {
            actualSize = buffer.getLong();
            zip64 = true;
        }
        if (compressedSize >= ZipConstants.ZIP64_MAGICVAL) {
            actualCompressedSize = buffer.getLong();
            zip64 = true;
        }
        if (offset >= ZipConstants.ZIP64_MAGICVAL) {
            actualOffset = buffer.getLong();
            zip64 = true;
        }
    }

    public CentralDirectoryHeader(CentralDirectoryHeader header) {
        madeVersion = header.madeVersion;
        extractVersion = header.extractVersion;
        flag = header.flag;
        method = header.method;
        time = header.time;
        crc = header.crc;
        compressedSize = header.compressedSize;
        size = header.size;
        name = header.name;
        extra = header.extra;
        comment = header.comment;
        diskNumberStart = header.diskNumberStart;
        internalAttrs = header.internalAttrs;
        offset = header.offset;
        actualOffset = header.actualOffset;
        actualCompressedSize = header.actualCompressedSize;
        actualSize = header.actualSize;
        zip64 = header.zip64;
    }

    public CentralDirectoryHeader(LocalFileHeader header) {
        this(header, null);
    }

    public CentralDirectoryHeader(LocalFileHeader header, DataDescriptor dataDescriptor) {
        extractVersion = header.extractVersion;
        madeVersion = header.extractVersion;
        flag = header.flag;
        method = header.method;
        time = header.time;
        crc = header.crc;
        compressedSize = header.compressedSize;
        actualCompressedSize = compressedSize;
        size = header.size;
        actualSize = size;
        name = header.name;
        extra = header.extra;
        comment = new byte[0];
        diskNumberStart = 0;
        internalAttrs = 0;
        offset = header.offset;
        actualOffset = offset;
        zip64 = header.isZip64();
        checkDataDescriptor(dataDescriptor);
        if (header.offset >= ZipConstants.ZIP64_MAGICVAL) {
            offset = ZipConstants.ZIP64_MAGICVAL;
            actualOffset = header.offset;
            zip64 = true;
        } else {
            offset = header.offset;
            actualOffset = offset;
        }
        if (zip64) {
            expandZip64();
        }

    }

    public static byte[] getExceptValue(int exceptTag, byte[] extra) {
        ByteBuffer buffer = ByteBuffer.wrap(extra).order(ByteOrder.LITTLE_ENDIAN);
        int off = 0;
        int len = extra.length;
        boolean found;
        int exceptPosition = 0;
        int exceptLen = 0;
        while (off + 4 < len) {
            found = (buffer.getShort(off) & 0xFFFF) == exceptTag;
            int sz = (buffer.getShort(off + 2) & 0xFFFF);
            off += 4;
            if (off + sz > len)         // invalid data
                break;
            if (found) {
                exceptPosition = off;
                exceptLen = sz;
                break;
            }
            off += sz;
        }
        byte[] result = new byte[extra.length - exceptLen];
        System.arraycopy(extra, 0, result, 0, exceptPosition);
        System.arraycopy(extra, exceptPosition + exceptLen, result, exceptPosition, extra.length - exceptPosition - exceptLen);
        return result;
    }

    public static byte[] getValue(int tag, byte[] extra) {
        ByteBuffer buffer = ByteBuffer.wrap(extra).order(ByteOrder.LITTLE_ENDIAN);
        int off = 0;
        int len = extra.length;
        boolean found;
        while (off + 4 < len) {
            found = (buffer.getShort(off) & 0xFFFF) == tag;
            int sz = (buffer.getShort(off + 2) & 0xFFFF);
            off += 4;
            if (off + sz > len)         // invalid data
                break;
            if (found) {
                byte[] result = new byte[sz];
                buffer.get(result);
                return result;
            }
            off += sz;
        }
        return new byte[0];
    }

    private void expandZip64() {
        int zip64Len = 0;
        if (size == ZipConstants.ZIP64_MAGICVAL) {
            zip64Len += 8;
        }

        if (compressedSize == ZipConstants.ZIP64_MAGICVAL) {
            zip64Len += 8;
        }
        if (offset == ZipConstants.ZIP64_MAGICVAL) {
            zip64Len += 8;
        }
        byte[] bytes = getExceptValue(ZipConstants.EXTID_ZIP64, extra);
        byte[] target = new byte[bytes.length + zip64Len + 4];
        System.arraycopy(bytes, 0, target, 0, bytes.length);
        int len = bytes.length;
        Util.writeShort(target, len, ZipConstants.EXTID_ZIP64);
        len += 2;
        Util.writeShort(target, len, zip64Len);
        len += 2;
        if (size == ZipConstants.ZIP64_MAGICVAL) {
            Util.writeLong(target, len, actualSize);
            len += 8;
        }

        if (compressedSize == ZipConstants.ZIP64_MAGICVAL) {
            Util.writeLong(target, len, actualCompressedSize);
            len += 8;
        }
        if (offset == ZipConstants.ZIP64_MAGICVAL) {
            Util.writeLong(target, len, actualOffset);
        }
    }

    private void checkDataDescriptor(DataDescriptor dataDescriptor) {
        if (dataDescriptor != null) {
            crc = dataDescriptor.getCrc();
            size = dataDescriptor.getSize();
            actualSize = size;
            if (size >= ZipConstants.ZIP64_MAGICVAL) {
                size = ZipConstants.ZIP64_MAGICVAL;
                actualSize = size;
                zip64 = true;
            }
            compressedSize = dataDescriptor.getSize();
            actualCompressedSize = compressedSize;
            if (compressedSize >= ZipConstants.ZIP64_MAGICVAL) {
                compressedSize = ZipConstants.ZIP64_MAGICVAL;
                actualCompressedSize = compressedSize;
                zip64 = true;
            }
        }
    }
}
