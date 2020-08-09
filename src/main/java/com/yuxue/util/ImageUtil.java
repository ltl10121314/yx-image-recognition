package com.yuxue.util;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yuxue.constant.Constant;
import com.yuxue.enumtype.PlateColor;


/**
 * 车牌图片处理工具类
 * 将原图，经过算法处理，得到车牌的图块
 * @author yuxue
 * @date 2020-05-18 12:07
 */
public class ImageUtil {

    private static String DEFAULT_BASE_TEST_PATH = "D:/PlateDetect/temp/";

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    // 车牌定位处理步骤，该map用于表示步骤图片的顺序
    private static Map<String, Integer> debugMap = Maps.newLinkedHashMap();
    static {
        debugMap.put("yuantu", 0); // 原图
        debugMap.put("gaussianBlur", 0); // 高斯模糊
        debugMap.put("gray", 0);  // 图像灰度化
        debugMap.put("sobel", 0); // Sobel 运算，得到图像的一阶水平方向导数
        debugMap.put("threshold", 0); //图像二值化
        debugMap.put("morphology", 0); // 图像闭操作
        debugMap.put("clearInnerHole", 0); // 降噪
        debugMap.put("clearSmallConnArea", 0); // 降噪
        debugMap.put("clearAngleConn", 0); // 降噪
        debugMap.put("clearHole", 0); // 降噪
        debugMap.put("contours", 0); // 提取外部轮廓
        debugMap.put("screenblock", 0); // 外部轮廓筛选
        debugMap.put("crop", 0); // 切图
        debugMap.put("resize", 0); // 切图resize

        // 设置index， 用于debug生成文件时候按名称排序
        Integer index = 100;
        for (Entry<String, Integer> entry : debugMap.entrySet()) {
            entry.setValue(index);
            index ++;
        }
    }

    public static void main(String[] args) {
        Instant start = Instant.now();
        String tempPath = DEFAULT_BASE_TEST_PATH + "test/";
        String filename = tempPath + "/100_yuantu.jpg";
        filename = tempPath + "/100_yuantu1.jpg";
        // filename = tempPath + "/109_crop_0.png";

        // 读取原图
        Mat src = Imgcodecs.imread(filename);

        Boolean debug = true;

        // 高斯模糊
        Mat gsMat = ImageUtil.gaussianBlur(src, debug, tempPath);

        // 灰度图
        Mat gray = ImageUtil.gray(gsMat, debug, tempPath);

        Mat sobel = ImageUtil.sobel(gray, debug, tempPath);

        Mat threshold = ImageUtil.threshold(sobel, debug, tempPath);

        // Mat scharr = ImageUtil.scharr(gray, debug, tempPath);
        // Mat threshold = ImageUtil.threshold(scharr, debug, tempPath);

        Mat morphology = ImageUtil.morphology(threshold, debug, tempPath);

        List<MatOfPoint> contours = ImageUtil.contours(src, morphology, debug, tempPath);

        Vector<Mat> rects = ImageUtil.screenBlock(src, contours, debug, tempPath);

        PlateUtil.loadSvmModel("D:/PlateDetect/train/plate_detect_svm/svm2.xml");
        PlateUtil.loadAnnModel("D:/PlateDetect/train/chars_recognise_ann/ann.xml");

        Vector<Mat> dst = new Vector<Mat>();
        PlateUtil.hasPlate(rects, dst, debug, tempPath);

        System.err.println("识别到的车牌数量：" + dst.size());
        dst.stream().forEach(inMat -> {
            PlateColor color = PlateUtil.getPlateColor(inMat, true, debug, tempPath);
            System.err.println(color.desc);

            Vector<Mat> charMat = new Vector<Mat>();
            PlateUtil.charsSegment(inMat, color, charMat, debug, tempPath);


        });

        /*String filename = tempPath + "/hsvMat_1590994270425.jpg";
        Mat src = Imgcodecs.imread(filename);
        Vector<Mat> charMat = new Vector<Mat>();
        PlateUtil.charsSegment(src, PlateColor.BLUE, charMat, true, tempPath);*/

        Instant end = Instant.now();
        System.err.println("总耗时：" + Duration.between(start, end).toMillis());

        // ImageUtil.rgb2Hsv(src, debug, tempPath);
        // ImageUtil.getHSVValue(src, debug, tempPath);
    }



    /**
     * 高斯模糊
     * @param inMat
     * @param debug
     * @return
     */
    public static final int DEFAULT_GAUSSIANBLUR_SIZE = 5;
    public static Mat gaussianBlur(Mat inMat, Boolean debug, String tempPath) {
        Mat dst = new Mat();
        Imgproc.GaussianBlur(inMat, dst, new Size(DEFAULT_GAUSSIANBLUR_SIZE, DEFAULT_GAUSSIANBLUR_SIZE), 0, 0, Core.BORDER_DEFAULT);
        if (debug) {
            Imgcodecs.imwrite(tempPath + debugMap.get("gaussianBlur") + "_gaussianBlur.jpg", dst);
        }
        return dst;
    }


    /**
     * 将图像进行灰度化
     * @param inMat
     * @param debug
     * @param tempPath
     * @return
     */
    public static Mat gray(Mat inMat, Boolean debug, String tempPath) {
        Mat dst = new Mat();
        Imgproc.cvtColor(inMat, dst, Imgproc.COLOR_BGR2GRAY);
        if (debug) {
            Imgcodecs.imwrite(tempPath + debugMap.get("gray") + "_gray.jpg", dst);
        }
        inMat.release();
        return dst;
    }


    /**
     * 对图像进行Sobel 运算，得到图像的一阶水平方向导数
     * @param inMat 灰度图
     * @param debug
     * @param tempPath
     * @return
     */
    public static final int SOBEL_SCALE = 1;
    public static final int SOBEL_DELTA = 0;
    public static final int SOBEL_X_WEIGHT = 1;
    public static final int SOBEL_Y_WEIGHT = 0;
    public static Mat sobel(Mat inMat, Boolean debug, String tempPath) {
        Mat dst = new Mat();
        Mat grad_x = new Mat();
        Mat grad_y = new Mat();
        Mat abs_grad_x = new Mat();
        Mat abs_grad_y = new Mat();

        // Sobel滤波 计算水平方向灰度梯度的绝对值
        Imgproc.Sobel(inMat, grad_x, CvType.CV_16S, 1, 0, 3, SOBEL_SCALE, SOBEL_DELTA, Core.BORDER_DEFAULT); 
        Core.convertScaleAbs(grad_x, abs_grad_x);   // 增强对比度

        Imgproc.Sobel(inMat, grad_y, CvType.CV_16S, 0, 1, 3, SOBEL_SCALE, SOBEL_DELTA, Core.BORDER_DEFAULT);
        Core.convertScaleAbs(grad_y, abs_grad_y);
        grad_x.release();
        grad_y.release();

        // 计算结果梯度
        Core.addWeighted(abs_grad_x, SOBEL_X_WEIGHT, abs_grad_y, SOBEL_Y_WEIGHT, 0, dst);
        abs_grad_x.release();
        abs_grad_y.release();

        if (debug) {
            Imgcodecs.imwrite(tempPath + debugMap.get("sobel") + "_sobel.jpg", dst);
        }
        return dst;
    }


    /**
     * 对图像进行scharr 运算，得到图像的一阶水平方向导数
     * @param inMat
     * @param debug
     * @param tempPath
     * @return
     */
    public static Mat scharr(Mat inMat, Boolean debug, String tempPath) {

        Mat dst = new Mat();

        Mat grad_x = new Mat();
        Mat grad_y = new Mat();
        Mat abs_grad_x = new Mat();
        Mat abs_grad_y = new Mat();

        //注意求梯度的时候我们使用的是Scharr算法，sofia算法容易收到图像细节的干扰
        //所谓梯度运算就是对图像中的像素点进行就导数运算，从而得到相邻两个像素点的差异值 by:Tantuo
        Imgproc.Scharr(inMat, grad_x, CvType.CV_32F, 1, 0);
        Imgproc.Scharr(inMat, grad_y, CvType.CV_32F, 0, 1);
        //openCV中有32位浮点数的CvType用于保存可能是负值的像素数据值
        Core.convertScaleAbs(grad_x, abs_grad_x);
        Core.convertScaleAbs(grad_y, abs_grad_y);
        //openCV中使用release()释放Mat类图像，使用recycle()释放BitMap类图像
        grad_x.release();
        grad_y.release();

        Core.addWeighted(abs_grad_x, 0.5, abs_grad_y, 0.5, 0, dst);
        abs_grad_x.release();
        abs_grad_y.release();
        if (debug) {
            Imgcodecs.imwrite(tempPath + debugMap.get("sobel") + "_sobel.jpg", dst);
        }
        return dst;
    }


    /**
     * 对图像进行二值化。将灰度图像（每个像素点有256个取值可能， 0代表黑色，255代表白色）  
     * 转化为二值图像（每个像素点仅有1和0两个取值可能）
     * @param inMat
     * @param debug
     * @param tempPath
     * @return
     */
    public static Mat threshold(Mat inMat, Boolean debug, String tempPath) {
        Mat dst = new Mat();
        Imgproc.threshold(inMat, dst, 100, 255, Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY);
        if (debug) {
            Imgcodecs.imwrite(tempPath + debugMap.get("threshold") + "_threshold.jpg", dst);
        }
        inMat.release();
        return dst;
    }


    /**
     * 使用闭操作。对图像进行闭操作以后，可以看到车牌区域被连接成一个矩形装的区域
     * @param inMat
     * @param debug
     * @param tempPath
     * @return
     */
    // public static final int DEFAULT_MORPH_SIZE_WIDTH = 15;
    // public static final int DEFAULT_MORPH_SIZE_HEIGHT = 3;
    public static final int DEFAULT_MORPH_SIZE_WIDTH = 9;
    public static final int DEFAULT_MORPH_SIZE_HEIGHT = 3;
    public static Mat morphology(Mat inMat, Boolean debug, String tempPath) {
        Mat dst = new Mat(inMat.size(), CvType.CV_8UC1);
        Size size = new Size(DEFAULT_MORPH_SIZE_WIDTH, DEFAULT_MORPH_SIZE_HEIGHT);
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, size);
        Imgproc.morphologyEx(inMat, dst, Imgproc.MORPH_CLOSE, element);
        if (debug) {
            Imgcodecs.imwrite(tempPath + debugMap.get("morphology") + "_morphology0.jpg", dst);
        }

        // 填补内部孔洞，为了去除小连通区域的时候，降低影响
        Mat a = clearInnerHole(dst, 8, 16, debug, tempPath);

        // 去除小连通区域
        Mat b = clearSmallConnArea(a, 1, 10, debug, tempPath);

        // 按斜边去除
        // Mat e = clearAngleConn(b, 5, debug, tempPath);

        // 填补边缘孔洞
        // Mat d = clearHole(a, 4, 2, debug, tempPath);

        return b;
    }


    /**
     * Find 轮廓 of possibles plates 求轮廓。求出图中所有的轮廓。
     * 这个算法会把全图的轮廓都计算出来，因此要进行筛选。
     * @param src 原图
     * @param inMat morphology Mat
     * @param debug
     * @param tempPath
     * @return
     */
    public static List<MatOfPoint> contours(Mat src, Mat inMat, Boolean debug, String tempPath) {
        List<MatOfPoint> contours = Lists.newArrayList();
        Mat hierarchy = new Mat();
        // 提取外部轮廓
        // CV_RETR_EXTERNAL只检测最外围轮廓，
        // CV_RETR_LIST   检测所有的轮廓
        // CV_CHAIN_APPROX_NONE 保存物体边界上所有连续的轮廓点到contours向量内
        Imgproc.findContours(inMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

        if (debug) {
            Mat result = new Mat();
            src.copyTo(result); //  复制一张图，不在原图上进行操作，防止后续需要使用原图
            // 将轮廓描绘到原图
            Imgproc.drawContours(result, contours, -1, new Scalar(0, 0, 255, 255));
            // 输出带轮廓的原图
            Imgcodecs.imwrite(tempPath + debugMap.get("contours") + "_contours.jpg", result);
        }
        return contours;
    }


    /**
     * 根据轮廓， 筛选出可能是车牌的图块
     * @param src
     * @param matVector
     * @param debug
     * @param tempPath
     * @return
     */
    public static final int DEFAULT_ANGLE = 30; // 角度判断所用常量
    public static final int TYPE = CvType.CV_8UC3;
    public static Vector<Mat> screenBlock(Mat src, List<MatOfPoint> contours, Boolean debug, String tempPath){
        Vector<Mat> dst = new Vector<Mat>();
        List<MatOfPoint> mv = Lists.newArrayList(); // 用于在原图上描绘筛选后的结果
        for (int i = 0, j = 0; i < contours.size(); i++) {
            MatOfPoint m1 = contours.get(i);
            MatOfPoint2f m2 = new MatOfPoint2f();
            m1.convertTo(m2, CvType.CV_32F);
            // RotatedRect 该类表示平面上的旋转矩形，有三个属性： 矩形中心点(质心); 边长(长和宽); 旋转角度
            // boundingRect()得到包覆此轮廓的最小正矩形， minAreaRect()得到包覆轮廓的最小斜矩形
            RotatedRect mr = Imgproc.minAreaRect(m2);

            double angle = Math.abs(mr.angle);

            if (checkPlateSize(mr) && angle <= DEFAULT_ANGLE) {  // 判断尺寸及旋转角度 ±30°，排除不合法的图块
                mv.add(contours.get(i));
                Size rect_size = new Size((int) mr.size.width, (int) mr.size.height);
                if (mr.size.width / mr.size.height < 1) {   // 宽度小于高度
                    angle = 90 + angle; // 旋转90°
                    rect_size = new Size(rect_size.height, rect_size.width);
                }

                // 旋转角度，根据需要是否进行角度旋转
                /*Mat img_rotated = new Mat();
                Mat rotmat = Imgproc.getRotationMatrix2D(mr.center, angle, 1); // 旋转
                Imgproc.warpAffine(src, img_rotated, rotmat, src.size()); // 仿射变换  考虑是否需要进行投影变换？
                 */
                
                // 切图
                Mat img_crop = new Mat();
                Imgproc.getRectSubPix(src, rect_size, mr.center, img_crop);
                if (debug) {
                    Imgcodecs.imwrite(tempPath + debugMap.get("crop") + "_crop_" + j + ".png", img_crop);
                }
                
                // 处理切图，调整为指定大小
                Mat resized = new Mat(Constant.DEFAULT_HEIGHT, Constant.DEFAULT_WIDTH, TYPE);
                Imgproc.resize(img_crop, resized, resized.size(), 0, 0, Imgproc.INTER_CUBIC);
                if (debug) {
                    Imgcodecs.imwrite(tempPath + debugMap.get("resize") + "_resize_" + j + ".png", resized);
                    j++;
                }
                dst.add(resized);
            }
        }
        if (debug) {
            Mat result = new Mat();
            src.copyTo(result); //  复制一张图，不在原图上进行操作，防止后续需要使用原图
            // 将轮廓描绘到原图
            Imgproc.drawContours(result, mv, -1, new Scalar(0, 0, 255, 255));
            // 输出带轮廓的原图
            Imgcodecs.imwrite(tempPath + debugMap.get("screenblock") + "_screenblock.jpg", result);
        }
        return  dst;
    }

    /**
     * 对minAreaRect获得的最小外接矩形
     * 判断面积以及宽高比是否在制定的范围内
     * 黄牌、蓝牌、绿牌
     * 国内车牌大小: 440mm*140mm，宽高比 3.142857
     * @param mr
     * @return
     */
    final static float DEFAULT_ERROR = 0.7f;    // 宽高比允许70%误差
    final static float DEFAULT_ASPECT = 3.142857f;
    public static final int DEFAULT_VERIFY_MIN = 1;
    public static final int DEFAULT_VERIFY_MAX = 30;
    private static boolean checkPlateSize(RotatedRect mr) {

        // 切图面积取值范围
        int min = 44 * 14 * DEFAULT_VERIFY_MIN;
        int max = 44 * 14 * DEFAULT_VERIFY_MAX;

        // 切图横纵比取值范围；关键在于纵横比例
        float rmin = DEFAULT_ASPECT - DEFAULT_ASPECT * DEFAULT_ERROR;
        float rmax = DEFAULT_ASPECT + DEFAULT_ASPECT * DEFAULT_ERROR;

        // 切图计算面积
        int area = (int) (mr.size.height * mr.size.width);
        // 切图宽高比
        double r = mr.size.width / mr.size.height;
        /*if (r < 1) {  // 注释掉，不处理width 小于height的图片
            r = mr.size.height / mr.size.width;
        }*/
        return min <= area && area <= max && rmin <= r && r <= rmax;
    }


    /**
     * rgb图像转换为hsv图像
     * @param inMat
     * @param debug
     * @param tempPath
     * @return
     */
    public static Mat rgb2Hsv(Mat inMat, Boolean debug, String tempPath) {
        // 转到HSV空间进行处理
        Mat dst = new Mat();
        Imgproc.cvtColor(inMat, dst, Imgproc.COLOR_BGR2HSV);
        List<Mat> hsvSplit = Lists.newArrayList();
        Core.split(dst, hsvSplit);
        // 直方图均衡化是一种常见的增强图像对比度的方法，使用该方法可以增强局部图像的对比度，尤其在数据较为相似的图像中作用更加明显
        Imgproc.equalizeHist(hsvSplit.get(2), hsvSplit.get(2));
        Core.merge(hsvSplit, dst);

        if (debug) {
            // Imgcodecs.imwrite(tempPath + "hsvMat_"+System.currentTimeMillis()+".jpg", dst);
        }
        return dst;
    }


    /**
     * 获取HSV中各个颜色所对应的H的范围
     * HSV是一种比较直观的颜色模型，所以在许多图像编辑工具中应用比较广泛，这个模型中颜色的参数分别是：色调（H, Hue），饱和度（S,Saturation），明度（V, Value）
     * 1.PS软件时，H取值范围是0-360，S取值范围是（0%-100%），V取值范围是（0%-100%）。         
     * 2.利用openCV中cvSplit函数的在选择图像IPL_DEPTH_32F类型时，H取值范围是0-360，S取值范围是0-1（0%-100%），V取值范围是0-1（0%-100%）。
     * 3.利用openCV中cvSplit函数的在选择图像IPL_DEPTH_8UC类型时，H取值范围是0-180，S取值范围是0-255，V取值范围是0-255
     * @param inMat
     * @param debug
     */
    public static void getHSVValue(Mat inMat, Boolean debug, String tempPath) {
        int nRows = inMat.rows();
        int nCols = inMat.cols();
        Map<Integer, Integer> map = Maps.newHashMap();
        for (int i = 0; i < nRows; ++i) {
            for (int j = 0; j < nCols; j += 3) {
                int H = (int)inMat.get(i, j)[0];
                // int S = (int)inMat.get(i, j)[1];
                // int V = (int)inMat.get(i, j)[2];
                if(map.containsKey(H)) {
                    int count = map.get(H);
                    map.put(H, count+1);
                } else {
                    map.put(H, 1);
                }
            }
        }
        Set<Integer> set = map.keySet();
        Object[] arr = set.toArray();
        Arrays.sort(arr);
        for (Object key : arr) {
            System.out.println(key + ": " + map.get(key));
        }
        return;
    }



    /**
     * 计算最大内接矩形
     * https://blog.csdn.net/cfqcfqcfqcfqcfq/article/details/53084090
     * @param inMat
     * @return
     */
    public static Rect maxAreaRect(Mat threshold, Point point) {
        int edge[] = new int[4];
        edge[0] = (int) point.x + 1;//top
        edge[1] = (int) point.y + 1;//right
        edge[2] = (int) point.y - 1;//bottom
        edge[3] = (int) point.x - 1;//left

        boolean[] expand = { true, true, true, true};//扩展标记位
        int n = 0;
        while (expand[0] || expand[1] || expand[2] || expand[3]){
            int edgeID = n % 4;
            expand[edgeID] = expandEdge(threshold, edge, edgeID);
            n++;
        }
        Point tl = new Point(edge[3], edge[0]);
        Point br = new Point(edge[1], edge[2]);
        return new Rect(tl, br);
    }


    /**
     * @brief expandEdge 扩展边界函数
     * @param img:输入图像，单通道二值图，深度为8
     * @param edge  边界数组，存放4条边界值
     * @param edgeID 当前边界号
     * @return 布尔值 确定当前边界是否可以扩展
     */
    public static boolean expandEdge(Mat img, int edge[], int edgeID) {
        int nc = img.cols();
        int nr = img.rows();

        switch (edgeID) {
        case 0:
            if (edge[0] > nr) {
                return false;
            }
            for (int i = edge[3]; i <= edge[1]; ++i) {
                if (img.get(edge[0], i)[0]== 255) {// 遇见255像素表明碰到边缘线
                    return false;
                }
            }
            edge[0]++;
            return true;
        case 1:
            if (edge[1] > nc) {
                return false;
            }
            for (int i = edge[2]; i <= edge[0]; ++i) {
                if (img.get(i, edge[1])[0] == 255)
                    return false;
            }
            edge[1]++;
            return true;
        case 2:
            if (edge[2] < 0) {
                return false;
            }
            for (int i = edge[3]; i <= edge[1]; ++i) {
                if (img.get(edge[2], i)[0] == 255)
                    return false;
            }
            edge[2]--;
            return true;
        case 3:
            if (edge[3] < 0) {
                return false;
            }
            for (int i = edge[2]; i <= edge[0]; ++i) {
                if (img.get(i, edge[3])[0] == 255)
                    return false;
            }
            edge[3]--;
            return true;
        default:
            return false;
        }
    }


    /**
     * 清除白色区域的内部黑色孔洞
     * rowLimit != colsLimit, 使用长方形比正方形好
     * 该算法比较耗时
     * @param inMat
     * @param rowLimit
     * @param colsLimit
     * @param debug
     * @param tempPath
     * @return
     */
    public static Mat clearInnerHole(Mat inMat, int rowLimit, int colsLimit, Boolean debug, String tempPath) {
        Instant start = Instant.now();
        int uncheck = 0, normal = 2, replace = 3, white = 255, black = 0;

        Mat dst = new Mat(inMat.size(), CvType.CV_8UC1);
        inMat.copyTo(dst);

        // 初始化的图像全部为0，未检查; 全黑图像
        Mat label = new Mat(inMat.size(), CvType.CV_8UC1);

        // 标记所有的白色区域
        for (int i = 0; i < inMat.rows(); i++) {
            for (int j = 0; j < inMat.cols(); j++) {
                // 白色点较少，遍历白色点速度快
                if (inMat.get(i, j)[0] == white && label.get(i, j)[0] == uncheck) {   // 对于二值图，0代表黑色，255代表白色
                    label.put(i, j, normal); // 中心点

                    // 执行两次，交换row 跟col；
                    int condition = 0;
                    do {
                        int x1 = i;
                        int x2 = i + rowLimit >= inMat.rows() ? inMat.rows() - 1 : i + rowLimit;
                        int y1 = j;
                        int y2 = j + colsLimit >= inMat.cols() ? inMat.cols() - 1 : j + colsLimit ;

                        int count = 0;
                        // 遍历四条边
                        for (int k = x1; k < x2; k++) {
                            if(inMat.get(k, y1)[0] == black || inMat.get(k, y2)[0] == black) {
                                count++;
                            }
                        }
                        for (int k = y1; k < y2; k++) {
                            if(inMat.get(x1, k)[0] == black || inMat.get(x2, k)[0] == black) {
                                count++;
                            }
                        }

                        // 根据中心点+limit，定位四个角生成一个矩形，
                        // 矩形四条边都是白色，内部的黑点标记为 要被替换的对象
                        if(count == 0 ) {
                            for (int n = x1; n < x2; n++) {
                                for (int m = y1; m < y2; m++) {
                                    if (inMat.get(n, m)[0] == black && label.get(n, m)[0] == uncheck) {
                                        label.put(n, m, replace);
                                    }
                                }
                            }
                        }
                        int ex = rowLimit;
                        rowLimit = colsLimit;
                        colsLimit = ex;

                        condition++;
                    } while (condition == 1);
                }
            }
        }


        for (int i = 0; i < inMat.rows(); i++) {
            for (int j = 0; j < inMat.cols(); j++) {
                if(label.get(i, j)[0] == replace) {
                    dst.put(i, j, white);
                }
            }
        }
        label.release();
        if (debug) {
            Imgcodecs.imwrite(tempPath + debugMap.get("clearInnerHole") + "_clearInnerHole.jpg", dst);
            Instant end = Instant.now();
            System.out.println("clearInnerHole执行耗时：" + Duration.between(start, end).toMillis());
        }
        return dst;
    }


    /**
     * 清除二值图像的黑洞
     * 按矩形清理
     * @param inMat  二值图像  0代表黑色，255代表白色
     * @param rowLimit 像素值
     * @param colsLimit 像素值
     * @param debug 
     * @param tempPath
     */
    public static Mat clearHole(Mat inMat, int rowLimit, int colsLimit, Boolean debug, String tempPath) {
        Instant start = Instant.now();
        int uncheck = 0, normal = 2, replace = 3, white = 255, black = 0;
        Mat dst = new Mat(inMat.size(), CvType.CV_8UC1);
        inMat.copyTo(dst);

        // 初始化的图像全部为0，未检查; 全黑图像
        Mat label = new Mat(inMat.size(), CvType.CV_8UC1);

        // 标记所有的白色区域
        for (int i = 0; i < inMat.rows(); i++) {
            for (int j = 0; j < inMat.cols(); j++) {
                if (inMat.get(i, j)[0] == white) {   // 对于二值图，0代表黑色，255代表白色
                    label.put(i, j, normal); // 中心点

                    // 执行两次，交换row 跟col；
                    int condition = 0;
                    do {
                        int x1 = i;
                        int x2 = i + rowLimit >= inMat.rows() ? inMat.rows() - 1 : i + rowLimit;
                        int y1 = j;
                        int y2 = j + colsLimit >= inMat.cols() ? inMat.cols() - 1 : j + colsLimit ;

                        int count = 0;
                        if(inMat.get(x1, y1)[0] == white) {// 左上角
                            count++;
                        }
                        if(inMat.get(x1, y2)[0] == white) { // 左下角
                            count++;
                        }
                        if(inMat.get(x2, y1)[0] == white) { // 右上角
                            count++;
                        }
                        if(inMat.get(x2, y2)[0] == white) { // 右下角
                            count++;
                        }

                        // 根据中心点+limit，定位四个角生成一个矩形，
                        // 将四个角都是白色的矩形，内部的黑点标记为 要被替换的对象
                        if(count >=4 ) {
                            for (int n = x1; n < x2; n++) {
                                for (int m = y1; m < y2; m++) {
                                    if (inMat.get(n, m)[0] == black && label.get(n, m)[0] == uncheck) {
                                        label.put(n, m, replace);
                                    }
                                }
                            }
                        }

                        int ex = rowLimit;
                        rowLimit = colsLimit;
                        colsLimit = ex;

                        condition++;
                    } while (condition == 1);
                }
            }
        }

        for (int i = 0; i < inMat.rows(); i++) {
            for (int j = 0; j < inMat.cols(); j++) {
                if(label.get(i, j)[0] == replace) {
                    dst.put(i, j, white); // 黑色替换成白色
                }
            }
        }
        if (debug) {
            Imgcodecs.imwrite(tempPath + debugMap.get("clearHole") + "_clearHole.jpg", dst);
            Instant end = Instant.now();
            System.out.println("clearHole执行耗时：" + Duration.between(start, end).toMillis());
        }
        return dst;
    }

    /**
     * 清除二值图像的细小连接
     * 按水平或者垂直方向清除
     * @param inMat
     * @param rowLimit
     * @param colsLimit
     * @param debug
     * @param tempPath
     * @return
     */
    public static Mat clearSmallConnArea(Mat inMat, int rowLimit, int colsLimit, Boolean debug, String tempPath) {
        Instant start = Instant.now();
        int uncheck = 0, normal = 2, replace = 3, white = 255, black = 0;

        Mat dst = new Mat(inMat.size(), CvType.CV_8UC1);
        inMat.copyTo(dst);

        // 初始化的图像全部为0，未检查; 全黑图像
        Mat label = new Mat(inMat.size(), CvType.CV_8UC1);

        // 标记所有的白色区域
        for (int i = 0; i < inMat.rows(); i++) {
            for (int j = 0; j < inMat.cols(); j++) {
                if (inMat.get(i, j)[0] == black) {   // 对于二值图，0代表黑色，255代表白色
                    label.put(i, j, normal); // 中心点
                    // 执行两次，交换row 跟col；
                    int condition = 0;
                    do {
                        int x1 = i;
                        int x2 = i + rowLimit >= inMat.rows() ? inMat.rows() - 1 : i + rowLimit;
                        int y1 = j;
                        int y2 = j + colsLimit >= inMat.cols() ? inMat.cols() - 1 : j + colsLimit ;

                        int count = 0;
                        if(inMat.get(x1, y1)[0] == black) {// 左上角
                            count++;
                        }
                        if(inMat.get(x1, y2)[0] == black) { // 左下角
                            count++;
                        }
                        if(inMat.get(x2, y1)[0] == black) { // 右上角
                            count++;
                        }
                        if(inMat.get(x2, y2)[0] == black) { // 右下角
                            count++;
                        }

                        // 根据 中心点+limit，定位四个角生成一个矩形，
                        // 将四个角都是黑色的矩形，内部的白点标记为 要被替换的对象
                        if(count >= 4) {
                            for (int n = x1; n < x2; n++) {
                                for (int m = y1; m < y2; m++) {
                                    if (inMat.get(n, m)[0] == white && label.get(n, m)[0] == uncheck) {
                                        label.put(n, m, replace);
                                    }
                                }
                            }
                        }
                        int ex = rowLimit;
                        rowLimit = colsLimit;
                        colsLimit = ex;

                        condition++;
                    } while (condition == 1);
                }
            }
        }

        for (int i = 0; i < inMat.rows(); i++) {
            for (int j = 0; j < inMat.cols(); j++) {
                if(label.get(i, j)[0] == replace) {
                    dst.put(i, j, black);   // 白色替换成黑色
                }
            }
        }
        if (debug) {
            Imgcodecs.imwrite(tempPath + debugMap.get("clearSmallConnArea") + "_clearSmallConnArea.jpg", dst);
            Instant end = Instant.now();
            System.out.println("clearSmallConnArea执行耗时：" + Duration.between(start, end).toMillis());
        }
        return dst;
    }


    /**
     * 清除二值图像的细小连接
     * 按45度斜边清除
     * @param inMat
     * @param limit
     * @param angle
     * @param debug
     * @param tempPath
     * @return
     */
    public static Mat clearAngleConn(Mat inMat, int limit, Boolean debug, String tempPath) {
        Instant start = Instant.now();
        int uncheck = 0, normal = 2, replace = 3, white = 255, black = 0;

        Mat dst = new Mat(inMat.size(), CvType.CV_8UC1);
        inMat.copyTo(dst);

        // 初始化的图像全部为0，未检查; 全黑图像
        Mat label = new Mat(inMat.size(), CvType.CV_8UC1);

        // 标记所有的白色区域
        for (int i = 0; i < inMat.rows(); i++) {
            for (int j = 0; j < inMat.cols(); j++) {
                if (inMat.get(i, j)[0] == black) {   // 对于二值图，0代表黑色，255代表白色
                    label.put(i, j, normal); // 中心点

                    int x1 = i;
                    int x2 = i + limit >= inMat.rows() ? inMat.rows() - 1 : i + limit;
                    int y1 = j;
                    int y2 = j + limit >= inMat.cols() ? inMat.cols() - 1 : j + limit ;

                    // 根据 中心点+limit，定位四个角生成一个矩形，
                    // 将2个角都是黑色的线，内部的白点标记为 要被替换的对象
                    // 【\】 斜对角线
                    if(inMat.get(x1, y1)[0] == black && inMat.get(x2, y2)[0] == black) {
                        for (int n = x1, m = y1; n < x2 && m < y2; n++, m++) {
                            if (inMat.get(n, m)[0] == white && label.get(n, m)[0] == uncheck) {
                                label.put(n, m, replace);
                            }
                        }
                    }
                    if(inMat.get(x1, y2)[0] == black && inMat.get(x2, y1)[0] == black) {
                        // 【/】 斜对角线
                        for (int n = x1, m = y2; n < x2 && m > y1; n++, m--) {
                            if (inMat.get(n, m)[0] == white && label.get(n, m)[0] == uncheck) {
                                label.put(n, m, replace);
                            }
                        }
                    }
                }
            }
        }
        // 白色替换成黑色
        for (int i = 0; i < inMat.rows(); i++) {
            for (int j = 0; j < inMat.cols(); j++) {
                if(label.get(i, j)[0] == replace) {
                    dst.put(i, j, black);
                }
            }
        }

        if (debug) {
            Imgcodecs.imwrite(tempPath + debugMap.get("clearAngleConn") + "_clearAngleConn.jpg", dst);
            Instant end = Instant.now();
            System.out.println("clearAngleConn执行耗时：" + Duration.between(start, end).toMillis());
        }
        return dst;
    }



}
