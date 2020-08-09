package com.yuxue.service.impl;

import java.io.File;
import java.util.List;

import org.springframework.stereotype.Service;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.yuxue.constant.Constant;
import com.yuxue.exception.ResultReturnException;
import com.yuxue.service.FileService;
import com.yuxue.util.FileUtil;


@Service
public class FileServiceImpl implements FileService {
    
    @Override
    public List<JSONObject> getFileTreeByDir(String dir, String typeFilter) {
        if(StringUtils.isEmpty(dir)){
            dir = Constant.DEFAULT_DIR;
        }
        if(StringUtils.isEmpty(typeFilter)){
            typeFilter = Constant.DEFAULT_TYPE;
        }

        File f = new File(dir);
        List<File> list = FileUtil.listFile(f, typeFilter, false);
        List<JSONObject> result = Lists.newArrayList();
        list.stream().forEach(n->{
            JSONObject jo = new JSONObject();
            jo.put("id", n.getAbsolutePath());
            jo.put("pid", n.getParentFile().getAbsolutePath());
            jo.put("filePath", n.getAbsolutePath());
            jo.put("fileName", n.getName());
            jo.put("isDir", n.isDirectory());
            result.add(jo);
        });
        return result;
    }
    

    @Override
    public File readFile(String filePath) {
        
        File f = new File(filePath);
        if(!f.exists() || f.isDirectory()) {
            throw new ResultReturnException("filePath参数异常，找不到指定的文件: " + filePath);
        }

        if(!f.exists() || f.isDirectory()) {
            throw new ResultReturnException("读取图片异常：" + f.getName());
        }
        return f;
    }




}
