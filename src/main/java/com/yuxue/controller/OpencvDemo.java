package com.yuxue.controller;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;


/**
 * opencv 官方demo
 * 用于测试opencv环境是否正常
 * windows下环境配置：
 * 1、官网下载对应版本的openvp：https://opencv.org/releases/page/2/  当前使用4.0.1版本
 * 2、双击exe文件安装，将 安装目录下\build\java\x64\opencv_java401.dll 拷贝到\build\x64\vc14\bin\目录下
 * 3、eclipse添加User Libraries
 * 4、项目右键build path，添加步骤三新增的lib
 * 
 * 官方demo，需要本地安装opencv，除该demo之前，均不需要安装，使用maven依赖即可，
 * 二者之间具体有什么差别，暂时还没有时间去深入研究
 * @author yuxue
 * @date 2020-04-22 14:04
 */
public class OpencvDemo {
    
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        System.out.println("Welcome to OpenCV " + Core.VERSION);
        
        
        Mat m1  = Mat.eye(3, 3, CvType.CV_8UC1);
        System.out.println("m = " + m1.dump());
        System.err.println("==================");
        
        
        Mat m = new Mat(5, 10, CvType.CV_8UC1, new Scalar(0));
        System.out.println("OpenCV Mat: " + m);
        Mat mr1 = m.row(1);
        mr1.setTo(new Scalar(1));
        Mat mc5 = m.col(5);
        mc5.setTo(new Scalar(5));
        System.out.println("OpenCV Mat data:\n" + m.dump());
        
        
    }

}
