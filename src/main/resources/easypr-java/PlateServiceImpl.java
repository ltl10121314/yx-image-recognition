package com.yuxue.service.impl;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_imgcodecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yuxue.constant.Constant;
import com.yuxue.easypr.core.CharsRecognise;
import com.yuxue.easypr.core.CoreFunc;
import com.yuxue.easypr.core.PlateDetect;
import com.yuxue.entity.PlateFileEntity;
import com.yuxue.entity.PlateRecoDebugEntity;
import com.yuxue.entity.TempPlateFileEntity;
import com.yuxue.enumtype.PlateColor;
import com.yuxue.mapper.PlateFileMapper;
import com.yuxue.mapper.PlateRecoDebugMapper;
import com.yuxue.mapper.TempPlateFileMapper;
import com.yuxue.service.PlateService;
import com.yuxue.util.FileUtil;



@Service
public class PlateServiceImpl implements PlateService {

    
    // 车牌定位处理步骤，该map用于表示步骤图片的顺序
    private static Map<String, Integer> debugMap = Maps.newLinkedHashMap();
    static {
        debugMap.put("result", 99);
        debugMap.put("debug_GaussianBlur", 0); // 高斯模糊
        debugMap.put("debug_gray", 1);  // 图像灰度化
        debugMap.put("debug_Sobel", 2); // Sobel 算子
        debugMap.put("debug_threshold", 3); //图像二值化
        debugMap.put("debug_morphology", 4); // 图像闭操作
        debugMap.put("debug_Contours", 5); // 提取外部轮廓
        debugMap.put("debug_result", 6); // 原图处理结果
        debugMap.put("debug_crop", 7); // 切图
        debugMap.put("debug_resize", 8); // 切图resize
        debugMap.put("debug_char_threshold", 9); // 
        // debugMap.put("debug_char_clearLiuDing", 10); // 去除柳钉
        debugMap.put("debug_specMat", 11); // 
        debugMap.put("debug_chineseMat", 12); // 
        debugMap.put("debug_char_auxRoi", 13); // 
    }
    

    @Autowired
    private PlateFileMapper plateFileMapper;

    @Autowired
    private PlateRecoDebugMapper plateRecoDebugMapper;

    @Autowired
    private TempPlateFileMapper tempPlateFileMapper;


    @Override
    public Object recognise(String filePath, boolean reRecognise) {
        filePath = filePath.replaceAll("\\\\", "/");
        File f = new File(filePath);
        PlateFileEntity e = null;

        Map<String, Object> paramMap = Maps.newHashMap();
        paramMap.put("filePath", filePath);
        List<PlateFileEntity> list= plateFileMapper.selectByCondition(paramMap);
        if(null == list || list.size() <= 0) {
            if(FileUtil.checkFile(f)) {
                e = new PlateFileEntity();
                e.setFileName(f.getName());
                e.setFilePath(f.getAbsolutePath().replaceAll("\\\\", "/"));
                e.setFileType(f.getName().substring(f.getName().lastIndexOf(".") + 1));
                plateFileMapper.insertSelective(e);
            }
            reRecognise = true;
        } else {
            e = list.get(0);
        }

        if(reRecognise) {
            doRecognise(f, e, 0); // 重新识别
            e = plateFileMapper.selectByPrimaryKey(e.getId()); // 重新识别之后，重新获取一下数据
        }

        // 查询数据库，返回结果
        paramMap.clear();
        paramMap.put("parentId", e.getId());
        e.setDebug(plateRecoDebugMapper.selectByCondition(paramMap));

        return e;
    }



    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Object refreshFileInfo() {
        File baseDir = new File(Constant.DEFAULT_DIR);
        if(!baseDir.exists() || !baseDir.isDirectory()) {
            return null;
        }
        List<TempPlateFileEntity> resultList = Lists.newArrayList();

        // 获取baseDir下第一层级的目录， 仅获取文件夹，不递归子目录，遍历
        List<File> folderList = FileUtil.listFile(baseDir, ";", false);
        folderList.parallelStream().forEach(folder -> {
            if(!folder.getName().equals("temp")) {
                // 遍历每一个文件夹， 递归获取文件夹下的图片
                List<File> imgList = FileUtil.listFile(folder, Constant.DEFAULT_TYPE, true);
                if(null != imgList && imgList.size() > 0) {
                    imgList.parallelStream().forEach(n->{
                        TempPlateFileEntity entity = new TempPlateFileEntity();
                        entity.setFilePath(n.getAbsolutePath().replaceAll("\\\\", "/"));
                        entity.setFileName(n.getName());
                        entity.setFileType(n.getName().substring(n.getName().lastIndexOf(".") + 1));
                        resultList.add(entity);
                    });
                }
            }
        });

        tempPlateFileMapper.turncateTable();
        tempPlateFileMapper.batchInsert(resultList);
        tempPlateFileMapper.updateFileInfo();

        return 1;
    }


    @Override
    public Object recogniseAll() {
        // 查询到还没有进行车牌识别的图片
        List<PlateFileEntity> list = plateFileMapper.getUnRecogniseList();
        
        // 开启多线程进行识别
        Random r = new Random(99);
        list.parallelStream().forEach(n->{
            File f = new File(n.getFilePath());
            if(FileUtil.checkFile(f)) {
                doRecognise(f, n, r.nextInt());
            }
        });

        return 1;
    }
   

    @Override
    public Object getProcessStep() {
        return debugMap;
    }


    /**
     * 单张图片 车牌识别
     * 拷贝文件到临时目录
     * 过程及结果更新数据库
     * @param f 调用方需要验证文件存在
     * @param result
     * @return
     */
    public Object doRecognise(File f, PlateFileEntity e, Integer seed) {

        // 插入识别过程图片数据信息 通过temp文件夹的文件，更新数据库
        List<PlateRecoDebugEntity> debug = Lists.newArrayList();
        
        Long ct = System.currentTimeMillis();
        String targetPath = Constant.DEFAULT_TEMP_DIR.concat(ct.toString() + seed)
                .concat(f.getAbsolutePath().substring(f.getAbsolutePath().lastIndexOf(".")));

        // 先将文件拷贝并且重命名到不包含中文及特殊字符的目录下
        FileUtil.copyAndRename(f.getAbsolutePath(), targetPath);

        // 开始识别，生成过程及结果切图，将识别结果更新到数据库
        Mat src = opencv_imgcodecs.imread(targetPath);

        String tempPath =  Constant.DEFAULT_TEMP_DIR + ct + "/";
        FileUtil.createDir(tempPath); // 创建文件夹

        // 车牌检测对象
        PlateDetect plateDetect = new PlateDetect();
        plateDetect.setPDLifemode(true);
        plateDetect.setDebug(true, tempPath); // 将过程的图块保存到盘符

        Vector<Mat> matVector = new Vector<Mat>();
        if (0 == plateDetect.plateDetect(src, matVector)) { // 定位及判断，获取到车牌图块Mat

            CharsRecognise cr = new CharsRecognise();
            cr.setCRDebug(true);
            for (int i = 0; i < matVector.size(); ++i) { // 遍历车牌图块Mat，进行识别
                Mat img = matVector.get(i);

                String palte = cr.charsRecognise(img, tempPath); // 字符识别
                PlateColor color = CoreFunc.getPlateType(img, true);
                String fileName = "result_" +  i + ".png";

                // 识别的车牌，保存图片文件
                String str = tempPath + fileName;
                // 此方法生成的文件，中文名称都是乱码，试了各种编解码均无效，OpenCV自身的编解码问题。
                opencv_imgcodecs.imwrite(str, img);
                // 重命名文件，让生成的文件包含中文
                // String newName = palte + "_"+ color + ".png";
                // FileUtil.renameFile(str, newName);
                
                PlateRecoDebugEntity de = new PlateRecoDebugEntity();
                de.setRecoPlate(palte);
                de.setFilePath(str);
                de.setFileName(fileName);
                de.setPlateColor(color.desc);
                de.setParentId(e.getId());
                de.setDebugType("result");
                de.setSort(debugMap.get("result"));
                debug.add(de);
            }
        } else {
            e.setRecoCorrect(3);    // 未检测到车牌
        }

        new File(targetPath).delete();    // 删除拷贝的文件

        e.setTempPath(tempPath);
        
        List<File> debugList = FileUtil.listFile(new File(tempPath), Constant.DEFAULT_TYPE, false);
        
        debugList.parallelStream().forEach(d -> {
            String name = d.getName().substring(0, d.getName().lastIndexOf("."));
            
            Pattern pattern = Pattern.compile("\\d+$");
            Matcher matcher = pattern.matcher(name);
            if(matcher.find()) {
                name = name.substring(0, name.lastIndexOf("_"));
            }
            
            if(!"result".equals(name)) {
                PlateRecoDebugEntity de = new PlateRecoDebugEntity();
                de.setRecoPlate("");
                de.setFilePath(d.getAbsolutePath().replaceAll("\\\\", "/"));
                de.setFileName(d.getName());
                de.setPlateColor("");
                de.setParentId(e.getId());
                de.setDebugType(name);
                de.setSort(debugMap.get(name));
                debug.add(de);
            }
        });
        
        // 更新图片主表信息
        plateFileMapper.updateByPrimaryKeySelective(e);
        
        plateRecoDebugMapper.deleteByParentId(e.getId());
        
        plateRecoDebugMapper.batchInsert(debug);

        return 1;
    }


}
