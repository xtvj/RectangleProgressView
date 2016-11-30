package io.github.xtvj.rectangleprogressview.ui;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import io.github.xtvj.rectangleprogressview.R;


public class RectangleProgressLoadingView extends View {
    // 画笔
    private Paint mPaint;

    //矩形
    private Path mPath;

    private PathMeasure mMeasure;
    private int mWidth;//view宽
    private int mHeight;//view高

    public void stop() {
        mCurrentState = State.NONE;
        if (mStartingAnimator.isRunning()) {
            mStartingAnimator.cancel();
        }
        invalidate();
    }

    public void start() {
        mCurrentState = State.SEARCHING;
        mStartingAnimator.start();
        invalidate();
    }

    //状态
    public static enum State {
        SEARCHING,
        NONE
    }

    //动画一周所用时间
    private int time = 3500;

    // 控制各个过程的动画
    private ValueAnimator mStartingAnimator;

    // 动效过程监听器
    private ValueAnimator.AnimatorUpdateListener mUpdateListener;
    private Animator.AnimatorListener mAnimatorListener;

    // 动画数值
    private float mAnimatorValue = 0;

    // 当前的状态
    private State mCurrentState = State.NONE;
    private Context mContext;

    //线条粗细
    private int mwidth = 20;


    public void setmWidth(int width){
        mwidth = width;
        mPaint.setStrokeWidth(mwidth);
        invalidate();
    }

    //radian默认弧度
    private int mRadian = 80;

    public void setRadian(int radian){
        mRadian = radian;
        invalidate();
    }


    public RectangleProgressLoadingView(Context context) {
        super(context);

    }

    public RectangleProgressLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RectangleProgressLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        initPaint();

        initListener();

        initAnimator();

        invalidate();

        mCurrentState = State.NONE;
//		mStartingAnimator.start();

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (mCurrentState) {
                case NONE:
                    break;
                case SEARCHING:
                    mStartingAnimator.start();
                    break;
            }

        }
    };


    private void initPaint() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(getResources().getColor(R.color.colorPrimary));
        mPaint.setStrokeWidth(mwidth);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setAntiAlias(true);
    }

    private RectF rectf;

    private void initPath() {

        rectf = new RectF(mWidth / 10, mWidth / 10, mWidth * 9 / 10, mHeight * 9 / 10);

        mPath = new Path();
        mPath.addRoundRect(rectf, mRadian*2, mRadian*2, Path.Direction.CCW);

        mMeasure = new PathMeasure();
        mMeasure.setPath(mPath, false);


    }

    private void initListener() {

        mUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimatorValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        };


        mAnimatorListener = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // getHandle发消息通知动画状态更新
                handler.sendEmptyMessage(0);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        };

    }

    private void initAnimator() {

        mStartingAnimator = ValueAnimator.ofFloat(0, 1).setDuration(time);
        mStartingAnimator.addUpdateListener(mUpdateListener);
        mStartingAnimator.addListener(mAnimatorListener);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY) {
            mWidth = widthSize;
        } else {
            mWidth = dpToPx(200, mContext);
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            mHeight = heightSize;
        } else {
            mHeight = dpToPx(200, mContext);
        }
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        switch (mCurrentState) {
            case NONE:
                if (mStartingAnimator.isRunning()) {
                    mStartingAnimator.cancel();
                }
                initPath();
                canvas.drawPath(mPath, mPaint);
                break;
            case SEARCHING:

                initPath();
                mMeasure.setPath(mPath, false);
                Path dst1 = new Path();
                float stop = mMeasure.getLength() * mAnimatorValue;
                float start = (float) (stop - ((0.5 - Math.abs(mAnimatorValue - 0.5)) * 2000f));
                mMeasure.getSegment(start, stop, dst1, true);


                canvas.drawPath(dst1, mPaint);

                break;
        }
    }


    public static int dpToPx(int dp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

}