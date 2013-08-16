package com.example.webviewscroll;

import java.util.IllegalFormatCodePointException;

import android.R.integer;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsoluteLayout;

public class WebViewZoom extends AbsoluteLayout {

	WebView mWebView = null;
	AbsoluteLayout.LayoutParams mWebViewLayout = null;
	Handler mHandler = null;
	
	interface OnContentMovedListener {
		abstract public void onContentMoved(int dx, int dy);
	}
	
	private OnContentMovedListener mOnContentMovedListener = null;
	
	public void setOnScrollListener(OnContentMovedListener onContentMovedListener) {
		mOnContentMovedListener = onContentMovedListener;
	}
	
	interface TopBarInterface {
		abstract public int getHeight();
	}
	
	private TopBarInterface mTopBar = null;
	
	public void setTopBar(TopBarInterface topBar) {
		mTopBar = topBar;
	}
	
	public WebViewZoom(Context context) {
		super(context);
		init();
	}

	public WebViewZoom(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public WebViewZoom(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		if (mWebView == null) {
			
			mHandler = new Handler();
			
			mWebView = new WebView(getContext()) {
				
				float dragBeginY = 0;
				int contentMovedY = 0;
				
				
				@Override
				public boolean onTouchEvent(MotionEvent event) {
//					Log.i("mWebView", "mWebView========" + event.toString());
					mHandler.removeCallbacks(null);

					Rect rect = new Rect();
					mWebView.getHitRect(rect);
					
					if (mTopBar == null) {
						return super.onTouchEvent(event);
					}
					
					boolean result = false;
					
					int dy = (int)(event.getY() - dragBeginY - contentMovedY + 0.5);
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						dragBeginY = event.getY();
						contentMovedY = 0;
						result = super.onTouchEvent(event);
					} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
						int contentTop = getContentTop() + dy;
						if (contentTop <= 0) {
							dy = -getContentTop();
						} else if (mTopBar.getHeight() <= contentTop) {
							dy = mTopBar.getHeight() - getContentTop();
						}
						contentTop = getContentTop() + dy;
						if (dy > 0 && getScrollY() <= 0 || dy < 0 && getScrollY() > 0) {
							moveContent(0, dy);
							dragBeginY -= dy;
							contentMovedY += dy;
							result = true;
						} else {
							dragBeginY = event.getY();
							contentMovedY = 0;
						}
					}
					if (!result) {
						result = super.onTouchEvent(event);
					}
					return result;
				}
				
				final static int SCROLL_INVALID_VALUE = ~0;
				final static long TIME_INVALID_VALUE = ~0;
				int lastY = SCROLL_INVALID_VALUE;
				long YTimeStampMS = TIME_INVALID_VALUE;
				int lastY2 = SCROLL_INVALID_VALUE;
				long Y2TimeStampMS = TIME_INVALID_VALUE;
				float pxPerSec = 0; 
				
				final static int RUNNABLE_INTERVAL_MS = 1000 / 30;

				private int mTopBarHeight = 0;
				
				@Override
				protected void onScrollChanged(int l, int t, int oldl, int oldt) {
//					Log.i("mWebView", "mWebView========" + l + ", " + t + ", " + oldl + ", " + oldt);
					super.onScrollChanged(l, t, oldl, oldt);
					lastY2 = lastY;
					Y2TimeStampMS = YTimeStampMS;
					lastY = t;
					YTimeStampMS = System.currentTimeMillis();
					
					if (t <= 0 && lastY != SCROLL_INVALID_VALUE && lastY2 != SCROLL_INVALID_VALUE
							&& YTimeStampMS != TIME_INVALID_VALUE && Y2TimeStampMS != TIME_INVALID_VALUE) {
						
						pxPerSec = (1.0f * lastY2 - lastY) * 1000 / (Y2TimeStampMS - YTimeStampMS);

						if (mTopBar != null) {
							mTopBarHeight = mTopBar.getHeight();
						}
						
						mHandler.postDelayed(new Runnable() {
							private void runImpl() {

								float dy = pxPerSec * (RUNNABLE_INTERVAL_MS * 1.0f / 1000);
								pxPerSec -= 10;
								
								if (dy <= 1) {
									pxPerSec = 0;
									lastY = SCROLL_INVALID_VALUE;
									lastY2 = SCROLL_INVALID_VALUE;
									YTimeStampMS = TIME_INVALID_VALUE;
									Y2TimeStampMS = TIME_INVALID_VALUE;
									return;
								}
								
								int contentTop = (int) (getContentTop() + dy + 0.5);
								if (contentTop <= 0) {
									dy = -getContentTop();
								} else if (mTopBar.getHeight() <= contentTop) {
									dy = mTopBar.getHeight() - getContentTop();
								}
								contentTop = (int) (getContentTop() + dy + 0.5);
								
								moveContent(0, (int)(dy + 0.5));
								
								if (getContentTop() < mTopBarHeight) {
									mHandler.postDelayed(new Runnable() {
										@Override
										public void run() {
											runImpl();
										}
									}, RUNNABLE_INTERVAL_MS);
								} else {
									;
								}
							}
							@Override
							public void run() {
								runImpl();
							}
						}, 0);
					}
				}
			};
			mWebViewLayout = new AbsoluteLayout.LayoutParams(500, 600, 0, 0);
			this.addView(mWebView, mWebViewLayout);
			mWebView.loadUrl("http://m.sina.com.cn");
			mWebView.setWebViewClient(new WebViewClient() {
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					view.loadUrl(url);
					return true;
				}
			});
		}
	}
	
	private void moveContent(int dx, int dy) {
		mWebViewLayout.x += dx;
		mWebViewLayout.y += dy;
		requestLayout();
		if (mOnContentMovedListener != null) {
			mOnContentMovedListener.onContentMoved(0, dy);
		}
		Log.i("moveContent", "moveContent, dy = " + dy);
	}

	public int getContentTop() {
		return mWebViewLayout.y;
	}
}
