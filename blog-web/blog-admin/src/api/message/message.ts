import request from '@/utils/request'

// 获取留言列表
export function getMessageListApi(params: any) {
  return request({
    url: '/act/message/list',
    method: 'get',
    params
  })
}

// 删除留言
export function deleteMessageApi(ids: any) {
  return request({
    url: `/act/message/delete/${ids}`,
    method: 'delete'
  })
}