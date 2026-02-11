import request from '@/utils/request'

// 获取标签列表
export function getTagListApi(params: any) {
  return request({
    url: '/act/tag/list',
    method: 'get',
    params
  })
}

// 新增标签
export function addTagApi(data: any) {
  return request({
    url: '/act/tag',
    method: 'post',
    data
  })
}

// 修改标签
export function updateTagApi(data: any) {
  return request({
    url: '/act/tag',
    method: 'put',
    data
  })
}

// 删除标签
export function deleteTagApi(ids: any) {
  return request({
    url: `/act/tag/delete/${ids}`,
    method: 'delete'
  })
}