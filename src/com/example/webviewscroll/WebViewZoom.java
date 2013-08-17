package com.example.webviewscroll;

import java.util.IllegalFormatCodePointException;

import android.R.integer;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsoluteLayout;
import android.widget.OverScroller;
import android.widget.Scroller;

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

				final static int SCROLL_INVALID_VALUE = ~0;
				final static long TIME_INVALID_VALUE = ~0;
				int lastY = SCROLL_INVALID_VALUE;
				long YTimeStampMS = TIME_INVALID_VALUE;
				int lastY2 = SCROLL_INVALID_VALUE;
				long Y2TimeStampMS = TIME_INVALID_VALUE;
				float pxPerSec = 0; 
				
				boolean touching = false;
				
				final static int RUNNABLE_INTERVAL_MS = 1000 / 60;
				final static int SPEED_DECREASE_PER_RUNNABLE = 10003 / (1000 / RUNNABLE_INTERVAL_MS);

				int topBarHeightCache = 0;
				
//				Scroller scroller = null;
//				
//				private void cleanScroller() {
//					if (scroller != null) {
//						scroller.abortAnimation();
//					}
//					scroller = null;
//				}
//				
//				private void setupScroller(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY) {
//					scroller = new Scroller(getContext());
//					scroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
//				}
				
				@Override
				public boolean onTouchEvent(MotionEvent event) {
//					Log.i("mWebView", "mWebView========" + event.toString());
					Rect rect = new Rect();
					mWebView.getHitRect(rect);
					
					if (mTopBar == null) {
						return super.onTouchEvent(event);
					}
					
					boolean result = false;
					
					int dy = (int)(event.getY() - dragBeginY - contentMovedY + 0.5);
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						touching = true;
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
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						touching = false;
					}
					if (!result) {
						result = super.onTouchEvent(event);
					}
					return result;
				}

				@Override
				protected void onScrollChanged(int l, int t, int oldl, int oldt) {
					
					super.onScrollChanged(l, t, oldl, oldt);
					
					
					lastY2 = lastY;
					Y2TimeStampMS = YTimeStampMS;
					lastY = t;
					YTimeStampMS = System.currentTimeMillis();
					

					boolean shouldMoveContent = true;
					
					if (touching) {
						shouldMoveContent = false;
//						Log.i("shouldMoveContent", "1");
					}
					
					if (  shouldMoveContent
					   && lastY != SCROLL_INVALID_VALUE
					   && lastY2 != SCROLL_INVALID_VALUE
					   && YTimeStampMS != TIME_INVALID_VALUE
					   && Y2TimeStampMS != TIME_INVALID_VALUE) {
						pxPerSec = (1.0f * lastY - lastY2) * 1000 / (Y2TimeStampMS - YTimeStampMS);
					} else {
						shouldMoveContent = false;
//						Log.i("shouldMoveContent", "2");
					}
					
					if (shouldMoveContent && pxPerSec > 0) {
						;
					} else {
						shouldMoveContent = false;
//						Log.i("shouldMoveContent", "3");
					}
					
					int guessNextScrollY = t - (lastY2 - lastY);
					
					if (shouldMoveContent && guessNextScrollY <= 0) {
						shouldMoveContent = true;
					} else {
						shouldMoveContent = false;
//						Log.i("shouldMoveContent", "4");
					}

					if (shouldMoveContent && pxPerSec > 0) {
						shouldMoveContent = true;
						flingScroll(0, 0);
//						Log.i("onScrollChanged", String.format("l = %d, t = %d, oldl = %d, oldt = %d", l, t, oldl, oldt));
					} else {
						shouldMoveContent = false;
//						Log.i("shouldMoveContent", "5");
					}
					
					if (shouldMoveContent) {
						
						if (mTopBar != null) {
							topBarHeightCache = mTopBar.getHeight();
						}
						
						mHandler.postDelayed(new Runnable() {
							private void runImpl(int fixDY) {
								
								if (Math.abs(pxPerSec) < 0.5) {
									return;
								}

								float dy = pxPerSec * (RUNNABLE_INTERVAL_MS * 1.0f / 1000) - fixDY;
								pxPerSec -= SPEED_DECREASE_PER_RUNNABLE;
								
								Log.i("runImpl", String.format("dy = %f, pxPerSec = %f", dy, pxPerSec));
								
								int contentTop = (int) (getContentTop() + dy + 0.5);
								if (topBarHeightCache <= contentTop) {
									dy = topBarHeightCache - getContentTop();
								}
								contentTop = (int) (getContentTop() + dy + 0.5);
								
								if (dy > 0) {
									moveContent(0, (int)(dy + 0.5), false);
								}
								
								if (getContentTop() < topBarHeightCache && dy - fixDY > 0) {
									mHandler.postDelayed(new Runnable() {
										@Override
										public void run() {
											runImpl(0);
										}
									}, RUNNABLE_INTERVAL_MS);
								} else {
									pxPerSec = 0;
									lastY = SCROLL_INVALID_VALUE;
									lastY2 = SCROLL_INVALID_VALUE;
									YTimeStampMS = TIME_INVALID_VALUE;
									Y2TimeStampMS = TIME_INVALID_VALUE;
								}
							}
							@Override
							public void run() {
								runImpl(mWebView.getScrollX());
								mWebView.scrollTo(mWebView.getScrollX(), 0);
							}
						}, YTimeStampMS - Y2TimeStampMS);
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
		moveContent(dx, dy, true);
		
	}
	
	private void moveContent(int dx, int dy, boolean requestLayout) {
		mWebViewLayout.x += dx;
		mWebViewLayout.y += dy;
		requestLayout();
		if (mOnContentMovedListener != null) {
			mOnContentMovedListener.onContentMoved(0, dy);
		}
	}

	public int getContentTop() {
		return mWebViewLayout.y;
	}
}
