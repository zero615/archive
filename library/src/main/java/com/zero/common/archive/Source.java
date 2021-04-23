package com.zero.common.archive;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface Source {
    int LEVEL_UNKNOWN = 11;

    InputStream getRawInputStream() throws IOException;

    InputStream getInputStream() throws IOException;

    String getName();

    long getCompressedSize();

    long getSize();

    long getDataOffset();

    int getMethod();

    boolean isDirectory();

    File getFile();

    long getCrc();

    int getLevel();
}