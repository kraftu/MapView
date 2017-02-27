package com.test.kraftu.mapview.view;


import android.content.Context;
import android.util.AttributeSet;

import com.test.kraftu.mapview.cache.DiskCache;
import com.test.kraftu.mapview.cache.MemoryCache;
import com.test.kraftu.mapview.cache.imp.LastUsageMemoryCache;
import com.test.kraftu.mapview.cache.imp.UnlimitedDiskCache;
import com.test.kraftu.mapview.core.TileManager;
import com.test.kraftu.mapview.core.TileResource;
import com.test.kraftu.mapview.core.imp.BaseTileManager;
import com.test.kraftu.mapview.core.imp.OpenMapTileResource;

public class OpencycleMapView extends BaseMapView {

    public OpencycleMapView(Context context) {
        super(context);
    }

    public OpencycleMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OpencycleMapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public TileManager getTileManager() {

        TileResource tileRes = new OpenMapTileResource();
        MemoryCache memoryCache = new LastUsageMemoryCache(LastUsageMemoryCache.DEFAULT_SIZE);
        DiskCache diskCache = new UnlimitedDiskCache(getContext().getCacheDir());

        return new BaseTileManager(tileRes, memoryCache, diskCache);
    }
}
