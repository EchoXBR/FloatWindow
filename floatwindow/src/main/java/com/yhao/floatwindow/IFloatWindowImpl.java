package com.yhao.floatwindow;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;

import com.yhao.floatwindow.menu.FloatMenu;
import com.yhao.floatwindow.menu.FloatMenuCfg;
import com.yhao.floatwindow.menu.MenuItem;
import com.yhao.floatwindow.utils.DensityUtil;

/**
 * Created by yhao on 2017/12/22.
 * https://github.com/yhaolpz
 */

public class IFloatWindowImpl extends IFloatWindow {


    public int floatballX, floatballY;
    public int mScreenWidth, mScreenHeight;
    FloatMenu floatMenu;
    boolean isExplan = false;
    WindowManager mWindowManager;
    private FloatWindow.B mB;
    private FloatView mFloatView;
    private FloatLifecycle mFloatLifecycle;
    private boolean isShow;
    private boolean once = true;
    private ValueAnimator mAnimator;
    private TimeInterpolator mDecelerateInterpolator;
    private float downX;
    private float downY;
    private float upX;
    private float upY;
    private boolean mClick = false;
    private int mSlop;

    private IFloatWindowImpl() {

    }

    IFloatWindowImpl(FloatWindow.B b) {
        mB = b;
        if (mB.mMoveType == MoveType.fixed) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                mFloatView = new FloatPhone(b.mApplicationContext, mB.mPermissionListener, this);
            } else {
                mFloatView = new FloatToast(b.mApplicationContext);
            }
        } else {
            mFloatView = new FloatPhone(b.mApplicationContext, mB.mPermissionListener, this);
            initTouchEvent();
            initClickEvent();
        }
        mFloatView.setSize(mB.mWidth, mB.mHeight);
        mFloatView.setGravity(mB.gravity, mB.xOffset, mB.yOffset);
        mFloatView.setView(mB.mView);
        mFloatLifecycle = new FloatLifecycle(mB.mApplicationContext, mB.mShow, mB.mActivities, new LifecycleListener() {
            @Override
            public void onShow() {
                show();
            }

            @Override
            public void onHide() {
                hide();
            }

            @Override
            public void onBackToDesktop() {
                if (!mB.mDesktopShow) {
                    hide();
                }
                if (mB.mViewStateListener != null) {
                    mB.mViewStateListener.onBackToDesktop();
                }
            }
        });
        computeScreenSize();
        initMenu();
    }

    @Override
    public void show() {
        if (once) {
            mFloatView.init();
            once = false;
            isShow = true;
        } else {
            if (isShow) {
                return;
            }
            getView().setVisibility(View.VISIBLE);
            isShow = true;
        }
        if (mB.mViewStateListener != null) {
            mB.mViewStateListener.onShow();
        }
    }

    @Override
    public void hide() {
        if (once || !isShow) {
            return;
        }
        getView().setVisibility(View.INVISIBLE);
        isShow = false;
        if (mB.mViewStateListener != null) {
            mB.mViewStateListener.onHide();
        }
    }

    @Override
    public boolean isShowing() {
        return isShow;
    }

    @Override
    void dismiss() {
        mFloatView.dismiss();
        isShow = false;
        if (mB.mViewStateListener != null) {
            mB.mViewStateListener.onDismiss();
        }
    }

    public int getMenuItemSize() {
        return mB.menuItems != null ? mB.menuItems.size() : 0;
    }

    public void closeMenu() {
        floatMenu.closeMenu();
    }

    public void reset() {
        //            floatBall.setVisibility(View.VISIBLE);
        //            floatBall.postSleepRunnable();
        floatMenu.detachFromWindow(mWindowManager);
    }

    private void inflateMenuItem() {
        floatMenu.removeAllItemViews();
        for (MenuItem item : mB.menuItems) {
            floatMenu.addItem(item);
        }

    }

    /**
     * 如果有菜单，则监听点击 展开关闭菜单
     */
    private void initClickEvent() {
        if (mB.menuItems.size() > 0) {
            mB.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    floatMenu.attachToWindow(mWindowManager);
                }
            });
        }
    }

    private void initMenu() {
        //2 需要显示悬浮菜单
        //2.1 初始化悬浮菜单配置，有菜单item的大小和菜单item的个数
        int menuSize = DensityUtil.dip2px(mB.mApplicationContext, mB.mWidth+200);
        int menuItemSize = DensityUtil.dip2px(mB.mApplicationContext, 40);
        FloatMenuCfg menuCfg = new FloatMenuCfg(menuSize, menuItemSize);
        floatMenu = new FloatMenu(mB.mApplicationContext, this, menuCfg);
    }

    @Override
    public void buildMenu() {
        inflateMenuItem();
    }

    @Override
    public void updateX(int x) {
        floatMenu.detachFromWindow(mWindowManager);
        isExplan = false;
        checkMoveType();
        mB.xOffset = x;
        mFloatView.updateX(x);
    }

    @Override
    public void updateY(int y) {
        floatMenu.detachFromWindow(mWindowManager);
        isExplan = false;
        checkMoveType();
        mB.yOffset = y;
        mFloatView.updateY(y);
    }


    @Override
    public void updateX(int screenType, float ratio) {
        checkMoveType();
        floatMenu.detachFromWindow(mWindowManager);
        isExplan = false;
        mB.xOffset = (int) ((screenType == Screen.width ?
                Util.getScreenWidth(mB.mApplicationContext) :
                Util.getScreenHeight(mB.mApplicationContext)) * ratio);
        mFloatView.updateX(mB.xOffset);

    }

    @Override
    public void updateY(int screenType, float ratio) {
        checkMoveType();
        floatMenu.detachFromWindow(mWindowManager);
        isExplan = false;
        mB.yOffset = (int) ((screenType == Screen.width ?
                Util.getScreenWidth(mB.mApplicationContext) :
                Util.getScreenHeight(mB.mApplicationContext)) * ratio);
        mFloatView.updateY(mB.yOffset);

    }

    @Override
    public int getX() {
        return mFloatView.getX();
    }

    @Override
    public int getY() {
        return mFloatView.getY();
    }

    @Override
    public View getView() {
        mSlop = ViewConfiguration.get(mB.mApplicationContext).getScaledTouchSlop();
        return mB.mView;
    }

    private void checkMoveType() {
        if (mB.mMoveType == MoveType.fixed) {
            throw new IllegalArgumentException("FloatWindow of this tag is not allowed to move!");
        }
    }

    public int getBallSize() {
        int startX = mFloatView.getX();
        int endX = (startX * 2 + mB.mView.getWidth() > Util.getScreenWidth(mB.mApplicationContext)) ?
                mB.mView.getWidth() :
                mB.mView.getWidth() + mB.mSlideLeftMargin;
        return endX;
    }

    private void initTouchEvent() {
        switch (mB.mMoveType) {
            case MoveType.inactive:
                break;
            default:
                getView().setOnTouchListener(new View.OnTouchListener() {
                    float lastX, lastY, changeX, changeY;
                    int newX, newY;

                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {

                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                downX = event.getRawX();
                                downY = event.getRawY();
                                lastX = event.getRawX();
                                lastY = event.getRawY();
                                cancelAnimator();
                                break;
                            case MotionEvent.ACTION_MOVE:
                                changeX = event.getRawX() - lastX;
                                changeY = event.getRawY() - lastY;
                                newX = (int) (mFloatView.getX() + changeX);
                                newY = (int) (mFloatView.getY() + changeY);
                                mFloatView.updateXY(newX, newY);
                                if (mB.mViewStateListener != null) {
                                    mB.mViewStateListener.onPositionUpdate(newX, newY);
                                }
                                lastX = event.getRawX();
                                lastY = event.getRawY();
                                break;
                            case MotionEvent.ACTION_UP:
                                upX = event.getRawX();
                                upY = event.getRawY();
                                mClick = (Math.abs(upX - downX) > mSlop) || (Math.abs(upY - downY) > mSlop);
                                switch (mB.mMoveType) {
                                    case MoveType.slide:
                                        int startX = mFloatView.getX();
                                        int endX = (startX * 2 + v.getWidth() > Util.getScreenWidth(mB.mApplicationContext)) ?
                                                Util.getScreenWidth(mB.mApplicationContext) - v.getWidth() - mB.mSlideRightMargin :
                                                mB.mSlideLeftMargin;
                                        mAnimator = ObjectAnimator.ofInt(startX, endX);
                                        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                            @Override
                                            public void onAnimationUpdate(ValueAnimator animation) {
                                                int x = (int) animation.getAnimatedValue();
                                                mFloatView.updateX(x);
                                                if (mB.mViewStateListener != null) {
                                                    mB.mViewStateListener.onPositionUpdate(x, (int) upY);
                                                }
                                            }
                                        });
                                        startAnimator();
                                        break;
                                    case MoveType.back:
                                        PropertyValuesHolder pvhX = PropertyValuesHolder.ofInt("x", mFloatView.getX(), mB.xOffset);
                                        PropertyValuesHolder pvhY = PropertyValuesHolder.ofInt("y", mFloatView.getY(), mB.yOffset);
                                        mAnimator = ObjectAnimator.ofPropertyValuesHolder(pvhX, pvhY);
                                        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                            @Override
                                            public void onAnimationUpdate(ValueAnimator animation) {
                                                int x = (int) animation.getAnimatedValue("x");
                                                int y = (int) animation.getAnimatedValue("y");
                                                mFloatView.updateXY(x, y);
                                                if (mB.mViewStateListener != null) {
                                                    mB.mViewStateListener.onPositionUpdate(x, y);
                                                }
                                            }
                                        });
                                        startAnimator();
                                        break;
                                    default:
                                        break;
                                }
                                break;
                            default:
                                break;
                        }
                        return mClick;
                    }
                });
        }
    }

    private void startAnimator() {
        if (mB.mInterpolator == null) {
            if (mDecelerateInterpolator == null) {
                mDecelerateInterpolator = new DecelerateInterpolator();
            }
            mB.mInterpolator = mDecelerateInterpolator;
        }
        mAnimator.setInterpolator(mB.mInterpolator);
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimator.removeAllUpdateListeners();
                mAnimator.removeAllListeners();
                mAnimator = null;
                if (mB.mViewStateListener != null) {
                    mB.mViewStateListener.onMoveAnimEnd();
                }
            }
        });
        mAnimator.setDuration(mB.mDuration).start();
        if (mB.mViewStateListener != null) {
            mB.mViewStateListener.onMoveAnimStart();
        }
    }

    private void cancelAnimator() {
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }
    }

    public void computeScreenSize() {
        mWindowManager = (WindowManager) mB.mApplicationContext.getSystemService(Context.WINDOW_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point point = new Point();
            mWindowManager.getDefaultDisplay().getSize(point);
            mScreenWidth = point.x;
            mScreenHeight = point.y;
        } else {
            mScreenWidth = mWindowManager.getDefaultDisplay().getWidth();
            mScreenHeight = mWindowManager.getDefaultDisplay().getHeight();
        }
    }


}
