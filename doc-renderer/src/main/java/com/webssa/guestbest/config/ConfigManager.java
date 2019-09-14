package com.webssa.guestbest.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.webssa.guestbest.GlobalProperties;
import com.webssa.guestbest.MyApplication;
import com.webssa.guestbest.config.model.ConfigModel;
import com.webssa.guestbest.rest.MyRestService;
import com.webssa.guestbest.rest.helper.ParserProvider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class ConfigManager {
    public static final String LOG_TAG = ConfigManager.class.getSimpleName();
    public static final boolean D = GlobalProperties.D;

    public static final String PREF_CONFIG = "config_json";

    private final SharedPreferences sPrefs;
    private String mConfigJson;
    private ConfigModel mConfig;

    private final Set<OnConfigUpdateListener> mObservers = new HashSet<>();

    public interface OnConfigUpdateListener {
        void onUpdateConfig(ConfigModel conf);
    }

    public void requestUpdates(OnConfigUpdateListener l) {
        boolean upd;
        synchronized (mObservers) {
            upd = mObservers.add(l);
        }
        if (upd && mConfig != null) {
            l.onUpdateConfig(mConfig);
        }
    }

    public void removeUpdates(OnConfigUpdateListener l) {
        synchronized (mObservers) {
            mObservers.remove(l);
        }
    }

    private ConfigManager(Context ctx) {
        sPrefs = ctx.getSharedPreferences(getClass().getSimpleName(), Context.MODE_PRIVATE);
        mConfigJson = sPrefs.getString(PREF_CONFIG, null);
        if (mConfigJson != null) {
            try {
                mConfig = ParserProvider.getRestApiMoshi().adapter(ConfigModel.class).fromJson(mConfigJson);
                //mLiveData.updateConfig(mConfig);
            } catch (Exception e) {
                mConfigJson = null;
            }
        }
    }

    /**
     * Call this to start remote update config from the backend.
     * The MyRestService@getConfig() will be called asynchronously.
     * The loaded Config will be saved by this manager and all LiveData will be notified with the update.
     */
    public void requestRemoteUpdateConfig() {
        if (D) Log.d(LOG_TAG, "---> Start Config remote update");
        MyRestService.INSTANCE.getConfig().enqueue(new Callback<ConfigModel>() {
            @Override
            public void onResponse(Call<ConfigModel> call, Response<ConfigModel> response) {
                if (response.code() >= 200 && response.code() < 300) {
                    if (response.body() != null) {
                        if (D) Log.d(LOG_TAG, "<--- Remote Config update OK");
                        setConfig(response.body());
                    } else {
                        if (D) Log.e(LOG_TAG, "<~~~ Remote Config update failed: No config entity was returned");
                    }
                } else {
                    if (D) Log.e(LOG_TAG, String.format("<~~~ Remote Config update failed: %d %s", response.code(), response.message()));
                }
            }

            @Override
            public void onFailure(Call<ConfigModel> call, Throwable t) {
                if (D) Log.e(LOG_TAG, "<~~~ Remote Config update failed: "+t.getMessage());
                t.printStackTrace();
            }
        });
    }

    boolean setConfig(@NonNull ConfigModel config) {
        if (!config.equals(mConfig)) {
            long tStart = System.currentTimeMillis();
            String configJson = ParserProvider.getRestApiMoshi().adapter(ConfigModel.class).toJson(config);
            long dt = System.currentTimeMillis() - tStart;
            sPrefs.edit().putString(PREF_CONFIG, configJson).apply();
            mConfig = config;
            mConfigJson = configJson;

            List<OnConfigUpdateListener> obss;
            synchronized (mObservers) {
                obss = new ArrayList<>(mObservers.size());
                obss.addAll(mObservers);
            }
            for (OnConfigUpdateListener obs : obss) {
                obs.onUpdateConfig(config);
            }
            return true;
        } else {
            return false;
        }
    }


    public static LiveData<ConfigModel> getConfig() {
        return new ConfigLiveData();
    }

    /**********************************************************************************************/
    /**********************************************************************************************/
    private static ConfigManager INSTANCE;

    public static ConfigManager getInstance() {
        if (INSTANCE == null) {
            synchronized (ConfigManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ConfigManager(MyApplication.getStaticAppContext());
                }
            }
        }
        return INSTANCE;
    }
}
