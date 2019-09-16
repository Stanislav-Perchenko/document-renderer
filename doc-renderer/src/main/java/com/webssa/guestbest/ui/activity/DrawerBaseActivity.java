package com.webssa.guestbest.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.webssa.guestbest.MyApplication;
import com.webssa.guestbest.R;
import com.webssa.guestbest.config.ConfigManager;
import com.webssa.guestbest.config.model.ConfigModel;

import java.util.Iterator;

public abstract class DrawerBaseActivity<NAVMODEL> extends RetrofitCallLifecycleActivity {
    public static final String TAG_LIFECYCLE = "LIFE_CYCLE";


    private Toolbar vToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ViewGroup vDrawer;
    private ViewGroup vDrawerItemsContainer;


    /**
     * Must be overridden by subclasses, so Content View can be set correctly by the BaseActivity.
     * This is important when layout needs to be substituted with DrawerLayout
     *
     * @return
     */
    public abstract int getContentViewId();

    protected abstract boolean shouldAddActionBar();

    @Nullable
    protected abstract String getScreenTitle();

    @Nullable
    protected abstract String getScreenSubtitle();

    @NonNull
    protected abstract Iterator<INavItemDescriptor<NAVMODEL>> getNavItemsIterator();


    /**
     * This method is called when a user clicks on a navigation item in the Drawer menu
     * @param item Data model item associated with this navigation item
     * @return true if this event was consumed and the Drawer must be closed
     */
    protected boolean onDrawerNavigationItemClicked(NAVMODEL item) {
        return true;
    }


    private boolean pendingNavLayoutRequest;
    protected final void requestDrawerNavigationLayout() {
        if (vDrawerItemsContainer != null) {
            pendingNavLayoutRequest = false;
            vDrawerItemsContainer.post(this::onLayoutDrawerNavigation);
        } else {
            pendingNavLayoutRequest = true;
        }
    }



    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View vFocused = getCurrentFocus();
        if (vFocused instanceof EditText) {
            int scrcoords[] = new int[2];
            vFocused.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + vFocused.getLeft() - scrcoords[0];
            float y = ev.getRawY() + vFocused.getTop() - scrcoords[1];
            if (ev.getAction() == MotionEvent.ACTION_UP
                    && (x < vFocused.getLeft() || x >= vFocused.getRight()
                    || y < vFocused.getTop() || y > vFocused.getBottom())) {
                if (onDispatchTouchOutsideEditableField((EditText) vFocused)) {
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    protected boolean onDispatchTouchOutsideEditableField(EditText edt) {
        return false;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG_LIFECYCLE, getClass().getSimpleName()+"->onCreate()");
        int contViewId = getContentViewId();
        if (contViewId > 0) {
            LayoutInflater inflater = LayoutInflater.from(this);
            View contentView = inflater.inflate(R.layout.activity_nav_drawer, null, false);
            mDrawerLayout = (DrawerLayout) contentView;
            inflater.inflate(contViewId, (ViewGroup) contentView.findViewById(R.id.content_frame), true);

            View vTb = contentView.findViewById(R.id.toolbar);
            if (vTb == null && shouldAddActionBar()) {
                vTb = inflater.inflate(R.layout.my_toolbar, null, false);

                LinearLayout layout = new LinearLayout(this);
                layout.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                llp.weight = 0;
                layout.addView(vTb, llp);
                llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                llp.weight = 1;
                layout.addView(contentView, llp);
                contentView = layout;
            }

            if (vTb instanceof Toolbar) {
                vToolbar = (Toolbar) vTb;
                setSupportActionBar((Toolbar) vTb);
            }

            setContentView(onContentViewCreated(contentView), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        } else {
            throw new IllegalStateException("No content resource from child Activity");
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            setupActionBar(actionBar);
        }
    }

    protected View onContentViewCreated(View contentView) {
        return contentView;
    }

    private void setupActionBar(ActionBar actionBar) {
        actionBar.setDisplayHomeAsUpEnabled(true);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, 0, 0);
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerToggle.getDrawerArrowDrawable().setSpinEnabled(false);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        actionBar.setHomeButtonEnabled(true);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Log.d(TAG_LIFECYCLE, getClass().getSimpleName()+"->onPostCreate()");
        vDrawer = (ViewGroup) findViewById(R.id.left_drawer);
        vDrawerItemsContainer = (ViewGroup) vDrawer.findViewById(R.id.nav_items_container);
        if (pendingNavLayoutRequest) {
            requestDrawerNavigationLayout();
        }

        ConfigManager.getConfig().observe(this, this::setupStyle);

        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }

        updateTitleAndSubtitle();
    }

    protected boolean updateTitleAndSubtitle() {
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            final String title = getScreenTitle();
            final String subtitle = getScreenSubtitle();
            ab.setTitle((title == null) ? "" : title);
            ab.setSubtitle((subtitle == null) ? "" : subtitle);
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG_LIFECYCLE, getClass().getSimpleName()+"->onNewIntent()");
    }


    private ConfigModel mConfig;

    private void setupStyle(ConfigModel conf) {
        mConfig = conf;
        if (vDrawer != null) vDrawer.setBackgroundColor(conf.getColorPrimary().getIntColor());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(conf.getColorPrimary().getIntColor()));
            int textColor = conf.getTextColorPrimary().getIntColor();
            if (vToolbar != null) {
                vToolbar.setTitleTextColor(textColor);
                vToolbar.setSubtitleTextColor(textColor);
            }
            if (mDrawerToggle != null) mDrawerToggle.getDrawerArrowDrawable().setColor(textColor);
        }

        for (int i=0; i<vDrawerItemsContainer.getChildCount(); i++) {
            int color = conf.getTextColorPrimary().getIntColor();
            ((TextView) vDrawerItemsContainer.getChildAt(i).findViewById(R.id.title)).setTextColor(color);
            if (conf.isMenuIconColorAsText()) {
                ((ImageView) vDrawerItemsContainer.getChildAt(i).findViewById(R.id.icon)).setImageTintList(ColorStateList.valueOf(color));
            }
        }
    }

    private void onLayoutDrawerNavigation() {
        vDrawerItemsContainer.removeAllViews();
        LayoutInflater inflater = getLayoutInflater();
        for (Iterator<INavItemDescriptor<NAVMODEL>> itr = getNavItemsIterator(); itr.hasNext(); ) {
            INavItemDescriptor<NAVMODEL> navItemDescr = itr.next();

            if (navItemDescr.getItemAssociatedModel() == null) {
                addSectionTitle(inflater, navItemDescr.getTitle());
            } else {
                addNavigationItem(inflater, navItemDescr);
            }
        }
    }

    private void addSectionTitle(LayoutInflater inflater, CharSequence title) {
        inflater.inflate(R.layout.drawer_nav_section_title, vDrawerItemsContainer, true);
        View child = vDrawerItemsContainer.getChildAt(vDrawerItemsContainer.getChildCount()-1);
        TextView tv = child.findViewById(R.id.title);
        tv.setText(title);
        if (mConfig != null) {
            int color = mConfig.getTextColorPrimary().getIntColor();
            tv.setTextColor(color);
            child.findViewById(R.id.divider).setBackgroundColor(color);
        }
    }

    private void addNavigationItem(LayoutInflater inflater, INavItemDescriptor<NAVMODEL> navItemDescr) {
        inflater.inflate(R.layout.drawer_nav_item, vDrawerItemsContainer, true);
        View vNavItem = vDrawerItemsContainer.getChildAt(vDrawerItemsContainer.getChildCount()-1);
        inflater.inflate(R.layout.drawer_nav_items_divider, vDrawerItemsContainer, true);



        TextView tv = vNavItem.findViewById(R.id.title);
        ImageView iv = vNavItem.findViewById(R.id.icon);

        if (mConfig != null) {
            int color = mConfig.getTextColorPrimary().getIntColor();
            tv.setTextColor(color);
            if (mConfig.isMenuIconColorAsText()) iv.setImageTintList(ColorStateList.valueOf(color));
        }

        tv.setText(navItemDescr.getTitle());

        if (navItemDescr.getIconBitmap() != null) {
            iv.setImageBitmap(navItemDescr.getIconBitmap());
        } else if (navItemDescr.getIconDrawable() != null) {
            iv.setImageDrawable(navItemDescr.getIconDrawable());
        } else if (navItemDescr.getIconResourceId() != null) {
            iv.setImageResource(navItemDescr.getIconResourceId());
        } else if (navItemDescr.getIconPath() != null) {
            int sz = getResources().getDimensionPixelSize(R.dimen.drawer_nav_item_icon_size);
            MyApplication.getPicasso().load(navItemDescr.getIconPath()).resize(sz, sz).centerCrop().into(iv);
        } else {
            iv.setImageDrawable(null);
        }

        if (navItemDescr.getItemAssociatedModel() != null) {
            vNavItem.setTag(navItemDescr.getItemAssociatedModel());
            vNavItem.setClickable(true);
            vNavItem.setOnClickListener(v -> {
                if (onDrawerNavigationItemClicked((NAVMODEL) v.getTag())) {
                    mDrawerLayout.closeDrawers();
                }
            });
        } else {
            vNavItem.setClickable(false);
        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        //----  Close vDrawer if opened  ----

        if (!closeDrawerForBackPressed()) {
            super.onBackPressed();
        }
    }


    /**
     * Checks if DrawerLayout is available and opened and close it.
     * @return true if the Drawer was closed.
     */
    protected boolean closeDrawerForBackPressed() {
        if (mDrawerLayout != null && vDrawer != null && mDrawerLayout.isDrawerVisible(vDrawer)) {
            mDrawerLayout.closeDrawers();
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG_LIFECYCLE, getClass().getSimpleName()+"->onStart()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG_LIFECYCLE, getClass().getSimpleName()+"->onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG_LIFECYCLE, getClass().getSimpleName()+"->onDestroy()");
    }



    /**********************************************************************************************/
    public interface INavItemDescriptor<T> {

        @Nullable T getItemAssociatedModel();
        @NonNull CharSequence getTitle();

        @Nullable
        default Uri getIconPath() {
            return null;
        }
        @Nullable
        default Integer getIconResourceId() {
            return null;
        }

        @Nullable
        default Drawable getIconDrawable() {
            return null;
        }


        @Nullable
        default Bitmap getIconBitmap() {
            return null;
        }
    }
}
