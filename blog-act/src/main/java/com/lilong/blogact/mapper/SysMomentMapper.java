package com.lilong.blogact.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lilong.blog.vo.moment.MomentPageVo;
import com.lilong.blogact.entity.SysMoment;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author: lilong
 * @date: 2025/2/5
 * @description:
 */
@Mapper
public interface SysMomentMapper extends BaseMapper<SysMoment> {


    IPage<MomentPageVo> selectPage(IPage<SysMoment> page);
}
