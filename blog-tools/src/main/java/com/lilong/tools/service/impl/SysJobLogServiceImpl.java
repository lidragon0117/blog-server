package com.lilong.tools.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lilong.blog.dto.job.JobLogQuery;
import com.lilong.blog.utils.PageUtil;
import com.lilong.tools.entity.SysJobLog;
import com.lilong.tools.mapper.SysJobLogMapper;
import com.lilong.tools.service.SysJobLogService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SysJobLogServiceImpl extends ServiceImpl<SysJobLogMapper, SysJobLog> implements SysJobLogService {

    @Override
    public Page<SysJobLog> selectJobLogPage(JobLogQuery query) {
        LambdaQueryWrapper<SysJobLog> wrapper = new LambdaQueryWrapper<SysJobLog>()
                .like(StringUtils.isNotBlank(query.getJobName()), SysJobLog::getJobName, query.getJobName())
                .eq(StringUtils.isNotBlank(query.getJobGroup()), SysJobLog::getJobGroup, query.getJobGroup())
                .eq(StringUtils.isNotBlank(query.getStatus()), SysJobLog::getStatus, query.getStatus())
                .orderByDesc(SysJobLog::getCreateTime);

        return page(PageUtil.getPage(), wrapper);
    }

    @Override
    public void addJobLog(SysJobLog jobLog) {
        baseMapper.insert(jobLog);
    }

    @Override
    public void cleanJobLog() {
        baseMapper.cleanJobLog();
    }
}
