package com.farasource.component.dropdown;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.res.ResourcesCompat;

public class DropdownView extends LinearLayout {

    private LinearLayout titleLinearLayout;
    private AppCompatImageView arrow;
    private TextView title;
    private TextView content;
    private View divider;
    private Drawable cardBackground, cardBackgroundExpanded, titleBackgroundExpanded;
    private float cardCornerRadius, elevation;
    private int titleTextColor, titleTextColorExpanded, contentTextColor;
    private int arrowTint, arrowTintExpanded;
    private int titleHeight, contentHeight, dividerHeight;
    private boolean useDivider, expanded, isMoving;
    @RotationModel
    private int arrowRotation;

    public DropdownView(@NonNull Context context) {
        this(context, null);
    }

    public DropdownView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DropdownView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
        TypedArray typed = context.obtainStyledAttributes(attrs, R.styleable.DropdownView, defStyleAttr, 0);
        elevation = typed.getDimension(R.styleable.DropdownView_elevation, getElevation());
        cardCornerRadius = typed.getDimension(R.styleable.DropdownView_cardCornerRadius, dpf(10));
        cardBackground = getMultiColourAttr(getContext(), typed, R.styleable.DropdownView_cardBackground, null);
        cardBackgroundExpanded = getMultiColourAttr(getContext(), typed, R.styleable.DropdownView_cardBackgroundExpanded, cardBackground);
        int resourceId = typed.getResourceId(R.styleable.DropdownView_arrow, R.drawable.ic_round_arrow_right_24);
        Drawable arrowIcon = ResourcesCompat.getDrawable(context.getResources(), resourceId, null);
        arrowTint = typed.getColor(R.styleable.DropdownView_arrowTint, Color.WHITE);
        arrowTintExpanded = typed.getColor(R.styleable.DropdownView_arrowTintExpanded, arrowTint);
        arrowRotation = typed.getInteger(R.styleable.DropdownView_arrowRotation, QUARTER);
        useDivider = typed.getBoolean(R.styleable.DropdownView_useDivider, false);
        dividerHeight = (int) typed.getDimension(R.styleable.DropdownView_dividerHeight, dpf(1));
        int dividerColor = typed.getColor(R.styleable.DropdownView_dividerColor, 0xffe2e2e2);
        titleBackgroundExpanded = getMultiColourAttr(getContext(), typed, R.styleable.DropdownView_titleBackgroundExpanded, null, true);
        String titleText = typed.getString(R.styleable.DropdownView_titleText);
        titleTextColor = typed.getColor(R.styleable.DropdownView_titleTextColor, Color.WHITE);
        titleTextColorExpanded = typed.getColor(R.styleable.DropdownView_titleTextColorExpanded, titleTextColor);
        float titleTextSize = typed.getDimension(R.styleable.DropdownView_titleTextSize, dpf(17));
        String titleTextFont = typed.getString(R.styleable.DropdownView_titleTextFont);
        boolean titleTextBold = typed.getBoolean(R.styleable.DropdownView_titleTextBold, true);
        String contentText = typed.getString(R.styleable.DropdownView_contentText);
        contentTextColor = typed.getColor(R.styleable.DropdownView_contentTextColor, Color.WHITE);
        float contentTextSize = typed.getDimension(R.styleable.DropdownView_contentTextSize, dpf(17));
        String contentTextFont = typed.getString(R.styleable.DropdownView_contentTextFont);
        boolean contentTextBold = typed.getBoolean(R.styleable.DropdownView_contentTextBold, false);
        expanded = typed.getBoolean(R.styleable.DropdownView_expanded, false);
        typed.recycle();
        titleLinearLayout = new LinearLayout(context);
        titleLinearLayout.setOrientation(HORIZONTAL);
        arrow = new AppCompatImageView(context);
        arrow.setImageDrawable(arrowIcon);
        title = new TextView(context);
        divider = new View(context);
        content = new TextView(context);
        content.setAutoLinkMask(Linkify.ALL);
        initTextView(title, titleText, titleTextSize, titleTextFont, titleTextBold);
        initTextView(content, contentText, contentTextSize, contentTextFont, contentTextBold);
        initView(dividerColor);
        if (isInEditMode()) {
            if (expanded) {
                arrow.setRotation(arrowRotation);
            } else {
                divider.setVisibility(GONE);
                content.setHeight(0);
            }
            onDropDownChange();
        }
        setElevation(elevation);
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());
        bundle.putBoolean("expanded", expanded);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            expanded = bundle.getBoolean("expanded");
            super.onRestoreInstanceState(bundle.getParcelable("superState"));
            return;
        }
        super.onRestoreInstanceState(state);
    }

    public void setExpanded(boolean expanded) {
        setExpanded(expanded, false);
    }

    public void setExpanded(boolean expanded, boolean animate) {
        if (this.expanded == expanded || isMoving) {
            return;
        }
        expandOrCollapseContent(expanded, animate);
    }

    @Override
    public void setElevation(float elevation) {
        this.elevation = dpf(elevation);
        super.setElevation(elevation);
    }

    public void setCardCornerRadius(float radius) {
        if (this.cardCornerRadius == radius) {
            return;
        }
        this.cardCornerRadius = radius;
        if (cardBackground instanceof PaintDrawable) {
            PaintDrawableSetCornerRadius((PaintDrawable) cardBackground, true);
        }
        if (cardBackgroundExpanded instanceof PaintDrawable) {
            PaintDrawableSetCornerRadius((PaintDrawable) cardBackgroundExpanded, true);
        }
        if (titleBackgroundExpanded instanceof PaintDrawable) {
            PaintDrawableSetCornerRadius((PaintDrawable) titleBackgroundExpanded, false);
        }
        setBackground(expanded ? cardBackground : cardBackgroundExpanded);
        titleLinearLayout.setBackground(expanded ? null : titleBackgroundExpanded);
    }

    public void setTitleBackgroundColor(@ColorInt int color) {
        setCardBackgroundColor(color);
    }

    public void setCardBackgroundColor(@ColorInt int color) {
        setCardBackground(createColorDrawable(color, true));
    }

    public void setCardBackground(Drawable drawable) {
        if (this.cardBackground == cardBackgroundExpanded) {
            cardBackgroundExpanded = drawable;
            if (expanded) {
                setBackground(drawable);
            }
        }
        this.cardBackground = drawable;
        if (!expanded) {
            setBackground(drawable);
        }
    }

    public void setCardBackgroundColorExpanded(@ColorInt int color) {
        setCardBackgroundExpanded(createColorDrawable(color, true));
    }

    public void setCardBackgroundExpanded(Drawable drawable) {
        this.cardBackgroundExpanded = drawable;
        if (expanded) {
            setBackground(drawable);
        }
    }

    public void setTitleBackgroundColorExpanded(@ColorInt int color) {
        setTitleBackgroundExpanded(createColorDrawable(color, false));
    }

    public void setTitleBackgroundExpanded(Drawable drawable) {
        this.titleBackgroundExpanded = drawable;
        if (expanded) {
            titleLinearLayout.setBackground(drawable);
        }
    }

    public void setArrow(@DrawableRes int resId) {
        arrow.setImageResource(resId);
    }

    public void setArrow(Drawable drawable) {
        arrow.setImageDrawable(drawable);
    }

    public void setArrowTint(@ColorInt int color) {
        if (this.arrowTint == color) {
            return;
        }
        if (this.arrowTint == arrowTintExpanded) {
            arrowTintExpanded = color;
            if (expanded) {
                if (Color.TRANSPARENT == color) {
                    arrow.clearColorFilter();
                } else {
                    arrow.setColorFilter(color);
                }
            }
        }
        this.arrowTint = color;
        if (Color.TRANSPARENT == color) {
            arrow.clearColorFilter();
        } else if (!expanded) {
            arrow.setColorFilter(color);
        }
    }

    public void setArrowTintExpanded(@ColorInt int color) {
        if (this.arrowTintExpanded == color) {
            return;
        }
        this.arrowTintExpanded = color;
        if (expanded) {
            arrow.setColorFilter(color);
        }
    }

    /**
     * @param arrowRotation use {@link #QUARTER} or {@link #HALF} or {@link #REVERSE_QUARTER} instead
     */
    public void setArrowRotation(@RotationModel int arrowRotation) {
        if (this.arrowRotation == arrowRotation) {
            return;
        }
        this.arrowRotation = arrowRotation;
        if (expanded) {
            arrow.setRotation(arrowRotation);
        }
    }

    public void setUseDivider(boolean useDivider) {
        if (this.useDivider == useDivider) {
            return;
        }
        this.useDivider = useDivider;
        divider.setVisibility(useDivider ? VISIBLE : GONE);
    }

    public void setDividerHeight(int height) {
        height = (int) dpf(height);
        if (dividerHeight == height) {
            return;
        }
        dividerHeight = height;
        divider.setLayoutParams(createLinear(LayoutParams.MATCH_PARENT, dividerHeight));
    }

    public void setDividerColor(@ColorInt int color) {
        divider.setBackgroundColor(color);
    }

    public void setTitleText(String title) {
        this.title.setText(title);
    }

    public void setTitleTextColor(@ColorInt int color) {
        this.titleTextColor = color;
        if (!expanded) {
            title.setTextColor(color);
        }
    }

    public void setTitleTextColorExpanded(@ColorInt int color) {
        this.titleTextColorExpanded = color;
        if (expanded) {
            title.setTextColor(color);
        }
    }

    public void setTitleTextSize(int size) {
        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
    }

    public void setTitleTypeface(Typeface titleTypeface) {
        setTitleTypeface(titleTypeface, true);
    }

    public void setTitleTypeface(Typeface titleTypeface, boolean titleTextBold) {
        title.setTypeface(titleTypeface, titleTextBold ? Typeface.BOLD : Typeface.NORMAL);
    }

    public void setContentText(String content) {
        this.content.setText(content);
        this.content.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void setHtmlContent(String content) {
        this.content.setText(textToHtml(content), TextView.BufferType.SPANNABLE);
        this.content.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void setContentTextColor(@ColorInt int color) {
        this.contentTextColor = color;
        content.setTextColor(color);
    }

    public void setContentTextSize(int size) {
        content.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
    }

    public void setContentTypeface(Typeface contentTypeface) {
        setContentTypeface(contentTypeface, false);
    }

    public void setContentTypeface(Typeface contentTypeface, boolean contentTextBold) {
        content.setTypeface(contentTypeface, contentTextBold ? Typeface.BOLD : Typeface.NORMAL);
    }

    /**
     * @deprecated use {@link #setCardBackground(Drawable)} or {@link #setCardBackgroundExpanded(Drawable)}
     */
    @Override
    @Deprecated
    public void setBackground(Drawable background) {
        super.setBackground(background);
    }

    /**
     * @deprecated use {@link #setCardBackgroundColor(int)} or {@link #setCardBackgroundColorExpanded(int)}
     */
    @Override
    @Deprecated
    public void setBackgroundColor(int color) {
        setCardBackgroundColor(color);
    }

    /**
     * @deprecated use {@link #setCardBackgroundColor(int)} or {@link #setCardBackgroundColorExpanded(int)}
     */
    @Override
    @Deprecated
    public void setBackgroundTintList(@Nullable ColorStateList tint) {
        setCardBackgroundColor(tint.getDefaultColor());
    }

    /**
     * @deprecated use {@link #setCardBackground(Drawable)} or {@link #setCardBackgroundExpanded(Drawable)}
     */
    @Override
    @Deprecated
    public void setBackgroundResource(int resid) {
        setCardBackground(ResourcesCompat.getDrawable(getResources(), resid, null));
    }

    /**
     * @deprecated use {@link #setUseDivider(boolean)}
     */
    @Deprecated
    @Override
    public void setShowDividers(int showDividers) {
        setUseDivider(showDividers != SHOW_DIVIDER_NONE);
    }

    @Override
    public void setDividerDrawable(Drawable divider) {
        if (this.divider == null) {
            return;
        }
        this.divider.setBackground(divider);
    }

    public void attachViewTo(ViewGroup root, int mv, int mh) {
        attachViewTo(root, mv, mh, mv, mh);
    }

    public void attachViewTo(ViewGroup root, int ms, int mt, int me, int mb) {
        if (root != null) {
            root.addView(this, createLinear(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT,
                    0, Gravity.NO_GRAVITY, ms, mt, me, mb));
        }
    }

    private void initTextView(TextView textView, String text, float textSize,
                              String textFont, boolean textBold) {
        if (!TextUtils.isEmpty(text)) {
            textView.setText(text);
        }
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        if (TextUtils.isEmpty(textFont)) {
            textView.setTypeface(textView.getTypeface(), textBold ? Typeface.BOLD : Typeface.NORMAL);
        } else {
            try {
                Typeface typeface = Typeface.createFromAsset(getResources().getAssets(), textFont);
                textView.setTypeface(typeface, textBold ? Typeface.BOLD : Typeface.NORMAL);
            } catch (Exception e) {
                //
            }
        }
    }

    private void initView(int dividerColor) {
        content.setTextColor(contentTextColor);
        content.setMovementMethod(LinkMovementMethod.getInstance());
        titleLinearLayout.setOnClickListener(v -> setExpanded(!expanded, true));
        title.setMaxLines(2);
        title.setEllipsize(TextUtils.TruncateAt.END);
        titleLinearLayout.addView(title, createLinear(0, LayoutParams.WRAP_CONTENT, 1, Gravity.CENTER_VERTICAL));
        titleLinearLayout.setPadding((int) dpf(10), 0, (int) dpf(10), 0);
        titleLinearLayout.addView(arrow, createLinear((int) dpf(24), (int) dpf(24), 0, Gravity.CENTER_VERTICAL, 10, 0, 0, 0));
        titleHeight = (int) dpf(60);
        addView(titleLinearLayout, createLinear(LayoutParams.MATCH_PARENT, titleHeight));
        divider.setBackgroundColor(dividerColor);
        divider.setVisibility(useDivider ? VISIBLE : GONE);
        addView(divider, createLinear(LayoutParams.MATCH_PARENT, dividerHeight, 0, Gravity.NO_GRAVITY, 0, 0, 0, 5));
        content.setPadding((int) dpf(10), 0, (int) dpf(10), (int) dpf(10));
        addView(content, createLinear(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        post(() -> expandOrCollapseContent(expanded, false));
    }

    private void requestHeight() {
        int widthMS = MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.AT_MOST);
        int heightMS = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        content.measure(widthMS, heightMS);
        contentHeight = (int) (content.getMeasuredHeight() + dpf(10));
    }

    @Keep
    private void setHeight(int height) {
        getLayoutParams().height = height;
        requestLayout();
    }

    private void onDropDownChange() {
        if (expanded) {
            setBackground(cardBackgroundExpanded);
            title.setTextColor(titleTextColorExpanded);
            titleLinearLayout.setBackground(titleBackgroundExpanded);
            if (Color.TRANSPARENT == arrowTintExpanded) {
                arrow.clearColorFilter();
            } else {
                arrow.setColorFilter(arrowTintExpanded);
            }
        } else {
            setBackground(cardBackground);
            title.setTextColor(titleTextColor);
            titleLinearLayout.setBackground(null);
            if (Color.TRANSPARENT == arrowTint) {
                arrow.clearColorFilter();
            } else {
                arrow.setColorFilter(arrowTint);
            }
        }
    }

    private Spanned textToHtml(String content) {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N ? Html.fromHtml(
                content,
                Html.FROM_HTML_MODE_LEGACY
        ) : Html.fromHtml(content);
    }

    private void expandOrCollapseContent(boolean expanded, boolean animate) {
        this.expanded = expanded;
        requestHeight();
        int height = titleHeight + contentHeight;
        if (animate) {
            isMoving = true;
            if (expanded) {
                onDropDownChange();
            }
            ObjectAnimator valueAnimator = ObjectAnimator.ofInt(this, "height",
                    expanded ? titleHeight : height, expanded ? height : titleHeight);
            valueAnimator.setDuration(250);
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    isMoving = false;
                    if (!expanded) {
                        onDropDownChange();
                    }
                }
            });
            valueAnimator.start();
            arrow.animate().rotation(expanded ? arrowRotation : 0).setDuration(250).start();
        } else {
            if (expanded) {
                arrow.setRotation(arrowRotation);
                setHeight(height);
            } else {
                arrow.setRotation(0);
                setHeight(titleHeight);
            }
            onDropDownChange();
        }
    }

    private Drawable getMultiColourAttr(@NonNull Context context,
                                        @NonNull TypedArray typed,
                                        int index,
                                        Drawable def) {
        return getMultiColourAttr(context, typed, index, def, false);
    }

    private Drawable getMultiColourAttr(@NonNull Context context,
                                        @NonNull TypedArray typed,
                                        int index,
                                        Drawable def,
                                        boolean force) {
        int color = Color.TRANSPARENT;
        try {
            color = typed.getColor(index, Color.TRANSPARENT);
        } catch (Exception e) {
            //
        }
        if (color == Color.TRANSPARENT) {
            Drawable drawable = typed.getDrawable(index);
            if (drawable != null) return drawable;
            else if (def != null || force) return def;
            color = fetchPrimaryColor();
        }
        return createColorDrawable(color, !force);
    }

    private int fetchPrimaryColor() {
        TypedValue outValue = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.colorPrimary, outValue, true);
        if (outValue.data > Color.TRANSPARENT) {
            return outValue.data;
        }
        return 0XFF607D8B;
    }

    private Drawable createColorDrawable(int color, boolean allCorners) {
        PaintDrawable drawable = new PaintDrawable();
        PaintDrawableSetCornerRadius(drawable, allCorners);
        PaintDrawableSetColor(drawable, color);
        return drawable;
    }

    private void PaintDrawableSetColor(PaintDrawable drawable, int color) {
        drawable.getPaint().setColor(color);
    }

    private void PaintDrawableSetCornerRadius(PaintDrawable drawable, boolean allCorners) {
        if (cardCornerRadius == 0) {
            return;
        }
        if (allCorners) {
            drawable.setCornerRadius(cardCornerRadius);
        } else {
            drawable.setCornerRadii(new float[]{cardCornerRadius, cardCornerRadius, cardCornerRadius, cardCornerRadius, 0, 0, 0, 0});
        }
    }

    private float dpf(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    private LinearLayout.LayoutParams createLinear(int w, int h) {
        return createLinear(w, h, 0, Gravity.NO_GRAVITY);
    }

    private LinearLayout.LayoutParams createLinear(int w, int h, int weight, int gravity) {
        return createLinear(w, h, weight, gravity, 0, 0, 0, 0);
    }

    private LinearLayout.LayoutParams createLinear(int w, int h, int weight, int gravity, int ms, int mt, int me, int mb) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(w, h);
        lp.weight = weight;
        lp.gravity = gravity;
        lp.topMargin = (int) dpf(mt);
        lp.bottomMargin = (int) dpf(mb);
        lp.setMarginStart((int) dpf(ms));
        lp.setMarginEnd((int) dpf(me));
        return lp;
    }

    public final static int QUARTER = 90;
    public final static int HALF = -180;
    public final static int REVERSE_QUARTER = -90;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            QUARTER,
            HALF,
            REVERSE_QUARTER,
    })
    public @interface RotationModel {}
}