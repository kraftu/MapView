package com.test.kraftu.mapview.core.imp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.test.kraftu.mapview.cache.DiskCache;
import com.test.kraftu.mapview.cache.MemoryCache;
import com.test.kraftu.mapview.cache.imp.LastUsageMemoryCache;
import com.test.kraftu.mapview.cache.imp.UnlimitedDiskCache;
import com.test.kraftu.mapview.core.TileManager;
import com.test.kraftu.mapview.core.TileManagerListener;
import com.test.kraftu.mapview.core.TileResource;
import com.test.kraftu.mapview.core.utils.MapThreadFactory;

import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;


public class BaseTileManager implements TileManager {
    public static final int THREAD_POOL_SIZE = 2;
    public static final boolean DEBUG = false;
    public static final String TAG = "BaseTileManager";

    private Handler mHandler = new Handler(Looper.myLooper());

    private MemoryCache mMemoryCache;

    private DiskCache mDiskCache;

    private TileResource mTileRes;

    private Reference<TileManagerListener> mTileListenerRef;

    private ExecutorService mExecutor;
    private HashMap<Integer,LoadBitmap> mListTask = new HashMap<>();

    public BaseTileManager(TileResource tileRes,
                           MemoryCache memoryCache, DiskCache diskCache) {
        mExecutor = Executors.newFixedThreadPool(THREAD_POOL_SIZE,
                new MapThreadFactory(TAG + "_Thread"));
        mTileRes = tileRes;
        mMemoryCache = memoryCache;
        mDiskCache = diskCache;
    }

    @Override
    public void updateVisibleTile(int startX, int endX, int startY, int endY) {
        for(int i = startX; i <= endX; i++){
            for(int j = startY; j <= endY; j++){
                Integer tileId = getTileId(i,j);
                if(!mListTask.containsKey(tileId) &&
                        mMemoryCache.get(tileId) == null) startLoadTask(i,j);
            }
        }
    }

    @Override
    public Bitmap getBitmapTile(int tileX, int tileY) {
        Integer tileId = getTileId(tileX,tileY);
        return mMemoryCache.get(tileId);
    }

    @Override
    public int getTileId(int tileX, int tileY) {
        return tileY * mTileRes.getCountColumnTile() + tileX;
    }

    @Override
    public TileResource getTileDownloader() {
        return mTileRes;
    }

    public void setTileManagerListener(TileManagerListener tileManagerListener) {
        this.mTileListenerRef = new WeakReference<>(tileManagerListener);
    }

    @Override
    public void cancelLoad() {
        if(DEBUG) Log.d(TAG,String.format("cancelLoad %d",mListTask.size()));

        for(Map.Entry<Integer,LoadBitmap> item : mListTask.entrySet()){
            item.getValue().cancelTask();
        }
    }

    @Override
    public void clearCache() {
        if(DEBUG) Log.d(TAG,String.format("clear cache"));
        if(mMemoryCache != null) mMemoryCache.clear();

        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if(mDiskCache != null) mDiskCache.clear();
            }
        });
    }

    private void startLoadTask(int tileX, int tileY){
        Integer tileId = getTileId(tileX,tileY);
        LoadBitmap loadBitmap = new LoadBitmap(tileId,tileX,tileY);
        mListTask.put(tileId,loadBitmap);
        mExecutor.submit(loadBitmap);
        if(DEBUG) Log.d(TAG,String.format("Create %s",loadBitmap.toString()));
    }


    private void notifyLoadedNewTile(int idTile){
        TileManagerListener listener = mTileListenerRef.get();
        if(listener != null) listener.loadedTile(idTile);
    }

    private class LoadBitmap implements Runnable{
        private static final String TAG = "LoadBitmap";
        private Integer tileId;
        private Integer tileX;
        private Integer tileY;
        private String url;
        private AtomicBoolean isCancel = new AtomicBoolean(false);
        private Bitmap bitmap;

        public LoadBitmap(int tileId, int tileX, int tileY) {
            this.tileId = tileId;
            this.tileX = tileX;
            this.tileY = tileY;
        }
        @Override
        public void run() {
            try {

                checkCancel();
                //Try loadTile from sd card cache
                url = mTileRes.getUriForTile(tileX,tileY);

                if (mDiskCache != null) {
                    File file = mDiskCache.get(url);
                    if (file.exists()) {
                        bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                        if(DEBUG)Log.d(TAG, String.format("id:%d from fromSd:%b",
                                tileId, bitmap!=null));
                    }
                }
                checkCancel();

                //Try loadTile from tileSource and save sd cache
                if (bitmap == null) {
                    bitmap = mTileRes.loadTile(url);
                    if(DEBUG)Log.d(TAG, String.format("id:%d from mTileRes:%b",
                            tileId, bitmap!=null));
                    if (bitmap != null && mDiskCache != null) {
                        mDiskCache.save(url, bitmap);
                    }
                }

                if (bitmap != null)
                    addCacheTileAndNotify(tileId,bitmap);
            }catch (Exception e){
                if(DEBUG)Log.e(TAG, String.format("id:%d exc::%s", tileId, e.getMessage()));
            }finally {
                loadedFinish();
            }

        }
        private void addCacheTileAndNotify(final Integer id, final Bitmap bitmap){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mMemoryCache.put(id, bitmap);
                    notifyLoadedNewTile(id);
                }
            });
        }

        private void loadedFinish(){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListTask.remove(tileId);
                }
            });
        }
        private void checkCancel() throws Exception{
            if(isCancel.get()) throw new Exception("TaskCancel");
        }

        public void cancelTask() {
            isCancel.set(true);
        }
        @Override
        public String toString() {
            return "LoadBitmap{" +
                    "tileId=" + tileId +
                    ", url='" + url + '\'' +
                    ", isCancel=" + isCancel +
                    ", bitmap=" + bitmap +
                    '}';
        }
    }
}
