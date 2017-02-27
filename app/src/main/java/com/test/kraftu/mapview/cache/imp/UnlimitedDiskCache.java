package com.test.kraftu.mapview.cache.imp;

import android.graphics.Bitmap;

import com.test.kraftu.mapview.cache.DiskCache;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class UnlimitedDiskCache implements DiskCache {
    public static final Bitmap.CompressFormat COMPRESS_FORMAT = Bitmap.CompressFormat.PNG;
    public static final int COMPRESS_QUALITY = 100;
    public static final String NAME_CACHE_DIR = "tiles";

    protected final File cacheDir;

    public UnlimitedDiskCache(File rootDir) {
        this.cacheDir = new File(rootDir, "/" + NAME_CACHE_DIR);
    }

    @Override
    public File getDirectory() {
        return cacheDir;
    }

    @Override
    public File get(String imageUri) {
        return getFile(imageUri);
    }

    @Override
    public boolean save(String imageUri, Bitmap bitmap) throws IOException {
        File imageFile = getFile(imageUri);
        OutputStream os = new BufferedOutputStream(new FileOutputStream(imageFile));
        boolean savedSuccessfully = false;
        try {
            savedSuccessfully = bitmap.compress(COMPRESS_FORMAT, COMPRESS_QUALITY, os);
        } finally {
            if(os != null)os.close();
        }
        return savedSuccessfully;
    }

    @Override
    public boolean remove(String imageUri) {
        return getFile(imageUri).delete();
    }

    @Override
    public String generateName(String url) {
        return String.valueOf(url.hashCode());
    }

    @Override
    public void clear() {
        File[] files = cacheDir.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
    }

    protected File getFile(String imageUri) {
        String fileName = generateName(imageUri);
        if (!cacheDir.exists()) {
            cacheDir.mkdir();
        }
        return new File(cacheDir, fileName);
    }

}
