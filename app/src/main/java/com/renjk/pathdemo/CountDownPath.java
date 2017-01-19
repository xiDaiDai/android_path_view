package com.renjk.pathdemo;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by admin on 2017/1/19.
 */
public class CountDownPath extends View {

    public static final String TAG="COUNT_DOWN_PATH";
    // 画笔
    private Paint mPaint;
    private Paint progressPaint;
    private Paint dotPaint;
    private TextPaint textPaint;

    // View 宽高
    private int mViewWidth;
    private int mViewHeight;

    private Matrix mMatrix;

    private float[] pos;            // 当前点的实际位置
    private float[] tan;                // 当前点的tangent值,用于计算图片所需旋转的角度

    private Bitmap mBitmap;             // 箭头图片

    private String num;             // 计时数字

    // 放大镜与外部圆环
    private Path pathCirle;
    private Path pathProgress;
    private Path pathDot;

    // 测量Path 并截取部分的工具
    private PathMeasure mMeasure;

    // 默认的动效周期 5s
    private int defaultDuration = 5000;
    private int currentDuration;

    // 控制各个过程的动画
    private ValueAnimator mStartingAnimator;

    // 动画数值(用于控制动画状态,因为同一时间内只允许有一种状态出现,具体数值处理取决于当前状态)
    private float mAnimatorValue = 0;

    // 动效过程监听器
    private ValueAnimator.AnimatorUpdateListener mUpdateListener;
    private Animator.AnimatorListener mAnimatorListener;

    // 用于控制动画状态转换
    private Handler mAnimatorHandler;

    public CountDownPath(Context context) {
        this(context,null);
    }

    public CountDownPath(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAll(context);
    }

    public void initAll(Context context) {

        initPaint();

        initPath(context);

        initListener();

        initHandler();

        initAnimator();

        // 进入开始动画
        mStartingAnimator.start();

    }




    private void initPaint() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(15);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setAntiAlias(true);

        progressPaint = new Paint();
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setColor(Color.BLUE);
        progressPaint.setStrokeWidth(15);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setAntiAlias(true); 
        
        dotPaint = new Paint();
        dotPaint.setStyle(Paint.Style.STROKE);
        dotPaint.setColor(Color.YELLOW);
        dotPaint.setStrokeWidth(15);
        dotPaint.setStrokeCap(Paint.Cap.ROUND);
        dotPaint.setAntiAlias(true);

        textPaint = new TextPaint();
        textPaint.setColor(Color.BLUE);
        textPaint.setTextSize(150);
        textPaint.setAntiAlias(true);
    }

    private void initPath(Context context) {

        num = String.valueOf(defaultDuration/1000);

        pos = new float[2];
        tan = new float[2];

        mMatrix = new Matrix();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;       // 缩放图片
        mBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher, options);

        pathCirle = new Path();
        pathProgress = new Path();
        pathDot = new Path();

        mMeasure = new PathMeasure();

        RectF oval = new RectF(-200, -200, 200, 200);          // 圆环
        pathProgress.addArc(oval, 270, 359.9f);

        pathCirle.addCircle(0, 0, 200, Path.Direction.CW);

        mMeasure.setPath(pathCirle, false);               //
    }

    private void initListener() {
        mUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimatorValue = (float) animation.getAnimatedValue();
                currentDuration = (int) mStartingAnimator.getCurrentPlayTime();
                num = String.valueOf(defaultDuration/1000-(int)(currentDuration/1000));
//                Log.e(TAG,currentDuration+"");
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
                mAnimatorHandler.sendEmptyMessage(0);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        };
    }

    private void initHandler() {
        mAnimatorHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                mStartingAnimator.removeAllListeners();
            }
        };
    }

    private void initAnimator() {
        mStartingAnimator = ValueAnimator.ofFloat(0,1).setDuration(defaultDuration);

        mStartingAnimator.setInterpolator(new LinearInterpolator());

        mStartingAnimator.addUpdateListener(mUpdateListener);

        mStartingAnimator.addListener(mAnimatorListener);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w;
        mViewHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawProgress(canvas);
    }

    private void drawProgress(Canvas canvas) {

        mPaint.setColor(Color.GRAY);
        progressPaint.setColor(Color.BLUE);

        canvas.translate(mViewWidth / 2, mViewHeight / 2);

        canvas.drawColor(Color.parseColor("#ffffff"));

        canvas.drawPath(pathCirle, mPaint);

        mMeasure.setPath(pathProgress, false);
        Path dst = new Path();
        mMeasure.getSegment(0,mMeasure.getLength() * mAnimatorValue, dst, true);
        canvas.drawPath(dst, progressPaint);



        mMeasure.getPosTan(mMeasure.getLength() * mAnimatorValue, pos, tan);        // 获取当前位置的坐标以及趋势
        mMatrix.reset();                                                        // 重置Matrix
        float degrees = (float) (Math.atan2(tan[1], tan[0]) * 180.0 / Math.PI); // 计算图片旋转角度

        mMatrix.postRotate(degrees, mBitmap.getWidth() / 2, mBitmap.getHeight() / 2);   // 旋转图片
        mMatrix.postTranslate(pos[0] - mBitmap.getWidth() / 2, pos[1] - mBitmap.getHeight() / 2);   // 将图片绘制中心调整到与当前点重合

        canvas.drawBitmap(mBitmap, mMatrix, dotPaint);

        if (!TextUtils.isEmpty(num)) {
            float textHeight = textPaint.descent() + textPaint.ascent();
            canvas.drawText(num, (- textPaint.measureText(num)) / 2.0f, (-textHeight) / 2.0f, textPaint);
        }

    }
}
