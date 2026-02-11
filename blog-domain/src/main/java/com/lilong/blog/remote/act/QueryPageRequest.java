package com.lilong.blog.remote.act;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : lilong
 * @date : 2026-02-10 21:13
 * @description :
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class QueryPageRequest<T> {
    /**
     * 分页参数
     */
    private Page<Object> page;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 其他排序
     */
    private String sortType;
    /**
     * 具体实体类
     */
    private T dto;
}
