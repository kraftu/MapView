package com.test.kraftu.mapview.cache;

import android.graphics.Bitmap;


public interface MemoryCache {

    boolean put(Integer key, Bitmap value);

    Bitmap get(Integer key);

    void remove(Integer key);

    void clear();
}
