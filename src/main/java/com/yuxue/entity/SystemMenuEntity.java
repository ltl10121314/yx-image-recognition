package com.yuxue.entity;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * t_system_menu
 * @author 
 */
@Data
@NoArgsConstructor
public class SystemMenuEntity implements Serializable {
    private Integer id;

    private String menuName;

    private String menuUrl;

    private Integer parentId;

    private Integer sort;

    private Integer menuLevel;

    private String menuIcon;

    private Integer showFlag;

    private Integer platform;

    private Integer menuType;

    private String permission;

    private Date updateTime;

    private Integer editorId;

    private String createTime;

    private Integer creatorId;

    private Integer version;

    private Integer delFlag;

    private static final long serialVersionUID = 1L;


}