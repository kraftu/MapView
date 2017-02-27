package com.test.kraftu.mapview.core;

import android.graphics.Bitmap;

public interface TileResource {
    Bitmap loadTile(String url);

    String getUriForTile(int tileColumn, int tileRow);

    int getCountColumnTile();

    int getCountRowTile();

    int getWidthTile();

    int getHeightTile();
}
