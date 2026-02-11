package com.lilong.blog.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author : lilong
 * @date : 2026-02-09 17:43
 * @description :
 */
@Data
public class SysRoleVo {

    private Integer id;

    @ApiModelProperty(value = "角色编码")
    private String code;

    @ApiModelProperty(value = "角色名称")
    private String name;

    @ApiModelProperty(value = "角色描述")
    private String remarks;

}
