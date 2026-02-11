import request from '@/utils/request'

export function getHotListApi(type) {
    return request({
        url: `/client/getHotSearch/${type}`,
        method: 'get'
    })
}