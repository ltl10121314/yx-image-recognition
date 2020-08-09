package com.yuxue.train;

import java.util.*;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_ml.*;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_imgcodecs;

import com.yuxue.easypr.core.Features;
import com.yuxue.easypr.core.SVMCallback;
import com.yuxue.util.Convert;
import com.yuxue.util.FileUtil;

/**
 * 基于org.bytedeco.javacpp包实现的训练
 * JavaCPP 是一个开源库，它提供了在 Java 中高效访问本地 C++的方法
 * 
 * 图片识别车牌训练
 * 训练出来的库文件，用于判断切图是否包含车牌
 * 
 * 训练的svm.xml应用：
 * 1、替换res/model/svm.xml文件
 * 2、修改com.yuxue.easypr.core.PlateJudge.plateJudge(Mat) 方法
 *      将样本处理方法切换一下，即将对应被注释掉的模块代码取消注释
 * @author yuxue
 * @date 2020-05-14 22:16
 */
public class SVMTrain1 {

    private SVMCallback callback = new Features();

    // 默认的训练操作的根目录
    private static final String DEFAULT_PATH = "D:/PlateDetect/train/plate_detect_svm/";

    // 训练模型文件保存位置
    private static final String MODEL_PATH = DEFAULT_PATH + "svm.xml";

    private static final String hasPlate = "HasPlate";
    private static final String noPlate = "NoPlate";

    public SVMTrain1() {
    }

    public SVMTrain1(SVMCallback callback) {
        this.callback = callback;
    }

    /**
     * 将learn文件夹下的图片，转存到tain test文件夹下，区分hasPalte noPlate
     * 随机选取bound%作为训练数据，30%作为测试数据
     * @param bound
     * @param name
     */
    private void learn2Plate(float bound, final String name) {
        final String filePath = DEFAULT_PATH + "learn/" + name;
        Vector<String> files = new Vector<String>();

        //// 获取该路径下的所有文件
        FileUtil.getFiles(filePath, files);
        int size = files.size();
        if (0 == size) {
            System.err.println("当前目录下没有文件: " + filePath);
            return;
        }
        Collections.shuffle(files, new Random(new Date().getTime()));

        //// 随机选取70%作为训练数据，30%作为测试数据
        int boundry = (int) (bound * size);

        // 重新创建目录
        FileUtil.recreateDir(DEFAULT_PATH + "train/" + name);
        FileUtil.recreateDir(DEFAULT_PATH + "test/" + name);

        for (int i = 0; i < boundry; i++) {
            Mat img = opencv_imgcodecs.imread(files.get(i));
            String str = DEFAULT_PATH + "train/" + name + "/" + name + "_" + Integer.valueOf(i).toString() + ".jpg";
            opencv_imgcodecs.imwrite(str, img);
        }

        for (int i = boundry; i < size; i++) {
            Mat img = opencv_imgcodecs.imread(files.get(i));
            String str = DEFAULT_PATH + "test/" + name + "/" + name + "_" + Integer.valueOf(i).toString() + ".jpg";
            opencv_imgcodecs.imwrite(str, img);
        }
    }

    /**
     * 获取训练图片
     * @param trainingImages
     * @param trainingLabels
     * @param name
     */
    private void getPlateTrain(Mat trainingImages, Vector<Integer> trainingLabels, final String name, int label) {
        // int label = 1;
        final String filePath = DEFAULT_PATH + "train/" + name;
        Vector<String> files = new Vector<String>();

        // 获取该路径下的所有文件
        FileUtil.getFiles(filePath, files);

        int size = files.size();
        if (null == files || size <= 0) {
            System.out.println("File not found in " + filePath);
            return;
        }
        for (int i = 0; i < size; i++) {
            // System.out.println(files.get(i));
            Mat inMat = opencv_imgcodecs.imread(files.get(i));
            // 调用回调函数决定特征
            // Mat features = this.callback.getHisteqFeatures(inMat);
            Mat features = this.callback.getHistogramFeatures(inMat);
            // 通过直方图均衡化后的彩色图进行预测
            Mat p = features.reshape(1, 1);
            p.convertTo(p, opencv_core.CV_32F);

            // 136  36  14688   1   变换尺寸
            // System.err.println(inMat.cols() + "\t" + inMat.rows() + "\t" + p.cols() + "\t" + p.rows());

            trainingImages.push_back(p); // 合并成一张图片
            trainingLabels.add(label);
        }
    }

    private void getPlateTest(MatVector testingImages, Vector<Integer> testingLabels, final String name, int label) {
        // int label = 1;
        final String filePath = DEFAULT_PATH + "test/" + name;
        Vector<String> files = new Vector<String>();
        FileUtil.getFiles(filePath, files);

        int size = files.size();
        if (0 == size) {
            System.out.println("File not found in " + filePath);
            return;
        }
        System.out.println("get " + name + " test!");
        for (int i = 0; i < size; i++) {
            Mat inMat = opencv_imgcodecs.imread(files.get(i));
            testingImages.push_back(inMat);
            testingLabels.add(label);
        }
    }

    // ! 测试SVM的准确率，回归率以及FScore
    public void getAccuracy(Mat testingclasses_preditc, Mat testingclasses_real) {
        int channels = testingclasses_preditc.channels();
        System.out.println("channels: " + Integer.valueOf(channels).toString());
        int nRows = testingclasses_preditc.rows();
        System.out.println("nRows: " + Integer.valueOf(nRows).toString());
        int nCols = testingclasses_preditc.cols() * channels;
        System.out.println("nCols: " + Integer.valueOf(nCols).toString());
        int channels_real = testingclasses_real.channels();
        System.out.println("channels_real: " + Integer.valueOf(channels_real).toString());
        int nRows_real = testingclasses_real.rows();
        System.out.println("nRows_real: " + Integer.valueOf(nRows_real).toString());
        int nCols_real = testingclasses_real.cols() * channels;
        System.out.println("nCols_real: " + Integer.valueOf(nCols_real).toString());

        double count_all = 0;
        double ptrue_rtrue = 0;
        double ptrue_rfalse = 0;
        double pfalse_rtrue = 0;
        double pfalse_rfalse = 0;

        for (int i = 0; i < nRows; i++) {

            final float predict = Convert.toFloat(testingclasses_preditc.ptr(i));
            final float real = Convert.toFloat(testingclasses_real.ptr(i));

            count_all++;

            // System.out.println("predict:" << predict).toString());
            // System.out.println("real:" << real).toString());

            if (predict == 1.0 && real == 1.0)
                ptrue_rtrue++;
            if (predict == 1.0 && real == 0)
                ptrue_rfalse++;
            if (predict == 0 && real == 1.0)
                pfalse_rtrue++;
            if (predict == 0 && real == 0)
                pfalse_rfalse++;
        }

        System.out.println("count_all: " + Double.valueOf(count_all).toString());
        System.out.println("ptrue_rtrue: " + Double.valueOf(ptrue_rtrue).toString());
        System.out.println("ptrue_rfalse: " + Double.valueOf(ptrue_rfalse).toString());
        System.out.println("pfalse_rtrue: " + Double.valueOf(pfalse_rtrue).toString());
        System.out.println("pfalse_rfalse: " + Double.valueOf(pfalse_rfalse).toString());

        double precise = 0;
        if (ptrue_rtrue + ptrue_rfalse != 0) {
            precise = ptrue_rtrue / (ptrue_rtrue + ptrue_rfalse);
            System.out.println("precise: " + Double.valueOf(precise).toString());
        } else {
            System.out.println("precise: NA");
        }

        double recall = 0;
        if (ptrue_rtrue + pfalse_rtrue != 0) {
            recall = ptrue_rtrue / (ptrue_rtrue + pfalse_rtrue);
            System.out.println("recall: " + Double.valueOf(recall).toString());
        } else {
            System.out.println("recall: NA");
        }

        if (precise + recall != 0) {
            double F = (precise * recall) / (precise + recall);
            System.out.println("F: " + Double.valueOf(F).toString());
        } else {
            System.out.println("F: NA");
        }
    }

    /**
     * 训练
     * @param dividePrepared
     * @return
     */
    public int svmTrain(boolean dividePrepared) {

        Mat classes = new Mat();
        Mat trainingData = new Mat();
        Mat trainingImages = new Mat();
        Vector<Integer> trainingLabels = new Vector<Integer>();

        // 分割learn里的数据到train和test里 // 从库里面选取训练样本
        if (!dividePrepared) {
            learn2Plate(0.1f, hasPlate);  // 性能不好的机器，最好不要挑选太多的样本，这个方案太消耗资源了。
            learn2Plate(0.1f, noPlate);
        }

        // System.err.println("Begin to get train data to memory");

        getPlateTrain(trainingImages, trainingLabels, hasPlate, 0);
        getPlateTrain(trainingImages, trainingLabels, noPlate, 1);

        // System.err.println(trainingImages.cols());

        trainingImages.copyTo(trainingData);
        trainingData.convertTo(trainingData, CV_32F);

        int[] labels = new int[trainingLabels.size()];
        for (int i = 0; i < trainingLabels.size(); ++i) {
            labels[i] = trainingLabels.get(i).intValue();
        }
        new Mat(labels).copyTo(classes);

        TrainData train_data = TrainData.create(trainingData, ROW_SAMPLE, classes);

        SVM svm = SVM.create();

        try {
            TermCriteria criteria = new TermCriteria(TermCriteria.EPS + TermCriteria.MAX_ITER, 20000, 0.0001);
            svm.setTermCriteria(criteria); // 指定
            svm.setKernel(SVM.RBF); // 使用预先定义的内核初始化
            svm.setType(SVM.C_SVC); // SVM的类型,默认是：SVM.C_SVC
            svm.setGamma(0.1); // 核函数的参数
            svm.setNu(0.1); // SVM优化问题参数
            svm.setC(1); // SVM优化问题的参数C
            svm.setP(0.1);
            svm.setDegree(0.1);
            svm.setCoef0(0.1);

            svm.trainAuto(train_data, 10,
                    SVM.getDefaultGrid(SVM.C), 
                    SVM.getDefaultGrid(SVM.GAMMA),
                    SVM.getDefaultGrid(SVM.P),
                    SVM.getDefaultGrid(SVM.NU),
                    SVM.getDefaultGrid(SVM.COEF),
                    SVM.getDefaultGrid(SVM.DEGREE),
                    true);

        } catch (Exception err) {
            System.out.println(err.getMessage());
        }

        System.out.println("Svm generate done!");

        /*FileStorage fsTo = new FileStorage(MODEL_PATH, FileStorage.WRITE);
        svm.write(fsTo, "svm");*/
        svm.save(MODEL_PATH);
        return 0;
    }

    // 测试
    public int svmPredict() {
        SVM svm = SVM.create();
        try {
            svm.clear();
            // svm = SVM.loadSVM(MODEL_PATH, "svm");
            svm = SVM.load(MODEL_PATH);
        } catch (Exception err) {
            System.err.println(err.getMessage());
            return 0; // next predict requires svm
        }

        System.out.println("Begin to predict");
        // Test SVM
        MatVector testingImages = new MatVector();
        Vector<Integer> testingLabels_real = new Vector<Integer>();

        // 将测试数据加载入内存
        getPlateTest(testingImages, testingLabels_real, hasPlate, 0);
        getPlateTest(testingImages, testingLabels_real, noPlate, 1);

        double count_all = 0;
        double ptrue_rtrue = 0;
        double ptrue_rfalse = 0;
        double pfalse_rtrue = 0;
        double pfalse_rfalse = 0;

        long size =  testingImages.size();
        System.err.println(size);

        for (int i = 0; i < size; i++) {
            Mat inMat = testingImages.get(i);

            // Mat features = callback.getHisteqFeatures(inMat);
            Mat features = callback.getHistogramFeatures(inMat);
            Mat p = features.reshape(1, 1);
            p.convertTo(p, opencv_core.CV_32F);

            // System.out.println(p.cols() + "\t" + p.rows() + "\t" + p.type());

            // samples.cols == var_count && samples.type() == CV_32F
            // var_count 的值会在svm.xml库文件中有体现
            float predoct = svm.predict(features);

            int predict = (int) predoct; // 预期值
            int real = testingLabels_real.get(i); // 实际值

            if (predict == 1 && real == 1)
                ptrue_rtrue++;
            if (predict == 1 && real == 0)
                ptrue_rfalse++;
            if (predict == 0 && real == 1)
                pfalse_rtrue++;
            if (predict == 0 && real == 0)
                pfalse_rfalse++;
        }

        count_all = size;
        System.out.println("Get the Accuracy!");

        System.out.println("count_all: " + Double.valueOf(count_all).toString());
        System.out.println("ptrue_rtrue: " + Double.valueOf(ptrue_rtrue).toString());
        System.out.println("ptrue_rfalse: " + Double.valueOf(ptrue_rfalse).toString());
        System.out.println("pfalse_rtrue: " + Double.valueOf(pfalse_rtrue).toString());
        System.out.println("pfalse_rfalse: " + Double.valueOf(pfalse_rfalse).toString());

        double precise = 0;
        if (ptrue_rtrue + ptrue_rfalse != 0) {
            precise = ptrue_rtrue / (ptrue_rtrue + ptrue_rfalse);
            System.out.println("precise: " + Double.valueOf(precise).toString());
        } else
            System.out.println("precise: NA");

        double recall = 0;
        if (ptrue_rtrue + pfalse_rtrue != 0) {
            recall = ptrue_rtrue / (ptrue_rtrue + pfalse_rtrue);
            System.out.println("recall: " + Double.valueOf(recall).toString());
        } else
            System.out.println("recall: NA");

        double Fsocre = 0;
        if (precise + recall != 0) {
            Fsocre = 2 * (precise * recall) / (precise + recall);
            System.out.println("Fsocre: " + Double.valueOf(Fsocre).toString());
        } else
            System.out.println("Fsocre: NA");
        return 0;
    }

    public static void main(String[] args) {
        SVMTrain1 s = new SVMTrain1();
        s.svmTrain(true);
        s.svmPredict();
    }

}
