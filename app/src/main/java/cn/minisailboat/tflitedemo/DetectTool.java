package cn.minisailboat.tflitedemo;

import android.content.Context;
import android.content.res.AssetFileDescriptor;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author xm
 * @version 1.0
 * @date 2024/03/10
 */
public class DetectTool {
    // 从Assets下加载.tflite文件
    private static MappedByteBuffer loadModelFile(Context context, String fileName) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(fileName);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // 构建Interpreter，这是tflite文件的解释器
    public static Interpreter getInterpreter(Context context, String fileName){
        Interpreter.Options options = new Interpreter.Options();
        options.setNumThreads(4);
        Interpreter interpreter = null;
        try {
            interpreter = new Interpreter(loadModelFile(context, fileName + ".tflite"), options);
        } catch (IOException e) {
            throw new RuntimeException("加载模型文件时出错。", e);
        }
        return interpreter;
    }
}
