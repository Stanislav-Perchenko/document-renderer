package com.webssa.guestbest.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.webssa.guestbest.R;
import com.webssa.guestbest.config.ConfigManager;
import com.webssa.guestbest.config.model.ConfigModel;
import com.webssa.guestbest.documents.model.DocumentIdPointer;
import com.webssa.guestbest.documents.model.DocumentRefRestModel;
import com.webssa.guestbest.rest.MyRestService;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends DrawerBaseActivity<DocumentIdPointer> {

    private ConfigModel mConfig;
    private final List<DocumentRefRestModel> mRemoteDocs = new LinkedList<>();

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
    protected Iterator<INavItemDescriptor<DocumentIdPointer>> getNavItemsIterator() {
        if (mConfig == null) {
            return new Iterator<INavItemDescriptor<DocumentIdPointer>>() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public INavItemDescriptor<DocumentIdPointer> next() {
                    throw new NoSuchElementException("0");
                }
            };
        } else {
            return getNavItemsIteratorInternal();
        }
    }

    @Override
    protected boolean onDrawerNavigationItemClicked(DocumentIdPointer item) {
        if (item.isLocal()) {
            Intent i = new Intent(this, DocumentDetailsActivity.class);
            i.putExtra(DocumentDetailsActivity.ARG_LOCAL_DOCUMENT, item.getLocalDocumentName());
            startActivity(i);
        } else {
            Toast.makeText(this, "Remote document: ID = "+item.getDocumentId(), Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        ConfigManager.getInstance().requestRemoteUpdateConfig();
        startLoadingDocumentIndex();
        ConfigManager.getConfig().observe(this, conf -> {
            mConfig = conf;
            requestDrawerNavigationLayout();
        });
    }

    private void startLoadingDocumentIndex() {
        Call<DocumentRefRestModel[]> call =  MyRestService.INSTANCE.getDocumentIndex();
        registerCall(call);
        call.enqueue(new Callback<DocumentRefRestModel[]>() {
            @Override
            public void onResponse(Call<DocumentRefRestModel[]> call, Response<DocumentRefRestModel[]> response) {
                unregisterCall(call);
                if (isDestroyed()) return;
                hideProgress();
                if (response.code() == 200) {
                    mRemoteDocs.clear();
                    mRemoteDocs.addAll(Arrays.asList(response.body()));
                    showLoadComplete();
                    if (mConfig != null) requestDrawerNavigationLayout();
                } else {
                    showError(String.format("%s - %s", response.code(), response.message()));
                }
            }

            @Override
            public void onFailure(Call<DocumentRefRestModel[]> call, Throwable t) {
                unregisterCall(call);
                hideProgress();
                showError(t.getMessage());
            }
        });
    }



    private void showError(String error) {
        //TODO Implement this !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    }

    private void hideProgress() {
        //TODO Implement this !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    }

    private void showLoadComplete() {
        //TODO Implement this !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    }




    private Iterator<INavItemDescriptor<DocumentIdPointer>>  getNavItemsIteratorInternal() {
        final List<INavItemDescriptor<DocumentIdPointer>> lst = new LinkedList<>();
        lst.add(new INavItemDescriptor<DocumentIdPointer>() {
            @Nullable
            @Override
            public DocumentIdPointer getItemAssociatedModel() {
                return null;
            }

            @NonNull
            @Override
            public CharSequence getTitle() {
                return "LOCAL";
            }
        });

        lst.add(new INavItemDescriptor<DocumentIdPointer>() {
            @Nullable
            @Override
            public DocumentIdPointer getItemAssociatedModel() {
                return new DocumentIdPointer(true, "ess203.json", null);
            }

            @NonNull
            @Override
            public CharSequence getTitle() {
                return "ESS203";
            }

            @Nullable
            @Override
            public Uri getIconPath() {
                return mConfig.getIconDocumentMenuItem();
            }
        });

        lst.add(new INavItemDescriptor<DocumentIdPointer>() {
            @Nullable
            @Override
            public DocumentIdPointer getItemAssociatedModel() {
                return null;
            }

            @NonNull
            @Override
            public CharSequence getTitle() {
                return "REMOTE";
            }
        });

        mRemoteDocs.stream().map(dRef -> new INavItemDescriptor<DocumentIdPointer>() {
            @Nullable
            @Override
            public DocumentIdPointer getItemAssociatedModel() {
                return new DocumentIdPointer(false, null, dRef.getDocumentId());
            }

            @NonNull
            @Override
            public CharSequence getTitle() {
                return dRef.getMeta().getDocumentTitle();
            }

            @Nullable
            @Override
            public Uri getIconPath() {
                return mConfig.getIconDocumentMenuItem();
            }
        }).collect(Collectors.toCollection(() -> lst));

        return lst.iterator();
    }

}
