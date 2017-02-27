package com.test.kraftu.mapview.core;


import android.graphics.Bitmap;

public interface TileManager {
    void updateVisibleTile(int startColumn, int endColumn,
                           int startRow, int endRow);

    Bitmap getBitmapTile(int column, int row);

    int getTileId(int column, int row);

    TileResource getTileDownloader();

    void setTileManagerListener(TileManagerListener listener);

    void cancelLoad();

    void clearCache();
}
