package com.zero.common.archive;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String args[]) throws IOException {
        File file = new File(args[0]);
        ZipFile zipFile = new ZipFile(file);
        List<ZipEntry> entries = new ArrayList<>(zipFile.entries().values());
        for (ZipEntry entry : entries) {
            if (entry.getOffset() < 0) {
                System.out.println(entry.getName() + " " + entry.getOffset());
            }
        }

//
//        ZipArchive archive = new ZipArchive(file,true);
//        archive.close();

    }
}
