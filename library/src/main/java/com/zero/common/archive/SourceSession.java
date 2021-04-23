package com.zero.common.archive;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SourceSession extends Session {

    private Source mSource;
    private int mMethod;

    public SourceSession(Source source) {
        this(source, source.getMethod());
    }

    public SourceSession(Source source, int method) {
        mSource = source;
        mMethod = method;
    }

    @Override
    protected LocalFileHeader onCreateLocalFileHeader() throws IOException {
        LocalFileHeader header = null;
        header = new LocalFileHeader(getName());
        header.offset = getOffset();
        header.method = mMethod;
        header.setSize(mSource.getCrc(), mSource.getSize(), mSource.getCompressedSize());
        return header;
    }

    @Override
    protected DataDescriptor onCreateDataDescriptor(LocalFileHeader localFileHeader) throws IOException {
        return null;
    }

    @Override
    protected void onCommit() throws IOException {
        InputStream inputStream;
        if (mMethod == ZipEntry.STORED) {
            inputStream = mSource.getInputStream();
        } else {
            inputStream = mSource.getRawInputStream();
        }
        OutputStream outputStream = mArchive.getOutputStream();
        Util.copy(inputStream, outputStream);
        inputStream.close();
        outputStream.close();
    }

    @Override
    public String getName() {
        return mSource.getName();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {

    }
}
