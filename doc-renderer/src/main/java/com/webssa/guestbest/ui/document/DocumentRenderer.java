package com.webssa.guestbest.ui.document;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.webssa.guestbest.MyApplication;
import com.webssa.guestbest.R;
import com.webssa.guestbest.documents.model.DocCellRestModel;
import com.webssa.guestbest.documents.model.DocMetaRestModel;
import com.webssa.guestbest.documents.model.DocSectionRestModel;
import com.webssa.guestbest.documents.model.DocSectionRowRestModel;
import com.webssa.guestbest.documents.model.DocStyleRestModel;
import com.webssa.guestbest.documents.model.DocStyleTextAlign;
import com.webssa.guestbest.documents.model.DocStyleVerticalAlign;
import com.webssa.guestbest.documents.model.DocumentRestModel;
import com.webssa.guestbest.utils.ViewUtils;
import com.webssa.library.widget.AnimatedOpenerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static android.widget.LinearLayout.LayoutParams.MATCH_PARENT;
import static android.widget.LinearLayout.LayoutParams.WRAP_CONTENT;

public class DocumentRenderer {

    private final int HEADER_PADDING_V, HEADER_PADDING_H;
    private final DocStyleRestModel defltHeaderStyle;

    private final Context context;
    private final ViewGroup vContainer;

    private final List<FoldableSectionDescriptor> mFoldableSections = new ArrayList<>();
    private boolean rendered;

    private final List<SectionTocItem> mTocItems = new ArrayList<>();

    private DocumentRenderer(ViewGroup container) {
        this.context = container.getContext();
        this.vContainer = container;
        Resources res = context.getResources();
        HEADER_PADDING_V = res.getDimensionPixelSize(R.dimen.document_header_padding_v);
        HEADER_PADDING_H = res.getDimensionPixelSize(R.dimen.document_header_padding_h);
        defltHeaderStyle = DocStyleRestModel.Companion.getDEFAULT_TEXT_STYLE();
    }

    public static DocumentRenderer forContainer(LinearLayout ll) {
        if (ll.getContext() == null) {
            throw new IllegalArgumentException("Context must be set for the View container");
        } else if (ll.getOrientation() != LinearLayout.VERTICAL) {
            throw new IllegalArgumentException("The LinearLAyout container must have vertical orientation");
        } else {
            return new DocumentRenderer(ll);
        }
    }

    public DocumentRenderer renderDocument(DocumentRestModel doc) {
        if (isRendered()) clear();
        renderMeta(doc.getMeta());
        int index = 0;
        for (DocSectionRestModel section : doc.getSections()) {
            renderSection(++index, section);
        }
        rendered = true;
        return this;
    }


    public void clear() {
        vContainer.removeAllViews();
        mFoldableSections.clear();
        mTocItems.clear();
        vImages.clear();
        rendered = false;
    }

    public boolean isRendered() {
        return rendered;
    }

    @NonNull
    public List<SectionTocItem> getTOC() {
        return Collections.unmodifiableList(mTocItems);
    }

    private void renderMeta(DocMetaRestModel meta) {
        //TODO Implement this
    }

    private void renderSection(int id, DocSectionRestModel section) {
        // Create and add section container
        LinearLayout v_section = new LinearLayout(context);
        v_section.setOrientation(LinearLayout.VERTICAL);
        vContainer.addView(v_section, MATCH_PARENT, WRAP_CONTENT);

        if (section.getSectionHeader() != null) {
            final SectionTocItem tocItem = SectionTocItem.getInstance("section-"+id, section.getSectionHeader().getTitle());
            mTocItems.add(tocItem);
            //v_section.setTag(tocItem);
            v_section.getViewTreeObserver().addOnGlobalLayoutListener(() -> ((SectionTocItemImpl) tocItem).setTop(v_section.getTop()));
        }


        View v_header;
        AnimatedOpenerView vAnimHeader = null;
        if ((section.getSectionHeader() != null) && section.isFoldable()) {
            v_header = vAnimHeader = AnimatedOpenerView.builder(context)
                    .setTitle(section.getSectionHeader().getTitle())
                    .setAngleOpened(0)
                    .setAngleClosed(180)
                    .setInitialOpened(!section.isFolded())
                    .setPaddingVertical(HEADER_PADDING_V)
                    .setPaddingHorizontal(HEADER_PADDING_H)
                    .setTextToMarkSpace(HEADER_PADDING_V)
                    .setIsFullViewClickable(true)
                    .build();
            applyStyleToText(vAnimHeader.getTitleTextView(), (section.getSectionHeader().getStyle() != null) ? section.getSectionHeader().getStyle() : defltHeaderStyle, false);
        } else if (section.getSectionHeader() != null) {
            v_header = new FrameLayout(context);
            v_header.setPadding(0, HEADER_PADDING_V, 0, HEADER_PADDING_V);

            TextView tv = new TextView(context);
            tv.setText(section.getSectionHeader().getTitle());
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
            lp.leftMargin = lp.rightMargin = HEADER_PADDING_H;
            ((FrameLayout) v_header).addView(tv, lp);

            applyStyleToText(tv, (section.getSectionHeader().getStyle() != null) ? section.getSectionHeader().getStyle() : defltHeaderStyle, false);
        } else {
            v_header = null;
        }

        if (v_header != null) {
            applyStyleToView(v_header, (section.getSectionHeader().getStyle() != null) ? section.getSectionHeader().getStyle() : defltHeaderStyle);

            if (section.getSectionHeader().getMarginTop() == 0) {
                v_section.addView(v_header, MATCH_PARENT, WRAP_CONTENT);
            } else {
                LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
                llp.topMargin = Math.round(ViewUtils.dp2px(context.getResources(), section.getSectionHeader().getMarginTop()));
                v_section.addView(v_header, llp);
            }
        }


        LinearLayout v_rows = new LinearLayout(context);
        v_rows.setOrientation(LinearLayout.VERTICAL);
        for (DocSectionRowRestModel row : section.getRows()) {
            renderRow(row, v_rows);
        }
        v_section.addView(v_rows, MATCH_PARENT, WRAP_CONTENT);


        if (vAnimHeader != null) {
            mFoldableSections.add(new FoldableSectionDescriptor(v_section, vAnimHeader, v_rows));
        }
    }

    public static View applyStyleToView(View v, DocStyleRestModel style) {
        v.setBackgroundColor((style.getBgColor() == null) ? Color.TRANSPARENT : style.getBgColor().getIntColor());
        if (v instanceof LinearLayout && ((LinearLayout) v).getOrientation() == LinearLayout.HORIZONTAL) {
            int grav;
            switch (style.getVerticalAlign()) {
                case BOT:
                    grav = Gravity.BOTTOM; break;
                case MID:
                    grav = Gravity.CENTER_VERTICAL; break;
                case TOP:
                default:
                    grav = Gravity.TOP;
            }
            ((LinearLayout) v).setGravity(grav);
        }
        return v;
    }

    public static TextView applyStyleToText(TextView tv, DocStyleRestModel style, boolean useVertAlign) {
        if (style.getTextColor() != null) {
            tv.setTextColor(style.getTextColor().getIntColor());
        }
        Integer tsz = style.getTextSize();
        if (tsz != null && tsz > 0) {
            tv.setTextSize(tsz);
        }
        switch (style.getTextWeight()) {
            case BOLD:
                tv.setTypeface(null, Typeface.BOLD);
                break;
            case REGULAR:
            default:
                tv.setTypeface(null, Typeface.NORMAL);
        }


        if (useVertAlign && style.getTextAlign() == DocStyleTextAlign.CENTER && style.getVerticalAlign() == DocStyleVerticalAlign.MID) {
            tv.setGravity(Gravity.CENTER);
        } else {
            int grav = 0;
            switch (style.getTextAlign()) {
                case RIGHT:
                    grav |= Gravity.RIGHT;
                    break;
                case CENTER:
                    grav |= Gravity.CENTER_HORIZONTAL;
                    break;
                case LEFT:
                    grav |= Gravity.LEFT;
            }

            if (useVertAlign) {
                switch (style.getVerticalAlign()) {
                    case BOT:
                        grav |= Gravity.BOTTOM;
                        break;
                    case MID:
                        grav |= Gravity.CENTER_VERTICAL;
                        break;
                    case TOP:
                        grav |= Gravity.TOP;
                        break;
                }
            }

            tv.setGravity(grav);
        }

        //TODO Support text decoration property !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        return tv;
    }


    private void renderRow(final DocSectionRowRestModel row, ViewGroup rowsContainer) {



        if (row.getCells().isEmpty()) {
            TextView tv = new TextView(context);
            tv.setText("{empty row}");
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            tv.setPadding(32, 32, 32, 32);
            rowsContainer.addView(tv, MATCH_PARENT, WRAP_CONTENT);
        } else {
            final LinearLayout v_row = new LinearLayout(context);
            v_row.setOrientation(LinearLayout.HORIZONTAL);
            v_row.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    v_row.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    renderCellsOnMeasuredRow_wrapSupport(row.getCells(), v_row);
                    loadImages();
                }
            });

            if (row.getMarginTop() > 0) {
                ViewGroup.MarginLayoutParams mlp = new ViewGroup.MarginLayoutParams(MATCH_PARENT, WRAP_CONTENT);
                mlp.topMargin = Math.round(ViewUtils.dp2px(context.getResources(), row.getMarginTop()));
                rowsContainer.addView(v_row, mlp);
            } else {
                rowsContainer.addView(v_row, MATCH_PARENT, WRAP_CONTENT);
            }
        }
    }

    private void renderCellsOnMeasuredRow_wrapSupport(List<DocCellRestModel> cells, LinearLayout v_row) {
        final int rowW = v_row.getMeasuredWidth();
        if (rowW <= 0) throw new IllegalStateException("Row is not measured");

        int sumW_precent = 0;
        int numUnweightCells = 0;
        int numWrapContCells = 0;
        String errMsg = null;
        for (DocCellRestModel c : cells) {
            if (c.getWidth() == -1) {
                numWrapContCells ++;
            } else if (c.getWidth() == 0) {
                numUnweightCells ++;
            } else if (c.getWidth() > 0) {
                sumW_precent += c.getWidth();
            } else {
                errMsg = String.format("Wrong cell width value - %d. Cell - %s", c.getWidth(), c.toString());
                break;
            }
        }

        if (errMsg != null) {
            //Skip next checks
        } if (sumW_precent > 100) {
            errMsg = String.format("Sum of all weights > 100. Ncells=%d, sumW=%d", cells.size(), sumW_precent);
        } else if ((sumW_precent == 100) && (numUnweightCells > 0)) {
            errMsg = String.format("Sum of all weights = 100 and has unweighted cells. Ncells=%d, sumW=%d", cells.size(), sumW_precent);
        } else if ((sumW_precent < 100) && (numUnweightCells == 0)) {
            errMsg = String.format("Sum of all weights < 100 and no unweighted cells. Ncells=%d, sumW=%d", cells.size(), sumW_precent);
        }

        if (errMsg != null) {
            TextView tv = new TextView(context);
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            tv.setText(errMsg);
            int ph = (int) ViewUtils.dp2px(context.getResources(), 24);
            int pv = (int) ViewUtils.dp2px(context.getResources(), 6);
            tv.setPadding(ph, pv, ph, pv);
            v_row.addView(tv, new LinearLayout.LayoutParams(0, WRAP_CONTENT, 1));
            return;
        }

        int rowWidthLeft = rowW;
        SparseArray<LinearLayout.LayoutParams> llppp = new SparseArray<>(numWrapContCells);
        SparseArray<View> wrappedCells = new SparseArray<>(numWrapContCells);
        int index;
        if (numWrapContCells > 0) {
            index = 0;
            int wSpec = View.MeasureSpec.makeMeasureSpec(rowW,  View.MeasureSpec.AT_MOST);
            int hSpec = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
            for (DocCellRestModel c : cells) {
                if (c.getWidth() == -1) {
                    View vCell = buildCell(c);
                    wrappedCells.put(index, vCell);
                    vCell.measure(wSpec, hSpec);
                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(vCell.getMeasuredWidth(), WRAP_CONTENT, 0);
                    if (c.getStyle() != null) applyStyleVerticalAlligmentToLayoutParams(c.getStyle(), llp);
                    llppp.put(index, llp);
                    rowWidthLeft -= vCell.getMeasuredWidth();
                }
                index ++;
            }
            if (rowWidthLeft < 0) rowWidthLeft = 0;
        }


        final int freeSpace = (sumW_precent == 100) ? 0 : rowWidthLeft - Math.round(rowWidthLeft * sumW_precent * 1f / 100);
        index = -1;
        for (DocCellRestModel c : cells) {
            index ++;
            if (c.getWidth() == -1) {
                v_row.addView(wrappedCells.get(index), llppp.get(index));
                continue;
            }

            int c_width = (c.getWidth() > 0)
                    ? Math.round(rowWidthLeft * c.getWidth() * 1f / 100)
                    : Math.round(freeSpace * 1f / numUnweightCells);

            int c_height = (c.getHeight() > 0)
                    ? c.getHeight() :
                    (c.getAspectRatio() > 0)
                            ? (int) Math.ceil(c_width * c.getAspectRatio())
                            : 0;

            View vCell = buildCell(c);
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(c_width, (c_height > 0) ? c_height : WRAP_CONTENT, 0);
            if (c.getStyle() != null) applyStyleVerticalAlligmentToLayoutParams(c.getStyle(), llp);
            v_row.addView(vCell, llp);
        }
    }

    private void applyStyleVerticalAlligmentToLayoutParams(DocStyleRestModel style, LinearLayout.LayoutParams llp) {
        switch (style.getVerticalAlign()) {
            case MID:
                llp.gravity = Gravity.CENTER_VERTICAL;
                break;
            case BOT:
                llp.gravity = Gravity.BOTTOM;
                break;
            case TOP:
            default:
                llp.gravity = Gravity.TOP;
        }
    }

    private final List<ImageView> vImages = new LinkedList<>();

    private View buildCell(DocCellRestModel cell) {
        DocStyleRestModel style = cell.getStyle();
        View v;
        if (cell.getOptText() != null) {
            TextView tv = new TextView(context);
            tv.setText(cell.getOptText());
            v = (style == null) ? tv : applyStyleToText(tv, style, cell.getAspectRatio() > 0);
        } else if (cell.getOptImage() != null) {
            ImageView iv = new ImageView(context);
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            iv.setTag(cell.getOptImage());
            vImages.add(iv);
            v = iv;
        } else if (cell.getOptVideo() != null) {
            TextView tv = new TextView(context);
            tv.setGravity(Gravity.CENTER);
            tv.setText("{video}");
            v = tv;
        } else {
            v = new View(context);
        }
        v.setBackgroundColor(((style == null) || (style.getBgColor() == null)) ? Color.TRANSPARENT : style.getBgColor().getIntColor());
        return v;
    }

    private void loadImages() {
        synchronized (vImages) {
            for (ImageView iv : vImages) {
                Uri img = (Uri) iv.getTag();
                MyApplication.getPicasso().load(img).into(iv);
            }
        }
    }


    /**********************************************************************************************/
    public interface SectionTocItem {
        String fingerprint();
        String title();
        int top();

        static SectionTocItem getInstance(String fingerprint, String title) {
            return new SectionTocItemImpl(fingerprint, title);
        }
    }

    private static class SectionTocItemImpl implements SectionTocItem {
        private final String fingerprint;
        private final String title;
        private int top;

        public SectionTocItemImpl(String fingerprint, String title) {
            this.fingerprint = fingerprint;
            this.title = title;
        }

        @Override
        public String fingerprint() {
            return fingerprint;
        }

        @Override
        public String title() {
            return title;
        }

        @Override
        public int top() {
            return top;
        }

        public void setTop(int top) {
            this.top = top;
            Log.d("TOC_ITEM", String.format("%s (%s) -> %s", fingerprint, title, top));
        }
    }
}
