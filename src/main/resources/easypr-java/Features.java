package com.yuxue.easypr.core;

import static com.yuxue.easypr.core.CoreFunc.features;
import static org.bytedeco.javacpp.opencv_core.merge;
import static org.bytedeco.javacpp.opencv_core.split;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_imgproc;

/**
 * 
 * @author yuxue
 * @date 2020-05-05 08:26
 */
public class Features implements SVMCallback {

    /***
     * EasyPR的getFeatures回调函数
     * 本函数是生成直方图均衡特征的回调函数
     * @param image
     * @return
     */
    @Override
    public Mat getHisteqFeatures(final Mat image) {
        return histeq(image);
    }
    
    private Mat histeq(Mat in) {
        Mat out = new Mat(in.size(), in.type());
        if (in.channels() == 3) {
            Mat hsv = new Mat();
            MatVector hsvSplit = new MatVector();
            opencv_imgproc.cvtColor(in, hsv, opencv_imgproc.CV_BGR2HSV);
            split(hsv, hsvSplit);
            opencv_imgproc.equalizeHist(hsvSplit.get(2), hsvSplit.get(2));
            merge(hsvSplit, hsv);
            opencv_imgproc.cvtColor(hsv, out, opencv_imgproc.CV_HSV2BGR);
            hsv = null;
            hsvSplit = null;
            System.gc();
        } else if (in.channels() == 1) {
            opencv_imgproc.equalizeHist(in, out);
        }
        return out;
    }
    
    /**
     * EasyPR的getFeatures回调函数
     * 本函数是获取垂直和水平的直方图图值
     * @param image
     * @return
     */
    @Override
    public Mat getHistogramFeatures(Mat image) {
        Mat grayImage = new Mat();
        opencv_imgproc.cvtColor(image, grayImage, opencv_imgproc.CV_RGB2GRAY);

        Mat img_threshold = new Mat();
        opencv_imgproc.threshold(grayImage, img_threshold, 0, 255, opencv_imgproc.CV_THRESH_OTSU + opencv_imgproc.CV_THRESH_BINARY);

        return features(img_threshold, 0);
    }

    /**
     * 本函数是获取SITF特征子的回调函数
     * 
     * @param image  
     * @return
     */
    @Override
    public Mat getSIFTFeatures(final Mat image) {
        // TODO: 待完善
        return null;
    }

    /**
     * 本函数是获取HOG特征子的回调函数
     * 
     * @param image
     * @return
     */
    @Override
    public Mat getHOGFeatures(final Mat image) {
        // TODO: 待完善
        return null;
    }

    
}
