package com.zero.common.archive;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DataDescriptor {
    private long crc; // crc-32 of entry data
    private long compressedSize;  // compressed count of entry data
    private long size = 0; // uncompressed count of entry data
    private boolean zip64;

    public DataDescriptor(long crc, long compressedSize, long size) {
        this.crc = crc;
        this.compressedSize = compressedSize;
        this.size = size;
        zip64 = size >= ZipConstants.ZIP64_MAGICVAL || compressedSize >= ZipConstants.ZIP64_MAGICVAL;

    }

    public DataDescriptor(ZipEntry entry, LocalFileHeader header) throws IOException {
        RandomAccessFile accessFile = new RandomAccessFile(entry.zipFile.getRawFile(), "r");
        long compressedSize = entry.getCompressedSize();
        long size = entry.getSize();
        accessFile.seek(header.getDataOffset() + compressedSize);
        if (size >= ZipConstants.ZIP64_MAGICVAL || compressedSize >= ZipConstants.ZIP64_MAGICVAL) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(24).order(ByteOrder.LITTLE_ENDIAN);
            byteBuffer.getInt();
            crc = byteBuffer.getInt() & 0xFFFFFFFFL;
            this.compressedSize = byteBuffer.getLong();
            this.size = byteBuffer.getLong();
            zip64 = true;
        } else {
            ByteBuffer byteBuffer = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);
            byteBuffer.getInt();
            crc = byteBuffer.getInt() & 0xFFFFFFFFL;
            this.compressedSize = byteBuffer.getInt() & 0xFFFFFFFFL;
            this.size = byteBuffer.getInt() & 0xFFFFFFFFL;
            zip64 = false;
        }
        accessFile.close();
    }

    public DataDescriptor(DataDescriptor dataDescriptor) {
        zip64 = dataDescriptor.zip64;
        crc = dataDescriptor.crc;
        size = dataDescriptor.size;
        compressedSize = dataDescriptor.compressedSize;
    }

    public boolean isZip64() {
        return zip64;
    }

    public long getDataDescriptorLength() {
        return zip64 ? 24 : 16;
    }

    public long getCrc() {
        return crc;
    }

    public long getSize() {
        return size;
    }

    public long getCompressedSize() {
        return compressedSize;
    }
}
