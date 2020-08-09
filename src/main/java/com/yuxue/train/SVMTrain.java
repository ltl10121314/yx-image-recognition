package com.yuxue.train;

import java.io.File;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.TermCriteria;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.Ml;
import org.opencv.ml.SVM;
import org.opencv.ml.TrainData;

import com.google.common.collect.Lists;
import com.yuxue.constant.Constant;
import com.yuxue.enumtype.Direction;
import com.yuxue.util.FileUtil;

/**
 * 基于org.opencv官方包实现的训练
 * 
 * windows下环境配置：
 * 1、官网下载对应版本的openvp：https://opencv.org/releases/page/2/  当前使用4.0.1版本
 * 2、双击exe文件安装，将 安装目录下\build\java\x64\opencv_java401.dll 拷贝到\build\x64\vc14\bin\目录下
 * 3、eclipse添加User Libraries
 * 4、项目右键build path，添加步骤三新增的lib
 * 
 * 图片识别车牌训练
 * 训练出来的库文件，用于判断切图是否包含车牌
 * 
 * 训练的svm.xml应用：
 * 1、替换res/model/svm.xml文件
 * 2、修改com.yuxue.easypr.core.PlateJudge.plateJudge(Mat) 方法
 *      将样本处理方法切换一下，即将对应被注释掉的模块代码取消注释
 * @author yuxue
 * @date 2020-05-13 10:10
 */
public class SVMTrain {

    // 默认的训练操作的根目录
    private static final String DEFAULT_PATH = "D:/PlateDetect/train/plate_detect_svm/";

    // 训练模型文件保存位置
    private static final String MODEL_PATH = DEFAULT_PATH + "svm2.xml";

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] arg) {
        // 训练， 生成svm.xml库文件
        train(); 

        // 识别，判断样本文件是否是车牌
        pridect(); 
    }

    public static void train() {

        // 正样本  // 136 × 36 像素  训练的源图像文件要相同大小
        List<File> imgList0 = FileUtil.listFile(new File(DEFAULT_PATH + "/learn/HasPlate"), Constant.DEFAULT_TYPE, false);

        // 负样本   // 136 × 36 像素 训练的源图像文件要相同大小
        List<File> imgList1 = FileUtil.listFile(new File(DEFAULT_PATH + "/learn/NoPlate"), Constant.DEFAULT_TYPE, false);

        // 标记：正样本用 0 表示，负样本用 1 表示。
        int labels[] = createLabelArray(imgList0.size(), imgList1.size());
        int sample_num = labels.length; // 图片数量

        // 用于存放所有样本的矩阵
        Mat trainingDataMat = null;

        // 存放标记的Mat,每个图片都要给一个标记
        Mat labelsMat = new Mat(sample_num, 1, CvType.CV_32SC1);
        labelsMat.put(0, 0, labels);

        for (int i = 0; i < sample_num; i++) {  // 遍历所有的正负样本，处理样本用于生成训练的库文件
            String path = "";
            if(i < imgList0.size()) {
                path = imgList0.get(i).getAbsolutePath();
            } else {
                path = imgList1.get(i - imgList0.size()).getAbsolutePath(); 
            }

            Mat inMat = Imgcodecs.imread(path);   // 读取样本文件
            Mat dst = getFeature(inMat);    // 获取样本文件的特征
            
            // 创建一个行数为sample_num, 列数为 rows*cols 的矩阵; 用于存放样本
            if (trainingDataMat == null) {
                trainingDataMat = new Mat(sample_num, dst.rows() * dst.cols(), CvType.CV_32F);
            }
            
            // 将样本矩阵转换成只有一行的矩阵，保存为float数组
            float[] arr = new float[dst.rows() * dst.cols()];
            int l = 0;
            for (int j = 0; j < dst.rows(); j++) { // 遍历行
                for (int k = 0; k < dst.cols(); k++) { // 遍历列
                    double[] a = dst.get(j, k);
                    arr[l] = (float) a[0];
                    l++;
                }
            }

            trainingDataMat.put(i, 0, arr); // 多张图的特征合并到一个矩阵
        }

        // Imgcodecs.imwrite(DEFAULT_PATH + "trainingDataMat.jpg", trainingDataMat);

        // 配置SVM训练器参数
        TermCriteria criteria = new TermCriteria(TermCriteria.EPS + TermCriteria.MAX_ITER, 20000, 0.0001);
        SVM svm = SVM.create();
        svm.setTermCriteria(criteria); // 指定
        svm.setKernel(SVM.RBF); // 使用预先定义的内核初始化
        svm.setType(SVM.C_SVC); // SVM的类型,默认是：SVM.C_SVC
        svm.setGamma(0.1); // 核函数的参数
        svm.setNu(0.1); // SVM优化问题参数
        svm.setC(1); // SVM优化问题的参数C
        svm.setP(0.1);
        svm.setDegree(0.1);
        svm.setCoef0(0.1);

        TrainData td = TrainData.create(trainingDataMat, Ml.ROW_SAMPLE, labelsMat);// 类封装的训练数据
        boolean success = svm.train(td.getSamples(), Ml.ROW_SAMPLE, td.getResponses());// 训练统计模型
        System.out.println("svm training result: " + success);
        svm.save(MODEL_PATH);// 保存模型
    }


    public static void pridect() {
        // 加载训练得到的 xml 模型文件
        SVM svm = SVM.load(MODEL_PATH); 

        // 136 × 36 像素   需要跟训练的源图像文件保持相同大小
        doPridect(svm, DEFAULT_PATH + "test/A01_NMV802_0.jpg");
        doPridect(svm, DEFAULT_PATH + "test/debug_resize_1.jpg");
        doPridect(svm, DEFAULT_PATH + "test/debug_resize_2.jpg");
        doPridect(svm, DEFAULT_PATH + "test/debug_resize_3.jpg");
        doPridect(svm, DEFAULT_PATH + "test/S22_KG2187_3.jpg");
        doPridect(svm, DEFAULT_PATH + "test/S22_KG2187_5.jpg");
    }

    public static void doPridect(SVM svm, String imgPath) {
        Mat src = Imgcodecs.imread(imgPath);
        Mat dst = getFeature(src);
        // 如果训练时使用这个标识，那么符合的图像会返回9.0
        float flag = svm.predict(dst);

        if (flag == 0) {
            System.err.println(imgPath + "： 目标符合");
        }
        if (flag == 1) {
            System.out.println(imgPath + "： 目标不符合");
        }
    }

    public static int[] createLabelArray(Integer i1, Integer i2) {
        int labels[] = new int[i1 + i2];

        for (int i = 0; i < labels.length; i++) {
            if(i < i1) {
                labels[i] = 0;
            } else {
                labels[i] = 1;
            }
        }
        return labels;
    }


    public static Mat getFeature(Mat inMat) {
        
        Mat histogram = getHistogramFeatures(inMat);
        Mat color = getColorFeatures(inMat);
        
        List<Mat> list = Lists.newArrayList();
        list.add(histogram);
        list.add(color);

        Mat dst = new Mat();
        // hconcat 水平拼接 // vconcat 垂直拼接
        Core.hconcat(list, dst);
        return dst;
    }


    public static Mat getHistogramFeatures(Mat src) {
        Mat img_grey = new Mat();
        Imgproc.cvtColor(src, img_grey, Imgproc.COLOR_BGR2GRAY);

        Mat img_threshold = new Mat();
        Imgproc.threshold(img_grey, img_threshold, 0, 255, Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY);

        // Histogram features
        float[] vhist = projectedHistogram(img_threshold, Direction.VERTICAL);
        float[] hhist = projectedHistogram(img_threshold, Direction.HORIZONTAL);

        // Last 10 is the number of moments components
        int numCols = vhist.length + hhist.length;

        Mat features = Mat.zeros(1, numCols, CvType.CV_32F);
        int j = 0;
        for (int i = 0; i < vhist.length; i++) {
            features.put(0, j, vhist[i]);
            j++;
        }
        for (int i = 0; i < hhist.length; i++) {
            features.put(0, j, hhist[i]);
            j++;
        }
        return features;
    }

    public static float[] projectedHistogram(Mat inMat, Direction direction){
        Mat img = new Mat();
        inMat.copyTo(img);
        int sz = img.rows();
        if(Direction.VERTICAL.equals(direction)) {
            sz = img.cols();
        }
        // 统计这一行或一列中，非零元素的个数，并保存到nonZeroMat中
        float[] nonZeroMat = new float[sz];
        Core.extractChannel(img, img, 0);   // 提取0通道
        for (int j = 0; j < sz; j++) {
            Mat data = Direction.HORIZONTAL.equals(direction) ? img.row(j) : img.col(j);
            int count = Core.countNonZero(data);
            nonZeroMat[j] = count;
        }
        // Normalize histogram
        float max = 1F;
        for (int j = 0; j < nonZeroMat.length; j++) {
            max = Math.max(max, nonZeroMat[j]);
        }
        for (int j = 0; j < nonZeroMat.length; j++) {
            nonZeroMat[j] /= max;
        }
        return nonZeroMat;
    }
    

    public static Mat getColorFeatures(Mat src) {
        Mat src_hsv = new Mat();
        Imgproc.cvtColor(src, src_hsv, Imgproc.COLOR_BGR2GRAY);

        int sz = 180;
        int[] h = new int[180];

        for (int i = 0; i < src_hsv.rows(); i++) {
            for (int j = 0; j < src_hsv.cols(); j++) {
                int H = (int) src_hsv.get(i, j)[0];// 0-180
                if (H > sz - 1) {
                    H = sz - 1;
                }
                if (H < 0) {
                    H = 0;
                } 
                h[H]++;
            }
        }
        // 创建黑色的图
        Mat features = Mat.zeros(1, sz, CvType.CV_32F);
        
        for (int j = 0; j < sz; j++) {
            features.put(0, j, (float)h[j]);
        }

        MinMaxLocResult m = Core.minMaxLoc(features);
        double max = m.maxVal;

        if (max > 0) {
            features.convertTo(features, -1, 1.0f / max, 0);
        }
        return features;
    }


}
