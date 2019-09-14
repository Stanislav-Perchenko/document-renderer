package com.webssa.guestbest.config;

import android.os.Looper;

import androidx.lifecycle.MutableLiveData;

import com.webssa.guestbest.config.model.ConfigModel;

class ConfigLiveData extends MutableLiveData<ConfigModel> {



    private final ConfigManager.OnConfigUpdateListener listener = new ConfigManager.OnConfigUpdateListener() {
        private ConfigModel mConf;

        @Override
        public void onUpdateConfig(ConfigModel conf) {
            boolean needUpd;
            synchronized (this) {
                needUpd = !conf.equals(mConf);
                mConf = conf;
            }


            if (needUpd && (Thread.currentThread() == Looper.getMainLooper().getThread())) {
                ConfigLiveData.this.setValue(conf);
            } else if (needUpd) {
                ConfigLiveData.this.postValue(conf);
            }
        }
    };

    @Override
    protected void onActive() {
        ConfigManager.getInstance().requestUpdates(listener);
    }

    @Override
    protected void onInactive() {
        ConfigManager.getInstance().removeUpdates(listener);
    }
}
