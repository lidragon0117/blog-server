package com.lilong.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lilong.blog.remote.system.QueryPermissionRequest;
import com.lilong.blog.vo.menu.RouterVO;
import com.lilong.system.entity.SysMenu;

import java.util.List;

public interface SysMenuService extends IService<SysMenu> {
    /**
     * 获取菜单树
     */
    List<SysMenu> getMenuTree();
    
    /**
     * 新增菜单
     */
    void addMenu(SysMenu menu);
    
    /**
     * 更新菜单
     */
    void updateMenu(SysMenu menu);
    
    /**
     * 删除菜单
     */
    void deleteMenu(Integer id);

    /**
     * 获取当前登录用户所拥有的菜单
     * @return
     */
    List<RouterVO> getCurrentUserMenu();
    /**
     * 根据条件获取权限列表
     * @param permissionRequest
     * @return
     */
    List<String> selectPermissionList(QueryPermissionRequest permissionRequest);
}