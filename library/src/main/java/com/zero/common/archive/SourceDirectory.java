package com.zero.common.archive;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

public class SourceDirectory {
    private File root;

    public SourceDirectory(File file) throws IOException {
        if (file.exists() && !file.isDirectory()) {
            throw new RuntimeException("" + file + " is not directory");
        }
        root = file;
    }

    public File getRoot() {
        return root;
    }

    public OutputStream getOutputStream(String name) throws FileNotFoundException {
        File file = new File(root, name);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        return new FileOutputStream(file);
    }

    public void copyFrom(ZipSourceFile sourceFile) throws IOException {
        copyFrom(sourceFile, null);
    }

    public void copyFrom(ZipSourceFile sourceFile, ZipSourceFile.SourceFilter filter) throws IOException {
        Collection<Source> collection = sourceFile.entries().values();
        for (Source source : collection) {
            String name = source.getName();
            if (filter == null || filter.accept(name)) {
                InputStream inputStream = source.getInputStream();
                OutputStream outputStream = getOutputStream(source.getName());
                Util.copy(inputStream, outputStream);
                inputStream.close();
                outputStream.close();
            }
        }
    }

    public File getFile(String name) {
        return new File(root, name);
    }

    public void copyFrom(ZipSourceFile sourceFile, String sourceName, String outName) throws IOException {
        InputStream inputStream = sourceFile.getInputStream(sourceName);
        OutputStream outputStream = getOutputStream(outName);
        Util.copy(inputStream, outputStream);
        inputStream.close();
        outputStream.close();
    }

    public void removeFile(String outFileName) {
        File file = new File(root, outFileName);
        file.delete();
    }

    public SourceDirectory create(String name) throws IOException {
        return new SourceDirectory(new File(root, name));
    }
}
