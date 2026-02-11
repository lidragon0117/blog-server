package com.lilong.blogrpc.act;

import com.lilong.blog.base.Result;
import com.lilong.blog.vo.article.CategoryListVo;
import com.lilong.blog.vo.article.SysCategoryVo;
import com.lilong.blogrpc.interceptor.RpcRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @author : lilong
 * @date : 2026-02-10 20:36
 * @description :
 */
@FeignClient(name = "blog-act-service", contextId = "categoryServiceRpc", configuration = RpcRequestInterceptor.class)
public interface CategoryServiceRpc {

    /**
     * 获取文章分类
     *
     * @return
     */
    @GetMapping("/act/category/selectArticleCategories")
    Result<List<CategoryListVo>> getArticleCategories();

    /**
     * 获取所有分类
     *
     * @return
     */
    @GetMapping("/act/category/all")
    Result<List<SysCategoryVo>> selectAllCategoryList();
}
