package com.yuxue.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统常量
 * @author yuxue
 * @date 2018-09-07
 */
public class Constant {

    public static final String UTF8 = "UTF-8";

    // 车牌识别， 默认车牌图片保存路径
    // public static String DEFAULT_DIR = "./PlateDetect/"; // 使用项目的相对路径
    public static String DEFAULT_DIR = "PlateDetect"; // 使用盘符的绝对路径

    // 车牌识别， 默认车牌图片处理过程temp路径
    // public static String DEFAULT_TEMP_DIR = "./PlateDetect/temp/"; // 使用项目的相对路径
    public static String DEFAULT_TEMP_DIR = "PlateDetect/temp"; // 使用盘符的绝对路径

    // 车牌识别，默认处理图片类型
    public static String DEFAULT_TYPE = "png,jpg,jpeg";

    public static String DEFAULT_ANN_PATH = "res/model/ann.xml";
    //public static String DEFAULT_ANN_PATH = "D:/PlateDetect/train/chars_recognise_ann/ann.xml";
    
    public static String DEFAULT_SVM_PATH = "res/model/svm.xml";
    
    public static final int DEFAULT_WIDTH = 136;    // cols
    public static final int DEFAULT_HEIGHT = 36;    // rows

    // 车牌识别，判断是否车牌的正则表达式
    public static String plateReg = "([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}(([0-9]{5}[DF])|([DF]([A-HJ-NP-Z0-9])[0-9]{4})))|([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}[A-HJ-NP-Z0-9]{4}[A-HJ-NP-Z0-9挂学警港澳]{1})";

    public static int predictSize = 10;

    
    public static int neurons = 40;

    // 中国车牌; 34个字符; 没有 字母I、字母O
    public final static char strCharacters[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', /* 没有I */ 'J', 'K', 'L', 'M', 'N', /* 没有O */'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

    // 没有I和0, 10个数字与24个英文字符之和
    public final static Integer numCharacter = strCharacters.length; 

    // 并不全面，有些省份没有训练数据所以没有字符
    // 有些后面加数字2的表示在训练时常看到字符的一种变形，也作为训练数据存储
    public final static String strChinese[] = { 
            "zh_cuan",  /*川*/
            "zh_e",     /*鄂*/
            "zh_gan",   /*赣*/
            "zh_gan1",  /*甘*/
            "zh_gui",   /*贵*/
            "zh_gui1",  /*桂*/
            "zh_hei",   /*黑*/
            "zh_hu",    /*沪*/
            "zh_ji",    /*冀*/
            "zh_jin",   /*津*/
            "zh_jing",  /*京*/
            "zh_jl",    /*吉*/
            "zh_liao",  /*辽*/
            "zh_lu",    /*鲁*/
            "zh_meng",  /*蒙*/
            "zh_min",   /*闽*/
            "zh_ning",  /*宁*/
            "zh_qing",  /*青*/
            "zh_qiong", /*琼*/
            "zh_shan",  /*陕*/
            "zh_su",    /*苏*/
            "zh_sx",    /*晋*/
            "zh_wan",   /*皖*/
            "zh_xiang", /*湘*/
            "zh_xin",   /*新*/
            "zh_yu",    /*豫*/
            "zh_yu1",   /*渝*/
            "zh_yue",   /*粤*/
            "zh_yun",   /*云*/
            "zh_zang",  /*藏*/
            "zh_zhe"    /*浙*/
    };
    
    /* 34+31=65 34个字符跟31个汉字 */
    public final static Integer numAll = strCharacters.length + strChinese.length; 

    public static Map<String, String> KEY_CHINESE_MAP = new HashMap<String, String>();
    static {
        if (KEY_CHINESE_MAP.isEmpty()) {
            KEY_CHINESE_MAP.put("zh_cuan", "川");
            KEY_CHINESE_MAP.put("zh_e", "鄂");
            KEY_CHINESE_MAP.put("zh_gan", "赣");
            KEY_CHINESE_MAP.put("zh_gan1", "甘");
            KEY_CHINESE_MAP.put("zh_gui", "贵");
            KEY_CHINESE_MAP.put("zh_gui1", "桂");
            KEY_CHINESE_MAP.put("zh_hei", "黑");
            KEY_CHINESE_MAP.put("zh_hu", "沪");
            KEY_CHINESE_MAP.put("zh_ji", "冀");
            KEY_CHINESE_MAP.put("zh_jin", "津");
            KEY_CHINESE_MAP.put("zh_jing", "京");
            KEY_CHINESE_MAP.put("zh_jl", "吉");
            KEY_CHINESE_MAP.put("zh_liao", "辽");
            KEY_CHINESE_MAP.put("zh_lu", "鲁");
            KEY_CHINESE_MAP.put("zh_meng", "蒙");
            KEY_CHINESE_MAP.put("zh_min", "闽");
            KEY_CHINESE_MAP.put("zh_ning", "宁");
            KEY_CHINESE_MAP.put("zh_qing", "青");
            KEY_CHINESE_MAP.put("zh_qiong", "琼");
            KEY_CHINESE_MAP.put("zh_shan", "陕");
            KEY_CHINESE_MAP.put("zh_su", "苏");
            KEY_CHINESE_MAP.put("zh_sx", "晋");
            KEY_CHINESE_MAP.put("zh_wan", "皖");
            KEY_CHINESE_MAP.put("zh_xiang", "湘");
            KEY_CHINESE_MAP.put("zh_xin", "新");
            KEY_CHINESE_MAP.put("zh_yu", "豫");
            KEY_CHINESE_MAP.put("zh_yu1", "渝");
            KEY_CHINESE_MAP.put("zh_yue", "粤");
            KEY_CHINESE_MAP.put("zh_yun", "云");
            KEY_CHINESE_MAP.put("zh_zang", "藏");
            KEY_CHINESE_MAP.put("zh_zhe", "浙");
        }
    }


}
