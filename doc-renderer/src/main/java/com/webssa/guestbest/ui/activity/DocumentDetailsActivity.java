package com.webssa.guestbest.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.webssa.guestbest.R;
import com.webssa.guestbest.documents.model.DocumentRestModel;
import com.webssa.guestbest.rest.MyRestService;
import com.webssa.guestbest.ui.document.DocumentRenderer;

import java.util.Iterator;
import java.util.NoSuchElementException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DocumentDetailsActivity extends DrawerBaseActivity<DocumentRenderer.SectionTocItem> {
    public static final String ARG_REMOTE_DOCUMENT = "remote_doc";
    public static final String ARG_LOCAL_DOCUMENT  = "local_doc";

    private ScrollView vScrollContainer;
    private LinearLayout vContent;
    private View vProgressOverlay;

    private DocumentRestModel mDocument;

    public DocumentRenderer renderer;


    @Override
    public int getContentViewId() {
        return R.layout.activity_document_details;
    }

    @Override
    protected boolean shouldAddActionBar() {
        return true;
    }

    @Nullable
    @Override
    protected String getScreenTitle() {
        return getString(R.string.screen_title_document_details);
    }

    @Nullable
    @Override
    protected String getScreenSubtitle() {
        return (mDocument == null) ? null : mDocument.getMeta().getDocumentTitle();
    }

    @NonNull
    @Override
    protected Iterator<INavItemDescriptor<DocumentRenderer.SectionTocItem>> getNavItemsIterator() {
        if (renderer != null) {
            return new Iterator<INavItemDescriptor<DocumentRenderer.SectionTocItem>>() {
                private Iterator<DocumentRenderer.SectionTocItem> srcItr = renderer.getTOC().iterator();
                @Override
                public boolean hasNext() {
                    return srcItr.hasNext();
                }

                @Override
                public INavItemDescriptor<DocumentRenderer.SectionTocItem> next() {
                    final DocumentRenderer.SectionTocItem tocItem = srcItr.next();
                    return new INavItemDescriptor<DocumentRenderer.SectionTocItem>() {
                        @Nullable
                        @Override
                        public DocumentRenderer.SectionTocItem getItemAssociatedModel() {
                            return tocItem;
                        }

                        @NonNull
                        @Override
                        public CharSequence getTitle() {
                            return tocItem.title();
                        }

                        @Nullable
                        @Override
                        public Integer getIconResourceId() {
                            return R.drawable.ic_arrow_right_filled_24dp;
                        }
                    };
                }
            };

        } else {
            return new Iterator<INavItemDescriptor<DocumentRenderer.SectionTocItem>>() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public INavItemDescriptor next() {
                    throw new NoSuchElementException();
                }
            };
        }
    }

    @Override
    protected boolean onDrawerNavigationItemClicked(DocumentRenderer.SectionTocItem item) {
        vScrollContainer.smoothScrollTo(0, item.top());
        return true;
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        vScrollContainer = findViewById(R.id.scroll_container);
        vContent = findViewById(R.id.document_content_container);
        vProgressOverlay = findViewById(R.id.progress_overlay);
        renderer = DocumentRenderer.forContainer(vContent);

        if (getIntent().hasExtra(ARG_LOCAL_DOCUMENT)) {
            loadLocalDocument(getIntent().getStringExtra(ARG_LOCAL_DOCUMENT));
            vProgressOverlay.setVisibility(View.VISIBLE);
        } else if (getIntent().hasExtra(ARG_REMOTE_DOCUMENT)) {
            vProgressOverlay.setVisibility(View.VISIBLE);
        } else {
            vProgressOverlay.setVisibility(View.GONE);
        }
    }

    private boolean destroyed;
    @Override
    protected void onDestroy() {
        destroyed = true;
        super.onDestroy();
    }

    private void loadLocalDocument(String docName) {
        MyRestService.INSTANCE.getLocalDocumentByName(docName).enqueue(new Callback<DocumentRestModel>() {
            @Override
            public void onResponse(Call<DocumentRestModel> call, Response<DocumentRestModel> response) {
                if (destroyed) return;
                vProgressOverlay.setVisibility(View.GONE);
                if (response.code() == 200) {
                    renderer.renderDocument(mDocument = response.body());
                    updateTitleAndSubtitle();
                    requestDrawerNavigationLayout();
                } else {
                    Toast.makeText(DocumentDetailsActivity.this, String.format("%d - %s", response.code(), response.message()), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DocumentRestModel> call, Throwable t) {
                if (destroyed) return;
                vProgressOverlay.setVisibility(View.GONE);
                t.printStackTrace();
                Toast.makeText(DocumentDetailsActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }




}
