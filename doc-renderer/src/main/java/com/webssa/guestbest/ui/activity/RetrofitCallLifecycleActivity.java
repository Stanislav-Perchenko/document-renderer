package com.webssa.guestbest.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import java.util.LinkedHashSet;
import java.util.Set;

import retrofit2.Call;

public class RetrofitCallLifecycleActivity extends AppCompatActivity {

    private final Set<Call<?>> mRetrofitCalls = new LinkedHashSet<>();

    protected void registerCall(Call<?> c) {
        if (!isDestroyed()) {
            mRetrofitCalls.add(c);
        }
    }

    protected void unregisterCall(Call<?> c) {
        mRetrofitCalls.remove(c);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRetrofitCalls.stream().filter(c -> !(c.isCanceled() || c.isExecuted()) ).forEach(c -> c.cancel());
        mRetrofitCalls.clear();
    }
}
