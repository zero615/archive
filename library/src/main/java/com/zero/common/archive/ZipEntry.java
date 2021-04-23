package com.zero.common.archive;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * 没有解析loc
 */
public class ZipEntry implements ZipConstants, Source, Comparable<ZipEntry> {
    public static final int STORED = 0;
    public static final int DEFLATED = 8;
    ZipFile zipFile;
    LocalFileHeader localFileHeader;
    DataDescriptor dataDescriptor;
    CentralDirectoryHeader centralDirectoryHeader;
    String decodeName;

    public ZipEntry(ZipEntry zipEntry) {
        zipFile = zipEntry.zipFile;
        localFileHeader = zipEntry.localFileHeader;
        centralDirectoryHeader = zipEntry.centralDirectoryHeader;
        decodeName = zipEntry.decodeName;
    }

    ZipEntry(ZipFile zipFile, ByteBuffer centralBuffer) {
        this.zipFile = zipFile;
        centralDirectoryHeader = new CentralDirectoryHeader(centralBuffer);
    }

    public boolean isDirectory() {
        return getName().endsWith("/");
    }

    @Override
    public File getFile() {
        return zipFile.getRawFile();
    }

    @Override
    public long getDataOffset() {
        return getLocalFileHeader().getDataOffset();
    }

    @Override
    public InputStream getRawInputStream() throws IOException {
        return zipFile.getRawInputStream(this);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return zipFile.getInputStream(this);
    }

    synchronized LocalFileHeader getLocalFileHeader() {
        if (localFileHeader == null) {
            try {
                localFileHeader = new LocalFileHeader(this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return localFileHeader;
    }

    synchronized DataDescriptor getDataDescriptor() throws IOException {
        if ((getFlag() & 8) == 8 && dataDescriptor == null) {
            LocalFileHeader header = getLocalFileHeader();
            dataDescriptor = new DataDescriptor(this, header);
        }
        return dataDescriptor;
    }

    @Override
    public int getMethod() {
        return centralDirectoryHeader.method;
    }

    @Override
    public String getName() {
        if (decodeName == null) {
            decodeName = new String(centralDirectoryHeader.name);
        }
        return decodeName;
    }

    public long getOffset() {
        return centralDirectoryHeader.actualOffset;
    }

    @Override
    public long getCompressedSize() {
        return centralDirectoryHeader.actualCompressedSize;
    }


    @Override
    public long getSize() {
        return centralDirectoryHeader.actualSize;
    }

    public int getFlag() {
        return centralDirectoryHeader.flag;
    }

    @Override
    public long getCrc() {
        return centralDirectoryHeader.crc;
    }

    @Override
    public int getLevel() {
        return LEVEL_UNKNOWN;
    }

    @Override
    public int compareTo(ZipEntry o) {
        long ret = 0;
        ret = getDataOffset() - o.getDataOffset();
        if (ret > 0) {
            return 1;
        } else if (ret < 0) {
            return -1;
        }
        return 0;

    }
}
