package com.lilong.blog.remote.system;

import lombok.Builder;
import lombok.Data;

/**
 * @author : lilong
 * @date : 2026-02-09 18:02
 * @description :
 */

@Data
@Builder
public class QueryPermissionRequest {
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 权限类型
     */
    private String type;
}
