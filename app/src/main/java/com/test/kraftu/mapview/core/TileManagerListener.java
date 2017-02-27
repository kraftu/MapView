package com.test.kraftu.mapview.core;


public interface TileManagerListener {
    void loadedTile(int tileId);

    void errorTile(int tileId, Exception exc);
}