package com.yuxue.service;

import java.io.File;
import java.util.List;

import com.alibaba.fastjson.JSONObject;


public interface FileService {
    
    List<JSONObject> getFileTreeByDir(String dir, String typeFilter);
    
    File readFile(String filePath);
    
    
}