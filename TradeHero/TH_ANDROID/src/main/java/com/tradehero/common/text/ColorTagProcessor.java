package com.tradehero.common.text;

import android.graphics.Color;
import android.text.style.ForegroundColorSpan;
import java.util.regex.Pattern;


public class ColorTagProcessor extends RichSpanTextProcessor
{
    private static final String THMarkdownURegexColor = "\\{(.+?)\\|(.+?)\\}"; /* "{color|text}" = text in specified color */

    @Override public String key()
    {
        return "color";
    }

    @Override public String getExtractionPattern()
    {
        return "$1";
    }

    @Override protected Object getSpanElement(String replacement, String[] matchStrings)
    {
        if (matchStrings.length >= 3) {
            return new ForegroundColorSpan(Color.parseColor(matchStrings[1]));
        } else {
            return null;
        }
    }

    @Override protected Pattern getPattern()
    {
        return Pattern.compile(THMarkdownURegexColor);
    }
}
