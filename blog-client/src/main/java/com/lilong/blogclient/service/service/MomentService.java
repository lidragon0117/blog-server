package com.lilong.blogclient.service.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lilong.blog.vo.moment.MomentPageVo;


/**
 * @author: lilong
 * @date: 2025/2/5
 * @description:
 */
public interface MomentService {
    IPage<MomentPageVo> getMomentList();

}
