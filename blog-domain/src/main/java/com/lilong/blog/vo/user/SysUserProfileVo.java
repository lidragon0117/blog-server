package com.lilong.blog.vo.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "个人信息")
public class SysUserProfileVo {

    @ApiModelProperty(value = "用户信息")
    private SysUserVo sysUser;

    @ApiModelProperty(value = "角色")
    private List<String> roles;
}
