import request from '@/utils/request'

/**
 * 获取相册列表
 */
export function listAlbumApi(params?: any) {
    return request({
        url: '/act/album/list',
        method: 'get',
        params
    })
}

/**
 * 获取所有相册
 */
export function listAlbumAllApi() {
    return request({
        url: '/act/album/all',
        method: 'get'
    })
}
/**
 * 获取相册详情
 */
export function detailAlbumApi(id: any) {
    return request({
        url: '/act/album/' + id,
        method: 'get'
    })
}

/**
 * 添加相册
 */
export function addAlbumApi(data: any) {
    return request({
        url: '/act/album/add',
        method: 'post',
        data
    })
}

/**
 * 修改相册
 */
export function updateAlbumApi(data: any) {
    return request({
        url: `/act/album/update`,
        method: 'put',
        data
    })
}


/**
 * 删除相册
 */
export function deleteAlbumApi(ids: number[] | number) {
    return request({
        url: `/act/album/delete/` + ids,
        method: 'delete'
    })
}


/**
 * 验证相册密码
 */
export function verifyAlbumPasswordApi(id : any, password:any) {
    return request({
      url: `/api/album/verify/${id}`,
      method: 'get',
      params: {
        password: password
      }
    })
  }



