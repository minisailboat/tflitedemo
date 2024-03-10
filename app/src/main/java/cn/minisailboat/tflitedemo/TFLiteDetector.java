package cn.minisailboat.tflitedemo;

import android.content.Context;
import android.graphics.Bitmap;

import java.util.List;

public class TFLiteDetector
{
    private Detector Detector;
    private final int TF_OD_API_INPUT_SIZE;
    private final boolean TF_OD_API_IS_QUANTIZED;
    private final String TF_OD_API_MODEL_FILE;
    private final String TF_OD_API_LABELS_FILE;
    private final int MaxResultsNum;//返回结果集最大长度
    private boolean InitFlag;
    public TFLiteDetector(int Input_Size, boolean Quantized, String Model_FileName, String Labels_FileName, int MaxResultsNum)
    {
        this.TF_OD_API_INPUT_SIZE = Input_Size;
        this.TF_OD_API_IS_QUANTIZED = Quantized;
        this.TF_OD_API_MODEL_FILE = Model_FileName;
        this.TF_OD_API_LABELS_FILE = Labels_FileName;
        this.MaxResultsNum = MaxResultsNum;
    }
    /**
     * 模型初始化方法 如果初始化失败则可以再执行该方法尝试重新初始化
     * 只要在 初始化失败/未初始化过 的情况下执行该方法才有效
     * @return true-加载成功可以使用 false-加载失败不可使用
     */
    public boolean Init()
    {
        if(!getInitFlag())
        {
            int CropSize = this.TF_OD_API_INPUT_SIZE;
            try
            {
                this.Detector = TFLiteObjectDetectionAPIModel.create((Context) MainActivity.getInstance(), this.TF_OD_API_MODEL_FILE,this.TF_OD_API_LABELS_FILE,this.TF_OD_API_INPUT_SIZE,this.TF_OD_API_IS_QUANTIZED,this.MaxResultsNum);
                CropSize = this.TF_OD_API_INPUT_SIZE;
                System.out.println("["+this.TF_OD_API_MODEL_FILE+"]模型 加载完毕");
                this.InitFlag = true;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.out.println("["+this.TF_OD_API_MODEL_FILE+"]模型 加载失败");
                this.InitFlag = false;
            }
        }
        return this.InitFlag;
    }
    /**
     * 该模型的初始化状态 如果加载成功则初始化成功 如果加载失败则初始化失败，该模型也无法使用
     * @return true-加载成功可以使用 false-加载失败不可使用
     */
    public boolean getInitFlag()
    {
        return this.InitFlag;
    }
    /**
     * 获取 Detector
     * @return 如果未加载成功则返回null
     */
    public Detector getDetector()
    {
        if(getInitFlag())
        {
            return this.Detector;
        }
        return null;
    }
    /**
     * 将图片进行对象检测
     * @param Image 进行对象检测的图片
     * @return 对象检测结果，如果传入图片为空则返回null
     */
    public List<Detector.Recognition> Recognize(Bitmap Image)
    {
        if(getInitFlag() && Image == null)
        {
            System.out.println("传入图片不能为空");
            return null;
        }
        return getDetector().recognizeImage(Image);
    }
}
