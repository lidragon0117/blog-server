package com.lilong.blog.vo.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * @author: lilong
 * @date: 2025/1/3
 * @description:
 */
@Data
public class OnlineUserVo extends SysUserVo {

    @ApiModelProperty(value = "token")
    private String tokenValue;

}
