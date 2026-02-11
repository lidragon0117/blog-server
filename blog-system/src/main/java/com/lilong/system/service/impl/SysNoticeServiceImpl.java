package com.lilong.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lilong.blog.base.Constants;
import com.lilong.blog.enums.NoticePosttionEnum;
import com.lilong.blog.exception.ServiceException;
import com.lilong.blog.utils.PageUtil;
import com.lilong.system.entity.SysNotice;
import com.lilong.system.mapper.SysNoticeMapper;
import com.lilong.system.service.SysNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 公告 服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice>
        implements SysNoticeService {

    /**
     * 查询公告分页列表
     */
    @Override
    public IPage<SysNotice> selectPage(SysNotice sysNotice) {
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        // 构建查询条件
        wrapper.like(sysNotice.getContent() != null, SysNotice::getContent, sysNotice.getContent());
        wrapper.eq(sysNotice.getIsShow() != null, SysNotice::getIsShow, sysNotice.getIsShow());
        wrapper.eq(sysNotice.getPosition() != null, SysNotice::getPosition, sysNotice.getPosition());
        return page(PageUtil.getPage(), wrapper);
    }

    /**
     * 查询公告列表
     */
    @Override
    public List<SysNotice> selectList(SysNotice sysNotice) {
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        // 构建查询条件
        wrapper.eq(sysNotice.getId() != null, SysNotice::getId, sysNotice.getId());
        wrapper.eq(sysNotice.getContent() != null, SysNotice::getContent, sysNotice.getContent());
        wrapper.eq(sysNotice.getIsShow() != null, SysNotice::getIsShow, sysNotice.getIsShow());
        wrapper.eq(sysNotice.getPosition() != null, SysNotice::getPosition, sysNotice.getPosition());
        wrapper.eq(sysNotice.getCreateTime() != null, SysNotice::getCreateTime, sysNotice.getCreateTime());
        return list(wrapper);
    }

    /**
     * 新增公告
     */
    @Override
    public boolean insert(SysNotice sysNotice) {
        if (sysNotice.getIsShow() == Constants.YES && sysNotice.getPosition().equals(NoticePosttionEnum.TOP.getCode())) {
            SysNotice one = baseMapper.selectOne(new LambdaQueryWrapper<SysNotice>()
                    .eq(SysNotice::getPosition, sysNotice.getPosition())
                    .eq(SysNotice::getIsShow,sysNotice.getIsShow()));
            if(one != null) {
                throw new ServiceException("显示的顶部公告只能有一个!");
            }
        }
        return save(sysNotice);
    }

    /**
     * 修改公告
     */
    @Override
    public boolean update(SysNotice sysNotice) {

        if (sysNotice.getIsShow() == Constants.YES && sysNotice.getPosition().equals(NoticePosttionEnum.TOP.getCode())) {
            SysNotice one = baseMapper.selectOne(new LambdaQueryWrapper<SysNotice>()
                    .eq(SysNotice::getPosition, sysNotice.getPosition())
                    .eq(SysNotice::getIsShow,sysNotice.getIsShow()));
            if(one != null && !one.getId().equals(sysNotice.getId())) {
                throw new ServiceException("显示的顶部公告只能有一个!");
            }
        }
        return updateById(sysNotice);
    }

    /**
     * 批量删除公告
     */
    @Override
    public boolean deleteByIds(List<Long> ids) {
        return removeByIds(ids);
    }
}
