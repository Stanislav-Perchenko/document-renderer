package com.webssa.guestbest;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.webssa.guestbest.rest.helper.HttpClientProvider;
import com.webssa.library.mypicasso.LruCache;
import com.webssa.library.mypicasso.OkHttp3Downloader;
import com.webssa.library.mypicasso.Picasso;

import java.util.List;

import static android.content.pm.ApplicationInfo.FLAG_LARGE_HEAP;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.HONEYCOMB;

public class MyApplication extends Application {

    private static Context appContext;
    private static boolean debuggable;
    private static Picasso mPicasso;
    private static LruCache mPicassoMemCache;

    private static ConnectivityManager appConnectManager;

    public static Context getStaticAppContext() {
        return appContext;
    }

    public static boolean isDebuggable() {
        return debuggable;
    }



    @Override
    public void onCreate() {
        super.onCreate();

        MyApplication.appContext = this;
        MyApplication.debuggable = (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        appConnectManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (isMainProcess()) {

            mPicassoMemCache = new LruCache(calculateMemoryCacheSize());
            mPicasso = new Picasso.Builder(this)
                    .loggingEnabled(false)
                    .memoryCache(mPicassoMemCache)
                    .downloader(new OkHttp3Downloader(HttpClientProvider.getInstance().getPicassoHttpClient()))
                    .build();
        }
    }

    private boolean isMainProcess() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            List<ActivityManager.RunningAppProcessInfo> processes = manager.getRunningAppProcesses();
            if (processes != null) {
                int pid = android.os.Process.myPid();
                for (ActivityManager.RunningAppProcessInfo processInfo : processes) {
                    if (processInfo.pid == pid) {
                        return getPackageName().equals(processInfo.processName);
                    }
                }
            }
        }
        return true;
    }


    private int calculateMemoryCacheSize() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        boolean largeHeap = (getApplicationInfo().flags & FLAG_LARGE_HEAP) != 0;
        int memoryClass = (largeHeap && SDK_INT >= HONEYCOMB) ? am.getLargeMemoryClass() : am.getMemoryClass();
        return Math.round(1024 * 1024 * memoryClass * GlobalProperties.MAX_IMAGE_MEM_CACHE_PART);
    }

    public static Picasso getPicasso() {
        return mPicasso;
    }

    public static LruCache getPicassoMemCache() {
        return mPicassoMemCache;
    }


    public static NetworkConnectionObserver getNetworkConnectionObserver() {
        return new NetworkConnectionObserver(appConnectManager);
    }


    public static class NetworkConnectionObserver {
        private boolean wifiConnected;
        private boolean mobileConnected;
        private boolean defaultConnected;

        private NetworkConnectionObserver(ConnectivityManager connMgr) {

            NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
            if (activeInfo != null) {
                defaultConnected = activeInfo.isConnected();
                switch (activeInfo.getType()) {
                    case ConnectivityManager.TYPE_WIFI:
                        wifiConnected = true;
                        activeInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                        mobileConnected = (activeInfo != null) ? activeInfo.isConnected() : false;
                        break;
                    case ConnectivityManager.TYPE_MOBILE:
                        mobileConnected = true;
                        activeInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                        wifiConnected = (activeInfo != null) ? activeInfo.isConnected() : false;
                        break;
                }
            }
        }

        public boolean isConnected() {
            return defaultConnected;
        }

        public boolean isWifiConnected() {
            return wifiConnected;
        }

        public boolean isMobileConnected() {
            return mobileConnected;
        }
    }
}
