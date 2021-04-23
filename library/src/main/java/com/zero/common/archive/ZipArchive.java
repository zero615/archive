package com.zero.common.archive;


import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ZipArchive implements Closeable, ZipConstants {
    private RandomAccessFile mRandomAccessFile;

    private Session mCurrentSession;
    private Map<String, Session> mSessions = new LinkedHashMap<>();

    private File mFile;
    private boolean mFinished;
    private String mComment;
    private boolean changed;

    public ZipArchive(File file) throws IOException {
        this(file, false);
    }

    public ZipArchive(File file, boolean append) throws IOException {
        mFile = file;
        File parent = mFile.getParentFile();
        if (parent != null && !parent.exists()) {
            if (!parent.mkdirs()) {
            }
        }
        mRandomAccessFile = new RandomAccessFile(file, "rw");
        if (append) {
            inflaterAppendSession();
        }
        changed = false;

    }

    public synchronized void commitSession() throws IOException {
        if (mCurrentSession != null) {
            mCurrentSession.commit();
        }
    }

    public synchronized Session putNextSession(String name, int method) throws IOException {
        return putNextSession(new ZipSession(name, method));
    }

    public synchronized Session putNextStoredSession(String name) throws IOException {
        ZipSession zipSession = new ZipSession(name, ZipEntry.STORED);
        return putNextSession(zipSession);
    }

    public synchronized Session putNextSourceSession(ZipEntry entry) throws IOException {
        return putNextSession(new SourceSession(entry));
    }

    public synchronized ZipSession putNextSession(String name) throws IOException {
        return (ZipSession) putNextSession(new ZipSession(name));
    }

    public synchronized Session putNextSession(Session session) throws IOException {
        deleteSession(session.getName());
        mCurrentSession = session;
        mSessions.put(session.getName(), session);
        session.attach(this);
        changed = true;
        return session;
    }

    public synchronized boolean deleteSession(String name) throws IOException {
        changed = true;
        commitSession();
        Session session = mSessions.remove(name);
        if (session != null) {
            session.setDeleted(true);
            return true;
        } else {
            return false;
        }
    }


    private void inflaterAppendSession() {
        try {
            ZipFile zipFile = new ZipFile(mFile);
            Map<String, ZipEntry> entries = zipFile.entries();
            List<ZipEntry> list = new ArrayList<>(entries.values());
            Collections.sort(list);
            for (ZipEntry entry : list) {
                putNextSession(new OriginSession(entry));
            }
        } catch (IOException e) {

        }
    }

    public void finish() throws IOException {
        if (mFinished) {
            return;
        }
        if (!changed) {
            mFinished = true;
            mRandomAccessFile.close();
            return;
        }
        commitSession();
        crunchArchive();
        mFinished = true;
        long position = mRandomAccessFile.getFilePointer();
        for (Session session : mSessions.values()) {
            CentralDirectoryHeader header = session.onCreateDirectoryHeader();
            session.onWriteDirectoryHeader(header);
        }
        long end = mRandomAccessFile.getFilePointer();
        writeEND(position, end - position);
        long offset = mRandomAccessFile.getFilePointer();
        mRandomAccessFile.getChannel().truncate(offset);
        mRandomAccessFile.close();
    }

    //todo 清除标记为删除的entry
    public void crunchArchive() throws IOException {
        if (!changed) {
            return;
        }
        long offset = 0L;
        List<Session> sessions = new ArrayList<>(mSessions.values());
        for (Session session : sessions) {
            long realOffset = session.getOffset();
            if (realOffset != offset) {
                OutputStream outputStream = new SeekOutputStream(mFile, offset);
                InputStream inputStream = new BoundInputStream(new SeekInputStream(mFile, realOffset), session.getSessionSize());
                Util.copy(inputStream, outputStream);
                outputStream.close();
                inputStream.close();
                session.onSessionChanged(offset);
            }
            offset += session.getSessionSize();
        }
        mRandomAccessFile.seek(offset);
    }

    @Override
    public void close() throws IOException {
        finish();
    }

    public File getRawFile() {
        return mFile;
    }

    SeekOutputStream getOutputStream() {
        return new SeekOutputStream(mRandomAccessFile);
    }


    private void writeEND(long off, long len) throws IOException {
        boolean hasZip64 = false;
        long xlen = len;
        long xoff = off;
        if (xlen >= ZIP64_MAGICVAL) {
            xlen = ZIP64_MAGICVAL;
            hasZip64 = true;
        }
        if (xoff >= ZIP64_MAGICVAL) {
            xoff = ZIP64_MAGICVAL;
            hasZip64 = true;
        }
        int count = mSessions.size();
        if (count >= ZIP64_MAGICCOUNT) {
            count = ZIP64_MAGICCOUNT;
            hasZip64 = true;
        }
        if (hasZip64) {
            long off64 = mRandomAccessFile.getFilePointer();

            //zip64 end of central directory record
            writeInt(ZIP64_ENDSIG);        // zip64 END record signature
            writeLong(ZIP64_ENDHDR - 12);  // size of zip64 end
            writeShort(45);                // version made by
            writeShort(45);                // version needed to extract
            writeInt(0);                   // number of this disk
            writeInt(0);                   // central directory start disk
            writeLong(mSessions.size());    // number of directory entires on disk
            writeLong(mSessions.size());    // number of directory entires
            writeLong(len);                // length of central directory
            writeLong(off);                // offset of central directory

            //zip64 end of central directory locator
            writeInt(ZIP64_LOCSIG);        // zip64 END locator signature
            writeInt(0);                   // zip64 END start disk
            writeLong(off64);              // offset of zip64 END
            writeInt(1);                   // total number of disks (?)
        }
        writeInt(ENDSIG);                 // END record signature
        writeShort(0);                    // number of this disk
        writeShort(0);                    // central directory start disk
        writeShort(count);                // number of directory entries on disk
        writeShort(count);                // total number of directory entries
        writeInt(xlen);                   // length of central directory
        writeInt(xoff);                   // offset of central directory
        if (mComment != null) {            // zip file comment
            writeShort(mComment.getBytes().length);
            writeBytes(mComment.getBytes(), 0, mComment.getBytes().length);
        } else {
            writeShort(0);
        }
    }

    void writeShort(int v) throws IOException {
        final RandomAccessFile archive = mRandomAccessFile;
        archive.write((v >>> 0) & 0xff);
        archive.write((v >>> 8) & 0xff);

    }

    void writeInt(long v) throws IOException {
        final RandomAccessFile archive = mRandomAccessFile;
        archive.write((int) ((v >>> 0) & 0xff));
        archive.write((int) ((v >>> 8) & 0xff));
        archive.write((int) ((v >>> 16) & 0xff));
        archive.write((int) ((v >>> 24) & 0xff));
    }


    void writeLong(long v) throws IOException {
        final RandomAccessFile archive = mRandomAccessFile;
        archive.write((int) ((v >>> 0) & 0xff));
        archive.write((int) ((v >>> 8) & 0xff));
        archive.write((int) ((v >>> 16) & 0xff));
        archive.write((int) ((v >>> 24) & 0xff));
        archive.write((int) ((v >>> 32) & 0xff));
        archive.write((int) ((v >>> 40) & 0xff));
        archive.write((int) ((v >>> 48) & 0xff));
        archive.write((int) ((v >>> 56) & 0xff));

    }

    public void writeBytes(byte[] b, int off, int len) throws IOException {
        mRandomAccessFile.write(b, off, len);
    }

    public long getOffset() throws IOException {
        return mRandomAccessFile.getFilePointer();
    }

    public Map<String, Session> sessions() {
        return mSessions;
    }


    public void setComment(String comment) {
        mComment = comment;
    }

    public void seek(long offset) throws IOException {
        mRandomAccessFile.seek(offset);
    }
}
