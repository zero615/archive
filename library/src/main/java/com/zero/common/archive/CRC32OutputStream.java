package com.zero.common.archive;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;

public class CRC32OutputStream extends BufferedOutputStream {
    private CRC32 mCRC32;
    private long mSize;

    public CRC32OutputStream(OutputStream out) {
        super(out);
        mCRC32 = new CRC32();
    }

    @Override
    public synchronized void write(int b) throws IOException {
        super.write(b);
        mCRC32.update(b);
        mSize += 1;
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        super.write(b, off, len);
        mCRC32.update(b, off, len);
        mSize += len;
    }

    public long getCRC() {
        return mCRC32.getValue();
    }

    public long getSize() {
        return mSize;
    }
}