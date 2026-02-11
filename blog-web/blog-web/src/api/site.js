import request from '@/utils/request'


export function getWebConfigApi() {
    return request({
        url: '/client/webConfig',
        method: 'get'
    })
}


export function getNoticeApi() {
    return request({
        url: '/client/getNotice',
        method: 'get'
    })
}
export function reportApi() {
    return request({
        url: '/client/report',
        method: 'get'
    })
}