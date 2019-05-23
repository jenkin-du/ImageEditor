package com.uestc.jenkin.imageeditor;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.edmodo.cropper.CropImageView;

/**
 * <pre>
 *     author : jenkin
 *     e-mail : jekin-du@foxmail.com
 *     time   : 2019/05/09
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class CropActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "CropActivity";
    private CropImageView cropImageIV;
    public static Bitmap processBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_big_img);
        //初始化
        initView();
        //初始化数据
        initData();
    }

    private void initData() {

        processBitmap = EditActivity.processBitmap;
        cropImageIV.setImageBitmap(processBitmap);
    }

    private void initView() {

        cropImageIV = findViewById(R.id.iv_crop_big_img);

        Button saveBtn = findViewById(R.id.btn_crop_big_img_save);
        Button unsaveBtn = findViewById(R.id.btn_crop_big_img_unsave);

        saveBtn.setOnClickListener(this);
        unsaveBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            //返回
            case R.id.btn_crop_big_img_unsave:
                finish();
                break;
            //保存编辑的图片
            case R.id.btn_crop_big_img_save:
                saveData();
                break;
        }

    }

    /**
     * 保存数据
     */
    private void saveData() {

        RectF rectF = cropImageIV.getActualCropRect();

        Intent intent = new Intent();
        intent.putExtra("rectangle", rectF);
        setResult(RESULT_OK, intent);
        finish();

    }
}
