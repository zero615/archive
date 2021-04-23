/*
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.zero.common.archive;

/*
 * This interface defines the constants that are used by the classes
 * which manipulate ZIP files.
 */
public interface ZipConstants {

    /*
     * Header signatures
     */
    long LOCSIG = 0x04034b50L;   // "PK\003\004"
    long EXTSIG = 0x08074b50L;   // "PK\007\008"
    long CENSIG = 0x02014b50L;   // "PK\001\002"
    long ENDSIG = 0x06054b50L;   // "PK\005\006"

    /*
     * Header sizes in bytes (including signatures)
     */
    int LOCHDR = 30;       // LOC header count
    int EXTHDR = 16;       // EXT header count
    int CENHDR = 46;       // CEN header count
    int ENDHDR = 22;       // END header count

    /*
     * Local file (LOC) header field offsets
     */
    int LOCVER = 4;        // version needed to extract
    int LOCFLG = 6;        // general purpose bit flag
    int LOCHOW = 8;        // compression method
    int LOCTIM = 10;       // modification time
    int LOCCRC = 14;       // uncompressed file crc-32 value
    int LOCSIZ = 18;       // compressed count
    int LOCLEN = 22;       // uncompressed count
    int LOCNAM = 26;       // filename length
    int LOCEXT = 28;       // extra field length

    /*
     * Extra local (EXT) header field offsets
     */
    int EXTCRC = 4;        // uncompressed file crc-32 value
    int EXTSIZ = 8;        // compressed count
    int EXTLEN = 12;       // uncompressed count

    /*
     * Central directory (CEN) header field offsets
     */
    int CENVEM = 4;        // version made by
    int CENVER = 6;        // version needed to extract
    int CENFLG = 8;        // encrypt, decrypt flags
    int CENHOW = 10;       // compression method
    int CENTIM = 12;       // modification time
    int CENCRC = 16;       // uncompressed file crc-32 value
    int CENSIZ = 20;       // compressed count
    int CENLEN = 24;       // uncompressed count
    int CENNAM = 28;       // filename length
    int CENEXT = 30;       // extra field length
    int CENCOM = 32;       // comment length
    int CENDSK = 34;       // disk number start
    int CENATT = 36;       // internal file attributes
    int CENATX = 38;       // external file attributes
    int CENOFF = 42;       // LOC header offset

    /*
     * End of central directory (END) header field offsets
     */
    int ENDSUB = 8;        // number of entries on this disk
    int ENDTOT = 10;       // total number of entries
    int ENDSIZ = 12;       // central directory count in bytes
    int ENDOFF = 16;       // offset of first CEN header
    int ENDCOM = 20;       // zip file comment length


    /*
     * ZIP64 constants
     */
    long ZIP64_ENDSIG = 0x06064b50L;  // "PK\006\006"
    long ZIP64_LOCSIG = 0x07064b50L;  // "PK\006\007"
    int ZIP64_ENDHDR = 56;           // ZIP64 end header count
    int ZIP64_LOCHDR = 20;           // ZIP64 end loc header count
    int ZIP64_EXTHDR = 24;           // EXT header count
    int ZIP64_EXTID = 0x0001;       // Extra field Zip64 header ID

    int ZIP64_MAGICCOUNT = 0xFFFF;
    long ZIP64_MAGICVAL = 0xFFFFFFFFL;

    /*
     * Zip64 End of central directory (END) header field offsets
     */
    int ZIP64_ENDLEN = 4;       // count of zip64 end of central dir
    int ZIP64_ENDVEM = 12;      // version made by
    int ZIP64_ENDVER = 14;      // version needed to extract
    int ZIP64_ENDNMD = 16;      // number of this disk
    int ZIP64_ENDDSK = 20;      // disk number of start
    int ZIP64_ENDTOD = 24;      // total number of entries on this disk
    int ZIP64_ENDTOT = 32;      // total number of entries
    int ZIP64_ENDSIZ = 40;      // central directory count in bytes
    int ZIP64_ENDOFF = 48;      // offset of first CEN header
    int ZIP64_ENDEXT = 56;      // zip64 extensible data sector

    /*
     * Zip64 End of central directory locator field offsets
     */
    int ZIP64_LOCDSK = 4;       // disk number start
    int ZIP64_LOCOFF = 8;       // offset of zip64 end
    int ZIP64_LOCTOT = 16;      // total number of disks

    /*
     * Zip64 Extra local (EXT) header field offsets
     */
    int ZIP64_EXTCRC = 4;       // uncompressed file crc-32 value
    int ZIP64_EXTSIZ = 8;       // compressed count, 8-byte
    int ZIP64_EXTLEN = 16;      // uncompressed count, 8-byte

    /*
     * Language encoding flag EFS
     */
    int EFS = 0x800;       // If this bit is set the filename and
    // comment fields for this file must be
    // encoded using UTF-8.

    /*
     * Constants below are defined here (instead of in ZipConstants)
     * to avoid being exposed as public fields of ZipFile, ZipEntry2,
     * ZipInputStream and ZipOutputstream.
     */

    /*
     * Extra field header ID
     */
    int EXTID_ZIP64 = 0x0001;    // Zip64
    int EXTID_NTFS = 0x000a;    // NTFS
    int EXTID_UNIX = 0x000d;    // UNIX
    int EXTID_EXTT = 0x5455;    // Info-ZIP Extended Timestamp

    /*
     * EXTT timestamp flags
     */
    int EXTT_FLAG_LMT = 0x1;       // LastModifiedTime
    int EXTT_FLAG_LAT = 0x2;       // LastAccessTime
    int EXTT_FLAT_CT = 0x4;       // CreationTime

    int MAX_COMMENT_LEN = 65535;
    int MAX_END_SEARCH = (MAX_COMMENT_LEN + ENDHDR + ZIP64_ENDHDR + ZIP64_LOCHDR);
}
