package com.zero.common.archive;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class Util {

    private static int EOF = -1;

    public static long copy(final InputStream input, final OutputStream output)
            throws IOException {
        final byte[] buffer = new byte[4096];
        long count = 0;
        int n;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    public static void closeQuietly(Closeable c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (final IOException ioe) {
            // ignore
        }
    }


    public static void writeLong(byte[] bytes, int offset, long v) {
        bytes[offset++] = (byte) ((v >>> 0) & 0xff);
        bytes[offset++] = (byte) ((v >>> 8) & 0xff);
        bytes[offset++] = (byte) ((v >>> 16) & 0xff);
        bytes[offset++] = (byte) ((v >>> 24) & 0xff);
        bytes[offset++] = (byte) ((v >>> 32) & 0xff);
        bytes[offset++] = (byte) ((v >>> 40) & 0xff);
        bytes[offset++] = (byte) ((v >>> 48) & 0xff);
        bytes[offset++] = (byte) ((v >>> 56) & 0xff);
    }

    public static void writeShort(byte[] bytes, int offset, int v) {
        bytes[offset++] = (byte) ((v >>> 0) & 0xff);
        bytes[offset++] = (byte) ((v >>> 8) & 0xff);
    }

    /**
     * Fetches unsigned 16-bit value from byte array at specified offset.
     * The bytes are assumed to be in Intel (little-endian) byte order.
     */
    public static final int get16(byte[] b, int off) {
        return toUnsignedInt(b[off]) | (toUnsignedInt(b[off + 1]) << 8);
    }

    public static int toUnsignedInt(byte x) {
        return ((int) x) & 0xff;
    }

    /**
     * Fetches unsigned 32-bit value from byte array at specified offset.
     * The bytes are assumed to be in Intel (little-endian) byte order.
     */
    public static final long get32(byte[] b, int off) {
        return (get16(b, off) | ((long) get16(b, off + 2) << 16)) & 0xffffffffL;
    }

    /**
     * Fetches signed 64-bit value from byte array at specified offset.
     * The bytes are assumed to be in Intel (little-endian) byte order.
     */
    public static final long get64(byte[] b, int off) {
        return get32(b, off) | (get32(b, off + 4) << 32);
    }
}
