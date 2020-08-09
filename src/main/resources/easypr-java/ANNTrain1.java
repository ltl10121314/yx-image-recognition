package com.yuxue.train;

import java.util.Vector;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_ml.*;

import org.bytedeco.javacpp.opencv_imgcodecs;
import org.bytedeco.javacpp.opencv_core.Mat;

import com.yuxue.constant.Constant;
import com.yuxue.easypr.core.CoreFunc;
import com.yuxue.util.FileUtil;

/**
 * 基于org.bytedeco.javacpp包实现的训练
 * 
 * 图片文字识别训练
 * 训练出来的库文件，用于识别图片中的文字
 * 
 * 训练的ann.xml应用：
 * 1、替换res/model/ann.xml文件
 * 2、修改com.yuxue.easypr.core.CharsIdentify.charsIdentify(Mat, Boolean, Boolean)方法
 * 
 * @author yuxue
 * @date 2020-05-14 22:16
 */
public class ANNTrain1 {

    private ANN_MLP ann = ANN_MLP.create();

    // 默认的训练操作的根目录
    private static final String DEFAULT_PATH = "D:/PlateDetect/train/chars_recognise_ann/";

    // 训练模型文件保存位置
    private static final String MODEL_PATH = "res/model/ann.xml";
    
    
    public void train(int _predictsize, int _neurons) {
        Mat samples = new Mat(); // 使用push_back，行数列数不能赋初始值
        Vector<Integer> trainingLabels = new Vector<Integer>();
        // 加载数字及字母字符
        for (int i = 0; i < Constant.numCharacter; i++) {
            String str = DEFAULT_PATH + "learn/" + Constant.strCharacters[i];
            Vector<String> files = new Vector<String>();
            FileUtil.getFiles(str, files);

            int size = (int) files.size();
            for (int j = 0; j < size; j++) {
                Mat img = opencv_imgcodecs.imread(files.get(j), 0);
                // System.err.println(files.get(j)); // 文件名不能包含中文
                Mat f = CoreFunc.features(img, _predictsize);
                samples.push_back(f);
                trainingLabels.add(i); // 每一幅字符图片所对应的字符类别索引下标
            }
        }

        // 加载汉字字符
        for (int i = 0; i < Constant.strChinese.length; i++) {
            String str = DEFAULT_PATH + "learn/" + Constant.strChinese[i];
            Vector<String> files = new Vector<String>();
            FileUtil.getFiles(str, files);

            int size = (int) files.size();
            for (int j = 0; j < size; j++) {
                Mat img = opencv_imgcodecs.imread(files.get(j), 0);
                // System.err.println(files.get(j));   // 文件名不能包含中文
                Mat f = CoreFunc.features(img, _predictsize);
                samples.push_back(f);
                trainingLabels.add(i + Constant.numCharacter);
            }
        }


        //440   vhist.length + hhist.length + lowData.cols() * lowData.rows();
        // CV_32FC1 CV_32SC1 CV_32F
        Mat classes = new Mat(trainingLabels.size(), Constant.numAll, CV_32F);
        
        float[] labels = new float[trainingLabels.size()];
        for (int i = 0; i < labels.length; ++i) {
            classes.ptr(i, trainingLabels.get(i)).putFloat(1.f);
            
        }

        // samples.type() == CV_32F || samples.type() == CV_32S 
        TrainData train_data = TrainData.create(samples, ROW_SAMPLE, classes);

        ann.clear();
        Mat layers = new Mat(1, 3, CV_32SC1);
        layers.ptr(0, 0).putInt(samples.cols());
        layers.ptr(0, 1).putInt(_neurons);
        layers.ptr(0, 2).putInt(classes.cols());

        System.out.println(layers);
        
        ann.setLayerSizes(layers);
        ann.setActivationFunction(ANN_MLP.SIGMOID_SYM, 1, 1);
        ann.setTrainMethod(ANN_MLP.BACKPROP);
        TermCriteria criteria = new TermCriteria(TermCriteria.EPS + TermCriteria.MAX_ITER, 30000, 0.0001);
        ann.setTermCriteria(criteria);
        ann.setBackpropWeightScale(0.1);
        ann.setBackpropMomentumScale(0.1);
        ann.train(train_data);

        //FileStorage fsto = new FileStorage(MODEL_PATH, FileStorage.WRITE);
        //ann.write(fsto, "ann");
        ann.save(MODEL_PATH);
    }
    
    
    public void predict() {
        ann.clear();
        ann = ANN_MLP.load(MODEL_PATH);
        //ann = ANN_MLP.loadANN_MLP(MODEL_PATH, "ann");
        Vector<String> files = new Vector<String>();
        FileUtil.getFiles(DEFAULT_PATH + "test/", files);
        
        for (String string : files) {
            Mat img = opencv_imgcodecs.imread(string);
            Mat f = CoreFunc.features(img, Constant.predictSize);
            
            // 140 predictSize = 10; vhist.length + hhist.length + lowData.cols() * lowData.rows();
            // 440 predictSize = 20;
            Mat output = new Mat(1, 140, CV_32F);
            //ann.predict(f, output, 0);  // 预测结果
            // System.err.println(string + "===>" + (int) ann.predict(f, output, 0));

            int index = (int) ann.predict(f, output, 0);
            
            String result = "";
            if (index < Constant.numCharacter) {
                result = String.valueOf(Constant.strCharacters[index]);
            } else {
                String s = Constant.strChinese[index - Constant.numCharacter];
                result = Constant.KEY_CHINESE_MAP.get(s);   // 编码转中文
            }
            System.err.println(string + "===>" + result);
            
            // ann.predict(f, output, 0);
            // System.err.println(string + "===>" + output.get(0, 0)[0]);
            
        }
    }

    public static void main(String[] args) {
        
        ANNTrain1 annT = new ANNTrain1();
        // 这里演示只训练model文件夹下的ann.xml，此模型是一个predictSize=10,neurons=40的ANN模型
        // 可根据需要训练不同的predictSize或者neurons的ANN模型
        // 根据机器的不同，训练时间不一样，但一般需要10分钟左右，所以慢慢等一会吧。
        annT.train(Constant.predictSize, Constant.neurons);

        annT.predict();
        
        System.out.println("The end.");
    }


}