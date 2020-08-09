package com.yuxue.train;

import java.io.File;
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
 * 基于org.opencv官方包实现的训练
 * 
 * 图片文字识别训练
 * 训练出来的库文件，用于识别图片中的中文字符
 * 测试了一段时间之后，发现把中文独立出来识别，准确率更高一点
 * 
 * @author yuxue
 * @date 2020-07-02 22:16
 */
public class CnANNTrain {

    private ANN_MLP ann = ANN_MLP.create();

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    // 默认的训练操作的根目录
    private static final String DEFAULT_PATH = "D:/PlateDetect/train/chars_recognise_ann/";

    // 训练模型文件保存位置
    private static final String MODEL_PATH = DEFAULT_PATH + "ann_cn.xml";


    public void train(int _predictsize, int _neurons) {
        Mat samples = new Mat(); // 使用push_back，行数列数不能赋初始值
        Vector<Integer> trainingLabels = new Vector<Integer>();
        Random rand = new Random();

        // 加载汉字字符
        for (int i = 0; i < Constant.strChinese.length; i++) {
            String str = DEFAULT_PATH + "learn/" + Constant.strChinese[i];
            Vector<String> files = new Vector<String>();
            FileUtil.getFiles(str, files);

            // int count = 300; // 控制从训练样本中，抽取指定数量的样本
            int count = files.size(); // 不添加随机样本
            for (int j = 0; j < count; j++) {

                String filename = "";
                if(j < files.size()) {
                    filename = files.get(j);
                } else {
                    filename = files.get(rand.nextInt(files.size() - 1));   // 样本不足，随机重复提取已有的样本
                }

                Mat img = Imgcodecs.imread(filename, 0);

                // 原图样本
                samples.push_back(PlateUtil.features(img, _predictsize));
                trainingLabels.add(i);

                // 增加随机平移样本
                samples.push_back(PlateUtil.features(PlateUtil.randTranslate(img), _predictsize));
                trainingLabels.add(i);

                // 增加随机旋转样本
                samples.push_back(PlateUtil.features(PlateUtil.randRotate(img), _predictsize));
                trainingLabels.add(i);

                // 增加腐蚀样本
                samples.push_back(PlateUtil.features(PlateUtil.erode(img), _predictsize));
                trainingLabels.add(i);
            }
        }

        samples.convertTo(samples, CvType.CV_32F);

        //440   vhist.length + hhist.length + lowData.cols() * lowData.rows();
        // CV_32FC1 CV_32SC1 CV_32F
        Mat classes = Mat.zeros(trainingLabels.size(), Constant.strChinese.length, CvType.CV_32F);

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
        for (int i = 0; i < Constant.strChinese.length; i++) {

            String strChinese = Constant.strChinese[i];
            String path = DEFAULT_PATH + "learn/" + strChinese;
            Vector<String> files = new Vector<String>();
            FileUtil.getFiles(path, files);

            for (String filePath : files) {
                Mat img = Imgcodecs.imread(filePath, 0);
                Mat f = PlateUtil.features(img, Constant.predictSize);

                int index = 0;
                double maxVal = -2;

                Mat output = new Mat(1, Constant.strChinese.length, CvType.CV_32F);
                ann.predict(f, output);  // 预测结果
                for (int j = 0; j < Constant.strChinese.length; j++) {
                    double val = output.get(0, j)[0];
                    if (val > maxVal) {
                        maxVal = val;
                        index = j;
                    }
                }

                // 腐蚀  -- 识别中文字符效果会好一点，识别数字及字母效果会更差
                f = PlateUtil.features(PlateUtil.erode(img), Constant.predictSize);
                ann.predict(f, output);  // 预测结果
                for (int j = 0; j < Constant.strChinese.length; j++) {
                    double val = output.get(0, j)[0];
                    if (val > maxVal) {
                        maxVal = val;
                        index = j;
                    }
                }

                String result = Constant.strChinese[index];

                if(result.equals(strChinese)) {
                    correct++;
                } else {
                    // 删除异常样本
                    /*File f1 = new File(filePath);
                    f1.delete();*/
                    
                    System.err.print(filePath);
                    System.err.println("\t预测结果：" + Constant.KEY_CHINESE_MAP.get(result));
                }
                total++;
            }
        }
        System.out.print("total:" + total);
        System.out.print("\tcorrect:" + correct);
        System.out.print("\terror:" + (total - correct));
        System.out.println("\t计算准确率为：" + correct / (total * 1.0));

        //预测结果：
        //单字符100样本数    total:3230  correct:2725    error:505   计算准确率为：0.8436532507739938
        //单字符200样本数    total:3230  correct:2889    error:341   计算准确率为：0.8944272445820434
        //单字符300样本数    total:3230  correct:2943    error:287   计算准确率为：0.9111455108359133
        //单字符400样本数    total:3230  correct:2937    error:293   计算准确率为：0.9092879256965944
        //无随机样本               total:3230  correct:3050    error:180   计算准确率为：0.9442724458204335
        //无随机，删除异常样本  total:3050  correct:2987    error:63    计算准确率为：0.979344262295082
        //无随机，删除异常样本  total:2987  correct:2973    error:14    计算准确率为：0.9953130231001004
        //无随机，删除异常样本  total:2987  correct:2932    error:55    计算准确率为：0.9815868764646802
        //无随机，删除异常样本  total:2987  correct:2971    error:16    计算准确率为：0.9946434549715434

        // 个人测试多次之后，得出结论：
        // 1、每个字符下样本数量不一致，最多的299个样本，最少的不到10个样本；从测试结果来看，样本太少会影响预测结果
        // 2、这里的训练跟测试的样本都是基于相同的样本文件，所以测试结果存在一定的局限性，仅供参考；
        // 3、测试过程中，使用了随机样本，实际上发现重复样本对预测结果影响不大
        // 4、中文字符分离出来之后，预测准确性要高很多
        // 5、随机平移、随机旋转、膨胀、腐蚀，会增加样本数量，同时增加预测准确性
        // 6、每次重新训练后，结果是不一致的，，没有重新训练，多次使用样本预测，结果是一致的
        // 7、经过多次测试，这里的训练方案跟预测结果，准确率在90%左右
        // 8、用于训练的样本，尽量要多一点，样本特征丰富一点，这样子可以提高准确性；但是用于预测的样本，要尽量规范、正常

        return;
    }


    public static void main(String[] args) {

        CnANNTrain annT = new CnANNTrain();

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