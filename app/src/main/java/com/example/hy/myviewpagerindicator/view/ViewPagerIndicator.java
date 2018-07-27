package com.example.hy.myviewpagerindicator.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.IntegerRes;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.hy.myviewpagerindicator.R;

import java.util.List;

/**
 * Created by HY on 2018/7/27.
 * ViewPagerIndicator
 */
@SuppressWarnings("unused")
public class ViewPagerIndicator extends LinearLayout {


    public static float RADIO_TRIANGLE_WIDTH = 1 / 6F;          //相对每个选项卡的宽度比例
    public static final int TRIANGLE = -1;      //三角形
    public static final int RECTANGULAR = -2;   //矩形

    public List<String> mTitles;                //标题


    private boolean isFirstShow = true;
    private final int DIMENSION_TRIANGLE_MAX_WIDTH = (int) (getScreenWidth() / 3 * RADIO_TRIANGLE_WIDTH);//三角形底边的最大宽度
    private static final int COLOR_TEXT_NORMAL = 0x77FFFFFF;    //默认普通标题颜色
    private static final int COLOR_TEXT_HIGHLIGHT = 0xFFFFFFFF; //默认选中时高亮颜色
    private static final int DEFAULT_TAB_COUNT = 3;             //默认


    private Paint paint;            //画笔
    private Path path;              //模拟三角形类

    private int type = TRIANGLE;    //Canvas绘制类型
    private int graphicsWidth = 0;      //绘制图形的宽
    private int graphicsHeight = 0;     //绘制图形的高
    private int initTranslationX;   //初始偏移位置
    private int mTranslationX;      //移动时偏移位置
    private int tabVisibleCount = DEFAULT_TAB_COUNT;    //一页所显示的tab数
    private int normalTextColor = COLOR_TEXT_NORMAL;             //标题未选中颜色
    private int highLightTextColor = COLOR_TEXT_HIGHLIGHT;       //标题选中颜色
    private ViewPager viewPager;


    //提供外部接口可供重写其他的OnPageChangeListener监听事件
    public interface PageOnChangeListener {
        void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);

        void onPageSelected(int position);

        void onPageScrollStateChanged(int state);
    }

    public PageOnChangeListener mListener;


    public void setPageOnChangeListener(PageOnChangeListener listener) {
        this.mListener = listener;
    }

    public ViewPagerIndicator(Context context) {
        this(context, null);
    }

    public ViewPagerIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        //获取可见tab的数量
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ViewPagerIndicator);
        tabVisibleCount = a.getInt(R.styleable.ViewPagerIndicator_visible_tab_count, DEFAULT_TAB_COUNT);

        if (tabVisibleCount < 0) {
            tabVisibleCount = DEFAULT_TAB_COUNT;
        }


        a.recycle();  //释放

        //初始化画笔
        paint = new Paint();
        paint.setAntiAlias(true);   //抗锯齿
        paint.setColor(Color.parseColor("#FFFFFF"));
        paint.setStyle(Paint.Style.FILL);
        paint.setPathEffect(new CornerPathEffect(5));   //圆角

    }

    @Override
    protected void dispatchDraw(Canvas canvas) {

        Log.i("ViewPagerIndicator", "dispatchDraw: ");

        canvas.save();

        //平移
        switch (type) {
            case TRIANGLE:
                canvas.translate(initTranslationX + mTranslationX, getHeight() + 5);
                break;
            case RECTANGULAR:
                Log.i("ViewPagerIndicator", "dispatchDraw: RECTANGULAR initTranslationX = " +
                        initTranslationX + "mTranslationX = " + mTranslationX);
                canvas.translate(initTranslationX + mTranslationX, getHeight());
                break;
        }
        canvas.drawPath(path, paint);


        canvas.restore();

        super.dispatchDraw(canvas);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.i("ViewPagerIndicator", "onSizeChanged: ");

        super.onSizeChanged(w, h, oldw, oldh);

        switch (type) {
            case TRIANGLE:
                graphicsWidth = (graphicsWidth == 0) ? Math.min((int) (w / tabVisibleCount * RADIO_TRIANGLE_WIDTH),
                        DIMENSION_TRIANGLE_MAX_WIDTH) : graphicsWidth;
                graphicsHeight = (graphicsHeight == 0) ? graphicsWidth / 2 : graphicsHeight;
                initTranslationX = w / tabVisibleCount / 2 - graphicsWidth / 2;
                initTriangle();     //初始化三角形
                break;
            case RECTANGULAR:
                Log.i("ViewPagerIndicator", "onSizeChanged:  RECTANGULAR");
                graphicsHeight = (graphicsHeight == 0) ? 5 : graphicsHeight;
                graphicsWidth = (graphicsWidth == 0) ? getWidth() / tabVisibleCount : graphicsHeight;
                initTranslationX = 0;
                initRectangular();  //初始化矩形
                break;
        }

    }

    @Override
    protected void onFinishInflate() {
        Log.i("ViewPagerIndicator", "onFinishInflate: ");
        super.onFinishInflate();

        int cCount = getChildCount();
        if (cCount == 0) return;

        for (int i = 0; i < cCount; i++) {
            View view = getChildAt(i);
            LinearLayout.LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            layoutParams.weight = 0;
            layoutParams.width = getScreenWidth() / tabVisibleCount;
            view.setLayoutParams(layoutParams);
        }
        setItemClickEvent();
    }

    /**
     * 获得屏幕的宽度
     *
     * @return int
     */
    private int getScreenWidth() {
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        if (windowManager != null) {
            windowManager.getDefaultDisplay().getMetrics(outMetrics);
        }
        return outMetrics.widthPixels;
    }

    /**
     * 初始化三角形
     */
    private void initTriangle() {

        path = new Path();

        path.moveTo(0, 0);
        path.lineTo(graphicsWidth, 0);                //绘制底边
        path.lineTo(graphicsWidth / 2, -graphicsHeight);  //右侧斜边
        path.close();                                  //闭合

    }

    /**
     * 初始化矩形
     */
    private void initRectangular() {

        path = new Path();

        path.moveTo(0, 0);
        path.lineTo(graphicsWidth, 0);                //绘制底边
        path.lineTo(graphicsWidth, -graphicsHeight);                //绘制底边
        path.lineTo(0, -graphicsHeight);                //绘制底边
        path.close();                                  //闭合

    }

    /**
     * 指示器跟随手指进行滚动
     *
     * @param position       当前选项卡position
     * @param positionOffset 相对选项卡位置Offset
     */
    public void scroll(int position, float positionOffset) {

        int tabWidth = getWidth() / tabVisibleCount;
        mTranslationX = (int) (tabWidth * (position + positionOffset));
        Log.i("IndicatorScroll: ", "position =  " + position + " ChildCount=" + getChildCount());
        Log.i("IndicatorScroll: ", "positionOffset =  " + positionOffset + " tabVisibleCount=" + tabVisibleCount);
        //容器移动 ，在tab处于移动至最后一个时
        if (position < getChildCount() - 2 && position >= tabVisibleCount - 2 &&
                positionOffset > 0 && getChildCount() > tabVisibleCount) {
            Log.i("IndicatorScroll: ", "满足if条件进来了 ");
            if (tabVisibleCount != 1) {
                this.scrollTo((int) (tabWidth * (position - (tabVisibleCount - 2)) + tabWidth * positionOffset), 0);

            } else {
                this.scrollTo((int) (position * tabWidth + tabWidth * positionOffset), 0);
            }

        }

        invalidate(); //重绘
    }

    public void setTabItemTitles(List<String> titles) {
        if (titles != null && titles.size() > 0) {
            this.removeAllViews();
            mTitles = titles;
            for (String title :
                    mTitles) {
                addView(generateTextView(title));
            }
        }
        setItemClickEvent();
    }


    /**
     * 根据title创建Tab
     *
     * @param title 标题
     * @return TextView
     */
    private View generateTextView(String title) {
        TextView textView = new TextView(getContext());
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lp.width = getScreenWidth() / tabVisibleCount;
        textView.setText(title);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        textView.setTextColor(normalTextColor);
        textView.setLayoutParams(lp);
        return textView;
    }


    /**
     * 设置关联的ViewPager
     *
     * @param viewPager viewPager对象
     * @param pos       viewPager初始位置
     */
    public void setViewPager(ViewPager viewPager, final int pos) {
        this.viewPager = viewPager;
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                if (mListener != null) {
                    mListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
                }


                Log.i("onPageScrolled", "position = " + position
                        + " positionOffset = " + positionOffset);
                //position * tabWidth + tabWidth * positionOffset
                if (isFirstShow) {
                    isFirstShow = false;
                    scroll(position, 0.01f);
                } else {
                    scroll(position, positionOffset);
                }

            }

            @Override
            public void onPageSelected(int position) {

                if (mListener != null) {
                    mListener.onPageSelected(position);
                }
                highLightTextView(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

                if (mListener != null) {
                    mListener.onPageScrollStateChanged(state);
                }

            }
        });
        this.viewPager.setCurrentItem(pos);
        highLightTextView(pos);
    }


    /**
     * 高亮某个Tab的文本
     *
     * @param pos 需要高亮的Tab的位置
     */
    private void highLightTextView(int pos) {
        resetTextViewColor();
        View view = getChildAt(pos);
        if (view instanceof TextView) {
            ((TextView) view).setTextColor(highLightTextColor);
        }
    }

    /**
     * 重置Tab文本颜色
     */
    private void resetTextViewColor() {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof TextView) {
                ((TextView) view).setTextColor(normalTextColor);
            }
        }
    }

    /**
     * 设置Tab点击事件
     */
    private void setItemClickEvent() {
        int cCount = getChildCount();
        for (int i = 0; i < cCount; i++) {
            final int j = i;
            View view = getChildAt(i);
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewPager.setCurrentItem(j);
                }
            });
        }
    }


    /**
     * 获取可见的Tab数量
     *
     * @return 可见的Tab数量
     */
    public int getTabVisibleCount() {
        return tabVisibleCount;
    }


    /**
     * 设置可见的Tab数量
     * 要在setTabItemTitles方法前使用
     *
     * @param count 可见的Tab数量
     */
    public void setTabVisibleCount(int count) {
        tabVisibleCount = count;
    }

    public int getNormalTextColor() {
        return normalTextColor;
    }

    public void setNormalTextColor(int normalTextColor) {
        this.normalTextColor = normalTextColor;
    }

    public int getHighLightTextColor() {
        return highLightTextColor;
    }

    public void setHighLightTextColor(int highLightTextColor) {
        this.highLightTextColor = highLightTextColor;
    }

    public int getType() {
        return type;
    }

    public void setType(@IntegerRes int type) {
        this.type = type;
    }

    public int getGraphicsWidth() {
        return graphicsWidth;
    }

    public void setGraphicsWidth(int graphicsWidth) {
        this.graphicsWidth = graphicsWidth;
    }

    public int getGraphicsHeight() {
        return graphicsHeight;
    }

    public void setGraphicsHeight(int graphicsHeight) {
        this.graphicsHeight = graphicsHeight;
    }


    public Paint getPaint() {
        return paint;
    }
}
