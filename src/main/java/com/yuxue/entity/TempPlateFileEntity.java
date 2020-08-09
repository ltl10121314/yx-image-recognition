package com.yuxue.entity;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * temp_plate_file
 * @author yuxue
 * 2020-04-30 09:39:59.928
 */
@Data
@NoArgsConstructor
public class TempPlateFileEntity implements Serializable {
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
    private Long fileLength;

    /**
     * parentId
     */
    private Integer parentId;

    /**
     * level
     */
    private Integer level;

    private static final long serialVersionUID = 1L;
}