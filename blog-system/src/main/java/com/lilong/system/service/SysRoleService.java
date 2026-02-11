package com.lilong.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lilong.blog.remote.system.QueryRoleRequest;
import com.lilong.blog.vo.SysRoleVo;
import com.lilong.system.entity.SysRole;

import java.util.List;

public interface SysRoleService extends IService<SysRole> {
    /**
     * 分页查询角色
     */
    IPage<SysRole> listRoles(String name);

    /**
     * 新增角色
     */
    void addRole(SysRole role);

    /**
     * 更新角色
     */
    void updateRole(SysRole role);

    /**
     * 删除角色
     */
    void delete(List<Integer> ids);


    /**
     * 获取角色所拥有的菜单权限
     * @param id
     * @return
     */
    List<Integer> getRoleMenus(Integer id);

    /**
     * 分配角色权限
     * @param id
     * @param menuIds
     * @return
     */
    Boolean updateRoleMenus(Integer id, List<Integer> menuIds);

    List<SysRoleVo> selectRoleList(QueryRoleRequest queryRoleRequest);

    List<String> selectRoleCodesByUserId(QueryRoleRequest queryRoleRequest);
}
