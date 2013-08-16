package com.example.webviewscroll;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class MainPageLayout extends RelativeLayout {

	public MainPageLayout(Context context) {
		super(context);
		init();
	}

	public MainPageLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MainPageLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		setMinimumHeight(60 + 600);
		setMinimumWidth(500);
	}
}
