package com.test.kraftu.mapview.cache.imp;

import android.graphics.Bitmap;

import com.test.kraftu.mapview.cache.MemoryCache;

import java.util.LinkedHashMap;
import java.util.Map;


public class LruMemoryCache implements MemoryCache {
    private final LinkedHashMap<Integer, Bitmap> map;
    private final int maxSize;
    private int size;

    public LruMemoryCache(int maxSize) {
        if (maxSize < 0) throw new IllegalArgumentException("maxSize <= 0");
        this.maxSize = maxSize;
        map = new LinkedHashMap<>();
    }

    @Override
    public boolean put(Integer key, Bitmap value) {
        if (key == null || value == null) {
            throw new NullPointerException("key or value is null");
        }

        synchronized (this) {
            size += sizeOf(value);
            Bitmap previous = map.put(key, value);
            if (previous != null) {
                size -= sizeOf(previous);
            }
        }
        trimToSize(maxSize);
        return true;
    }

    @Override
    public Bitmap get(Integer key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }

        synchronized (this) {
            return map.get(key);
        }
    }

    @Override
    public void remove(Integer key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }
        synchronized (this) {
            Bitmap previous = map.remove(key);
            if (previous != null) {
                size -= sizeOf(previous);
            }
        }
    }

    private void trimToSize(int maxSize) {
        while (true) {
            Integer key;
            Bitmap value;
            synchronized (this) {
                if (size < 0 || (map.isEmpty() && size != 0)) {
                    throw new IllegalStateException(getClass().getName() +
                            ".sizeOf() is reporting inconsistent results!");
                }

                if (size <= maxSize || map.isEmpty()) {
                    break;
                }

                Map.Entry<Integer, Bitmap> toEvict = map.entrySet().iterator().next();
                if (toEvict == null) {
                    break;
                }
                key = toEvict.getKey();
                value = toEvict.getValue();
                map.remove(key);
                size -= sizeOf(value);
                if (value != null) {
                    value.recycle();
                }
            }
        }
    }

    @Override
    public void clear() {
        trimToSize(1);
    }

    private int sizeOf(Bitmap value) {
        return value.getRowBytes() * value.getHeight();
    }

    @Override
    public String toString() {
        return "LruMemoryCache{" +
                "maxSize=" + maxSize +
                ", size=" + size +
                '}';
    }
}