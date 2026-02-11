package com.lilong.blog.remote.system;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : lilong
 * @date : 2026-02-09 17:42
 * @description :
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryRoleRequest {

    private Long userId;

}
