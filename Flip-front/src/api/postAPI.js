import request from "@/utils/request";

/*发布帖子*/
export function doPost(title, content, tags) {
    const data = {
        title,
        content,
        tags,
    };
    return request({
        url: '/post',
        headers: {
            requireToken: true
        },
        data: data,
        method: 'post'
    })
}

/*修改帖子内容*/
export function doPostEditedContentAPI(pid, title, content) {
    const data = {
        id: pid,
        title: title,
        content: content
    };
    return request({
        url: '/postContent',
        headers: {
            requireToken: true
        },
        data: data,
        method: 'put'
    })
}

/*修改帖子标题*/
export function doPostEditedTitleAPI(pid, title) {
    return request({
        url: '/postTitle',
        headers: {
            requireToken: true
        },
        params: {
            pid: pid,
            title: title
        },
        method: 'put'
    })
}

export function doChangeTagOfPostAPI(pid, tags) {
    return request({
        url: '/tagOfPost',
        data: {
            id: pid,
            tags,
        },
        method: 'put',
        headers: {
            requireToken: true,
        }
    })
}

/*帖子详情*/
export function getPostInfoAPI(pid) {
    return request({
        url: '/postInfo',
        params: {
            pid: pid
        },
        method: 'get'
    })
}

/*获取最新主题列表*/
export function getLatestPostsAPI(currentPage) {
    return request({
        url: '/list',
        headers: {
            requireToken: false
        },
        params: {
            node: 'latestPost',
            page: currentPage,
        },
        method: 'get'
    })
}

/*获取全部主题*/
export function getAllPostsAPI(currentPage) {
    return request({
        url: '/list',
        headers: {
            requireToken: false
        },
        params: {
            node: 'allPost',
            page: currentPage,
        },
        method: 'get',
    })
}

/*获取热门主题*/
export function getHotPostsAPI(currentPage) {
    return request({
        url: 'list',
        headers: {
            requireToken: false
        },
        params: {
            node: 'hotPost',
            page: currentPage,
        },
        method: 'get',
    })
}