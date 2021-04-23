package com.zero.common.archive;

import java.io.IOException;

public class OriginSession extends Session {
    private ZipEntry mZipEntry;

    OriginSession(ZipEntry entry) {
        mZipEntry = entry;
    }

    @Override
    protected void onPrepare() {
        super.onPrepare();
    }

    @Override
    protected void onWriteDataDescriptor(DataDescriptor dataDescriptor) throws IOException {
        mArchive.seek(getDataOffset() + mZipEntry.getCompressedSize() + dataDescriptor.getDataDescriptorLength());
    }

    @Override
    protected void onWriteLocalFileHeader(LocalFileHeader header) throws IOException {

    }

    @Override
    protected LocalFileHeader onCreateLocalFileHeader() throws IOException {
        return new LocalFileHeader(mZipEntry.getLocalFileHeader());
    }

    @Override
    protected DataDescriptor onCreateDataDescriptor(LocalFileHeader localFileHeader) throws IOException {
        return mZipEntry.getDataDescriptor();
    }

    @Override
    protected void onCommit() throws IOException {
        mArchive.seek(getDataOffset() + mZipEntry.getCompressedSize());
    }

    @Override
    public String getName() {
        return mZipEntry.getName();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {

    }
}
