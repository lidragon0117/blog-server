import request from '@/utils/request'

// 查询定时任务列表
export function listJobApi(params?: any) {
  return request({
    url: '/tool/monitor/job/list',
    method: 'get',
    params
  })
}

// 查询定时任务详细
export function getJobApi(jobId: string | number) {
  return request({
    url: '/tool/monitor/job/' + jobId,
    method: 'get'
  })
}

// 新增定时任务
export function addJobApi(data: any) {
  return request({
    url: '/tool/monitor/job',
    method: 'post',
    data
  })
}

// 修改定时任务
export function updateJobApi(data: any) {
  return request({
    url: '/tool/monitor/job',
    method: 'put',
    data
  })
}

// 删除定时任务
export function delJobApi(id: any) {
  return request({
    url: `/tool/monitor/job/delete/${id}`,
    method: 'delete'
  })
}

// 导出定时任务
export function exportJobApi(params?: any) {
  return request({
    url: '/tool/monitor/job/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 定时任务状态修改
export function changeJobStatusApi(jobId: string | number, status: string) {
  return request({
    url: '/tool/monitor/job/changeStatus',
    method: 'put',
    data: { jobId, status }
  })
}

// 定时任务立即执行一次
export function runJobApi(job: any) {
  return request({
    url: '/tool/monitor/job/run',
    method: 'put',
    data: job
  })
}
