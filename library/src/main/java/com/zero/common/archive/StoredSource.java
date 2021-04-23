package com.zero.common.archive;

import java.io.IOException;
import java.io.InputStream;

public class StoredSource extends SourceWrapper {
    public StoredSource(Source source) {
        super(source);
    }

    @Override
    public InputStream getRawInputStream() throws IOException {
        return getInputStream();
    }

    @Override
    public int getMethod() {
        return ZipEntry.STORED;
    }
}
