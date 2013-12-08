package com.giorgioaresu.batchrenamer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class HeaderView extends View {
    private String mText = "HeaderView"; // TODO: use a default from R.string...
    private int mTextColor = Color.RED; // TODO: use a default from R.color...
    private float mTextDimension = 0; // TODO: use a default from R.dimen...
    private Drawable mDrawable;

    private TextPaint mTextPaint;
    private float mTextWidth;
    private float mTextHeight;

    public HeaderView(Context context) {
        super(context);
        init(null, 0);
    }

    public HeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public HeaderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.HeaderView, defStyle, 0);

        try {
            if (a.hasValue(R.styleable.HeaderView_text)) {
                mText = a.getString(
                        R.styleable.HeaderView_text);
            }

            mTextColor = a.getColor(
                    R.styleable.HeaderView_textColor,
                    mTextColor);
            // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
            // values that should fall on pixel boundaries.
            mTextDimension = a.getDimension(
                    R.styleable.HeaderView_textDimension,
                    mTextDimension);

            if (a.hasValue(R.styleable.HeaderView_drawable)) {
                mDrawable = a.getDrawable(
                        R.styleable.HeaderView_drawable);
                mDrawable.setCallback(this);
            }
        } finally {
            a.recycle();
        }

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();
    }

    private void invalidateTextPaintAndMeasurements() {
        mTextPaint.setTextSize(mTextDimension);
        mTextPaint.setColor(mTextColor);
        mTextWidth = mTextPaint.measureText(mText);

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextHeight = fontMetrics.bottom;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        // Draw the text.
        canvas.drawText(mText,
                paddingLeft + (contentWidth - mTextWidth) / 2,
                paddingTop + (contentHeight + mTextHeight) / 2,
                mTextPaint);

        // Draw the example drawable on top of the text.
        if (mDrawable != null) {
            mDrawable.setBounds(paddingLeft, paddingTop,
                    paddingLeft + contentWidth, paddingTop + contentHeight);
            mDrawable.draw(canvas);
        }
    }

    /**
     * Gets the text attribute value.
     * @return The text attribute value.
     */
    public String getText() {
        return mText;
    }

    /**
     * Sets the text attribute value.
     * @param text The text attribute value.
     */
    public void setText(String text) {
        mText = text;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the textColor attribute value.
     * @return The textColor attribute value.
     */
    public int getTextColor() {
        return mTextColor;
    }

    /**
     * Sets the view's textColor attribute value. This color is the font color.
     * @param color The color attribute value to use.
     */
    public void setTextColor(int color) {
        mTextColor = color;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the textDimension attribute value.
     * @return The dimension attribute value.
     */
    public float getTextDimension() {
        return mTextDimension;
    }

    /**
     * Sets the view's text dimension attribute value. This dimension
     * is the font size.
     * @param dimension The text dimension attribute value to use.
     */
    public void setTextDimension(float dimension) {
        mTextDimension = dimension;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the drawable attribute value.
     * @return The drawable attribute value.
     */
    public Drawable getDrawable() {
        return mDrawable;
    }

    /**
     * Sets the view's drawable attribute value. This drawable is drawn
     * on the right.
     * @param drawable The example drawable attribute value to use.
     */
    public void setExampleDrawable(Drawable drawable) {
        mDrawable = drawable;
    }
}
