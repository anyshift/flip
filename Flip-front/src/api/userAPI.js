import request from "@/utils/request";

export function getProfileAPI() {
    return request({
        url: '/profile',
        headers: {
            requireToken: true
        },
        method: 'get'
    })
}

export function getUserProfileAPI(username) {
    return request({
        url: '/userProfile',
        params: {
            username,
        },
        method: 'get'
    })
}

export function getUserPostsAPI(username) {
    return request({
        url: '/userPosts',
        params: {
            username,
        },
        method: 'get'
    })
}

export function updateNicknameAPI(nickname) {
    return request({
        url: '/updateNickname',
        params: {
            nickname: nickname
        },
        headers: {
            requireToken: true,
        },
        method: 'get'
    })
}


export function updatePasswordAPI(currentPassword, newPassword, confirmNewPassword) {
    return request({
        url: '/updatePassword',
        data: {
            currentPassword: currentPassword,
            newPassword: newPassword,
            confirmNewPassword: confirmNewPassword,
        },
        headers: {
            requireToken: true,
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        method: 'post'
    })
}