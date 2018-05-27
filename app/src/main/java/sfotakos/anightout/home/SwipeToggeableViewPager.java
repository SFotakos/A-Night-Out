package sfotakos.anightout.home;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import sfotakos.anightout.R;

public class SwipeToggeableViewPager extends ViewPager {

    private boolean swipePagingEnable;

    public SwipeToggeableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray =
                context.obtainStyledAttributes(attrs, R.styleable.SwipeToggeableViewPager, 0, 0);
        try {
            swipePagingEnable = typedArray.getBoolean(
                    R.styleable.SwipeToggeableViewPager_swipePagingEnabled, true);
        } finally {
            typedArray.recycle();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return this.swipePagingEnable && super.onTouchEvent(event);

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return this.swipePagingEnable && super.onInterceptTouchEvent(event);

    }

    public void setSwipePagingEnabled(boolean enabled) {
        this.swipePagingEnable = enabled;
    }
}