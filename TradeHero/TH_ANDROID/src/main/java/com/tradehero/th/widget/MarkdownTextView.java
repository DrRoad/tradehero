package com.tradehero.th.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import com.tradehero.common.text.OnElementClickListener;
import com.tradehero.common.text.RichTextCreator;
import javax.inject.Inject;

/** Created with IntelliJ IDEA. User: tho Date: 9/17/13 Time: 11:18 AM Copyright (c) TradeHero */
public class MarkdownTextView extends TextView implements OnElementClickListener
{
    @Inject RichTextCreator parser;

    private OnElementClickListener onElementClickListener;

    //<editor-fold desc="Constructors">
    public MarkdownTextView(Context context)
    {
        this(context, null);
    }

    public MarkdownTextView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public MarkdownTextView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    //</editor-fold>

    @Override public void setText(CharSequence text, BufferType type)
    {
        if (parser != null)
        {
            text = parser.load(text.toString().trim()).create();
        }
        super.setText(text, type);
    }

    @Override public void onClick(View textView, String data, String key, String[] matchStrings)
    {
        onElementClickListener.onClick(textView, data, key, matchStrings);
    }

    public void setOnElementClickListener(OnElementClickListener listener)
    {
        this.onElementClickListener = listener;
    }

    @Override public boolean onTouchEvent(MotionEvent event)
    {
        super.onTouchEvent(event);
        return false;
    }


}
