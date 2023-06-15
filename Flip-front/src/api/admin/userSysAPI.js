import request from "@/utils/request";

export function getAllUserAPI() {
    return request({
        url: '/sys-ctrl/users',
        method: 'get',
        headers: {
            requireToken: true
        }
    })
}

export function bannedUserAPI(form) {
    const data = {
        uid: form.uid,
        deadline: form.datetime,
        reason: form.reason,
    }
    return request({
        url: '/sys-ctrl/banUser',
        method: 'post',
        data,
        headers: {
            requireToken: true
        }
    })
}

export function cancelBanUserAPI(uid) {
    return request({
        url: '/sys-ctrl/banUser',
        method: 'delete',
        params: {
            uid,
        },
        headers: {
            requireToken: true
        }
    })
}