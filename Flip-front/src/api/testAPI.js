import request from "@/utils/request";

export function testGetRoleAPI() {
    return request({
        url: '/testRole',
        method: 'get',
        headers: {
            requireToken: true
        },
    })
}

export function testGetPermission() {
    return request({
        url: '/getPermission',
        method: 'get',
        headers: {
            requireToken: true
        },
    })
}