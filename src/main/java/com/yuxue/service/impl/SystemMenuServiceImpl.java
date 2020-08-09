package com.yuxue.service.impl;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.yuxue.entity.SystemMenuEntity;
import com.yuxue.mapper.SystemMenuMapper;
import com.yuxue.service.SystemMenuService;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 * @author yuxue
 * @date 2019-06-20 16:15:23
 */
@Service
public class SystemMenuServiceImpl  implements SystemMenuService {

    @Autowired
    private SystemMenuMapper systemMenuMapper;
    

    @Override
    public SystemMenuEntity getByPrimaryKey(Integer id) {
        SystemMenuEntity entity = systemMenuMapper.selectByPrimaryKey(id);
        return entity;
    }
    
    @Override
    public PageInfo<SystemMenuEntity> queryByPage(Integer pageNo, Integer pageSize, Map<String, Object> map) {
    	PageHelper.startPage(pageNo, pageSize);
		PageInfo<SystemMenuEntity> page = new PageInfo(systemMenuMapper.selectByCondition(map));
		return page;
    }
    
    @Override
	public List<SystemMenuEntity> queryByCondition(Map<String, Object> map) {
		return systemMenuMapper.selectByCondition(map);
	}
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Map<String, Object> save(SystemMenuEntity entity) {
    	entity.setId(0);
    	systemMenuMapper.insertSelective(entity);
    	
    	Map<String, Object> result = new HashMap<>();
    	result.put("id" , entity.getId());
    	return result;
    }

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Integer deleteById(Integer id){
		return systemMenuMapper.deleteByPrimaryKey(id);
	}

	@Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Integer updateById(SystemMenuEntity systemMenuEntity) {
    	if(null == systemMenuEntity || systemMenuEntity.getId() <= 0){
    		return 0;
    	}
    	return systemMenuMapper.updateByPrimaryKeySelective(systemMenuEntity);
    }
	
	
	@Override
    public Object getUserMenu() {
        Map<String, Object> map = Maps.newHashMap();
        //根据角色查询菜单--未完成 //根据层级 sort排序
        map.put("showFlag", 1);
        List<SystemMenuEntity> menus = systemMenuMapper.selectByCondition(map);
        
        //按层级封装，最多三级
        Map<String, Object> result = Maps.newHashMap();
        
        result.put("first", menus.stream().filter(n -> {
            return n.getMenuLevel() == 1;
        }));
        result.put("second", menus.stream().filter(n -> {
            return n.getMenuLevel() == 2;
        }));
        result.put("third", menus.stream().filter(n -> {
            return n.getMenuLevel() == 3;
        }));
        return result;
    }
}
