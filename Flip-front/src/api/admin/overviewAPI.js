import request from "../../utils/request";

export function getSystemAPI() {
    return request({
        url: '/systemInfo',
        method: 'get',
        headers: {
            requireToken: true,
        }
    })
}

export function getForumAPI() {
    return request({
        url: '/forumInfo',
        method: 'get',
    })
}