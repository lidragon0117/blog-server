package com.lilong.blog.dto.user;

import com.lilong.blog.vo.user.SysUserVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "新增用户参数")
public class SysUserAddAndUpdateDto {

    @ApiModelProperty(value = "用户信息")
    private SysUserVo user;

    @ApiModelProperty(value = "角色ID列表")
    private List<Integer> roleIds;
}
