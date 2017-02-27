package com.test.kraftu.mapview.core.utils;


import java.util.concurrent.ThreadFactory;

public class MapThreadFactory implements ThreadFactory{
    public static final int THREAD_PRIORITY = Thread.MIN_PRIORITY;

    private String mName;

    public MapThreadFactory(String mName) {
        this.mName = mName;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName(mName);
        thread.setPriority(THREAD_PRIORITY);
        return thread;
    }
}
