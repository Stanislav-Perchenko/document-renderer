package com.webssa.guestbest.rest.helper;

import android.util.Log;

import com.webssa.guestbest.BuildConfig;
import com.webssa.guestbest.GlobalProperties;
import com.webssa.guestbest.MyApplication;
import com.webssa.guestbest.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class HttpClientProvider {
    private static final String TAG = "HttpClientIntercept";

    private static HttpClientProvider instance;


    public static HttpClientProvider getInstance() {
        if (instance == null) {
            synchronized (HttpClientProvider.class) {
                if (instance == null) {
                    instance = new HttpClientProvider();
                }
            }
        }
        return instance;
    }


    private final OkHttpClient mApiHttpClient;
    private final OkHttpClient mPicassoHttpClient;

    private HttpClientProvider() {
        List<Protocol> protocols = new ArrayList<Protocol>(1);
        protocols.add(Protocol.HTTP_1_1);


        OkHttpClient client = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(3, 150, TimeUnit.SECONDS))
                .protocols(protocols)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .addNetworkInterceptor((chain) -> chain.proceed(chain.request().newBuilder().removeHeader("User-Agent").header("User-Agent", GlobalProperties.HTTP_USER_AGENT_NAME).build()))
                .build();



        OkHttpClient.Builder builder = client.newBuilder()
                .addNetworkInterceptor(mDefaultHeadersInterceptor)
                .addNetworkInterceptor(new HttpLoggingInterceptor().setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE));
        if (!GlobalProperties.HTTP_UPSTREAM_USE_GZIP) {
            builder.addNetworkInterceptor(new NoGzipInterceptor());
        }
        mApiHttpClient = builder.build();

        try {
            File cacheDir = FileUtils.getFinalCacheDirectory(MyApplication.getStaticAppContext(), GlobalProperties.PICASSO_CACHE_DIR_NAME);
            long cacheMaxSize = FileUtils.calculateDiskCacheSize(cacheDir, GlobalProperties.IMAGE_DISC_CACHE_ALLOWED_PART, GlobalProperties.MIN_IMAGE_DISK_CACHE_SIZE, GlobalProperties.MAX_IMAGE_DISK_CACHE_SIZE);
            mPicassoHttpClient = client.newBuilder().cache(new Cache(cacheDir, cacheMaxSize)).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public OkHttpClient getApiHttpClient() {
        return mApiHttpClient;
    }


    public OkHttpClient getPicassoHttpClient() {
        return mPicassoHttpClient;
    }

    /**********************************************************************************************/
    private final Interceptor mDefaultHeadersInterceptor = (chain) -> {
        Request newRequest = chain.request().newBuilder()
                .header("Connection", "Keep-Alive")
                .header("Accept", "application/json")
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();
        return chain.proceed(newRequest);
    };




    private class NoGzipInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {

            Request newRequest = chain.request().newBuilder().removeHeader("Accept-Encoding").build();



            Log.i(TAG, String.format(GlobalProperties.FORMATTER_LOCALE, "Sending request %s on %s\n%s", newRequest.url(), chain.connection(), newRequest.headers()));
            HttpClientUtils.printLongString(TAG, "Request data - " + HttpClientUtils.getBodyContentFromRequest(TAG, newRequest));

            long tStart = System.nanoTime();
            Response origResp = chain.proceed(newRequest);
            long tEnd = System.nanoTime();


            Object[] objs = HttpClientUtils.getBodyContentFromResponse(TAG, origResp);


            Log.d(TAG, String.format(GlobalProperties.FORMATTER_LOCALE, "Received response for %s in %.1fms\n%s", origResp.request().url(), (tEnd - tStart) / 1e6d, origResp.headers()));
            HttpClientUtils.printLongString(TAG, "Response data - " + objs[1]);

            return (Response)objs[0];
        }
    }
}
