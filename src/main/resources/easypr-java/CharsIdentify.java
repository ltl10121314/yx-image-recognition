package com.yuxue.easypr.core;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_ml.ANN_MLP;

import com.yuxue.constant.Constant;
import com.yuxue.util.Convert;


/**
 * 字符检测
 * @author yuxue
 * @date 2020-04-24 15:31
 */
public class CharsIdentify {

    private ANN_MLP ann=ANN_MLP.create();

    public CharsIdentify() {
        loadModel(Constant.DEFAULT_ANN_PATH);
    }

    public void loadModel(String path) {
        this.ann.clear();
        // 加载ann配置文件  图像转文字的训练库文件
        //ann=ANN_MLP.loadANN_MLP(path, "ann"); 
        ann = ANN_MLP.load(path);
    }
    

    /**
     * @param input
     * @param isChinese
     * @return
     */
    public String charsIdentify(final Mat input, final Boolean isChinese, final Boolean isSpeci) {
        String result = "";
        
        /*String name = "D:/PlateDetect/train/chars_recognise_ann/" + System.currentTimeMillis() + ".jpg";
        opencv_imgcodecs.imwrite(name, input);
        Mat img = opencv_imgcodecs.imread(name);
        Mat f = CoreFunc.features(img, Constant.predictSize);*/
        
        Mat f = CoreFunc.features(input, Constant.predictSize);

        int index = this.classify(f, isChinese, isSpeci);
        
        System.err.print(index);
        if (index < Constant.numCharacter) {
            result = String.valueOf(Constant.strCharacters[index]);
        } else {
            String s = Constant.strChinese[index - Constant.numCharacter];
            result = Constant.KEY_CHINESE_MAP.get(s);   // 编码转中文
        }
        System.err.println(result);
        return result;
    }
    
    private int classify(final Mat f, final Boolean isChinses, final Boolean isSpeci) {
        int result = -1;
        
        Mat output = new Mat(1, 140, opencv_core.CV_32F);

        ann.predict(f, output, 0);  // 预测结果

        int ann_min = (!isChinses) ? ((isSpeci) ? 10 : 0) : Constant.numCharacter;
        int ann_max = (!isChinses) ? Constant.numCharacter : Constant.numAll;

        float maxVal = -2;

        for (int j = ann_min; j < ann_max; j++) {
            float val = Convert.toFloat(output.ptr(0, j));
            if (val > maxVal) {
                maxVal = val;
                result = j;
            }
        }
        return result;
    }


}
