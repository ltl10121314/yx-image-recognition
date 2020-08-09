package com.yuxue.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.ANN_MLP;
import org.opencv.ml.SVM;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yuxue.constant.Constant;
import com.yuxue.enumtype.Direction;
import com.yuxue.enumtype.PlateColor;
import com.yuxue.train.SVMTrain;


/**
 * 车牌处理工具类
 * 车牌切图按字符分割
 * 字符识别
 * 未完成
 * @author yuxue
 * @date 2020-05-28 15:11
 */
public class PlateUtil {

    // 车牌定位处理步骤，该map用于表示步骤图片的顺序
    private static Map<String, Integer> debugMap = Maps.newLinkedHashMap();
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        debugMap.put("platePredict", 0); 
        debugMap.put("colorMatch", 0); 
        debugMap.put("plateThreshold", 0); 
        debugMap.put("plateContours", 0); 
        debugMap.put("plateRect", 0); 
        debugMap.put("plateCrop", 0); 
        debugMap.put("char_clearLiuDing", 0); // 去除柳钉
        debugMap.put("specMat", 0); 
        debugMap.put("chineseMat", 0);
        debugMap.put("char_auxRoi", 0);

        // 设置index， 用于debug生成文件时候按名称排序
        Integer index = 200;
        for (Entry<String, Integer> entry : debugMap.entrySet()) {
            entry.setValue(index);
            index ++;
        }
        
        // 这个位置加载模型文件会报错，暂时没时间定位啥问题报错
        /*loadSvmModel("D:/PlateDetect/train/plate_detect_svm/svm2.xml");
        loadAnnModel("D:/PlateDetect/train/chars_recognise_ann/ann.xml");*/
    }

    private static SVM svm = SVM.create();

    private static ANN_MLP ann=ANN_MLP.create();

    public static void loadSvmModel(String path) {
        svm.clear();
        svm=SVM.load(path);
    }

    // 加载ann配置文件  图像转文字的训练库文件
    public static void loadAnnModel(String path) {
        ann.clear();
        ann = ANN_MLP.load(path);
    }


    public static void main(String[] args) {
        /*System.err.println(PalteUtil.isPlate("粤AI234K"));
        System.err.println(PalteUtil.isPlate("鄂CD3098"));*/

    }


    /**
     * 根据正则表达式判断字符串是否是车牌
     * @param str
     * @return
     */
    public static Boolean isPlate(String str) {
        Pattern p = Pattern.compile(Constant.plateReg);
        Boolean bl = false;
        Matcher m = p.matcher(str);
        while(m.find()) {
            bl = true;
            break;
        }
        return bl;
    }


    /**
     * 输入车牌切图集合，判断是否包含车牌
     * @param inMat
     * @param dst 包含车牌的图块
     */
    public static void hasPlate(Vector<Mat> inMat, Vector<Mat> dst, Boolean debug, String tempPath) {
        int i = 0;
        for (Mat src : inMat) {
            if(src.rows() == Constant.DEFAULT_HEIGHT && src.cols() == Constant.DEFAULT_WIDTH) {
                Mat samples = SVMTrain.getFeature(src);
                float flag = svm.predict(samples);
                if (flag == 0) {
                    dst.add(src);
                    if(debug) {
                        System.err.println("目标符合");
                        Imgcodecs.imwrite(tempPath + debugMap.get("platePredict") + "_platePredict" + i + ".png", src);
                    }
                    i++;
                } else {
                    System.out.println("目标不符合");
                }
            } else {
                System.err.println("非法图块");
            }
        }
        return;
    }


    /**
     * 判断切图车牌颜色
     * @param inMat
     * @return
     */
    public static PlateColor getPlateColor(Mat inMat, Boolean adaptive_minsv, Boolean debug, String tempPath) {
        // 判断阈值
        final float thresh = 0.70f;
        if(colorMatch(inMat, PlateColor.GREEN, adaptive_minsv, debug, tempPath) > thresh) {
            return PlateColor.GREEN;
        }
        if(colorMatch(inMat, PlateColor.YELLOW, adaptive_minsv, debug, tempPath) > thresh) {
            return PlateColor.YELLOW;
        }
        if(colorMatch(inMat, PlateColor.BLUE, adaptive_minsv, debug, tempPath) > thresh) {
            return PlateColor.BLUE;
        }
        return PlateColor.UNKNOWN;
    }


    /**
     * 颜色匹配计算
     * @param inMat
     * @param r
     * @param adaptive_minsv
     * @param debug
     * @param tempPath
     * @return
     */
    public static Float colorMatch(Mat inMat, PlateColor r, Boolean adaptive_minsv, Boolean debug, String tempPath) {
        final float max_sv = 255;
        final float minref_sv = 64;
        final float minabs_sv = 95;

        Mat hsvMat = ImageUtil.rgb2Hsv(inMat, debug, tempPath);

        // 匹配模板基色,切换以查找想要的基色
        int min_h = r.minH;
        int max_h = r.maxH;
        float diff_h = (float) ((max_h - min_h) / 2);
        int avg_h = (int) (min_h + diff_h);

        for (int i = 0; i < hsvMat.rows(); ++i) {
            for (int j = 0; j < hsvMat.cols(); j += 3) {
                int H = (int)hsvMat.get(i, j)[0];
                int S = (int)hsvMat.get(i, j)[1];
                int V = (int)hsvMat.get(i, j)[2];

                boolean colorMatched = false;

                if ( min_h < H && H <= max_h) {
                    int Hdiff = Math.abs(H - avg_h);
                    float Hdiff_p = Hdiff / diff_h;
                    float min_sv = 0;
                    if (adaptive_minsv) {
                        min_sv = minref_sv - minref_sv / 2 * (1 - Hdiff_p);
                    } else {
                        min_sv = minabs_sv;
                    }
                    if ((min_sv < S && S <= max_sv) && (min_sv < V && V <= max_sv)) {
                        colorMatched = true;
                    }
                }

                if (colorMatched == true) {
                    hsvMat.put(i, j, 0, 0, 255);
                } else {
                    hsvMat.put(i, j, 0, 0, 0);
                }
            }
        }

        // 获取颜色匹配后的二值灰度图
        List<Mat> hsvSplit = Lists.newArrayList();
        Core.split(hsvMat, hsvSplit);
        Mat gray = hsvSplit.get(2);

        float percent = (float) Core.countNonZero(gray) / (gray.rows() * gray.cols());
        if (debug) {
            Imgcodecs.imwrite(tempPath + debugMap.get("colorMatch") + "_colorMatch.jpg", gray);
        }
        return percent;
    }



    /**
     * 车牌切图，分割成单个字符切图
     * @param inMat 输入原始图像
     * @param charMat 返回字符切图vector
     * @param debug
     * @param tempPath
     */
    public static final int DEFAULT_ANGLE = 30; // 角度判断所用常量
    public static void charsSegment(Mat inMat, PlateColor color, Vector<Mat> charMat, Boolean debug, String tempPath) {
        Mat gray = new Mat();
        Imgproc.cvtColor(inMat, gray, Imgproc.COLOR_BGR2GRAY);

        Mat threshold = new Mat();
        switch (color) {
        case BLUE:
            Imgproc.threshold(gray, threshold, 10, 255, Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY);
            break;

        case YELLOW:
            Imgproc.threshold(gray, threshold, 10, 255, Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY_INV);
            break;

        case GREEN:
            Imgproc.threshold(gray, threshold, 10, 255, Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY_INV);
            break;

        default:
            return;
        }

        // 图片处理，降噪等
        if (debug) {
            Imgcodecs.imwrite(tempPath + debugMap.get("plateThreshold") + "_plateThreshold.jpg", threshold);
        }

        // 获取轮廓
        Mat contour = new Mat();
        threshold.copyTo(contour);

        List<MatOfPoint> contours = Lists.newArrayList();
        // 提取外部轮廓
        Imgproc.findContours(contour, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

        if (debug) {
            Mat result = new Mat();
            inMat.copyTo(result);
            Imgproc.drawContours(result, contours, -1, new Scalar(0, 0, 255, 255));
            Imgcodecs.imwrite(tempPath + debugMap.get("plateContours") + "_plateContours.jpg", result);
        }


        Vector<Rect> rt = new Vector<Rect>();
        for (int i = 0; i < contours.size(); i++) {
            Rect mr = Imgproc.boundingRect(contours.get(i));
            /*if(debug) {
                Mat mat = new Mat(threshold, mr);
                Imgcodecs.imwrite(tempPath + debugMap.get("plateRect") + "_plateRect_" + i + ".jpg", mat);
            }*/
            if (checkCharSizes(mr)) {
                rt.add(mr);
            }
        }
        if(null == rt || rt.size() <= 0) {
            return;
        }
        Vector<Rect> sorted = new Vector<Rect>();
        sortRect(rt, sorted);

        String plate = "";
        Vector<Mat> dst = new Vector<Mat>();
        
        for (int i = 0; i < sorted.size(); i++) {
            Mat img_crop = new Mat(threshold, sorted.get(i));
            img_crop = preprocessChar(img_crop);
            dst.add(img_crop);
            if(debug) {
                Imgcodecs.imwrite(tempPath + debugMap.get("plateCrop") + "_plateCrop_" + i + ".jpg", img_crop);
            }
            
            Mat f = features(img_crop, Constant.predictSize);
            
            // 字符预测
            Mat output = new Mat(1, 140, CvType.CV_32F);
            int index = (int) ann.predict(f, output, 0);
            
            if (index < Constant.numCharacter) {
                plate += String.valueOf(Constant.strCharacters[index]);
            } else {
                String s = Constant.strChinese[index - Constant.numCharacter];
                plate += Constant.KEY_CHINESE_MAP.get(s);
            }
        }
        System.err.println("===>" + plate);
        
        return;
    }

    /**
     * 字符预处理: 统一每个字符的大小
     * @param in
     * @return
     */
    final static int CHAR_SIZE = 20;
    private static Mat preprocessChar(Mat in) {
        int h = in.rows();
        int w = in.cols();
        Mat transformMat = Mat.eye(2, 3, CvType.CV_32F);
        int m = Math.max(w, h);
        transformMat.put(0, 2, (m - w) / 2f);
        transformMat.put(1, 2, (m - h) / 2f);

        Mat warpImage = new Mat(m, m, in.type());
        Imgproc.warpAffine(in, warpImage, transformMat, warpImage.size(), Imgproc.INTER_LINEAR, Core.BORDER_CONSTANT, new Scalar(0));

        Mat resized = new Mat(CHAR_SIZE, CHAR_SIZE, CvType.CV_8UC3);
        Imgproc.resize(warpImage, resized, resized.size(), 0, 0, Imgproc.INTER_CUBIC);

        return resized;
    }



    /**
     * 字符尺寸验证；去掉尺寸不符合的图块
     * 此处计算宽高比意义不大，因为字符 1 的宽高比干扰就已经很大了
     * @param r
     * @return
     */
    public static Boolean checkCharSizes(Rect r) {
        float minHeight = 15f;
        float maxHeight = 35f;
        double charAspect = r.size().width / r.size().height;
        return charAspect <1 && minHeight <= r.size().height && r.size().height < maxHeight;
    }



    /**
     * 将Rect按位置从左到右进行排序
     * @param vecRect
     * @param out
     * @return
     */
    public static void sortRect(Vector<Rect> vecRect, Vector<Rect> out) {
        Map<Integer, Integer> map = Maps.newHashMap();
        for (int i = 0; i < vecRect.size(); ++i) {
            map.put(vecRect.get(i).x, i);
        }
        Set<Integer> set = map.keySet();
        Object[] arr = set.toArray();
        Arrays.sort(arr);
        for (Object key : arr) {
            out.add(vecRect.get(map.get(key)));
        }
        return;
    }


    
    public static float[] projectedHistogram(final Mat img, Direction direction) {
        int sz = 0;
        switch (direction) {
        case HORIZONTAL:
            sz = img.rows();
            break;

        case VERTICAL:
            sz = img.cols();
            break;

        default:
            break;
        }

        // 统计这一行或一列中，非零元素的个数，并保存到nonZeroMat中
        float[] nonZeroMat = new float[sz];
        Core.extractChannel(img, img, 0);
        for (int j = 0; j < sz; j++) {
            Mat data = (direction == Direction.HORIZONTAL) ? img.row(j) : img.col(j);
            int count = Core.countNonZero(data);
            nonZeroMat[j] = count;
        }
        // Normalize histogram
        float max = 0;
        for (int j = 0; j < nonZeroMat.length; ++j) {
            max = Math.max(max, nonZeroMat[j]);
        }
        if (max > 0) {
            for (int j = 0; j < nonZeroMat.length; ++j) {
                nonZeroMat[j] /= max;
            }
        }
        return nonZeroMat;
    }


    public static Mat features(Mat in, int sizeData) {

        float[] vhist = projectedHistogram(in, Direction.VERTICAL);
        float[] hhist = projectedHistogram(in, Direction.HORIZONTAL);

        Mat lowData = new Mat();
        if (sizeData > 0) {
            Imgproc.resize(in, lowData, new Size(sizeData, sizeData));
        }

        int numCols = vhist.length + hhist.length + lowData.cols() * lowData.rows();
        Mat out = new Mat(1, numCols, CvType.CV_32F);

        int j = 0;
        for (int i = 0; i < vhist.length; ++i, ++j) {
            out.put(0, j, vhist[i]);
        }
        for (int i = 0; i < hhist.length; ++i, ++j) {
            out.put(0, j, hhist[i]);
        }

        for (int x = 0; x < lowData.cols(); x++) {
            for (int y = 0; y < lowData.rows(); y++, ++j) {
                double[] val = lowData.get(x, y);
                out.put(0, j, val[0]);
            }
        }
        return out;
    }


    

    /**
     * 进行膨胀操作
     * @param inMat
     * @return
     */
    public static Mat dilate(Mat inMat) {
        Mat result = inMat.clone();
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2));
        Imgproc.dilate(inMat, result, element);
        return result;
    }

    /**
     * 进行腐蚀操作
     * @param inMat
     * @return
     */
    public static Mat erode(Mat inMat) {
        Mat result = inMat.clone();
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2));
        Imgproc.erode(inMat, result, element);
        return result;
    }


    /**
     * 随机数平移
     * @param inMat
     * @return
     */
    public static Mat randTranslate(Mat inMat) {
        Random rand = new Random();
        Mat result = inMat.clone();
        int ran_x = rand.nextInt(10000) % 5 - 2; // 控制在-2~3个像素范围内
        int ran_y = rand.nextInt(10000) % 5 - 2;
        return translateImg(result, ran_x, ran_y);
    }


    /**
     * 随机数旋转
     * @param inMat
     * @return
     */
    public static Mat randRotate(Mat inMat) {
        Random rand = new Random();
        Mat result = inMat.clone();
        float angle = (float) (rand.nextInt(10000) % 15 - 7); // 旋转角度控制在-7~8°范围内
        return rotateImg(result, angle);
    }


    /**
     * 平移
     * @param img
     * @param offsetx
     * @param offsety
     * @return
     */
    public static Mat translateImg(Mat img, int offsetx, int offsety){
        Mat dst = new Mat();
        //定义平移矩阵
        Mat trans_mat = Mat.zeros(2, 3, CvType.CV_32FC1);
        trans_mat.put(0, 0, 1);
        trans_mat.put(0, 2, offsetx);
        trans_mat.put(1, 1, 1);
        trans_mat.put(1, 2, offsety);
        Imgproc.warpAffine(img, dst, trans_mat, img.size());    // 仿射变换
        return dst;
    }


    /**
     * 旋转角度
     * @param source
     * @param angle
     * @return
     */
    public static Mat rotateImg(Mat source, float angle){
        Point src_center = new Point(source.cols() / 2.0F, source.rows() / 2.0F);
        Mat rot_mat = Imgproc.getRotationMatrix2D(src_center, angle, 1);
        Mat dst = new Mat();
        // 仿射变换 可以考虑使用投影变换; 这里使用放射变换进行旋转，对于实际效果来说感觉意义不大，反而会干扰结果预测
        Imgproc.warpAffine(source, dst, rot_mat, source.size());    
        return dst;
    }



}
