package com.test.kraftu.mapview.cache;

import android.graphics.Bitmap;

import java.io.File;
import java.io.IOException;


public interface DiskCache {

    File getDirectory();

    File get(String imageUri);

    boolean save(String imageUri, Bitmap bitmap) throws IOException;

    boolean remove(String imageUri);

    String generateName(String url);

    void clear();
}
