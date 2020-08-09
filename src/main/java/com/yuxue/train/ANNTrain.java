package com.yuxue.train;

import java.util.Random;
import java.util.Vector;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.TermCriteria;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.ml.ANN_MLP;
import org.opencv.ml.Ml;
import org.opencv.ml.TrainData;

import com.yuxue.constant.Constant;
import com.yuxue.util.FileUtil;
import com.yuxue.util.PlateUtil;


/**
 * 基于org.opencv包实现的训练
 * 
 * 图片文字识别训练
 * 训练出来的库文件，用于识别图片中的数字及字母
 * 
 * 测试了一段时间之后，发现把中文独立出来识别，准确率更高一点
 * 
 * 训练的ann.xml应用：
 * 1、替换res/model/ann.xml文件
 * 2、修改com.yuxue.easypr.core.CharsIdentify.charsIdentify(Mat, Boolean, Boolean)方法
 * 
 * @author yuxue
 * @date 2020-05-14 22:16
 */
public class ANNTrain {

    private ANN_MLP ann = ANN_MLP.create();

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    // 默认的训练操作的根目录
    private static final String DEFAULT_PATH = "D:/PlateDetect/train/chars_recognise_ann/";

    // 训练模型文件保存位置
    private static final String MODEL_PATH = DEFAULT_PATH + "ann.xml";

    public void train(int _predictsize, int _neurons) {
        Mat samples = new Mat(); // 使用push_back，行数列数不能赋初始值
        Vector<Integer> trainingLabels = new Vector<Integer>();
        Random rand = new Random();
        // 加载数字及字母字符
        for (int i = 0; i < Constant.numCharacter; i++) {
            String str = DEFAULT_PATH + "learn/" + Constant.strCharacters[i];
            Vector<String> files = new Vector<String>();
            FileUtil.getFiles(str, files);  // 文件名不能包含中文

            // int count = 200; // 控制从训练样本中，抽取指定数量的样本
            int count = files.size(); // 控制从训练样本中，抽取指定数量的样本
            for (int j = 0; j < count; j++) {

                String filename = "";
                if(j < files.size()) {
                    filename = files.get(j);
                } else {
                    filename = files.get(rand.nextInt(files.size() - 1));   // 样本不足，随机重复提取已有的样本
                }

                Mat img = Imgcodecs.imread(filename, 0);

                Mat f = PlateUtil.features(img, _predictsize);
                samples.push_back(f);
                trainingLabels.add(i); // 每一幅字符图片所对应的字符类别索引下标

                // 增加随机平移样本
                samples.push_back(PlateUtil.features(PlateUtil.randTranslate(img), _predictsize));
                trainingLabels.add(i); 

                // 增加随机旋转样本
                samples.push_back(PlateUtil.features(PlateUtil.randRotate(img), _predictsize));
                trainingLabels.add(i); 

                // 增加膨胀样本
                samples.push_back(PlateUtil.features(PlateUtil.dilate(img), _predictsize));
                trainingLabels.add(i); 

                // 增加腐蚀样本
                /*samples.push_back(PlateUtil.features(PlateUtil.erode(img), _predictsize));
                trainingLabels.add(i); */
            }
        }

        samples.convertTo(samples, CvType.CV_32F);

        //440   vhist.length + hhist.length + lowData.cols() * lowData.rows();
        // CV_32FC1 CV_32SC1 CV_32F
        Mat classes = Mat.zeros(trainingLabels.size(), Constant.strCharacters.length, CvType.CV_32F);

        float[] labels = new float[trainingLabels.size()];
        for (int i = 0; i < labels.length; ++i) {
            classes.put(i, trainingLabels.get(i), 1.f);
        }

        // samples.type() == CV_32F || samples.type() == CV_32S 
        TrainData train_data = TrainData.create(samples, Ml.ROW_SAMPLE, classes);

        ann.clear();
        Mat layers = new Mat(1, 3, CvType.CV_32F);
        layers.put(0, 0, samples.cols());   // 样本特征数 140  10*10 + 20+20
        layers.put(0, 1, _neurons); // 神经元个数
        layers.put(0, 2, classes.cols());   // 字符数

        ann.setLayerSizes(layers);
        ann.setActivationFunction(ANN_MLP.SIGMOID_SYM, 1, 1);
        ann.setTrainMethod(ANN_MLP.BACKPROP);
        TermCriteria criteria = new TermCriteria(TermCriteria.EPS + TermCriteria.MAX_ITER, 30000, 0.0001);
        ann.setTermCriteria(criteria);
        ann.setBackpropWeightScale(0.1);
        ann.setBackpropMomentumScale(0.1);
        ann.train(train_data);

        // FileStorage fsto = new FileStorage(MODEL_PATH, FileStorage.WRITE);
        // ann.write(fsto, "ann");
        ann.save(MODEL_PATH);
    }


    public void predict() {
        ann.clear();
        ann = ANN_MLP.load(MODEL_PATH);

        int total = 0;
        int correct = 0;

        // 遍历测试样本下的所有文件，计算预测准确率
        for (int i = 0; i < Constant.strCharacters.length; i++) {

            char c = Constant.strCharacters[i];
            String path = DEFAULT_PATH + "learn/" + c;

            Vector<String> files = new Vector<String>();
            FileUtil.getFiles(path, files);

            for (String filePath : files) {

                Mat img = Imgcodecs.imread(filePath, 0);
                Mat f = PlateUtil.features(img, Constant.predictSize);

                int index = 0;
                double maxVal = -2;
                Mat output = new Mat(1, Constant.strCharacters.length, CvType.CV_32F);
                ann.predict(f, output);  // 预测结果
                for (int j = 0; j < Constant.strCharacters.length; j++) {
                    double val = output.get(0, j)[0];
                    if (val > maxVal) {
                        maxVal = val;
                        index = j;
                    }
                }

                // 膨胀
                f = PlateUtil.features(PlateUtil.dilate(img), Constant.predictSize);
                ann.predict(f, output);  // 预测结果
                for (int j = 0; j < Constant.strCharacters.length; j++) {
                    double val = output.get(0, j)[0];
                    if (val > maxVal) {
                        maxVal = val;
                        index = j;
                    }
                }

                String result = String.valueOf(Constant.strCharacters[index]);
                if(result.equals(String.valueOf(c))) {
                    correct++;
                } else {
                    // 删除异常样本
                    /*File f1 = new File(filePath);
                    f1.delete();*/

                    System.err.print(filePath);
                    System.err.println("\t预测结果：" + result);
                }
                total++;
            }

        }

        System.out.print("total:" + total);
        System.out.print("\tcorrect:" + correct);
        System.out.print("\terror:" + (total - correct));
        System.out.println("\t计算准确率为：" + correct / (total * 1.0));

        //牛逼，我操     total:13178  correct:13139   error:39    计算准确率为：0.9970405220822584

        return;
    }

    public static void main(String[] args) {

        ANNTrain annT = new ANNTrain();
        // 这里演示只训练model文件夹下的ann.xml，此模型是一个predictSize=10,neurons=40的ANN模型
        // 可根据需要训练不同的predictSize或者neurons的ANN模型
        // 根据机器的不同，训练时间不一样，但一般需要10分钟左右，所以慢慢等一会吧
        // 可以考虑中文，数字字母分开训练跟识别，提高准确性
        annT.train(Constant.predictSize, Constant.neurons);

        annT.predict();

        System.out.println("The end.");
        return;
    }


}