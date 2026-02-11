import request from '@/utils/request'


/**
 * 获取友链列表
 * @returns 
 */
export function getFriendsApi() {
    return request({
        url: '/client/friend/list',
        method: 'get'
    })
}

/**
 * 申请友链
 * @returns 
 */
export function applyFriendApi(data) {
    return request({
        url: '/client/friend/apply',
        method: 'post',
        data
    })
}