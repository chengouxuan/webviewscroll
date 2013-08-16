package com.example.webviewscroll;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

@SuppressLint("NewApi")
public class MainActivity extends Activity {

	MainPageLayout mContentView = null;
	AddressBar mAddressBar = null;
	WebViewZoom mWebViewZoom = null;
	RelativeLayout.LayoutParams mWebViewLayoutParams = null;
	
	private void setupContentView() {
		if (mContentView != null) {
			return;
		}
		
		mContentView = new MainPageLayout(this);
		mAddressBar = new AddressBar(this);
		mWebViewZoom = new WebViewZoom(this);
		mWebViewZoom.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Log.i("mWebViewZoom", "" + v.toString() + "======" + event.toString());
				return false;
			}
		});
		
		mContentView.addView(mAddressBar, new RelativeLayout.LayoutParams(500, 60));
		mContentView.addView(mWebViewZoom);

		mWebViewZoom.setTopBar(new WebViewZoom.TopBarInterface() {

			@Override
			public int getHeight() {
				return mAddressBar.getLayoutParams().height;
			}
		});
		
		mWebViewZoom.setOnScrollListener(new WebViewZoom.OnContentMovedListener() {
			
			@Override
			public void onContentMoved(int dx, int dy) {
				Log.i("OnScrollListener", "OnScrollListener===== dy = " + dy);
				
				int newBottom = mAddressBar.getContentBottom() + dy;
				if (0 <= newBottom && newBottom <= mAddressBar.getContentHeight()) {
					mAddressBar.moveContent(0, dy);
				}
			}
		});
		
		setContentView(mContentView);
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupContentView();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
