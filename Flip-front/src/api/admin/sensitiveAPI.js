import request from "../../utils/request";

export function addSensitiveWordAPI(word) {
    const data = {
        word,
    }
    return request({
        url: '/sys-ctrl/sensitiveWord',
        method: 'post',
        data,
        headers: {
            requireToken: true
        }
    })
}

export function getSensitiveWordsAPI() {
    return request({
        url: '/sys-ctrl/sensitiveWord',
        method: 'get',
        headers: {
            requireToken: true
        }
    })
}

export function updateSensitiveWordAPI(sensitive) {
    const data = {
        id: sensitive.id,
        word: sensitive.word,
    }
    return request({
        url: '/sys-ctrl/sensitiveWord',
        method: 'put',
        data,
        headers: {
            requireToken: true
        }
    })
}