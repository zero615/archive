package com.zero.common.archive;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class Session extends OutputStream implements ZipConstants {
    protected final byte[] scratch = new byte[8];
    protected ZipArchive mArchive;
    private boolean mCommitted;
    private boolean mDeleted;
    private long mSessionSize;
    private LocalFileHeader mLocalFileHeader;
    private DataDescriptor mDataDescriptor;
    private long mOffset;
    private byte[] mComment;

    Session() {
    }

    final void attach(ZipArchive archive) throws IOException {
        mArchive = archive;
        mOffset = archive.getOffset();
        mLocalFileHeader = onCreateLocalFileHeader();
        onWriteLocalFileHeader(mLocalFileHeader);
        onPrepare();
    }

    public void setComment(byte[] mComment) {
        this.mComment = mComment;
    }

    protected void onPrepare() {

    }

    protected void onSessionChanged(long offset) {
        mOffset = offset;
        mLocalFileHeader.offset = offset;
    }

    protected abstract LocalFileHeader onCreateLocalFileHeader() throws IOException;

    protected void onWriteLocalFileHeader(LocalFileHeader header) throws IOException {
        final ZipArchive archive = mArchive;
        archive.writeInt(LOCSIG);
        archive.writeShort(header.extractVersion);
        archive.writeShort(header.flag);
        archive.writeShort(header.method);
        archive.writeInt(header.time);
        archive.writeInt(header.crc);
        archive.writeInt(header.compressedSize);
        archive.writeInt(header.size);
        archive.writeShort(header.name.length);
        archive.writeShort(header.extra.length);
        archive.writeBytes(header.name, 0, header.name.length);
        archive.writeBytes(header.extra, 0, header.extra.length);
    }

    protected abstract DataDescriptor onCreateDataDescriptor(LocalFileHeader localFileHeader) throws IOException;

    protected void onWriteDataDescriptor(DataDescriptor dataDescriptor) throws IOException {
        if (dataDescriptor != null) {
            final ZipArchive archive = mArchive;
            archive.writeInt(ZipConstants.EXTSIG);
            archive.writeInt(dataDescriptor.getCrc());
            if (dataDescriptor.isZip64()) {
                archive.writeLong(dataDescriptor.getCompressedSize());
                archive.writeLong(dataDescriptor.getSize());
            } else {
                archive.writeInt(dataDescriptor.getCompressedSize());
                archive.writeInt(dataDescriptor.getSize());
            }
        }

    }

    protected CentralDirectoryHeader onCreateDirectoryHeader() throws IOException {
        CentralDirectoryHeader header = new CentralDirectoryHeader(mLocalFileHeader, mDataDescriptor);
        if (mComment != null) {
            header.comment = mComment;
        }
        return header;
    }

    protected void onWriteDirectoryHeader(CentralDirectoryHeader entry) throws IOException {
        ZipArchive archive = mArchive;
        archive.writeInt(CENSIG);  // CEN header signature
        archive.writeShort(entry.madeVersion);
        archive.writeShort(entry.extractVersion);
        archive.writeShort(entry.flag);
        archive.writeShort(entry.method);
        archive.writeInt(entry.time);
        archive.writeInt(entry.crc);
        archive.writeInt(entry.compressedSize);
        archive.writeInt(entry.size);
        archive.writeShort(entry.name.length);
        archive.writeShort(entry.extra.length);
        archive.writeShort(entry.comment.length);
        archive.writeShort(entry.diskNumberStart);
        archive.writeShort(entry.internalAttrs);
        archive.writeInt(entry.externalAttrs);
        archive.writeInt(entry.offset);
        archive.writeBytes(entry.name, 0, entry.name.length);
        archive.writeBytes(entry.extra, 0, entry.extra.length);
        archive.writeBytes(entry.comment, 0, entry.comment.length);
    }

    public LocalFileHeader getLocalFileHeader() {
        return mLocalFileHeader;
    }


    public long getDataOffset() {
        return mLocalFileHeader.getDataOffset();
    }

    public long getOffset() {
        return mOffset;
    }

    boolean isDeleted() {
        return mDeleted;
    }

    void setDeleted(boolean deleted) {
        mDeleted = deleted;
    }

    @Override
    public final void write(int b) throws IOException {
        scratch[0] = (byte) (b & 0xff);
        write(scratch, 0, 1);
    }

    @Override
    public final void write(byte[] b) throws IOException {
        super.write(b);
    }

    public long[] getValue(int tag, byte[] extra) {
        ByteBuffer buffer = ByteBuffer.wrap(extra).order(ByteOrder.LITTLE_ENDIAN);
        int off = 0;
        int len = extra.length;
        boolean found;
        while (off + 4 < len) {
            found = buffer.getShort(off) == tag;
            int sz = buffer.getShort(off + 2);
            off += 4;
            if (off + sz > len)         // invalid data
                break;
            if (found) {
                long[] result = new long[sz / 8];
                for (int i = 0; i < result.length; i++) {
                    result[i] = buffer.getLong(off);
                    off += 8;
                }
                return result;
            }
            off += sz;
        }
        return new long[0];
    }


    @Override
    public abstract void write(byte[] b, int off, int len) throws IOException;

    public boolean isCommitted() {
        return mCommitted;
    }

    public void commit() throws IOException {
        if (mArchive == null) {
            throw new RuntimeException("the session is not attach");
        }
        if (mCommitted) {
            return;
        }
        onCommit();
        mCommitted = true;
        mDataDescriptor = onCreateDataDescriptor(mLocalFileHeader);
        if (mDataDescriptor != null) {
            onWriteDataDescriptor(mDataDescriptor);
        }
        mSessionSize = mArchive.getOffset() - getOffset();
    }

    @Override
    public void close() throws IOException {
        super.close();
        commit();
    }

    protected void onCommit() throws IOException {

    }

    public long getSessionSize() {
        return mSessionSize;
    }


    public abstract String getName();
}
