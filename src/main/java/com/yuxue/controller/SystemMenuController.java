package com.yuxue.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yuxue.entity.SystemMenuEntity;
import com.yuxue.service.SystemMenuService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;



@Api(description = "菜单管理")
@RestController
@RequestMapping("/systemMenu")
public class SystemMenuController {

    @Autowired
    private SystemMenuService service;

    /**
     * 分页查询
     * @param pageNo
     * @param pageSize
     * @param entity
     */
    @ApiOperation(value = "分页获取记录", notes = "分页获取记录")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageNo", value = "当前页码", required = true, paramType = "query", dataType = "Integer", defaultValue = "1"),
        @ApiImplicitParam(name = "pageSize", value = "每页数量", required = true, paramType = "query", dataType = "Integer", defaultValue = "10"),
        @ApiImplicitParam(name = "map", value = "举例：{} or {\"name\":\"张三\"}", dataType = "entity")
    })
    @RequestMapping(value = "/queryByPage", method = RequestMethod.POST)
    public Object queryByPage(@RequestParam Integer pageNo, @RequestParam Integer pageSize, @RequestBody Map<String, Object> map) {
        return service.queryByPage(pageNo, pageSize, map);
    }
    
    @ApiOperation(value = "按条件查询", notes = "不分页", response = SystemMenuEntity.class)
    @ApiImplicitParams({
        @ApiImplicitParam(name = "map", value = "举例：{} or {\"name\":\"张三\"}", dataType = "entity")
    })
    @RequestMapping(value = "/queryByCondition", method = RequestMethod.POST)
    public Object queryByCondition(@RequestBody Map<String, Object> map) {
        return service.queryByCondition(map);
    }
    
    
    /**
     * Post请求，新增数据，成功返回ID
     * @param entity
     */
    @ApiOperation(value = "新增数据，成功返回ID", notes = "新增数据，成功返回ID")
    @ApiImplicitParam(name = "entity", value = "举例：{} or {\"name\":\"张三\"}", required = true, dataType = "entity")
    @RequestMapping(value = "", method = RequestMethod.POST)
    public Object save(@RequestBody SystemMenuEntity entity) {
        return service.save(entity);
    }
    
    
    /**
     * 获取登录用户的权限下菜单
     * @return
     */
    @ApiOperation(value = "获取登录用户菜单", notes = "")
    @GetMapping("/getUserMenu")
    public Object getUserMenu() {
        return service.getUserMenu();
    }
    



}

