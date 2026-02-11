package com.lilong.blogact.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lilong.blog.helper.CurrentUserHelper;
import com.lilong.blog.utils.PageUtil;
import com.lilong.blogact.entity.SysMoment;
import com.lilong.blogact.mapper.SysMomentMapper;
import com.lilong.blogact.service.SysMomentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 说说 服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysMomentServiceImpl extends ServiceImpl<SysMomentMapper, SysMoment> implements SysMomentService {

    /**
     * 查询说说分页列表
     */
    @Override
    public IPage<SysMoment> selectPage(SysMoment sysMoment) {
        return page(PageUtil.getPage(), new LambdaQueryWrapper<SysMoment>()
                .orderByDesc(SysMoment::getCreateTime));
    }

    @Override
    public Object add(SysMoment sysMoment) {
        sysMoment.setUserId(CurrentUserHelper.getUserId());
        return save(sysMoment);
    }
}
