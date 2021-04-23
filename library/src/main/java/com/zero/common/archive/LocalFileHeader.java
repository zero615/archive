package com.zero.common.archive;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class LocalFileHeader {
    private static final byte[] EMPTY = new byte[0];
    public int extractVersion = 20;
    public int flag = 0; // general purpose flag
    public int method = ZipEntry.DEFLATED;  // compression method
    public long time = 0;
    public long crc; // crc-32 of entry data
    public long compressedSize;  // compressed count of entry data
    public long size = 0; // uncompressed count of entry data
    public byte[] name = EMPTY;    // optional name field data for entry
    public byte[] extra = EMPTY;  // optional extra field data for entry
    public long length;
    //内部使用
    long offset;


    LocalFileHeader(ZipEntry entry) throws IOException {
        RandomAccessFile accessFile = new RandomAccessFile(entry.zipFile.getRawFile(), "r");
        accessFile.seek(entry.getOffset());
        ByteBuffer byteBuffer = ByteBuffer.allocate(ZipConstants.LOCHDR);
        accessFile.readFully(byteBuffer.array());
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.getInt();
        offset = entry.getOffset();
        extractVersion = byteBuffer.getShort() & 0xFFFF;
        flag = byteBuffer.getShort() & 0xFFFF;
        method = byteBuffer.getShort() & 0xFFFF;
        time = byteBuffer.getInt() & 0xFFFFFFFFL;
        crc = byteBuffer.getInt() & 0xFFFFFFFFL;
        compressedSize = byteBuffer.getInt() & 0xFFFFFFFFL;
        size = byteBuffer.getInt() & 0xFFFFFFFFL;
        name = new byte[byteBuffer.getShort() & 0xFFFF];
        extra = new byte[byteBuffer.getShort() & 0xFFFF];
        accessFile.readFully(name);
        accessFile.readFully(extra);
        accessFile.close();
    }

    LocalFileHeader(String name) {
        this.name = name.getBytes();
    }

    public LocalFileHeader(LocalFileHeader header) {
        offset = header.offset;
        extractVersion = header.extractVersion;
        flag = header.flag;
        method = header.method;
        time = header.time;
        crc = header.crc;
        compressedSize = header.compressedSize;
        size = header.size;
        name = header.name;
        extra = header.extra;
    }

    boolean isZip64() {
        return size >= ZipConstants.ZIP64_MAGICVAL || compressedSize >= ZipConstants.ZIP64_MAGICVAL;
    }

    void setSize(long crc, long size, long compressedSize) {
        this.crc = crc;
        if (size >= ZipConstants.ZIP64_MAGICVAL || compressedSize >= ZipConstants.ZIP64_MAGICVAL) {
            this.size = ZipConstants.ZIP64_MAGICVAL;
            this.compressedSize = ZipConstants.ZIP64_MAGICVAL;
            byte[] bytes = CentralDirectoryHeader.getExceptValue(ZipConstants.ZIP64_EXTID, extra);
            byte[] target = new byte[bytes.length + 20];
            System.arraycopy(bytes, 0, target, 0, bytes.length);
            int len = bytes.length;
            Util.writeShort(target, len, ZipConstants.EXTID_ZIP64);
            len += 2;
            Util.writeShort(target, len, 16);
            len += 2;
            Util.writeLong(target, len, size);
            len += 8;
            Util.writeLong(target, len, compressedSize);
            extra = target;
        } else {
            this.size = size;
            this.compressedSize = compressedSize;
        }
    }

    public long getDataOffset() {
        return offset + ZipConstants.LOCHDR + name.length + extra.length;
    }
}
