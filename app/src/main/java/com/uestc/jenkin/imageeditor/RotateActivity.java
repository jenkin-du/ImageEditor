package com.uestc.jenkin.imageeditor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;



/**
 * <pre>
 *     author : jenkin
 *     e-mail : jekin-du@foxmail.com
 *     time   : 2019/05/09
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class RotateActivity extends Activity implements View.OnTouchListener, View.OnClickListener {

    private static final String TAG = "RotateActivity";

    private Bitmap srcBitmap;
    private RotateImageView rotateImageView;

    private float cx = 0;
    private float cy = 0;

    /**
     * 触摸点角度
     */
    private float downDegree = 0;
    /**
     * 当前图片的角度
     */
    private float rotateDegree = 0;
    /**
     * 经过计算后的图片角度
     */
    private float moveDegree = 0;

    private int scaleX = 1;
    private int scaleY = 1;

    private int scope = 1;

    private EditActivity.RotateListener rotateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rotate_big_img);
        //初始化
        initView();
        //初始化数据
        initData();
    }

    private void initData() {

        srcBitmap = EditActivity.processBitmap;
        rotateImageView.setImageBitmap(srcBitmap);

        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        assert wm != null;
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;         // 屏幕宽度（像素）
        int height = dm.heightPixels;       // 屏幕高度（像素）

        cx = width / 2;
        cy = height / 2;

        rotateListener = rotateImageView.rotateListener;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {

        rotateImageView = findViewById(R.id.iv_rotate_big_img);
        rotateImageView.setOnTouchListener(this);


        Button saveBtn = findViewById(R.id.btn_rotate_big_img_save);
        Button unsaveBtn = findViewById(R.id.btn_rotate_big_img_unsave);
        Button resetBtn = findViewById(R.id.btn_rotate_big_img_reset);

        saveBtn.setOnClickListener(this);
        unsaveBtn.setOnClickListener(this);
        resetBtn.setOnClickListener(this);

        ImageView rotateBtn = findViewById(R.id.btn_rotate_big_img_rotate);
        ImageView mirrorBtn = findViewById(R.id.btn_rotate_big_img_mirror);


        rotateBtn.setOnClickListener(this);
        mirrorBtn.setOnClickListener(this);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {


        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downDegree = getDegree(event);
                downDegree = downDegree <= 0 ? downDegree + 360 : downDegree;
//                LogUtil.getInstance().d(TAG, "触摸点角度:" + downDegree);
                break;
            case MotionEvent.ACTION_MOVE:
                // 计算旋转的角度
                float degree = getDegree(event);
                degree = degree <= 0 ? degree + 360 : degree;
                moveDegree = degree - downDegree + rotateDegree;
                moveDegree = moveDegree < 0 ? moveDegree + 360 : moveDegree >= 360 ? moveDegree - 360 : moveDegree;
                rotateImage(moveDegree);

                rotateListener.onRotate(moveDegree);

                break;
            case MotionEvent.ACTION_UP:
                rotateDegree = moveDegree;
                if ((rotateDegree >= 0 && rotateDegree < 45) || (rotateDegree >= 315 && rotateDegree < 360)) {
                    scope = 1;
                }
                if (rotateDegree >= 135 && rotateDegree < 225) {
                    scope = 3;
                }

                if (rotateDegree >= 45 && rotateDegree < 135) {
                    scope = 2;
                }
                if (rotateDegree >= 225 && rotateDegree < 315) {
                    scope = 4;
                }
        }
        return true;
    }


    @Override
    public void onClick(View v) {

        Matrix matrix;
        Bitmap bitmap;

        switch (v.getId()) {
            //返回
            case R.id.btn_rotate_big_img_unsave:
                finish();
                break;
            //保存编辑的图片
            case R.id.btn_rotate_big_img_save:
                saveData();
                break;

            //整数角度旋转
            case R.id.btn_rotate_big_img_rotate:

                rotateDegree += 90;
                if (rotateDegree >= 360) {
                    rotateDegree -= 360;
                }


                if ((rotateDegree >= 0 && rotateDegree < 45) || (rotateDegree >= 315 && rotateDegree < 360)) {
                    scope = 1;
                }
                if (rotateDegree >= 135 && rotateDegree < 225) {
                    scope = 3;
                }

                if (rotateDegree >= 45 && rotateDegree < 135) {
                    scope = 2;
                }
                if (rotateDegree >= 225 && rotateDegree < 315) {
                    scope = 4;
                }

                rotateImage(rotateDegree);

                rotateListener.onRotate(rotateDegree);
                rotateImageView.invalidate();
                break;
            //镜像
            case R.id.btn_rotate_big_img_mirror:

                matrix = new Matrix();
                if (scope == 1 || scope == 3) {
                    scaleX *= -1;
                    matrix.setScale(scaleX, 1);
                }


                if (scope == 2 || scope == 4) {
                    scaleY *= -1;
                    matrix.setScale(1, scaleY);
                }

                matrix.postRotate(rotateDegree);

                bitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(), srcBitmap.getHeight(), matrix, true);
                rotateImageView.setImageBitmap(bitmap);



                break;

            //复位
            case R.id.btn_rotate_big_img_reset:

                rotateDegree = 0;
                scaleX = 1;
                scaleY = 1;
                scope = 1;
                rotateImageView.setImageBitmap(srcBitmap);

                rotateListener.onRotate(rotateDegree);
                rotateImageView.invalidate();

                break;
        }
    }

    /**
     * 保存数据
     */
    private void saveData() {

        Intent intent = new Intent();
        intent.putExtra("degree", rotateDegree);
        intent.putExtra("scaleX", scaleX);
        intent.putExtra("scaleY", scaleY);
        setResult(RESULT_OK, intent);
        finish();
    }


    // 取旋转角度
    private float getDegree(MotionEvent event) {

        double dx = event.getRawX() - cx;
        double dy = event.getRawY() - cy;

        double radians = Math.atan2(dy, dx);
        return (float) Math.toDegrees(radians);
    }


    /**
     * 旋转图片
     */
    private void rotateImage(float degree) {

        Matrix matrix = new Matrix();
        if (scope == 1 || scope == 3) {
            matrix.setScale(scaleX, 1);
        }
        if (scope == 2 || scope == 4) {
            matrix.setScale(1, scaleY);
        }
        matrix.postRotate(degree);
        int width = srcBitmap.getWidth();
        int height = srcBitmap.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(srcBitmap, 0, 0, width, height, matrix, true);
        rotateImageView.setImageBitmap(bitmap);
    }
}
