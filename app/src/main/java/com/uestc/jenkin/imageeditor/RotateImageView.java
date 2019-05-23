package com.uestc.jenkin.imageeditor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.ImageView;


/**
 * <pre>
 *     author : jenkin
 *     e-mail : jekin-du@foxmail.com
 *     time   : 2019/04/28
 *     desc   :
 *     version: 1.0
 * </pre>
 */
@SuppressLint("AppCompatCustomView")
public class RotateImageView extends ImageView {

    String TAG = "RotateImageView";

    Paint paint = new Paint();
    RectF oval = new RectF();

    //阴影
    RectF shadowRect = new RectF();
    Paint shadowPaint = new Paint();

    //刻度
    Paint markPaint = new Paint();
    //刻度长度
    int markLength = 30;

    //文字
    Paint textPaint = new Paint();


    public boolean isDraw = true;

    private float rotateDegree;

    Bitmap bmp;
    Paint bmpPaint = new Paint();

    int cycleRadius = 0;
    int bmpCX = 0;
    int bmpCY = 0;
    int bmpWidth = 0;
    int bmpHeight = 0;

    Canvas bmpCanvas;

    Bitmap trangleBmp;

    public EditActivity.RotateListener rotateListener = new EditActivity.RotateListener() {
        @Override
        public void onRotate(float degree) {

            rotateDegree = degree;
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.O)
    public RotateImageView(Context context) {
        this(context, null);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public RotateImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        setWillNotDraw(false);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public RotateImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWillNotDraw(false);

        //弧线
        paint.setColor(getResources().getColor(R.color.rotate_cycle));//颜色
        paint.setStyle(Paint.Style.STROKE);//设置填充样式
        paint.setAntiAlias(true);//抗锯齿功能
        paint.setDither(false);
        paint.setStrokeWidth(20);//线宽

        //阴影
        shadowPaint.setColor(0x88565656);
        shadowPaint.setStyle(Paint.Style.FILL);

        //刻度
        markPaint.setStrokeWidth(15);
        markPaint.setColor(getResources().getColor(R.color.rotate_mark));
        markPaint.setDither(false);
        markPaint.setAntiAlias(true);//抗锯齿功能
        markPaint.setStyle(Paint.Style.STROKE);

        //文字
        textPaint.setColor(getResources().getColor(R.color.rotate_mark));
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(20);
        textPaint.setTextAlign(Paint.Align.CENTER);


        WindowManager wm = (WindowManager) App.getInstance().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        assert wm != null;
        wm.getDefaultDisplay().getMetrics(dm);
        int screenHeight = dm.heightPixels;       // 屏幕高度（像素）

        cycleRadius = screenHeight / 4;
        bmpWidth = cycleRadius * 2 + 400;
        bmpHeight = cycleRadius * 2 + 400;

        bmp = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888, true);
        bmpCanvas = new Canvas(bmp);

        //画弧线
        bmpCX = bmpWidth / 2;
        bmpCY = bmpHeight / 2;

        oval.left = bmpCX - cycleRadius;
        oval.top = bmpCY - cycleRadius;
        oval.right = bmpCX + cycleRadius;
        oval.bottom = bmpCY + cycleRadius;

        trangleBmp = BitmapFactory.decodeResource(getResources(), R.mipmap.trangle);


    }


    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isDraw) {

            float x = (getWidth() - getHeight() / 2) / 2;
            float y = getHeight() / 4;

            //画阴影
            float height = getHeight() - 2 * y;
            float radius = height / 2;
            int cx = (int) (x + radius);
            int cy = (int) (y + radius);

            shadowRect.left = 0;
            shadowRect.top = (float) (y + radius + radius * Math.sin(Math.toRadians(35)));
            shadowRect.right = getWidth();
            shadowRect.bottom = getHeight();
            canvas.drawRect(shadowRect, shadowPaint);


            //清除所画内容
            bmpCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            //画弧线
            bmpCanvas.drawArc(oval, 0, 360, false, paint);
            //画弧线
            bmpCanvas.drawBitmap(trangleBmp, bmpCX - trangleBmp.getWidth() / 2, bmpCY + cycleRadius + 40, bmpPaint);

            //画刻度
            int degree = 0;
            while (degree < 360) {

                int x1 = (int) (bmpCX + cycleRadius * Math.cos(Math.toRadians(degree + rotateDegree)));
                int y1 = (int) (bmpCY + cycleRadius * Math.sin(Math.toRadians(degree + rotateDegree)));

                int x2 = (int) (bmpCX + (cycleRadius - markLength) * Math.cos(Math.toRadians(degree + rotateDegree)));
                int y2 = (int) (bmpCY + (cycleRadius - markLength) * Math.sin(Math.toRadians(degree + rotateDegree)));

                //画线
                bmpCanvas.drawLine(x1, y1, x2, y2, markPaint);
                degree += 15;
            }

            //画度数
            int label = 180;
            for (int i = 0; i < 24; i++) {

                String str = String.valueOf(label);
                int tx = (int) (bmpCX + (cycleRadius + 40) * Math.cos(Math.toRadians(90 - label + rotateDegree)));
                int ty = (int) (bmpCY + (cycleRadius + 40) * Math.sin(Math.toRadians(90 - label + rotateDegree)));

                bmpCanvas.drawText(str, tx, ty, textPaint);
                label -= 15;
            }


            //裁剪bmp
            //先给定角度旋转
            int sy = (int) (bmpCY + (cycleRadius) * Math.sin(Math.toRadians(35)));
            Bitmap tempBmp = Bitmap.createBitmap(bmp, 0, sy, bmp.getWidth(), bmp.getHeight() - sy);
            canvas.drawBitmap(tempBmp, (float) (cx - bmp.getWidth() / 2), (float) (cy + radius * Math.sin(Math.toRadians(35))), bmpPaint);
        }
    }
}
