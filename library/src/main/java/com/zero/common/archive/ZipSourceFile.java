package com.zero.common.archive;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ZipSourceFile {
    private Map<String, Source> mEntries = new LinkedHashMap<>();

    public ZipSourceFile() {

    }

    public ZipSourceFile(File file) throws IOException {
        addExtensionFile(file);
    }

    public ZipSourceFile(List<File> files) throws IOException {
        for (File file : files) {
            addExtensionFile(file);
        }
    }

    public InputStream getInputStream(String name) throws IOException {
        return getSource(name).getInputStream();
    }

    public InputStream getRawInputStream(String name) throws IOException {
        return getSource(name).getRawInputStream();
    }

    public Source getSource(String name) {
        return mEntries.get(name);
    }

    public Map<String, Source> getSources() {
        return mEntries;
    }

    public void addExtensionFile(File file, SourceFilter filter) throws IOException {
        if (file.isDirectory()) {
            List<String> list = new ArrayList<>();
            Map<String, Source> sourceMap = new LinkedHashMap<>(list.size());
            collectFileNames(file, "", list);
            for (String name : list) {
                if (filter == null || filter.accept(name)) {
                    sourceMap.put(name, new FileSource(new File(file, name), name));
                }
            }
            mEntries.putAll(sourceMap);
        } else {
            ZipFile zipFile = new ZipFile(file);
            Map<String, ZipEntry> map = zipFile.entries();
            Map<String, Source> sourceMap = new LinkedHashMap<>(map.size());
            for (ZipEntry entry : map.values()) {
                if (filter == null || filter.accept(entry.getName())) {
                    sourceMap.put(entry.getName(), new ZipEntry(entry));
                }
            }
            mEntries.putAll(sourceMap);
        }
    }

    public void addExtensionAll(Map<String, Source> sources) {
        mEntries.putAll(sources);
    }

    public void addExtensionSource(Source source) {
        mEntries.put(source.getName(), source);
    }

    public void addExtensionFile(File file) throws IOException {
        addExtensionFile(file, null);
    }

    private void collectFileNames(File root, String parent, List<String> list) {
        if (root.isFile()) {
            throw new RuntimeException(root + " is directory");
        }
        File[] files = root.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isFile()) {
                list.add(parent + file.getName());
            } else {
                collectFileNames(file, parent + file.getName() + "/", list);
            }
        }
    }


    public Map<String, Source> entries() {
        return mEntries;
    }


    public ZipSourceFile sub(String dir) {
        if (!dir.endsWith("/")) {
            dir += "/";
        }
        Map<String, Source> sources = new LinkedHashMap<>();
        for (String key : mEntries.keySet()) {
            if (key.startsWith(dir)) {
                String name = key.replace(dir, "");
                if (!name.isEmpty()) {
                    sources.put(name, new SubSource(mEntries.get(key), name));
                }
            }
        }
        ZipSourceFile sourceFile = new ZipSourceFile();
        sourceFile.mEntries = sources;
        return sourceFile;
    }

    public interface SourceFilter {

        boolean accept(String name);
    }

    private static class SubSource extends SourceWrapper {
        Source source;
        String name;

        public SubSource(Source source, String name) {
            super(source);
            this.source = source;
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }


    }
}
