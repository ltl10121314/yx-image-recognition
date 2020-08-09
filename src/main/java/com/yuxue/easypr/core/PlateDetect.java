package com.yuxue.easypr.core;

import java.util.Vector;

import org.bytedeco.javacpp.opencv_core.Mat;


/**
 * 车牌检测识别
 * 分两个步骤： 1、车牌定位 2、车牌判断
 * @author yuxue
 * @date 2020-04-24 15:33
 */
public class PlateDetect {

    // 车牌定位， 图片处理对象
    private PlateLocate plateLocate = new PlateLocate();

    // 切图判断对象
    private PlateJudge plateJudge = new PlateJudge();

    /**
     * @param src 图片路径，不能包含中文及特殊字符
     * @param resultVec 车牌的图块集合
     * @return the error number
     *         <ul>
     *         <li>0: plate detected successfully;
     *         <li>-1: source Mat is empty;
     *         <li>-2: plate not detected.
     *         </ul>
     */
    public int plateDetect(final Mat src, Vector<Mat> resultVec) {
        Vector<Mat> matVec = plateLocate.plateLocate(src);  // 定位

        if (0 == matVec.size()) {
            return -1;
        }

        if (0 != plateJudge.plateJudge(matVec, resultVec)) { //对多幅图像进行SVM判断
            return -2;
        }
        return 0;
    }


    /**
     * 生活模式与工业模式切换
     * @param pdLifemode
     */
    public void setPDLifemode(boolean pdLifemode) {
        plateLocate.setLifemode(pdLifemode);
    }

    public void setGaussianBlurSize(int gaussianBlurSize) {
        plateLocate.setGaussianBlurSize(gaussianBlurSize);
    }

    public final int getGaussianBlurSize() {
        return plateLocate.getGaussianBlurSize();
    }

    public void setMorphSizeWidth(int morphSizeWidth) {
        plateLocate.setMorphSizeWidth(morphSizeWidth);
    }

    public final int getMorphSizeWidth() {
        return plateLocate.getMorphSizeWidth();
    }

    public void setMorphSizeHeight(int morphSizeHeight) {
        plateLocate.setMorphSizeHeight(morphSizeHeight);
    }

    public final int getMorphSizeHeight() {
        return plateLocate.getMorphSizeHeight();
    }

    public void setVerifyError(float verifyError) {
        plateLocate.setVerifyError(verifyError);
    }

    public final float getVerifyError() {
        return plateLocate.getVerifyError();
    }

    public void setVerifyAspect(float verifyAspect) {
        plateLocate.setVerifyAspect(verifyAspect);
    }

    public final float getVerifyAspect() {
        return plateLocate.getVerifyAspect();
    }

    public void setVerifyMin(int verifyMin) {
        plateLocate.setVerifyMin(verifyMin);
    }

    public void setVerifyMax(int verifyMax) {
        plateLocate.setVerifyMax(verifyMax);
    }

    public void setJudgeAngle(int judgeAngle) {
        plateLocate.setJudgeAngle(judgeAngle);
    }
    
    public void setDebug(boolean debug, String tempPath) {
        plateLocate.setDebug(debug);
        plateLocate.setTempPath(tempPath);
    }

}
