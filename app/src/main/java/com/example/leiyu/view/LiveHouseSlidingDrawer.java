package com.example.leiyu.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.OverScroller;
import android.widget.RelativeLayout;

import com.example.leiyu.myprocessor.R;
import com.example.leiyu.util.UIUtil;


/**
 * Created by tiansj on 16/4/18.
 */
public class LiveHouseSlidingDrawer extends RelativeLayout implements View.OnClickListener {

    private static final String TAG = "LiveHouseSlidingDrawer";
    private static final int DEFAULT_TB_SHOW_DURATION = 300;    // 显示滑动时间ms
    private static final int DEFAULT_TB_HIDE_DURATION = 150;    // 隐藏滑动时间ms
    private static final int DEFAULT_LR_SHOW_DURATION = 200;    // 显示滑动时间ms
    private static final int MIN_DISTANCE_FOR_FLING = 100; // dips
    private static final int MIN_FLING_VELOCITY = 400;    // 左右滑动初始有效速率 dips转换

    private VelocityTracker mVelocityTracker;
    private OverScroller mScroller;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private int mVerticalRange;
    private int mHorizontalRange;
    private int mTouchSlop;
    private int mFlingDistance;

    private RelativeLayout mLayoutSliderContainer;
    private LinearLayout mPrevLayout;
    private ImageView mPrevImageView;
    private LinearLayout mNextLayout;
    private ImageView mNextImageView;

    private Bitmap defaultBlurBitmap;
    private Bitmap prevBlurBitmap;
    private Bitmap nextBlurBitmap;

    private boolean mIsContentViewShow = false;
    private boolean isPrevImageShow = false;
    private boolean isNextImageShow = false;
    private boolean isTbEnabled = true;
    private boolean isLrEnabled = true;
    private boolean isClearScreen = false;

    private int mScreenWidth = 0;
    private int mContentViewHeight = 0;

    private int mStartY = 0;
    private int mEndY = 0;
    private int mScrollY = 0;

    private int mStartX = 0;
    private int mEndX = 0;
    private int mScrollX = 0; // 负左正右（相对down位置）

    private StatusListener mStatusListener;

    private Bitmap mBlurBitMap; // 高斯模糊之后的bitmap

    public void setStatusListener(StatusListener statusListener) {
        mStatusListener = statusListener;
    }

    public interface StatusListener {
        void onPrevViewShow(Bitmap bitmap);

        void onNextViewShow(Bitmap bitmap);
    }

    public LiveHouseSlidingDrawer(Context context) {
        super(context);
        init(context);
    }

    public LiveHouseSlidingDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LiveHouseSlidingDrawer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        this.setBackgroundColor(Color.argb(0, 0, 0, 0));
        setSoundEffectsEnabled(false);
        setClickable(true);
        setOnClickListener(this);

        mScroller = new OverScroller(context);
        mScreenWidth = UIUtil.getScreenWidth(context);
        mContentViewHeight = UIUtil.getScreenHeight(context) - UIUtil.getStatusBarHeight(context);

        final float density = context.getResources().getDisplayMetrics().density;
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mFlingDistance = (int) (MIN_DISTANCE_FOR_FLING * density);
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);  // 大约44px，和viewpager一致
        mMinimumVelocity = (int) (MIN_FLING_VELOCITY * density); // mMinimumVelocity=1100
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity(); // mMaximumVelocity=22000
//        mMinimumVelocity=configuration.getScaledMinimumFlingVelocity();
        mVerticalRange = mContentViewHeight / 4;
        mHorizontalRange = mScreenWidth / 3;
        defaultBlurBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.lf_viewer_nohead_default);
    }


    public void init(RelativeLayout container) {
        mLayoutSliderContainer = container; // 外部container
        LayoutInflater.from(getContext()).inflate(R.layout.lf_vsd_layout_slider_view, this, true);
        mPrevLayout = (LinearLayout) findViewById(R.id.lf_rw_layout_prev);
        mPrevImageView = (ImageView) findViewById(R.id.lf_rw_imageView_prev);
        mPrevImageView.setImageBitmap(defaultBlurBitmap);
        mNextLayout = (LinearLayout) findViewById(R.id.lf_rw_layout_next);
        mNextImageView = (ImageView) findViewById(R.id.lf_rw_imageView_next);
        mNextImageView.setImageBitmap(defaultBlurBitmap);
        this.post(new Runnable() {
            @Override
            public void run() {
                mContentViewHeight = mLayoutSliderContainer.getHeight();
                mVerticalRange = mContentViewHeight / 4; // 重新设置Range
                // 初始化隐藏两个View
                mPrevLayout.scrollTo(0, mContentViewHeight);
                mNextLayout.scrollTo(0, -mContentViewHeight);
            }
        });
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        mScrollX = mEndX - mStartX;
        mScrollY = mEndY - mStartY;

        // 重置状态
        if (mPrevLayout.getScrollY() >= mContentViewHeight && mNextLayout.getScrollY() <= -mContentViewHeight) {
            isPrevImageShow = isNextImageShow = false;
        }
        if (mLayoutSliderContainer.getScrollX() == 0 || mLayoutSliderContainer.getScrollX() == -mScreenWidth) {
            mIsContentViewShow = false;
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartX = mEndX = (int) ev.getX();
                mStartY = mEndY = (int) ev.getY();
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                // 判断方向
                if (!mIsContentViewShow && !isPrevImageShow && !isNextImageShow) { // 初始化状态
                    mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    float mVelocityX = mVelocityTracker.getXVelocity();
                    float mVelocityY = mVelocityTracker.getYVelocity();
                    if (Math.abs(mScrollX) > Math.abs(mScrollY)) { // X方向滑动
                        if (!(mLayoutSliderContainer.getScrollX() == 0 && mScrollX < 0)) { // 防止左滑超出边界
                            if (Math.abs(mScrollX) > mTouchSlop && Math.abs(mVelocityY / mVelocityX) <= Math.sqrt(3) / 3) {
                                mIsContentViewShow = true;
                            }
                        }
                    } else { // Y方向滑动
                        if (Math.abs(mScrollY) > mTouchSlop) {
                            if (mVelocityY > 0 && Math.abs(mVelocityX / mVelocityY) <= Math.sqrt(3) / 3) {  // 方向是正下方
                                isPrevImageShow = true;
                            } else if (mVelocityY < 0 && Math.abs(mVelocityX / mVelocityY) <= Math.sqrt(3) / 3) {   // 方向是正上方
                                isNextImageShow = true;
                            }
                        }
                    }
                }

                // 移动View
                if ((isPrevImageShow || isNextImageShow) && !mIsContentViewShow) {
                    handleTbActionMove((int) ev.getY());
                } else if (mIsContentViewShow && !isPrevImageShow && !isNextImageShow) {
                    handleLrActionMove((int) ev.getX());
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                // 决定现实还是隐藏
                if ((isPrevImageShow || isNextImageShow) && !mIsContentViewShow) {
                    handleTbActionEnd();
                } else if (mIsContentViewShow && !isPrevImageShow && !isNextImageShow) {
                    handleLrActionEnd();
                }
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
            default:
                break;
        }
        mEndX = (int) ev.getX();
        mEndY = (int) ev.getY();
        if (isPrevImageShow || isNextImageShow || mIsContentViewShow) {
            return true;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 滑动中手势View处理
     */
    private void handleTbActionMove(int actionY) {
        if (!isTbEnabled) {
            return;
        }
        if (isPrevImageShow) {
            mPrevLayout.scrollBy(0, -(actionY - mEndY));
        } else if (isNextImageShow) {
            mNextLayout.scrollBy(0, -(actionY - mEndY));
        }
    }

    /**
     * 滑动结束View处理
     */
    private void handleTbActionEnd() {
        if (!isTbEnabled) {
            return;
        }
        mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
        if (isPrevImageShow) {
            if ((mVelocityTracker.getYVelocity() >= mMinimumVelocity && mScrollY > mFlingDistance) || mScrollY > mVerticalRange) {
                // 手势向下滑动，松开手后，显示View
                setTbEnabled(false);
                startScroller(mPrevLayout.getScrollY(), -mPrevLayout.getScrollY(), DEFAULT_TB_SHOW_DURATION);
                if (mStatusListener != null) {
                    //为了防止因消息队列太多导致回调慢的问题,进而页面暂停问题
                    getHandler().removeCallbacksAndMessages(null);
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mStatusListener.onPrevViewShow(prevBlurBitmap);
                        }
                    }, DEFAULT_TB_SHOW_DURATION + 30);
                }
            } else {
                // 手势向下滑动，松开手后，隐藏View
                startScroller(mPrevLayout.getScrollY(), mContentViewHeight - mPrevLayout.getScrollY(), DEFAULT_TB_HIDE_DURATION);
            }
        } else if (isNextImageShow) {
            if ((-mVelocityTracker.getYVelocity() >= mMinimumVelocity && Math.abs(mScrollY) > mFlingDistance) || Math.abs(mScrollY) > mVerticalRange) {
                // 手势向上滑动，松开手后，显示NextImageView
                setTbEnabled(false);
                startScroller(mNextLayout.getScrollY(), -mNextLayout.getScrollY(), DEFAULT_TB_SHOW_DURATION);
                if (mStatusListener != null) {
                    //为了防止因消息队列太多导致回调慢的问题,进而页面暂停问题
                    getHandler().removeCallbacksAndMessages(null);
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mStatusListener.onNextViewShow(nextBlurBitmap);
                        }
                    }, DEFAULT_TB_SHOW_DURATION + 30);
                }
            } else {
                // 手势向上滑动，松开手后，恢复显示NextImageView
                startScroller(mNextLayout.getScrollY(), -(mContentViewHeight + mNextLayout.getScrollY()), DEFAULT_TB_HIDE_DURATION);
            }
        }
    }

    /**
     * Scroller滑动处理封装
     *
     * @param startY
     * @param dy
     */
    private void startScroller(int startY, int dy, int duration) {
        mScroller.startScroll(0, startY, 0, dy, duration); // dy 正值向上，负值向下
        invalidate();   // 通知UI线程的更新
    }

    /**
     * 左右滑动中手势View处理
     */
    private void handleLrActionMove(int actionX) {
        if (!isLrEnabled) {
            return;
        }
        // View滑动的X距离，屏幕可见区域是，0 到 -screenWidth
        int viewScrollX = mLayoutSliderContainer.getScrollX();
        if (viewScrollX <= 0 && viewScrollX >= -mScreenWidth) {
            int x = -(actionX - mEndX);
            if (x > 0) { // 处理左滑中边界超出问题
                if (Math.abs(viewScrollX) < Math.abs((x))) {
                    x = Math.abs(viewScrollX);
                }
            }
            mLayoutSliderContainer.scrollBy(x, 0);
        }
    }

    // 速率+位置+手势方向，决定view最终显示位置
    private void handleLrActionEnd() {
        if (!isLrEnabled) {
            return;
        }
        mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
        float xVelocity = mVelocityTracker.getXVelocity(); // 向右>0，向左<0
        int viewScrollX = mLayoutSliderContainer.getScrollX(); // View滑动的X距离，屏幕可见区域是，0 到 -screenWidth
        if (Math.abs(xVelocity) >= mMinimumVelocity) { // 优先速率判断
            if (xVelocity > 0) { // 向右
                hideContentView();
            } else {  // 向左
                showContentView();
            }
        } else {
            if (viewScrollX > -mHorizontalRange) { // view位置判断
                showContentView();
            } else if (viewScrollX < (-mScreenWidth + mHorizontalRange)) { // view移动到屏幕右侧隐藏
                hideContentView();
            } else { // 手势方向 >0向右
                if (mScrollX > 0) { // 手势向右滑动
                    hideContentView();
                } else {
                    showContentView();
                }
            }
        }
    }

    private void showContentView() {
        startLrScroller(mLayoutSliderContainer.getScrollX(), -mLayoutSliderContainer.getScrollX());
        if (isClearScreen) {
            isClearScreen = false;
        }
    }

    private void hideContentView() {
        startLrScroller(mLayoutSliderContainer.getScrollX(), -(mScreenWidth + mLayoutSliderContainer.getScrollX()));
        if (!isClearScreen) {
            isClearScreen = true;
        }
    }

    /**
     * Scroller滑动处理封装
     *
     * @param startX
     * @param dx     正值向左，负值向右
     */
    private void startLrScroller(int startX, int dx) {
        mScroller.startScroll(startX, 0, dx, 0, DEFAULT_LR_SHOW_DURATION);
        invalidate();
    }

    /**
     * Scroller滑动中对View的处理
     */
    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            if (isPrevImageShow) {
                mPrevLayout.scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            } else if (isNextImageShow) {
                mNextLayout.scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            }
            if (mIsContentViewShow) {
                mLayoutSliderContainer.scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            }
            postInvalidate();
        }
        super.computeScroll();
    }

    /**
     * 外部调用，隐藏 PrevView or NextView
     */
    public void dismiss() {
        mPrevLayout.scrollTo(0, mContentViewHeight);
        mNextLayout.scrollTo(0, -mContentViewHeight);
    }

    /**
     * 设置是否可上下滑动
     *
     * @param isEnabled
     */
    public void setTbEnabled(boolean isEnabled) {
        this.isTbEnabled = isEnabled;
    }

    /**
     * 获取是否可上下滑动
     */
    public boolean isTbEnabled() {
        return isTbEnabled;
    }

    /**
     * 设置是否可左右滑动
     *
     * @param isEnabled
     */
    public void setLrEnabled(boolean isEnabled) {
        this.isLrEnabled = isEnabled;
    }

    /**
     * 更新PrevImageView图片
     *
     * @param url
     */
    public void updatePrevImageView(final String url) {
        prevBlurBitmap = defaultBlurBitmap;
        if(null == url || "".equals(url)){
            updatePrevImageView();
        }else{
            /*ImageLoader.getInstance().loadImage(url, new ImageSize(50, 50), new SimpleImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    updatePrevImageView();
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    if (loadedImage != null) {
                        try {
                            mBlurBitMap = LFBlur.doBlur(getContext(), loadedImage, 10); //mBlurAlgorithm.get().blur(loadedImage, 10);
                            if (null == mBlurBitMap) {
                                return;
                            }
                            // prev bitmap
                            prevBlurBitmap = mBlurBitMap; //mBlurBitMap;
                            updatePrevImageView();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });*/
        }
    }

    /**
     * 更新NextImageView图片
     *
     * @param url
     */
    public void updateNextImageView(final String url) {
        nextBlurBitmap = defaultBlurBitmap;
        if(null == url || "".equals(url)){
            updateNextImageView();
        }else{
            /*ImageLoader.getInstance().loadImage(url, new ImageSize(50, 50), new SimpleImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    updateNextImageView();
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    if (loadedImage != null) {
                        try {
                            mBlurBitMap = LFBlur.doBlur(getContext(), loadedImage, 10); //mBlurAlgorithm.get().blur(loadedImage, 10);
                            if (null == mBlurBitMap) {
                                return;
                            }
                            // 设置next bitmap
                            nextBlurBitmap = mBlurBitMap; //mBlurBitMap;
                            updateNextImageView();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });*/
        }
    }

    private void updatePrevImageView() {
        this.post(new Runnable() {
            @Override
            public void run() {
                mPrevImageView.setImageBitmap(prevBlurBitmap);
            }
        });
    }

    private void updateNextImageView() {
        this.post(new Runnable() {
            @Override
            public void run() {
                mNextImageView.setImageBitmap(nextBlurBitmap);
            }
        });
    }

    public boolean isClearScreen() {
        return isClearScreen;
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }


}