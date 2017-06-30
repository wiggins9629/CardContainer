package com.wiggins.cardcontainer.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RelativeLayout;

import com.wiggins.cardcontainer.R;
import com.wiggins.cardcontainer.utils.ListUtil;

import java.util.ArrayList;

/**
 * @Description 可拖拽式层叠卡片容器
 * @Author 一花一世界
 */
public class CardContainerView extends RelativeLayout {

    private Context mContext;
    // 指定剩余卡片还剩下多少时加载更多
    private int mLoadSize = 2;
    // 是否执行加载更多：加载更多时卡片依次添加在后面，添加卡片时卡片是依次添加在上面
    private boolean isLoadMore = false;
    // 保存当前容器中的卡片
    private ArrayList<View> mCardList = new ArrayList<>();
    // 加载更多监听器
    private LoadMore mLoadMore;
    // 左右滑动监听器
    private LeftOrRight mLeftOrRight;

    private float marginLeft = 35;
    private float marginRight = 35;
    private float marginTop = 100;
    private float marginBottom = 130;
    private int mLastY = 0;
    private int mLastX = 0;
    private int mCardLeft;
    private int mCardTop;
    private int mCardRight;
    private int mCardBottom;
    private boolean mLeftOut = false;
    private boolean mRightOut = false;
    private boolean mOnTouch = true;

    public CardContainerView(Context context) {
        this(context, null);
    }

    public CardContainerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
    }

    @Override
    public void addView(View card) {
        if (isLoadMore) {
            this.mCardList.add(ListUtil.getSize(mCardList), card);
        } else {
            this.mCardList.add(card);
        }
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        this.addView(card, 0, layoutParams);
        card.setOnTouchListener(onTouchListener);
        if (!isLoadMore) {
            this.setLayoutParams(card, mCardList.size());
        }
    }

    /**
     * 设置卡片相对位置
     *
     * @param card  卡片视图
     * @param index 卡片标签
     */
    private void setLayoutParams(View card, int index) {
        LayoutParams params = new LayoutParams(card.getLayoutParams());
        params.topMargin = dip2px(marginTop) + getResources().getDimensionPixelSize(R.dimen.margin_tiny) * index;
        params.bottomMargin = dip2px(marginBottom) - getResources().getDimensionPixelSize(R.dimen.margin_tiny) * index;
        params.leftMargin = dip2px(marginLeft);
        params.rightMargin = dip2px(marginRight);
        card.setLayoutParams(params);
    }

    /**
     * 每次移除时需要重置剩余卡片的位置
     */
    private void resetLayoutParams() {
        for (int i = 0; i < mCardList.size(); i++) {
            setLayoutParams(mCardList.get(i), i);
        }
    }

    private OnTouchListener onTouchListener = new OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (mOnTouch && v.equals(mCardList.get(0))) {
                int rawY = (int) event.getRawY();
                int rawX = (int) event.getRawX();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        getLayout();
                        mLastY = (int) event.getRawY();
                        mLastX = (int) event.getRawX();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int offsetY = rawY - mLastY;
                        int offsetX = rawX - mLastX;
                        mCardList.get(0).layout(mCardList.get(0).getLeft() + offsetX, mCardList.get(0).getTop() + offsetY, mCardList.get(0).getRight() + offsetX, mCardList.get(0).getBottom() + offsetY);
                        mRightOut = mCardList.get(0).getLeft() > getDisplayMetrics(mContext).widthPixels / 2;
                        mLeftOut = mCardList.get(0).getRight() < getDisplayMetrics(mContext).widthPixels / 2;
                        mLastY = rawY;
                        mLastX = rawX;
                        break;
                    case MotionEvent.ACTION_UP:
                        change();
                        break;
                }
            }
            return true;
        }
    };

    private void getLayout() {
        mCardLeft = mCardList.get(0).getLeft();
        mCardTop = mCardList.get(0).getTop();
        mCardRight = mCardList.get(0).getRight();
        mCardBottom = mCardList.get(0).getBottom();
    }

    private void change() {
        if (mLeftOut) {
            // 向左边滑出
            out(true);
        } else if (mRightOut) {
            // 向右边滑出
            out(false);
        } else {
            // 复位
            reset();
        }
    }

    private class CardIndex {
        int left;
        int top;
        int right;
        int bottom;

        CardIndex(int left, int top, int right, int bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }

        int getLeft() {
            return left;
        }

        int getTop() {
            return top;
        }

        int getRight() {
            return right;
        }

        int getBottom() {
            return bottom;
        }
    }

    private class PointEvaluator implements TypeEvaluator {

        @Override
        public Object evaluate(float fraction, Object startValue, Object endValue) {
            CardIndex startPoint = (CardIndex) startValue;
            CardIndex endPoint = (CardIndex) endValue;
            int left = (int) (startPoint.getLeft() + fraction * (endPoint.getLeft() - startPoint.getLeft()));
            int top = (int) (startPoint.getTop() + fraction * (endPoint.getTop() - startPoint.getTop()));
            int right = (int) (startPoint.getRight() + fraction * (endPoint.getRight() - startPoint.getRight()));
            int bottom = (int) (startPoint.getBottom() + fraction * (endPoint.getBottom() - startPoint.getBottom()));
            return new CardIndex(left, top, right, bottom);
        }
    }

    /**
     * 卡片复位
     */
    private void reset() {
        CardIndex oldCardIndex = new CardIndex(mCardLeft, mCardTop, mCardRight, mCardBottom);
        CardIndex newCardIndex = new CardIndex(mCardList.get(0).getLeft(), mCardList.get(0).getTop(), mCardList.get(0).getRight(), mCardList.get(0).getBottom());
        animator(newCardIndex, oldCardIndex);
    }

    /**
     * 卡片滑出方向
     *
     * @param left 是否向左滑出
     */
    private void out(boolean left) {
        if (left) {
            // 向左滑出
            leftOut();
        } else {
            // 向右滑出
            rightOut();
        }
    }

    /**
     * 向左滑出
     */
    private void leftOut() {
        CardIndex oldCardIndex = new CardIndex(-mCardRight, mCardTop, 0, mCardBottom);
        CardIndex newCardIndex = new CardIndex(mCardList.get(0).getLeft(), mCardList.get(0).getTop(), mCardList.get(0).getRight(), mCardList.get(0).getBottom());
        animator(newCardIndex, oldCardIndex);
    }

    /**
     * 向右滑出
     */
    private void rightOut() {
        CardIndex oldCardIndex = new CardIndex(getDisplayMetrics(mContext).widthPixels, mCardTop, getDisplayMetrics(mContext).widthPixels + (mCardRight - mCardLeft), mCardBottom);
        CardIndex newCardIndex = new CardIndex(mCardList.get(0).getLeft(), mCardList.get(0).getTop(), mCardList.get(0).getRight(), mCardList.get(0).getBottom());
        animator(newCardIndex, oldCardIndex);
    }

    private void animator(CardIndex newCard, CardIndex oldCard) {
        ValueAnimator animator = ValueAnimator.ofObject(new PointEvaluator(), newCard, oldCard);
        animator.setDuration(200);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mOnTouch = false;
                CardIndex value = (CardIndex) animation.getAnimatedValue();
                mCardList.get(0).layout(value.left, value.top, value.right, value.bottom);
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mRightOut || mLeftOut) {
                    removeTopCard();
                    if (mLeftOrRight != null) {
                        mLeftOrRight.leftOrRight(mLeftOut);
                    }
                }
                mOnTouch = true;
            }
        });
        animator.start();
    }

    /**
     * 移除顶部卡片(无动画)
     */
    public void removeTopCard() {
        if (!ListUtil.isEmpty(this.mCardList)) {
            removeView(this.mCardList.remove(0));
            if (mCardList.size() == mLoadSize) {
                if (mLoadMore != null) {
                    this.isLoadMore = true;
                    this.mLoadMore.load();
                    this.isLoadMore = false;
                    this.resetLayoutParams();
                }
            }
        }
    }

    /**
     * 移除顶部卡片（有动画）
     *
     * @param left 是否向左移除
     */
    public void removeTopCard(boolean left) {
        if (this.mOnTouch) {
            this.mLeftOut = left;
            this.mRightOut = !this.mLeftOut;
            this.getLayout();
            this.out(left);
        }
    }

    /**
     * 当剩余卡片等于Size时加载更多
     */
    public void setLoadSize(int size) {
        this.mLoadSize = size;
    }

    /**
     * 距离左边的边距
     *
     * @param marginLeft 距离左边的边距（单位：dp）
     */
    public void marginLeft(float marginLeft) {
        this.marginLeft = marginLeft;
    }

    /**
     * 距离右边的边距
     *
     * @param marginRight 距离右边的边距（单位：dp）
     */
    public void marginRight(float marginRight) {
        this.marginRight = marginRight;
    }

    /**
     * 距离上边的边距
     *
     * @param marginTop 距离上边的边距（单位：dp）
     */
    public void marginTop(float marginTop) {
        this.marginTop = marginTop;
    }

    /**
     * 距离下边的边距
     *
     * @param marginBottom 距离下边的边距（单位：dp）
     */
    public void marginBottom(float marginBottom) {
        this.marginBottom = marginBottom;
    }

    /**
     * 加载更多监听
     *
     * @param listener {@link LoadMore}
     */
    public void setLoadMoreListener(LoadMore listener) {
        this.mLoadMore = listener;
    }

    /**
     * 左右滑动监听
     *
     * @param listener {@link LeftOrRight}
     */
    public void setLeftOrRightListener(LeftOrRight listener) {
        this.mLeftOrRight = listener;
    }

    public interface LoadMore {
        void load();
    }

    public interface LeftOrRight {
        void leftOrRight(boolean left);
    }

    private DisplayMetrics getDisplayMetrics(Context _context) {
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        ((Activity) _context).getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        return mDisplayMetrics;
    }

    private int dip2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
