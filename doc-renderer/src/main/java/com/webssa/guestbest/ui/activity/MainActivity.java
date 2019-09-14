package com.webssa.guestbest.ui.activity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.webssa.guestbest.R;
import com.webssa.guestbest.config.ConfigManager;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class MainActivity extends DrawerBaseActivity<String> {

    @Override
    public int getContentViewId() {
        return R.layout.activity_main;
    }

    @Override
    protected boolean shouldAddActionBar() {
        return true;
    }

    @Nullable
    @Override
    protected String getScreenTitle() {
        return "Main Screen"; //TODO Change this with resources !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    }

    @Nullable
    @Override
    protected String getScreenSubtitle() {
        return null;
    }

    @NonNull
    @Override
    protected Iterator<INavItemDescriptor<String>> getNavItemsIterator() {
        return new Iterator<INavItemDescriptor<String>>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public INavItemDescriptor<String> next() {
                throw new NoSuchElementException("0");
            }
        };
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        ConfigManager.getInstance().requestRemoteUpdateConfig();
    }
}
