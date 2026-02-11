package com.lilong.blogact.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lilong.blog.enums.FriendStatusEnum;
import com.lilong.blog.utils.PageUtil;
import com.lilong.blogact.entity.SysFriend;
import com.lilong.blogact.mapper.SysFriendMapper;
import com.lilong.blogact.service.SysFriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class SysFriendServiceImpl extends ServiceImpl<SysFriendMapper, SysFriend> implements SysFriendService {

    @Override
    public IPage<SysFriend> selectPage(SysFriend sysFriend) {
        LambdaQueryWrapper<SysFriend> wrapper = new LambdaQueryWrapper<SysFriend>()
                .eq(sysFriend.getName() != null, SysFriend::getName, sysFriend.getName())
                .eq(sysFriend.getStatus() != null, SysFriend::getStatus, sysFriend.getStatus());
        return page(PageUtil.getPage(), wrapper);
    }

    /**
     * 修改友情链接
     */
    @Override
    public boolean update(SysFriend sysFriend) {
        return updateById(sysFriend);
    }

    @Override
    public List<SysFriend> selectList() {
        return baseMapper.selectList(new LambdaQueryWrapper<SysFriend>()
                .select(SysFriend::getId, SysFriend::getName, SysFriend::getInfo, SysFriend::getAvatar
                        , SysFriend::getUrl)
                .eq(SysFriend::getStatus, FriendStatusEnum.UP.getCode())
                .orderByAsc(SysFriend::getSort));
    }
}
