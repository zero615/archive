package com.zero.common.archive;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class SourceWrapper implements Source {
    private Source source;

    public SourceWrapper(Source source) {
        this.source = source;
    }

    @Override
    public InputStream getRawInputStream() throws IOException {
        return source.getRawInputStream();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return source.getInputStream();
    }

    @Override
    public String getName() {
        return source.getName();
    }

    @Override
    public long getCompressedSize() {
        return source.getCompressedSize();
    }

    @Override
    public long getSize() {
        return source.getSize();
    }

    @Override
    public long getDataOffset() {
        return source.getDataOffset();
    }

    @Override
    public int getMethod() {
        return source.getMethod();
    }

    @Override
    public boolean isDirectory() {
        return source.isDirectory();
    }

    @Override
    public File getFile() {
        return source.getFile();
    }

    @Override
    public long getCrc() {
        return source.getCrc();
    }

    @Override
    public int getLevel() {
        return source.getLevel();
    }
}
