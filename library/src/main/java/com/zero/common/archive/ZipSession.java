package com.zero.common.archive;

import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;


public class ZipSession extends Session {
    private final int level;
    private final String name;
    private int method;

    private CRC32OutputStream mOutputStream;
    private CRC32OutputStream mInnerOutputStream;

    public ZipSession(String name) {
        this(name, ZipEntry.DEFLATED, Deflater.DEFAULT_COMPRESSION);
    }

    public ZipSession(String name, int method) {
        this(name, method, Deflater.DEFAULT_COMPRESSION);
    }

    public ZipSession(String name, int method, int level) {
        this.name = name;
        this.level = level;
        this.method = method;
    }

    public void setMethod(int method) {
        this.method = method;
    }

    @Override
    protected LocalFileHeader onCreateLocalFileHeader() throws IOException {
        LocalFileHeader fileHeader = new LocalFileHeader(name);
        fileHeader.offset = getOffset();
        fileHeader.method = method;
        return fileHeader;
    }

    @Override
    protected void onPrepare() {
        if (method == ZipEntry.DEFLATED) {
            mInnerOutputStream = new CRC32OutputStream(mArchive.getOutputStream());
            mOutputStream = new CRC32OutputStream(new DeflaterOutputStream(mInnerOutputStream, new Deflater(level, true), 512, true));
        } else {
            mInnerOutputStream = new CRC32OutputStream(mArchive.getOutputStream());
            mOutputStream = mInnerOutputStream;
        }
    }

    @Override
    public void flush() throws IOException {
        super.flush();
        mOutputStream.flush();
        mInnerOutputStream.flush();
    }

    @Override
    public void close() throws IOException {
        super.close();

    }

    @Override
    protected DataDescriptor onCreateDataDescriptor(LocalFileHeader localFileHeader) throws IOException {
        return null;
    }

    @Override
    protected void onCommit() throws IOException {
        super.onCommit();
        mOutputStream.close();
        mInnerOutputStream.close();
        LocalFileHeader header = getLocalFileHeader();
        if (header.crc == 0) {
            header.setSize(mOutputStream.getCRC(), mOutputStream.getSize(), mInnerOutputStream.getSize());
            fixLocalFileHeader(header);
        }
    }

    private void fixLocalFileHeader(LocalFileHeader header) throws IOException {
        if (header.isZip64()) {
            //move file
            byte[] buffer = new byte[4 * 1024 * 1024];
            SeekInputStream source = new SeekInputStream(mArchive.getRawFile(), getDataOffset());
            long actualSize = mInnerOutputStream.getSize();
            int len = source.read(buffer);
            mArchive.seek(getOffset());
            onWriteLocalFileHeader(header);
            SeekOutputStream outputStream = mArchive.getOutputStream();
            outputStream.write(buffer, 0, len);
            actualSize -= len;
            while (actualSize > 0) {
                len = source.read(buffer);
                actualSize -= len;
                outputStream.write(buffer, 0, len);
            }
            source.close();
            outputStream.close();
        } else {
            mArchive.seek(getOffset());
            onWriteLocalFileHeader(header);
            mArchive.seek(getDataOffset() + mInnerOutputStream.getSize());
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        mOutputStream.write(b, off, len);
    }


    @Override
    public String getName() {
        return name;
    }
}
