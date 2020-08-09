package com.yuxue.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.yuxue.exception.ResultReturnException;
import com.yuxue.service.PlateService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;



@Api(description = "车牌识别")
@RestController
@RequestMapping("/plate")
public class PlateController {

    @Autowired
    private PlateService service;

     
    /**
     * 扫描d:/PlateDetect目录图片的基础信息
     * 将扫描到的信息，更新到数据库
     * 排除temp目录
     */
    @ApiOperation(value = "更新IMG文件基础信息", notes = "")
    @RequestMapping(value = "/refreshFileInfo", method = RequestMethod.GET)
    public void refreshFileInfo() {
        service.refreshFileInfo();
    }

    
    /**
     * 根据数据库的图片基础信息，进行车牌识别
     * 更新图片识别信息到数据库
     * 生成识别结果; 多线程执行
     */
    @ApiOperation(value = "图片车牌识别", notes = "路径不能包含中文，opencv路径转码过程乱码会报异常")
    @RequestMapping(value = "/recogniseAll", method = RequestMethod.GET)
    public Object recogniseAll() {
        return service.recogniseAll();
    }
    
    
    
    /**
     * 车牌识别接口
     * 输入：图片path
     * 处理：识别过程切图，识别结果切图；切图保存到temp/timestamp文件夹，图片文件名按timestamp排序
     *      操作过程结果保存数据库，操作前检查数据库及temp文件夹下是否有对应的切图文件
     * 输出：返回过程切图、识别结果切图文件路径集合
     */
    @ApiOperation(value = "图片车牌识别", notes = "路径不能包含中文，opencv路径转码过程乱码会报异常")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "filePath", value = "文件路径", required = true, paramType = "query", dataType = "String"),
        @ApiImplicitParam(name = "reRecognise", value = "重新识别", paramType = "query", dataType = "Boolean", defaultValue="false")
    })
    @RequestMapping(value = "/recognise", method = RequestMethod.GET)
    public Object recognise(String filePath, Boolean reRecognise) {
        try {
            if(null != filePath) {
                filePath = URLDecoder.decode(filePath, "utf-8");
            }
            if(null == reRecognise) {
                reRecognise = false;
            }
        } catch (UnsupportedEncodingException e) {
            throw new ResultReturnException("filePath参数异常");
        }
        return service.recognise(filePath, reRecognise);
    }
    
    
    @ApiOperation(value = "获取处理步骤", notes = "")
    @RequestMapping(value = "/getProcessStep", method = RequestMethod.GET)
    public Object getProcessStep() {
        return service.getProcessStep();
    }
    
    
    
    
}
