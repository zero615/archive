package com.zero.common.archive;

import java.nio.ByteBuffer;

public class EndRecord {
    public int mSize;
    int mDiskNumber;
    int mDiskCentralStart;
    long mDiskNumberStart;
    long mNumEntries;
    long mDirSize;
    long mOffset;
    int mCommentLength;
    byte[] mComments;

    public EndRecord(ByteBuffer byteBuffer) {
        mDiskNumber = byteBuffer.getShort() & 0xFFFF;
        mDiskCentralStart = byteBuffer.getShort() & 0xFFFF;
        mDiskNumberStart = byteBuffer.getShort() & 0xffFF;
        mNumEntries = byteBuffer.getShort() & 0xFFFF;
        mDirSize = byteBuffer.getInt() & 0xFFFFFFFFL;
        mOffset = byteBuffer.getInt() & 0xFFFFFFFFL;
        mCommentLength = byteBuffer.getShort() & 0xFFFF;
        byte[] comments = new byte[mCommentLength];
        byteBuffer.get(comments);
        mComments = comments;
    }

    public void setCommentLength(int commentLength) {
        mCommentLength = commentLength;
    }

    public void setComments(byte[] comments) {
        mComments = comments;
    }

    public void setDiskCentralStart(int diskCentralStart) {
        mDiskCentralStart = diskCentralStart;
    }

    public void setDiskNumber(int diskNumber) {
        mDiskNumber = diskNumber;
    }

    public void setDiskNumberStart(long diskNumberStart) {
        mDiskNumberStart = diskNumberStart;
    }

    public long getOffset() {
        return mOffset;
    }

    public void setOffset(long offset) {
        mOffset = offset;
    }

    public long getNumEntries() {
        return mNumEntries;
    }

    public void setNumEntries(long numEntries) {
        mNumEntries = numEntries;
    }

    public long getDirSize() {
        return mDirSize;
    }

    public void setDirSize(long dirSize) {
        mDirSize = dirSize;
    }
}
