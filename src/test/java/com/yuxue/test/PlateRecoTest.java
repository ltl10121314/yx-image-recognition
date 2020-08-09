package com.yuxue.test;

import org.bytedeco.javacpp.opencv_imgproc;

import java.io.File;
import java.util.Vector;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_highgui;
import org.bytedeco.javacpp.opencv_imgcodecs;
import org.junit.Test;

import com.yuxue.easypr.core.CharsIdentify;
import com.yuxue.easypr.core.CharsRecognise;
import com.yuxue.easypr.core.CoreFunc;
import com.yuxue.easypr.core.PlateDetect;
import com.yuxue.easypr.core.PlateLocate;
import com.yuxue.enumtype.Direction;
import com.yuxue.enumtype.PlateColor;

/**
 * EasyPr车牌识别测试类 
 * - https://gitee.com/easypr/EasyPR 很老的项目了 
 * - EasyPR是一个开源的中文车牌识别系统 
 * - 基于openCV开源库 
 * - 能够识别中文；例如车牌为苏EUK722的图片 
 * - 识别率较高；图片清晰情况下，车牌检测与字符识别可以达到80%以上的精度
 * @author yuxue
 * @date 2020-04-19 21:20
 */
public class PlateRecoTest {

    /**
     * 图片车牌识别
     * 注意，图片res目录需要放到跟src同级的目录下
     */
    @Test
    public void testPlateRecognise() {
        String imgPath = "res/image/test_image/plate_recognize.jpg";

        Mat src = opencv_imgcodecs.imread(imgPath);

        // 车牌检测对象
        PlateDetect plateDetect = new PlateDetect();
        plateDetect.setPDLifemode(true);
        Vector<Mat> matVector = new Vector<Mat>();
        if (0 == plateDetect.plateDetect(src, matVector)) { // 检测到车牌

            // 字符识别对象
            CharsRecognise cr = new CharsRecognise();
            for (int i = 0; i < matVector.size(); ++i) { // 遍历检测返回的Mat集合，进行识别
                Mat img = matVector.get(i);

                String palte = cr.charsRecognise(img, "tem/"); // 字符识别
                PlateColor color = CoreFunc.getPlateType(img, true);

                System.err.println("识别到的车牌: " + palte + "_" + color.desc);
                // 识别的车牌，保存图片文件 //需要先创建文件夹
                String str = "d:/PlateDetect/" + palte + "_"+ color + "_" + System.currentTimeMillis() +".png";
                String str1 = "d:/PlateDetect/" + i + ".png";

                // 此方法生成的文件，中文名称都是乱码，试了各种编解码均无效，OpenCV自身的编解码问题。
                opencv_imgcodecs.imwrite(str1, img);

                // 重命名文件，让生成的文件包含中文
                File targetFile = new File(str);
                File file = new File(str1);
                file.renameTo(targetFile);
            }
        }
    }


    /**
     * 车牌检测 = 车牌定位 + 车牌判断
     * 针对定位操作返回的Mat集合，进行判断
     * 识别出集合里面哪些块是车牌
     */
    @Test
    public void testPlateDetect() {
        String imgPath = "res/image/test_image/test.jpg";

        Mat src = opencv_imgcodecs.imread(imgPath);
        PlateDetect plateDetect = new PlateDetect();
        plateDetect.setPDLifemode(true);
        Vector<Mat> matVector = new Vector<Mat>();
        if (0 == plateDetect.plateDetect(src, matVector)) {
            for (int i = 0; i < matVector.size(); ++i) {
                Mat img = matVector.get(i);
                // 弹窗显示
                opencv_highgui.imshow("Plate Detected", img);

                String str = "d:/test/" + i + ".png";
                opencv_imgcodecs.imwrite(str, img);
            }
        }
    }


    /**
     * 车牌定位
     * 处理原始图像，将可能为车牌的块识别出来
     * 返回结果里面，可能包含很多块，部分是车牌，部分不是车牌
     */
    @Test
    public void testPlateLocate() {
        String imgPath = "res/image/test_image/test.jpg";

        Mat src = opencv_imgcodecs.imread(imgPath);

        PlateLocate plate = new PlateLocate();
        plate.setDebug(true);
        plate.setLifemode(true);

        Vector<Mat> resultVec = plate.plateLocate(src);

        int num = resultVec.size();
        for (int j = 0; j < num; j++) {
            Mat img = resultVec.get(j);
            // showImage("Plate Located " + j, resultVec.get(j));

            String str = "d:/test/" + j + ".png";
            opencv_imgcodecs.imwrite(str, img);
        }
        return;
    }


    /**
     * 文字识别
     */
    @Test
    public void testCharsRecognise() {
        String imgPath = "res/image/test_image/chars_recognise_huAGH092.jpg";

        Mat src = opencv_imgcodecs.imread(imgPath);
        CharsRecognise cr = new CharsRecognise();
        cr.setCRDebug(true);
        String result = cr.charsRecognise(src, "tem/");
        System.out.println("Chars Recognised: " + result);
    }


    /**
     * 车牌颜色检测
     */
    @Test
    public void testColorDetect() {
        String imgPath = "res/image/test_image/core_func_yellow.jpg";
        Mat src = opencv_imgcodecs.imread(imgPath);
        PlateColor color = CoreFunc.getPlateType(src, true);
        System.out.println("Color Deteted: " + color);
    }


    /**
     * 投影直方图
     */
    @Test
    public void testProjectedHistogram() {
        String imgPath = "res/image/test_image/chars_identify_E.jpg";
        Mat src = opencv_imgcodecs.imread(imgPath);
        CoreFunc.projectedHistogram(src, Direction.HORIZONTAL);
    }


    /**
     * 字符识别
     */
    @Test
    public void testCharsIdentify() {
        String imgPath = "res/image/test_image/chars_identify_E.jpg";

        Mat src = opencv_imgcodecs.imread(imgPath);
        CharsIdentify charsIdentify = new CharsIdentify();
        String result = charsIdentify.charsIdentify(src, false, true);
        System.out.println(result);
    }




    /**
     * 测试检测绿牌颜色
     */
    @Test
    public void testGreenColorReco() {
        String imgPath = "res/image/test_image/debug_resize_2.jpg";
        Mat src = opencv_imgcodecs.imread(imgPath);

        // 判断绿色车牌
        Mat src_hsv = new Mat();
        opencv_imgproc.cvtColor(src, src_hsv, opencv_imgproc.CV_BGR2HSV);
        src_hsv = CoreFunc.colorMatch(src, PlateColor.GREEN, true);
        System.err.println(CoreFunc.plateColorJudge(src, PlateColor.GREEN, true));
        String str = "d:/PlateDetect/src_hsv.png";
        opencv_imgcodecs.imwrite(str, src_hsv);
    }

    @Test
    public void testGreenPlate() {
        String imgPath = "res/image/test_image/debug_resize_2.jpg";
        Mat src = opencv_imgcodecs.imread(imgPath);

        // 车牌检测对象
        PlateDetect plateDetect = new PlateDetect();
        plateDetect.setPDLifemode(true);
        plateDetect.setDebug(false, ""); // 将过程的图块保存到盘符

        Vector<Mat> matVector = new Vector<Mat>();

        System.err.println(plateDetect.plateDetect(src, matVector));
        System.err.println(matVector.size());

        for (int i = 0; i < matVector.size(); ++i) { // 遍历车牌图块Mat，进行识别
            Mat img = matVector.get(i);

            String str = "d:/PlateDetect/temp/result_.png";
            opencv_imgcodecs.imwrite(str, img);

        }

    }




}
