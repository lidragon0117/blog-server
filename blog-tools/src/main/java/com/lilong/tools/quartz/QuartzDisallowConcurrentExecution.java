package com.lilong.tools.quartz;

import com.lilong.tools.entity.SysJob;
import com.lilong.tools.utils.JobInvokeUtils;
import org.quartz.JobExecutionContext;

/**
 * @author lilong
 * @date 2021/12/8
 * @apiNote 定时任务处理（禁止并发执行）
 */
public class QuartzDisallowConcurrentExecution extends AbstractQuartzJob {
    @Override
    protected void doExecute(JobExecutionContext context, SysJob job) throws Exception {
        JobInvokeUtils.invokeMethod(job);
    }
}
