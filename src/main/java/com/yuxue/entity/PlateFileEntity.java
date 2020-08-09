package com.yuxue.entity;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * t_plate_file
 * @author yuxue
 * 2020-04-30 11:04:47.169
 */
@Data
@NoArgsConstructor
public class PlateFileEntity implements Serializable {
    /**
     * id
     */
    private Integer id;

    /**
     * fileName
     */
    private String fileName;

    /**
     * filePath
     */
    private String filePath;

    /**
     * fileType
     */
    private String fileType;

    /**
     * fileLength
     */
    private Integer fileLength;

    /**
     * plate
     */
    private String plate;

    /**
     * plateColor
     */
    private String plateColor;

    /**
     * lastRecoTime
     */
    private String lastRecoTime;

    /**
     * tempPath
     */
    private String tempPath;

    /**
     * recoPlate
     */
    private String recoPlate;

    /**
     * recoColor
     */
    private String recoColor;

    /**
     * recoCorrect
     * 0未识别 1正确 2错误 3未检测到车牌
     */
    private Integer recoCorrect;
    
    private List<PlateRecoDebugEntity> debug;

    private static final long serialVersionUID = 1L;
}