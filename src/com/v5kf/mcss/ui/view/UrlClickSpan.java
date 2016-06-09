package com.v5kf.mcss.ui.view;

import com.v5kf.mcss.utils.Logger;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;

public class UrlClickSpan extends URLSpan { 
    String text;

    public UrlClickSpan(String url) {
        super(url);
        this.text = url;
    }

    @Override
    public void onClick(View widget) {
    	super.onClick(widget);
//        processHyperLinkClick(text);
    	Logger.d("UrlClickSpan", "点击超链接");
    }
}
