package com.ttdt.Util.lrc;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import com.ttdt.MyApplication;
import com.ttdt.R;

import java.util.List;

/***
 * 须知：
 * 在ViewGroup里面 scrollTo，scrollBy方法移动的是子View
 * 在View里面scrollTo，scrollBy方法移动的是View里面绘制的内容
 * 要点：
 * 1:歌词的上下平移用什么实现？
 * 用Scroller实现，Scroller只是一个工具而已，
 * 真正实现滚动效果的还是View的scrollTo方法
 * 2：歌词的水平滚动怎么实现？
 * 通过属性动画ValueAnimator控制高亮歌词绘制的x轴起始坐标
 *
 * @author Ligang  2014/8/19
 * @改进者 刘裕育 2017/10/06
 * 改进地方：①：歌词可缩放
 *           ②：移动歌词之后不立即更新进度，而是由用户选择是否更新
 *           ③：移动歌词时，惯性移动
 *
 */
public class LrcView extends View implements ILrcView {

    //速度追踪器
    private VelocityTracker mTracker;

    /**
     * 所有的歌词
     ***/
    private List<LrcRow> mLrcRows;
    /**
     * 无歌词数据的时候 显示的默认文字
     **/
    private static final String DEFAULT_TEXT = "暂时没有歌词";
    /**
     * 默认文字的字体大小
     **/
    private static final float SIZE_FOR_DEFAULT_TEXT = CommonUtils.dip2px(MyApplication.instance, 17);

    /**
     * 画高亮歌词的画笔
     ***/
    private Paint mPaintForHighLightLrc;
    /**
     * 高亮歌词的默认字体大小
     ***/
    private static final float DEFAULT_SIZE_FOR_HIGHT_LIGHT_LRC = CommonUtils.dip2px(MyApplication.instance, 15);
    /**
     * 高亮歌词当前的字体大小
     ***/
    private float mCurSizeForHightLightLrc = DEFAULT_SIZE_FOR_HIGHT_LIGHT_LRC;
    /**
     * 高亮歌词的默认字体颜色
     **/
    private static final int DEFAULT_COLOR_FOR_HIGHT_LIGHT_LRC = 0xffffffff;

    /**
     * 高亮歌词当前的字体颜色
     **/
    private int mCurColorForHightLightLrc = DEFAULT_COLOR_FOR_HIGHT_LIGHT_LRC;

    /**
     * 画其他歌词的画笔
     ***/
    private Paint mPaintForOtherLrc;
    /**
     * 其他歌词的默认字体大小
     ***/
//    private static final float DEFAULT_SIZE_FOR_OTHER_LRC = CommonUtils.dip2px(MyApplication.instance,25);
    private static final float DEFAULT_SIZE_FOR_OTHER_LRC = DEFAULT_SIZE_FOR_HIGHT_LIGHT_LRC;
    /**
     * 其他歌词当前的字体大小
     ***/
    private float mCurSizeForOtherLrc = DEFAULT_SIZE_FOR_OTHER_LRC;
    /**
     * 其他歌词的默认字体颜色
     **/
    private static final int DEFAULT_COLOR_FOR_OTHER_LRC = 0x80ffffff;
    /**
     * 其他歌词当前的字体颜色
     **/
    private int mCurColorForOtherLrc = DEFAULT_COLOR_FOR_OTHER_LRC;


    /**
     * 画时间线的画笔
     ***/
    private Paint mPaintForTimeLine;
    /***
     * 时间线的颜色
     **/
    private static final int COLOR_FOR_TIME_LINE = 0xff999999;
    /**
     * 时间文字大小
     **/
    private static final int SIZE_FOR_TIME = CommonUtils.dip2px(MyApplication.instance, 12);
    /**
     * 是否画时间线
     **/
    private boolean mIsDrawTimeLine = false;

    /**
     * 歌词间默认的行距
     **/
    private static final float DEFAULT_PADDING = CommonUtils.dip2px(MyApplication.instance, 17);
    /**
     * 歌词当前的行距
     **/
    private float mCurPadding = DEFAULT_PADDING;

    /**
     * 歌词的最大缩放比例
     **/
    public static final float MAX_SCALING_FACTOR = 1.5f;
    /**
     * 歌词的最小缩放比例
     **/
    public static final float MIN_SCALING_FACTOR = 0.5f;
    /**
     * 默认缩放比例
     **/
    private static final float DEFAULT_SCALING_FACTOR = 1.0f;
    /**
     * 歌词的当前缩放比例
     **/
    private float mCurScalingFactor = DEFAULT_SCALING_FACTOR;

    /**
     * 实现歌词竖直方向平滑滚动的辅助对象
     **/
    private Scroller mScroller;
    /***
     * 移动一句歌词的持续时间
     **/
    private static final int DURATION_FOR_LRC_SCROLL = 1500;
    /***
     * 停止触摸时 如果View需要滚动 时的持续时间
     **/
    private static final int DURATION_FOR_ACTION_UP = 400;

    /**
     * 控制文字缩放的因子
     **/
    private float mCurFraction = 0;
    private int mTouchSlop;

    private int TO_NORMAL = 0;
    private int SLIDE = 1;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == TO_NORMAL){
                toNormal();
            }
        }
    };

    private Bitmap arrowBitmap;
    private int size = 0;

    public LrcView(Context context) {
        super(context);
        init(context);
    }

    public LrcView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    /**
     * 初始化画笔等
     */
    @Override
    public void init(Context context) {
        mScroller = new Scroller(getContext());
        mPaintForHighLightLrc = new Paint();
        mPaintForHighLightLrc.setColor(mCurColorForHightLightLrc);
        mPaintForHighLightLrc.setTextSize(mCurSizeForHightLightLrc);
        mPaintForHighLightLrc.setAntiAlias(true);

        mPaintForOtherLrc = new Paint();
        mPaintForOtherLrc.setColor(mCurColorForOtherLrc);
        mPaintForOtherLrc.setTextSize(mCurSizeForOtherLrc);
        mPaintForOtherLrc.setAntiAlias(true);

        mPaintForTimeLine = new Paint();
        mPaintForTimeLine.setColor(COLOR_FOR_TIME_LINE);
        mPaintForTimeLine.setTextSize(SIZE_FOR_TIME);

        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDensity = 30;
        options.inTargetDensity = 30;
        arrowBitmap = BitmapFactory.decodeResource(context.getResources(), R.raw.lrc_arrow, options);
    }

    private int mTotleDrawRow;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mLrcRows == null || mLrcRows.size() == 0) {
            //画默认的显示文字
            mPaintForOtherLrc.setTextSize(SIZE_FOR_DEFAULT_TEXT);
            float textWidth = mPaintForOtherLrc.measureText(DEFAULT_TEXT);
            float textX = (getWidth() - textWidth) / 2;
            canvas.drawText(DEFAULT_TEXT, textX, getHeight() / 2, mPaintForOtherLrc);
            return;
        }
        if (mTotleDrawRow == 0) {
            //初始化将要绘制的歌词行数
            mTotleDrawRow = (int) (getHeight() / (mCurSizeForOtherLrc + mCurPadding)) + 4;
        }
        //因为不需要将所有歌词画出来
        int minRaw = mCurRow - (mTotleDrawRow - 1) / 2;
        int maxRaw = mCurRow + (mTotleDrawRow - 1) / 2;
        minRaw = Math.max(minRaw, 0); //处理上边界
        maxRaw = Math.min(maxRaw, mLrcRows.size() - 1); //处理下边界
        //实现渐变的最大歌词行数
        int count = Math.max(maxRaw - mCurRow, mCurRow - minRaw);
        if (count == 0) {
            return;
        }
        //两行歌词间字体颜色变化的透明度
        int alpha = (0xFF - 0x11) / count;
        //画出来的第一行歌词的y坐标
        float rowY = getHeight() / 2 + minRaw * (mCurSizeForOtherLrc + mCurPadding);
        for (int i = minRaw; i <= maxRaw; i++) {

            if (i == mCurRow) {//画高亮歌词
                //因为有缩放效果，所有需要动态设置歌词的字体大小
                float textSize = mCurSizeForOtherLrc + (mCurSizeForHightLightLrc - mCurSizeForOtherLrc) * mCurFraction;
                mPaintForHighLightLrc.setTextSize(textSize);

                String text = mLrcRows.get(i).getContent();//获取到高亮歌词
                float textWidth = mPaintForHighLightLrc.measureText(text);//用画笔测量歌词的宽度
                if (textWidth > getWidth()) {
                    //如果歌词宽度大于view的宽，则需要动态设置歌词的起始x坐标，以实现水平滚动
                    canvas.drawText(text, mCurTextXForHighLightLrc, rowY, mPaintForHighLightLrc);
                } else {
                    //如果歌词宽度小于view的宽，则让歌词居中显示
                    float textX = (getWidth() - textWidth) / 2;
                    canvas.drawText(text, textX, rowY, mPaintForHighLightLrc);
                }
            } else {
//                if (i == mLastRow) {//画高亮歌词的上一句
//                    //因为有缩放效果，所有需要动态设置歌词的字体大小
//                    float textSize = mCurSizeForHightLightLrc - (mCurSizeForHightLightLrc - mCurSizeForOtherLrc) * mCurFraction;
//                    mPaintForOtherLrc.setTextSize(textSize);
//                } else {//画其他的歌词
                mPaintForOtherLrc.setTextSize(mCurSizeForOtherLrc);
//                }
                String text = mLrcRows.get(i).getContent();
                float textWidth = mPaintForOtherLrc.measureText(text);
                float textX = (getWidth() - textWidth) / 2;
                //如果计算出的textX为负数，将textX置为0(实现：如果歌词宽大于view宽，则居左显示，否则居中显示)
                textX = Math.max(textX, 0);
                //实现颜色渐变  从0xFFFFFFFF 逐渐变为 0x11FFFFFF(颜色还是白色，只是透明度变化)
                int curAlpha = 255 - (Math.abs(i - mCurRow) - 1) * alpha; //求出当前歌词颜色的透明度
                //mPaintForOtherLrc.setColor(0x1000000*curAlpha+0xffffff);
                canvas.drawText(text, textX, rowY, mPaintForOtherLrc);
            }
            //计算出下一行歌词绘制的y坐标
            rowY += mCurSizeForOtherLrc + mCurPadding;
        }

        //画时间线和时间
        if (mIsDrawTimeLine) {
            float y = getHeight() / 2 + getScrollY();
            float x = getWidth();
            canvas.drawBitmap(arrowBitmap, -20, y - arrowBitmap.getHeight() / 2, null);
            canvas.drawText(mLrcRows.get(mCurRow).getTimeStr().substring(0, 5), x - 105, y + SIZE_FOR_TIME / 2, mPaintForTimeLine);
            canvas.drawLine(60, y, getWidth() - 110, y, mPaintForTimeLine);
        }

    }

    /**
     * 是否可拖动歌词
     **/
    private boolean canDrag = false;
    /**
     * 事件的第一次的y坐标
     **/
    private float firstY;
    /**
     * 事件的上一次的y坐标
     **/
    private float lastY;
    private float lastX;

    /**
     * 歌词处于的四种模式
     */
    private int NORMAL_MODEL = 0;
    private int DRAG_MODEL = 1;//正在移动模式
    private int TOW_FINGER_MODEL = 2;//双指模式
    private int WAIT_FOR_UPDATE = 3;//等待更新模式，拉动歌词
    private int MODEL = 0;

    private boolean isFirstMove = false;//是否是第一次移动
    private int moverNumber = 0;//移动的次数小于五次，则判断为点击

    private float seepY = 0;
    private int number = 0;
    private int result = 0;
    private final float maxOffset = CommonUtils.dip2px(MyApplication.instance, 300);

    /**
     * 初始化速度追踪器
     */
    private void initVelocity() {
        if (mTracker == null) {
            mTracker = VelocityTracker.obtain();
        } else {
            mTracker.clear();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mLrcRows == null || mLrcRows.size() == 0) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(!mScroller.isFinished()){
                    mScroller.forceFinished(true);
                }
                //初始化各种变量
                initXY(event);
                //初始化速度追踪器
                initVelocity();
                mTracker.addMovement(event);
                break;
            case MotionEvent.ACTION_MOVE:
                moverNumber++;
                //双指模式
                if (towPointer(event))
                    return true;
                //如果是普通模式，那么变模式成歌词拖动模式
                if (MODEL == NORMAL_MODEL) {
                    MODEL = DRAG_MODEL;
                }
                if (!canDrag) {
                    if (Math.abs(event.getRawY() - firstY) > mTouchSlop && Math.abs(event.getRawY() - firstY) > Math.abs(event.getRawX() - lastX)) {
                        canDrag = true;
                        mIsDrawTimeLine = true;
                        mScroller.forceFinished(true);
                        stopScrollLrc();
                        mCurFraction = 1;
                    }
                    lastY = event.getRawY();
                }
                if (canDrag) {
                    mTracker.addMovement(event);
                    mTracker.computeCurrentVelocity(1000);
                    //最大的速度
                    updateMaxSeepY();
                    float offset = event.getRawY() - lastY;//偏移量
                    //获得检查后的偏移Y方向坐标
                    result = getCheckResult((int) offset);
                    scrollTo(0, result);
                    lastY = event.getRawY();
                    //更新当前高亮歌词行
                    updateCurRow(getScrollY());
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                //惯性移动
                inertialMobile(event);
                //取消速度追踪器
                cancelTracker();
                //是否更新歌词
                if (isUpdateProgress(event))
                    return true;

                //更新模式
                if (updateModel())
                    return true;

                break;
        }
        return true;
    }

    private int getCheckResult(int offset) {
        result = getScrollY() - offset;
        if (result < 0) {
            result = 0;
        } else if (result + getHeight() > size) {
            result = size - getHeight();
        }
        return result;
    }

    private void updateMaxSeepY() {
        float y1 = mTracker.getYVelocity();
        y1 = Math.abs(y1);
        if(y1 > 0 && y1 < 1000){
            number++;
        }else if(y1 > 1000){
            number = 0;
        }
        seepY = Math.max(seepY, y1);
    }

    private void updateCurRow(int y) {
        int currentRow = (int) (y/ (mCurSizeForOtherLrc + mCurPadding));
        currentRow = Math.min(currentRow, mLrcRows.size() - 1);
        currentRow = Math.max(currentRow, 0);
        mCurRow = currentRow;
    }

    private boolean towPointer(MotionEvent event) {
        if (event.getPointerCount() == 2) {//双指模式
            MODEL = TOW_FINGER_MODEL;
            towPaintSale(event);
            return true;
        }
        //如果在双指模式，不做处理，双指同时按下，然后一个手指抬起，这时候处在双指模式
        if (MODEL == TOW_FINGER_MODEL) {
            return true;
        }
        return false;
    }

    private boolean updateModel() {
        if (MODEL == TOW_FINGER_MODEL) {
            MODEL = NORMAL_MODEL;
            return true;
        }
        if(MODEL == WAIT_FOR_UPDATE){
            handler.removeMessages(TO_NORMAL);
            return true;
        }
        if (MODEL == DRAG_MODEL) {
            handler.removeMessages(TO_NORMAL);
            handler.sendEmptyMessageDelayed(TO_NORMAL,500000);
            MODEL = WAIT_FOR_UPDATE;
            return true;
        }
        MODEL = NORMAL_MODEL;
        return false;
    }

    /**
     * 是否要更新歌词，在UP事件中做判断
     * @param event
     * @return
     */
    private boolean isUpdateProgress(MotionEvent event) {
        if (moverNumber < 5 && MODEL == WAIT_FOR_UPDATE) {
            float x = event.getX();
            float y = event.getY();
            if (x > 0 && x < arrowBitmap.getWidth() + 40) {
                if (y > (getHeight() / 2 - (arrowBitmap.getHeight() / 2) * 2) && y < (getHeight() / 2 + (arrowBitmap.getHeight() / 2) * 2)) {
                    UpChangProgress();
                }
            }
            handler.removeMessages(TO_NORMAL);
            toNormal();
            return true;
        }
        moverNumber = 0;
        return false;
    }

    private void inertialMobile(MotionEvent event) {
        int maxOffset1 = getMaxOffset1();
        float rawY = event.getRawY();
        if(rawY - firstY > 0){
            maxOffset1 = -maxOffset1;
        }
        smoothScrollTo1((int) maxOffset1, 500);
        seepY = Integer.MIN_VALUE;
        number = 0;
    }

    /**
     * 根据number，获得需要惯性移动的距离
     * @return
     */
    private int getMaxOffset1() {
        int maxOffset1 = (int) maxOffset;
        if(number > 5){
            maxOffset1 = 0;
        } else if(seepY > 0 && seepY < 2000){
            maxOffset1 = 0;
        }else if (seepY > 4000 && seepY < 6000) {
            maxOffset1 = (int) (maxOffset * 1.5);
        } else if (seepY > 6000 && seepY < 8000) {
            maxOffset1 = (int) (maxOffset * 2.8);
        } else if (seepY > 8000 && seepY < 10000) {
            maxOffset1 = (int) (maxOffset * 3);
        } else if (seepY > 10000 && seepY < 15000) {
            maxOffset1 = (int) (maxOffset * 3.2);
        } else if (seepY > 15000 && seepY < 20000) {
            maxOffset1 = (int) (maxOffset * 3.5);
        } else if (seepY > 20000) {
            maxOffset1 = (int) (maxOffset * 3.8);
        }
        return maxOffset1;
    }

    private void initXY(MotionEvent event) {
        firstY = event.getRawY();
        lastX = event.getRawX();
        lastY = event.getRawY();
        isFirstMove = true;
    }

    private void cancelTracker(){
        if(mTracker!=null){
            mTracker.recycle();
            mTracker = null;
        }
    }

    private void toNormal() {
        moverNumber = 0;
        canDrag = false;
        mIsDrawTimeLine = false;
        MODEL = NORMAL_MODEL;
    }

    private void UpChangProgress() {
        if (!canDrag) {
            if (onLrcClickListener != null) {
                onLrcClickListener.onClick();
            }
        } else {
            if (onSeekToListener != null && mCurRow != -1) {
                onSeekToListener.onSeekTo(mLrcRows.get(mCurRow).getTime());
            }
            if (getScrollY() < 0) {
                smoothScrollTo(0, DURATION_FOR_ACTION_UP);
            } else if (getScrollY() > mLrcRows.size() * (mCurSizeForOtherLrc + mCurPadding) - mCurPadding) {
                smoothScrollTo((int) (mLrcRows.size() * (mCurSizeForOtherLrc + mCurPadding) - mCurPadding), DURATION_FOR_ACTION_UP);
            }
            invalidate();
        }
    }

    private PointF lastOneFinger = new PointF();
    private PointF lastTowFinger = new PointF();

    private void towPaintSale(MotionEvent event) {
        if (isFirstMove) {
            setLastFinger(event);
            isFirstMove = false;
            return;
        }
        float proportion = getProportion(event);
        if (proportion < 0.5F) {
            proportion = 0.5F;
        } else if (proportion > 2.5F) {
            proportion = 2.5F;
        }
        setLrcScalingFactor(proportion);
        setLastFinger(event);
    }

    private float getProportion(MotionEvent event) {
        float x0 = event.getX();
        float x1 = event.getX(1);
        float y0 = event.getY();
        float y1 = event.getY(1);

        //上一次的触摸的两指之间的距离
        double powX1 = Math.pow(lastTowFinger.x - lastOneFinger.x, 2);
        double powY1 = Math.pow(lastTowFinger.y - lastOneFinger.y, 2);
        double sqrt1 = Math.sqrt(powX1 + powY1);

        //这一次的触摸的两指之间的距离
        double powX2 = Math.pow(x0 - x1, 2);
        double powY2 = Math.pow(y0 - y1, 2);
        double sqrt2 = Math.sqrt(powX2 + powY2);

        double pro = sqrt2 - sqrt1;
        if (pro == 0) {
            return 1;
        }
        double v = pro / 200;
        return (float) (mCurScalingFactor + v);
    }

    private void setLastFinger(MotionEvent event) {
        lastOneFinger.x = event.getX(0);
        lastOneFinger.y = event.getY(0);
        lastTowFinger.x = event.getX(1);
        lastTowFinger.y = event.getY(1);
    }

    /**
     * 为LrcView设置歌词List集合数据
     */
    @Override
    public void setLrcRows(List<LrcRow> lrcRows) {
        reset();
        this.mLrcRows = lrcRows;
//        size = (int) ((mCurSizeForOtherLrc + mCurPadding) * (mLrcRows.size() - 1));
        size = (int) (mLrcRows.size() * (mCurSizeForOtherLrc + mCurPadding) - mCurPadding) + getHeight();
        invalidate();
    }

    /**
     * 当前高亮歌词的行号
     **/
    private int mCurRow = -1;
    /**
     * 上一次的高亮歌词的行号
     **/
    private int mLastRow = -1;

    public void seekTo(int progress) {
        if (MODEL == DRAG_MODEL || MODEL == WAIT_FOR_UPDATE) {
            return;
        }
        seekTo(progress, false, false);
    }

    @Override
    public void seekTo(int progress, boolean fromSeekBar, boolean fromSeekBarByUser) {
        if (mLrcRows == null || mLrcRows.size() == 0) {
            return;
        }
        //如果是由seekbar的进度改变触发 并且这时候处于拖动状态，则返回
        if (fromSeekBar && canDrag) {
            return;
        }
        for (int i = mLrcRows.size() - 1; i >= 0; i--) {
            if (progress >= mLrcRows.get(i).getTime()) {
                if (mCurRow != i) {
                    mLastRow = mCurRow;
                    mCurRow = i;
                    if (fromSeekBarByUser) {
                        if (!mScroller.isFinished()) {
                            mScroller.forceFinished(true);
                        }
                        scrollTo(getScrollX(), (int) (mCurRow * (mCurSizeForOtherLrc + mCurPadding)));
                    } else {
                        smoothScrollTo((int) (mCurRow * (mCurSizeForOtherLrc + mCurPadding)), DURATION_FOR_LRC_SCROLL);
                    }
                    //如果高亮歌词的宽度大于View的宽，就需要开启属性动画，让它水平滚动
                    float textWidth = mPaintForHighLightLrc.measureText(mLrcRows.get(mCurRow).getContent());
//                    log("textWidth=" + textWidth + "getWidth()=" + getWidth());
                    if (textWidth > getWidth()) {
                        if (fromSeekBarByUser) {
                            mScroller.forceFinished(true);
                        }
                        startScrollLrc(getWidth() - textWidth, (long) (mLrcRows.get(mCurRow).getTotalTime() * 0.6));
                    }
                    invalidate();
                }
                break;
            }
        }
    }

    /**
     * 控制歌词水平滚动的属性动画
     ***/
    private ValueAnimator mAnimator;

    /**
     * 开始水平滚动歌词
     *
     * @param endX     歌词第一个字的最终的x坐标
     * @param duration 滚动的持续时间
     */
    private void startScrollLrc(float endX, long duration) {
        if (mAnimator == null) {
            mAnimator = ValueAnimator.ofFloat(0, endX);
            mAnimator.addUpdateListener(updateListener);
        } else {
            mCurTextXForHighLightLrc = 0;
            mAnimator.cancel();
            mAnimator.setFloatValues(0, endX);
        }
        mAnimator.setDuration(duration);
        mAnimator.setStartDelay((long) (duration * 0.3)); //延迟执行属性动画
        mAnimator.start();
    }

    /**
     * 停止歌词的滚动
     */
    private void stopScrollLrc() {
        if (mAnimator != null) {
            mAnimator.cancel();
        }
        mCurTextXForHighLightLrc = 0;
    }

    /**
     * 高亮歌词当前的其实x轴绘制坐标
     **/
    private float mCurTextXForHighLightLrc;
    /***
     * 监听属性动画的数值值的改变
     */
    AnimatorUpdateListener updateListener = new AnimatorUpdateListener() {

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            //TODO
            mCurTextXForHighLightLrc = (Float) animation.getAnimatedValue();
            invalidate();
        }
    };

    /**
     * 设置歌词的缩放比例
     */
    @Override
    public void setLrcScalingFactor(float scalingFactor) {
        mCurScalingFactor = scalingFactor;
        mCurSizeForHightLightLrc = DEFAULT_SIZE_FOR_HIGHT_LIGHT_LRC * mCurScalingFactor;
        mCurSizeForOtherLrc = DEFAULT_SIZE_FOR_OTHER_LRC * mCurScalingFactor;
        mCurPadding = DEFAULT_PADDING * mCurScalingFactor;
        mTotleDrawRow = (int) (getHeight() / (mCurSizeForOtherLrc + mCurPadding)) + 3;
        scrollTo(getScrollX(), (int) (mCurRow * (mCurSizeForOtherLrc + mCurPadding)));
        invalidate();
        mScroller.forceFinished(true);
    }

    /**
     * 重置
     */
    @Override
    public void reset() {
        if (!mScroller.isFinished()) {
            mScroller.forceFinished(true);
        }
        mLrcRows = null;
        scrollTo(getScrollX(), 0);
        invalidate();
    }


    /**
     * 平滑的移动的距离
     * dstY为正则往下，dstY为负则往上
     */
    private void smoothScrollTo1(int dy, int duration) {
        int result = getScrollY() + dy;
        if (result < 0) {
            dy = -getScrollY();
        } else if (result + getHeight() > size) {
            dy = size - getHeight() - getScrollY();
        }
        mScroller.startScroll(getScrollX(), getScrollY(), 0, dy, duration);
        invalidate();
    }

    /**
     * 移动到某一个坐标 关于Y轴
     * @param dstY
     * @param duration
     */
    private void smoothScrollTo(int dstY, int duration) {
        int oldScrollY = getScrollY();
        int offset = dstY - oldScrollY;
        mScroller.startScroll(getScrollX(), oldScrollY, getScrollX(), offset, duration);
        invalidate();
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (!mScroller.isFinished()) {
            if (mScroller.computeScrollOffset()) {
                int oldY = getScrollY();
                int y = mScroller.getCurrY();
                if (oldY != y) {
                    scrollTo(getScrollX(), y);
                }
                mCurFraction = mScroller.timePassed() * 3f / DURATION_FOR_LRC_SCROLL;
                mCurFraction = Math.min(mCurFraction, 1F);
                updateCurRow(getScrollY());

                invalidate();
            }
        }
    }

    /**
     * 返回当前的歌词缩放比例
     *
     * @return
     */
    public float getmCurScalingFactor() {
        return mCurScalingFactor;
    }

    private OnSeekToListener onSeekToListener;

    public void setOnSeekToListener(OnSeekToListener onSeekToListener) {
        this.onSeekToListener = onSeekToListener;
    }

    public interface OnSeekToListener {
        void onSeekTo(int progress);
    }

    private OnLrcClickListener onLrcClickListener;

    public void setOnLrcClickListener(OnLrcClickListener onLrcClickListener) {
        this.onLrcClickListener = onLrcClickListener;
    }

    public interface OnLrcClickListener {
        void onClick();
    }

//    public void log(Object o) {
//        Log.e("LrcView", o + "");
//    }
}
//if (getScrollY() - offset < 0) {
//                        if (offset > 0) {
//                            offset = offset / 3;
//                        }
//                    } else if (getScrollY() - offset > mLrcRows.size() * (mCurSizeForOtherLrc + mCurPadding) - mCurPadding) {
//                        if (offset < 0) {
//                            offset = offset / 3;
//                        }
//                    }
