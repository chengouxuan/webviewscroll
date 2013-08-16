package com.example.webviewscroll;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsoluteLayout;

public class AddressBar extends AbsoluteLayout {
	
	AddressBarImpl mAddressBarImpl = null;
	AbsoluteLayout.LayoutParams mAddressBarImplLayoutParams = null;

	public AddressBar(Context context) {
		super(context);
		init();
	}

	public AddressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public AddressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init() {
		
		if (mAddressBarImpl != null) {
			return;
		}
		
		setMinimumHeight(60);
		setMinimumWidth(500);
		setBackgroundColor(0x00000000);
		
		mAddressBarImpl = new AddressBarImpl(getContext());
		mAddressBarImplLayoutParams = new AbsoluteLayout.LayoutParams(60, 500, 0, -60);
		addView(mAddressBarImpl);
	}

	public void moveContent(int dx, int dy) {
		mAddressBarImplLayoutParams.x += dx;
		mAddressBarImplLayoutParams.y += dy;
		requestLayout();
	}

	private static class AddressBarImpl extends View {
		public AddressBarImpl(Context context) {
			super(context);
			init();
		}

		public AddressBarImpl(Context context, AttributeSet attrs) {
			super(context, attrs);
			init();
		}

		public AddressBarImpl(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
			init();
		}
		
		private void init() {
			setMinimumHeight(60);
			setMinimumWidth(500);
			setBackgroundColor(0xff0000ff);
		}
	}

	public int getContentTop() {
		return mAddressBarImplLayoutParams.y;
	}

	public int getContentBottom() {
		return mAddressBarImplLayoutParams.y + mAddressBarImplLayoutParams.height;
	}

	public int getContentHeight() {
		return mAddressBarImplLayoutParams.height;
	}
	
	
}
