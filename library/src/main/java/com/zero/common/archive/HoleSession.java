package com.zero.common.archive;

import java.io.IOException;

public class HoleSession extends ZipSession {
    private long crc;
    private long size;
    private long compressedSize;

    public HoleSession(String name, int method, long crc, long size, long compressedSize) {
        super(name, method);
        this.crc = crc;
        this.size = size;
        this.compressedSize = compressedSize;
    }

    @Override
    protected LocalFileHeader onCreateLocalFileHeader() throws IOException {
        LocalFileHeader header = super.onCreateLocalFileHeader();
        header.setSize(crc, size, compressedSize);
        return header;
    }

    @Override
    protected void onCommit() throws IOException {
        super.onCommit();
        mArchive.seek(getDataOffset() + compressedSize);
    }
}
