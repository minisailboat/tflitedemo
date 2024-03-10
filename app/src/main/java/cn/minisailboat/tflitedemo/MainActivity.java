package cn.minisailboat.tflitedemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageView = null;

    private static MainActivity MATH = null;

    public static MainActivity getInstance() {
        return MATH;
    }

    public TFLiteDetector tfLiteDetector = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MATH = this;
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        imageView = findViewById(R.id.image);

        // tfLiteDetector = new TFLiteDetector(640,true,"jtd_16.tflite","jtd_lable.txt",3);
        // tfLiteDetector.Init();
        // if (tfLiteDetector.getDetector() == null) {
        //     Toast.makeText(this, "加载失败...", Toast.LENGTH_LONG).show();
        //     return;
        // }
    }

    public Bitmap resizeBitmap(Bitmap source, int maxSize) {
        int outWidth;
        int outHeight;
        int inWidth = source.getWidth();
        int inHeight = source.getHeight();
        if(inWidth > inHeight){
            outWidth = maxSize;
            outHeight = (inHeight * maxSize) / inWidth;
        } else {
            outHeight = maxSize;
            outWidth = (inWidth * maxSize) / inHeight;
        }
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(source, outWidth, outHeight, false);
        Bitmap outputImage = Bitmap.createBitmap(maxSize, maxSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outputImage);
        canvas.drawColor(Color.WHITE);
        int left = (maxSize - outWidth) / 2;
        int top = (maxSize - outHeight) / 2;
        canvas.drawBitmap(resizedBitmap, left, top, null);
        return outputImage;
    }

    public float[][][][] bitmapToFloatArray(Bitmap bitmap) {

        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        // 初始化一个float数组
        float[][][][] result = new float[1][height][width][3];
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                // 获取像素值
                int pixel = bitmap.getPixel(j, i);
                // 将RGB值分离并进行标准化（假设你需要将颜色值标准化到0-1之间）
                result[0][i][j][0] = ((pixel >> 16) & 0xFF) / 255.0f;
                result[0][i][j][1] = ((pixel >> 8) & 0xFF) / 255.0f;
                result[0][i][j][2] = (pixel & 0xFF) / 255.0f;
            }
        }
        return result;
    }

    public void identifyJTD(Bitmap bitmap) {

        // List<Detector.Recognition> recognitions = tfLiteDetector.getDetector().recognizeImage(bitmap);
        // for (Detector.Recognition item : recognitions) {
        //     System.out.println();
        //     System.out.println("ID" + item.getId());
        //     System.out.println("名称" + item.getTitle());
        //     System.out.println("得分：" + item.getConfidence());
        // }

        // 构建解释器
        Interpreter interpreter = DetectTool.getInterpreter(this, "jtd_16");
        if (interpreter == null) {
            System.out.println("加载失败");
            return;
        }
        System.out.println("加载完成");
        // 将要处理的Bitmap图像缩放为640×640
        Bitmap resize_bitmap = resizeBitmap(bitmap, 640);
        // 转换为输入层(1, 640, 640, 3)结构的float数组
        float[][][][] input_arr = bitmapToFloatArray(resize_bitmap);
        // 构建一个空的输出结构
        float[][][] outArray = new float[1][7][8400]; // 中心点x, 中心点y, 宽度w, 高度h, 分类1的得分, 分类2的得分, 分类3的得分
        // 运行解释器，input_arr是输入，它会将结果写到outArray中
        interpreter.run(input_arr, outArray);
        // System.out.println(Arrays.deepToString(outArray[0]));
        // 取出(1, 7, 8400)中的(7, 8400)
        float[][] matrix_2d = outArray[0];
        // (7, 8400)变为(8400, 7)
        float[][] outputMatrix = new float[8400][7];
        for (int i = 0; i < 8400; i++) {
            for (int j = 0; j < 6; j++) {
                outputMatrix[i][j] = matrix_2d[j][i];
            }
        }
        // 中心点x, 中心点y, 宽度w, 高度h, 分类1的得分, 分类2的得分, 分类3的得分
        for (float[] item: outputMatrix) {
            if (item[4] > 5 || item[5] > 5 || item[6] > 5) {
                System.out.println("中心点：" + new Point((int) item[0], (int) item[1]));
                System.out.println("宽高点：" + item[2] + " x " + item[3]);
                System.out.println("分类一：" + item[4]);
                System.out.println("分类二：" + item[5]);
                System.out.println("分类三：" + item[6]);
            }
        }

        // float threshold = 0.6f; // 类别准确率筛选
        // float non_max = 0.8f; // nms非极大值抑制
        // ArrayList<float[]> boxes = new ArrayList<>();
        // ArrayList<Float> maxScores = new ArrayList();
        // for (float[] detection : outputMatrix) {
        //     // 6位数中的后两位是两类的置信度
        //     float[] score = Arrays.copyOfRange(detection, 4, 6);
        //     float maxValue = score[0];
        //     float maxIndex = 0;
        //     for(int i=1; i < score.length;i++){
        //         if(score[i] > maxValue){ // 找出最大的一项
        //             maxValue = score[i];
        //             maxIndex = i;
        //         }
        //     }
        //     if (maxValue >= threshold) { // 如果置信度超过60%则记录
        //         detection[4] = maxIndex;
        //         detection[5] = maxValue;
        //         boxes.add(detection); // 筛选后的框
        //         maxScores.add(maxValue); // 筛选后的准确率
        //     }
        // }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imgUri = data.getData();
            try {
                // 使用选定的图像
                // 此处可以将选择的图片传递给 ImageView 或其他 UI 控件，或者对其进行处理
                Bitmap bitmap = getBitmapFromUri(imgUri);
                imageView.setImageBitmap(bitmap);
                identifyJTD(bitmap);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        // 使用 ContentResolver 从 URI 获取输入流
        InputStream inputStream = getContentResolver().openInputStream(uri);
        // 使用 BitmapFactory 解码输入流为 Bitmap
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        // 关闭输入流
        if (inputStream != null) {
            inputStream.close();
        }
        // 返回 Bitmap
        return bitmap;
    }

    public void selectAndUseImg(View view) {
        openGallery();
    }
}