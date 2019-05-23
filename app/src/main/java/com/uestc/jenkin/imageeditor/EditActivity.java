package com.uestc.jenkin.imageeditor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * <pre>
 *     author : jenkin
 *     e-mail : jekin-du@foxmail.com
 *     time   : 2019/04/26
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class EditActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "EditActivity";

    private ImageView processImageIV;
    public static Bitmap processBitmap;
    private int type;

    private static final int ROTATE_REQUEST = 1;
    private static final int CROP_REQUEST = 2;

    private boolean downSample = false;
    private float scale = 1;

    private Button saveBtn;

    //操作步骤，
    private ArrayList<ProcessStep> processSteps = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_big_img);
        //初始化view
        initView();
        //初始化数据
        initData();
    }

    /**
     * 初始化数据
     */
    private void initData() {

        processBitmap = MainActivity.bitmap;

        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        assert wm != null;
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;         // 屏幕宽度（像素）
        int height = dm.heightPixels;       // 屏幕高度（像素）

        //若屏幕图片的屏幕宽度和高度大于屏幕的宽度和高度，则进行图像的缩放

        if (processBitmap.getWidth() > width || processBitmap.getHeight() > height) {
            //对图像进行降采样
            downSample = true;

            float scaleX = width / (float) processBitmap.getWidth();
            float scaleY = height / (float) processBitmap.getHeight();
            scale = Math.min(scaleX, scaleY);

            Matrix matrix = new Matrix();
            matrix.setScale(scale, scale);
            processBitmap = Bitmap.createBitmap(processBitmap, 0, 0, processBitmap.getWidth(), processBitmap.getHeight(), matrix, true);

        }
        processImageIV.setImageBitmap(processBitmap);
    }

    /**
     * 初始化view
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initView() {

        Button unsaveBtn = findViewById(R.id.btn_edit_big_img_unsave);
        saveBtn = findViewById(R.id.btn_edit_big_img_save);
        Button rotateBtn = findViewById(R.id.btn_edit_big_img_rotate);
        Button cropBtn = findViewById(R.id.btn_edit_big_img_crop);

        processImageIV = findViewById(R.id.iv_edit_show);


        unsaveBtn.setOnClickListener(this);
        saveBtn.setOnClickListener(this);
        rotateBtn.setOnClickListener(this);
        cropBtn.setOnClickListener(this);

        saveBtn.setEnabled(false);

    }

    @Override
    public void onClick(View v) {

        Intent intent;

        switch (v.getId()) {

            //返回
            case R.id.btn_edit_big_img_unsave:
                intent = new Intent();
                intent.putExtra("changed", true);
                setResult(RESULT_OK, intent);
                finish();
                break;
            //保存编辑的图片
            case R.id.btn_edit_big_img_save:
                saveBitmapAndFinish();
                break;
            //旋转图片
            case R.id.btn_edit_big_img_rotate:

                intent = new Intent();
                intent.setClass(EditActivity.this, RotateActivity.class);
                startActivityForResult(intent, ROTATE_REQUEST);
                break;
            //裁剪图片
            case R.id.btn_edit_big_img_crop:

                intent = new Intent();
                intent.setClass(EditActivity.this, CropActivity.class);
                startActivityForResult(intent, CROP_REQUEST);
                break;
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            ProcessStep step;
            switch (requestCode) {
                //旋转
                case ROTATE_REQUEST:

                    saveBtn.setEnabled(true);

                    step = new ProcessStep();
                    step.mode = Mode.ROTATE;
                    step.obj1 = data.getFloatExtra("degree", 0);
                    step.obj2 = data.getIntExtra("scaleX", 1);
                    step.obj3 = data.getIntExtra("scaleY", 1);
                    processSteps.add(step);

                    float degree = (float) step.obj1;
                    int scaleX = (int) step.obj2;
                    int scaleY = (int) step.obj3;

                    Matrix matrix = new Matrix();
                    if ((degree <= 45 && degree > -45) || (degree > 135 && degree <= 225)) {
                        matrix.setScale(scaleX, 1);
                    }
                    if ((degree > 45 && degree <= 135) || (degree > 225 && degree <= 315)) {
                        matrix.setScale(1, scaleY);
                    }
                    matrix.postRotate(degree);

                    processBitmap = Bitmap.createBitmap(processBitmap, 0, 0, processBitmap.getWidth(), processBitmap.getHeight(), matrix, true);
                    processImageIV.setImageBitmap(processBitmap);
                    break;
                //裁剪
                case CROP_REQUEST:

                    saveBtn.setEnabled(true);

                    step = new ProcessStep();
                    step.mode = Mode.CROP;
                    step.obj1 = data.getParcelableExtra("rectangle");

                    processSteps.add(step);

                    RectF rectF = (RectF) step.obj1;
                    processBitmap = Bitmap.createBitmap(processBitmap, (int) rectF.left, (int) rectF.top, (int) (rectF.right - rectF.left), (int) (rectF.bottom - rectF.top));
                    processImageIV.setImageBitmap(processBitmap);
                    break;
            }
        }
    }

    private void saveBitmapAndFinish() {


        final Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        Intent intent = new Intent();
                        intent.putExtra("changed", true);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });
            }
        };
        Thread thread = new Thread() {
            @Override
            public void run() {

                Bitmap bitmap = MainActivity.bitmap;

                for (int i = 0; i < processSteps.size(); i++) {

                    ProcessStep step = processSteps.get(i);

                    //旋转
                    if (step.mode.equals(Mode.ROTATE)) {

                        float degree = (float) step.obj1;
                        int scaleX = (int) step.obj2;
                        int scaleY = (int) step.obj3;

                        Matrix matrix = new Matrix();
                        if ((degree <= 45 && degree > -45) || (degree > 135 && degree <= 225)) {
                            matrix.setScale(scaleX, 1);
                        }
                        if ((degree > 45 && degree <= 135) || (degree > 225 && degree <= 315)) {
                            matrix.setScale(1, scaleY);
                        }
                        matrix.postRotate(degree);


                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    }

                    //裁剪
                    if (step.mode.equals(Mode.CROP)) {

                        if (downSample) {
                            RectF rectF = (RectF) step.obj1;
                            int left = (int) (rectF.left / scale);
                            int top = (int) (rectF.top / scale);
                            int width = (int) ((rectF.right - rectF.left) / scale);
                            int height = (int) ((rectF.bottom - rectF.top) / scale);

                            bitmap = Bitmap.createBitmap(bitmap, left, top, width, height);
                        } else {
                            RectF rectF = (RectF) step.obj1;
                            bitmap = Bitmap.createBitmap(bitmap, (int) rectF.left, (int) rectF.top, (int) (rectF.right - rectF.left), (int) (rectF.bottom - rectF.top));
                        }
                    }
                }

                MainActivity.bitmap = bitmap;

                handler.sendEmptyMessage(0);
            }
        };
        thread.start();

    }

    private enum Mode {
        NUll,
        ROTATE,//旋转
        CROP//裁剪
    }

    public class ProcessStep {

        public Mode mode;
        public Object obj1;
        public Object obj2;
        public Object obj3;

        ProcessStep() {

        }
    }


    public interface RotateListener {

        void onRotate(float degree);
    }
}
