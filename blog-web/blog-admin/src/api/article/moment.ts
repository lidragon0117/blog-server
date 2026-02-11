import request from '@/utils/request'

/**
 * 获取说说列表
 */
export function getSysMomentListApi(params?: any) {
    return request({
        url: '/act/moment/list',
        method: 'get',
        params
    })
}

/**
 * 添加说说
 */
export function addSysMomentApi(data: any) {
    return request({
        url: '/act/moment/add',
        method: 'post',
        data
    })
}

/**
 * 修改说说
 */
export function updateSysMomentApi(data: any) {
    return request({
        url: `/act/moment/update`,
        method: 'put',
        data
    })
}


/**
 * 删除说说
 */
export function deleteSysMomentApi(ids: number[] | number) {
    return request({
        url: `/act/moment/delete/` + ids,
        method: 'delete'
    })
}


