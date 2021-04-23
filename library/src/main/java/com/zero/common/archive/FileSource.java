package com.zero.common.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileSource implements Source {
    File mFile;
    String mName;

    public FileSource(File file, String name) {
        mFile = file;
        mName = name;
    }

    @Override
    public InputStream getRawInputStream() throws IOException {
        return new FileInputStream(mFile);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(mFile);
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public long getCompressedSize() {
        return mFile.length();
    }

    @Override
    public long getSize() {
        return mFile.length();
    }

    @Override
    public long getDataOffset() {
        return 0;
    }

    @Override
    public int getMethod() {
        return ZipEntry.STORED;
    }

    @Override
    public boolean isDirectory() {
        return mFile.isDirectory();
    }

    @Override
    public File getFile() {
        return mFile;
    }

    @Override
    public long getCrc() {
        return 0;
    }

    @Override
    public int getLevel() {
        return 0;
    }

}