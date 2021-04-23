package com.zero.common.archive;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipOutputStream;

public class ZipFile implements ZipConstants {

    private static final boolean LOGV = false;
    private static final String LOG_TAG = "zip";
    private final File mFile;
    private Map<String, ZipEntry> entries;
    private EndRecord mEndRecord;

    public ZipFile(File file) throws IOException {
        mFile = file;
        RandomAccessFile accessFile = null;
        try {
            file.getParentFile().mkdirs();
            accessFile = new RandomAccessFile(file, "r");
            mEndRecord = parseEndRecord(accessFile, false);
        } catch (IOException e) {
            Util.closeQuietly(accessFile);
            throw e;
        }
    }


    public static EndRecord parseEndRecord(ByteBuffer byteBuffer, boolean strictMode) {
        int endRecordIndex;
        EndRecord record = null;
        for (endRecordIndex = byteBuffer.limit() - ENDHDR; endRecordIndex >= 0; endRecordIndex--) {
            if (byteBuffer.get(endRecordIndex) == 0x50 && byteBuffer.getInt(endRecordIndex) == ENDSIG) {

                if (LOGV) {
                    System.out.println(LOG_TAG + "+++ Found EndRecord at index: " + endRecordIndex);
                }
                byteBuffer.position(endRecordIndex + 4);
                record = new EndRecord(byteBuffer);
                break;
            }
        }
        if (record == null) {
            throw new RuntimeException("error format ");
        }
        boolean zip64 = false;
        //do zip 64 check
        if (record.mDirSize >= ZIP64_MAGICVAL || record.mOffset >= ZIP64_MAGICVAL || record.mNumEntries >= ZIP64_MAGICCOUNT) {
            zip64 = true;
        }
        if (zip64) {
            endRecordIndex = endRecordIndex - ZIP64_LOCHDR;
        }
        if (zip64 && byteBuffer.get(endRecordIndex) == 0x50 && byteBuffer.getInt(endRecordIndex) == ZIP64_LOCSIG) {
            if (LOGV) {
                System.out.println(LOG_TAG + "+++ Found EndRecord at index: " + endRecordIndex);
            }

            ZipOutputStream outputStream;
            long offset = byteBuffer.get(endRecordIndex + ZIP64_LOCOFF);
            if (!strictMode) {
                endRecordIndex = endRecordIndex - ZIP64_ENDHDR;
                if (byteBuffer.get(endRecordIndex) == 0x50 && byteBuffer.getInt(endRecordIndex) == ZIP64_ENDSIG) {
                    //zip64 end of central directory record
                    byteBuffer.position(endRecordIndex + 12 + 4);
                    record.mDiskNumber = byteBuffer.getInt();
                    record.mDiskCentralStart = byteBuffer.getInt();
                    record.mDiskNumberStart = byteBuffer.getLong();
                    record.mNumEntries = byteBuffer.getLong();
                    record.mDirSize = byteBuffer.getLong();
                    record.mOffset = byteBuffer.getLong();
                }
            } else {
                //todo 基于offset找到endEntry
            }
        }
        record.mSize = byteBuffer.limit() - endRecordIndex;
        return record;
    }

    private static EndRecord parseEndRecord(RandomAccessFile randomAccessFile, boolean strictMode) throws IOException {
        long fileLength = randomAccessFile.length();
        if (fileLength < ENDHDR) {
            throw new IOException();
        }
        long readAmount = MAX_END_SEARCH;
        if (readAmount > fileLength)
            readAmount = fileLength;
        long searchStart = fileLength - readAmount;
        randomAccessFile.seek(searchStart);
        ByteBuffer byteBuffer = ByteBuffer.allocate((int) readAmount);
        byte[] buffer = byteBuffer.array();
        randomAccessFile.readFully(buffer);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return parseEndRecord(byteBuffer, false);
    }

    public EndRecord getEndRecord() {
        return mEndRecord;
    }

    public Map<String, ZipEntry> entries() throws IOException {
        if (entries == null) {
            entries = parseZipEntries();
        }
        return entries;
    }

    public ZipEntry getEntry(String name) throws IOException {
        return entries().get(name);
    }

    public InputStream getInputStream(ZipEntry entry) throws IOException {
        InputStream stream = new FileInputStream(getRawFile());
        stream.skip(entry.getDataOffset());
        stream = new BoundInputStream(stream, entry.getCompressedSize());
        if (entry.getMethod() == ZipEntry.STORED) {
            return stream;
        } else {
            return new BufferedInputStream(new InflaterInputStream(stream, new Inflater(true)));
        }
    }

    public InputStream getRawInputStream(ZipEntry entry) throws IOException {
        return getRawInputStream(entry.getDataOffset(), entry.getCompressedSize());
    }

    private Map<String, ZipEntry> parseZipEntries() throws IOException {
        final EndRecord record = mEndRecord;
        RandomAccessFile accessFile = null;
        try {
            accessFile = new RandomAccessFile(mFile, "r");
            FileChannel channel = accessFile.getChannel();
            accessFile.seek(record.mOffset);
            ByteBuffer centralBuffer = ByteBuffer.allocateDirect((int) record.mDirSize).order(ByteOrder.LITTLE_ENDIAN);
            channel.read(centralBuffer);
            channel.close();
            centralBuffer.flip();
            final int number = (int) mEndRecord.mNumEntries;
            Map<String, ZipEntry> map = new LinkedHashMap<>(number);
            for (int i = 0; i < number; i++) {
                ZipEntry entry = new ZipEntry(this, centralBuffer);
                map.put(entry.getName(), entry);
            }
            return map;
        } finally {
            Util.closeQuietly(accessFile);
        }

    }


    public InputStream getRawInputStream(long offset, long length) throws IOException {
        InputStream inputStream = new FileInputStream(getRawFile());
        inputStream.skip(offset);
        return new BoundInputStream(inputStream, length);
    }

    public File getRawFile() {
        return mFile;
    }
}
