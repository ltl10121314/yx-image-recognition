package com.yuxue.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.yuxue.annotation.RetExclude;
import com.yuxue.exception.ResultReturnException;
import com.yuxue.service.FileService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;




@Api(description = "文件管理")
@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService service;

    
    /**
     * 加载文件树结构
     * 输入：文件夹路径，缺省值：D:\\PlateDetect\\ 文件类型，缺省值：png,jpg,jpeg
     * 输出：当前目录下第一层级文件的list
     * @param dir
     * @return
     */
    @ApiOperation(value = "获取文件结构", notes = "")
    @ApiImplicitParam(name = "dir", value = "文件夹路径", required = true, paramType = "query", dataType = "String")
    @RequestMapping(value = "/getFileTreeByDir", method = RequestMethod.GET)
    public Object getFileTreeByDir(String dir, String typeFilter) {
        try {
            if(null != dir) {
                dir = URLDecoder.decode(dir, "utf-8");
            }
        } catch (UnsupportedEncodingException e) {
            throw new ResultReturnException("dir参数异常");
        }
        return service.getFileTreeByDir(dir, typeFilter);
    }
    
    
    /**
     * 预览图片文件
     * @param filePath
     * @param response
     * @return
     * @throws IOException
     */
    @RetExclude
    @ApiOperation(value = "预览文件", notes = "根据路径，直接读取盘符文件; 返回输出流")
    @GetMapping(value = "/readFile", produces= {"image/jpeg"})
    public ResponseEntity<InputStreamResource> readFile(String filePath, HttpServletResponse response) throws IOException {
        try {
            filePath = URLDecoder.decode(filePath, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new ResultReturnException("filePath参数异常");
        }
        //文件输出流，输出到客户端
        File file = service.readFile(filePath);
        InputStreamResource isr = new InputStreamResource(new FileInputStream(file));
        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<InputStreamResource>(isr, headers, HttpStatus.OK);
    }
    
    
    
    
    
}
