import request from '@/utils/request'

// 获取评论列表
export function getCommentListApi(params: any) {
  return request({
    url: '/act/comment/list',
    method: 'get',
    params
  })
}

// 删除评论
export function deleteCommentApi(ids: any) {
  return request({
    url: `/act/comment/delete/${ids}`,
    method: 'delete'
  })
}