package com.yuxue.service;

import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageInfo;
import com.yuxue.entity.SystemMenuEntity;


/**
 * 服务实现层接口
 * @author yuxue
 * @date 2019-06-20 16:15:23
 */
public interface SystemMenuService {
	
    public SystemMenuEntity getByPrimaryKey(Integer id);
    
    public PageInfo<SystemMenuEntity> queryByPage(Integer pageNo, Integer pageSize, Map<String, Object> map);
    
    public List<SystemMenuEntity> queryByCondition(Map<String, Object> map);
    
    public Map<String, Object> save(SystemMenuEntity systemMenuEntity);

	public Integer deleteById(Integer id);

    public Integer updateById(SystemMenuEntity systemMenuEntity);
    
    public Object getUserMenu();
}
