package com.lilong.blogclient.service.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lilong.blog.utils.PageUtil;
import com.lilong.blog.vo.moment.MomentPageVo;
import com.lilong.blogclient.service.service.MomentService;
import com.lilong.blogrpc.act.MomentServiceRpc;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author: lilong
 * @date: 2025/2/5
 * @description:
 */
@Service
@RequiredArgsConstructor
public class MomentServiceImpl implements MomentService {

    private final MomentServiceRpc momentServiceRpc;

    @Override
    public IPage<MomentPageVo> getMomentList() {
        return momentServiceRpc.selectPage(PageUtil.getPage()).getData();
    }
}
