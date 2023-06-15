import request from "@/utils/request";

/**
 * 根级评论
 * @param pid 评论的帖子ID
 * @param fromUid 评论者的UID
 * @param content 评论的内容
 * @returns {*}
 */
export function doCommentAPI(pid, fromUid, content) {
    const data = {
        pid: pid,
        fromUid: fromUid,
        content: content,
    }
    return request({
        url: '/doComment',
        headers: {
            requireToken: true
        },
        data: data,
        method: 'post'
    })
}

/**
 * 楼中楼回复
 * @param pid 帖子ID
 * @param fromUid 回复者UID
 * @param toUid 被回复者UID
 * @param content 回复内容
 * @param parentId 父级ID
 * @param replyId 回复ID
 * @returns {*}
 */
export function doReplyAPI(pid, fromUid, toUid, content, parentId, replyId) {
    const data = {
        pid,
        fromUid,
        toUid,
        content,
        parentId,
        replyId,
    }
    return request({
        url: '/doReply',
        headers: {
            requireToken: true
        },
        data,
        method: 'post',
    })
}

/**
 * 获取评论列表(不获取回复)
 * @param pid 帖子ID
 * @returns {*}
 */
export function getCommentsAPI(pid) {
    return request({
        url: '/getComments',
        params: {
            pid: pid,
        },
        method: 'get'
    })
}

/**
 * 获取回复列表
 * @param pid 帖子ID
 * @param parentId 父级ID
 * @returns {*}
 */
export function getRepliesAPI(pid, parentId) {
    return request({
        url: '/getReplies',
        params: {
            pid: pid,
            parentId: parentId
        },
        method: 'get'
    })
}