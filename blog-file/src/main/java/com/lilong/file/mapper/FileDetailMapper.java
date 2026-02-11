package com.lilong.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lilong.file.entity.FileDetail;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件记录表 Mapper接口
 */
@Mapper
public interface FileDetailMapper extends BaseMapper<FileDetail> {
}
