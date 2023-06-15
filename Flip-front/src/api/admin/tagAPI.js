import request from "@/utils/request";

export function getAllTagAPI() {
    return request({
        url: '/tags',
        method: 'get',
    })
}

export function getTagsAndOptionsAPI() {
    return request({
        url: '/tagsAndOptions',
        method: 'get',
    })
}

export function getTagAPI(tagLabel, currentPage) {
    return request({
        url: '/tag',
        params: {
            label: tagLabel,
            page: currentPage,
        },
        method: 'get',
    })
}

export function addTagAPI(tagLabel, tagName, tagIcon, tagOption, tagDesc) {
    const data = {
        label: tagLabel,
        name: tagName,
        icon: tagIcon,
        optionId: tagOption,
        detail: tagDesc,
    };
    return request({
        url: '/sys-ctrl/tag',
        data,
        method: 'post',
        headers: {
            requireToken: true
        }
    })
}

export function updateTagAPI(tag) {
    const data = {
        id: tag.id,
        label: tag.label,
        name: tag.name,
        icon: tag.icon,
        detail: tag.detail,
        optionId: tag.option,
    };
    return request({
        url: '/sys-ctrl/tag',
        data,
        method: 'put',
        headers: {
            requireToken: true
        }
    })
}

export function deleteTagAPI(tag) {
    const data = {
        id: tag.id,
        label: tag.label,
        name: tag.name
    };
    return request({
        url: '/sys-ctrl/tag',
        data,
        method: 'delete',
        headers: {
            requireToken: true
        }
    })
}

export function forceDeleteTagAPI(tag) {
    const data = {
        id: tag.id,
        label: tag.label,
        name: tag.name
    };
    return request({
        url: '/sys-ctrl/tagForce',
        data,
        method: 'delete',
        headers: {
            requireToken: true
        }
    })
}

export function addTagOptionAPI(optionLabel, optionName) {
    const data = {
        label: optionLabel,
        name: optionName,
    }
    return request({
        url: '/sys-ctrl/tagOption',
        data,
        method: 'post',
        headers: {
            requireToken: true,
        }
    })
}

export function getAllTagOptionsAPI() {
    return request({
        url: '/sys-ctrl/tagOption',
        method: 'get',
    })
}

export function updateTagOptionAPI(tagOption) {
    const data = {
        id: tagOption.id,
        label: tagOption.label,
        name: tagOption.name,
    }
    return request({
        url: '/sys-ctrl/tagOption',
        data,
        method: 'put',
        headers: {
            requireToken: true,
        }
    })
}

export function deleteTagOptionAPI(tagOption) {
    const data = {
        id: tagOption.id,
        label: tagOption.label,
        name: tagOption.name,
    }
    return request({
        url: '/sys-ctrl/tagOption',
        data,
        method: 'delete',
        headers: {
            requireToken: true,
        }
    })
}

export function forceDeleteTagOptionAPI(tagOption) {
    const data = {
        id: tagOption.id,
        label: tagOption.label,
        name: tagOption.name,
    }
    return request({
        url: '/sys-ctrl/tagOptionForce',
        data,
        method: 'delete',
        headers: {
            requireToken: true,
        }
    })
}