package com.flip.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flip.domain.entity.Post;
import com.flip.domain.entity.PostTag;
import com.flip.domain.entity.User;
import com.flip.mapper.PostMapper;
import com.flip.mapper.PostTagMapper;
import com.flip.mapper.UserMapper;
import com.flip.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    private final PostMapper postMapper;

    private final PostTagMapper postTagMapper;

    private final UserMapper userMapper;

    @Override
    public void savePostTag(PostTag postTag) {
        postTagMapper.insert(postTag);
    }

    /**
     * 获取最新主题列表
     * @param currentPage 当前页码
     * @return 最新主题列表
     */
    @Override
    public Map<String, Object> getLatestPostList(Integer currentPage) {
        QueryWrapper<Post> postQueryWrapper = new QueryWrapper<>();
        postQueryWrapper.select("id, uid, title, create_time as createTime, priority, status, type, reply_number as replyNumber, view_number as viewNumber, last_comment_time as lastCommentTime")
                .orderByDesc("createTime");

        return getPostList(currentPage, postQueryWrapper);
    }

    @Override
    public Map<String, Object> getALlPostList(Integer currentPage) {
        QueryWrapper<Post> postQueryWrapper = new QueryWrapper<>();
        postQueryWrapper.select("id, uid, title, create_time as createTime, priority, status, type, reply_number as replyNumber, view_number as viewNumber, last_comment_time as lastCommentTime")
                .orderByDesc("lastCommentTime");

        return getPostList(currentPage, postQueryWrapper);
    }

    @Override
    public Map<String, Object> getHotPostList(Integer currentPage) {
        QueryWrapper<Post> postQueryWrapper = new QueryWrapper<>();
        postQueryWrapper.select("id, uid, title, create_time as createTime, priority, status, type, reply_number as replyNumber, view_number as viewNumber, last_comment_time as lastCommentTime")
                .gt("reply_number", 0).orderByDesc("replyNumber").orderByDesc("lastCommentTime");

        return getPostList(currentPage, postQueryWrapper);
    }

    private Map<String, Object> getPostList(Integer currentPage, QueryWrapper<Post> postQueryWrapper) {
        int current = ObjectUtil.isNull(currentPage) ? 1 : currentPage;
        long sizePerPage = 30L;

        IPage<Map<String, Object>> page = new Page<>(current, sizePerPage);
        IPage<Map<String, Object>> mapsPage = postMapper.selectMapsPage(page, postQueryWrapper);
        List<Map<String, Object>> maps = mapsPage.getRecords();

        List<Map<String, Object>> posts = new ArrayList<>();
        User user;

        for (Map<String, Object> map : maps) {

            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.select("username, nickname, avatar").eq("uid", map.get("uid"));
            user = userMapper.selectOne(userQueryWrapper);

            map.put("author", user.getUsername());
            map.put("nickname", user.getNickname());
            map.put("avatar", user.getAvatar());

            posts.add(map);
        }

        Map<String, Object> mapTransfer = new HashMap<>();
        mapTransfer.put("posts", posts);
        mapTransfer.put("totalPage", mapsPage.getPages()); /*总页码数*/
        mapTransfer.put("currentPage", mapsPage.getCurrent()); /*当前页码*/
        mapTransfer.put("sizePerPage", sizePerPage); /*每页显示多少条帖子*/
        mapTransfer.put("totalItems", mapsPage.getTotal()); /*总帖子数*/

        return mapTransfer;
    }

    @Override
    public void deletePostTags(Long pid) {
        QueryWrapper<PostTag> tagPostQueryWrapper = new QueryWrapper<>();
        tagPostQueryWrapper.eq("pid", pid);
        postTagMapper.delete(tagPostQueryWrapper);
    }

    @Override
    public void addTagToPost(PostTag postTag) {
        postTagMapper.insert(postTag);
    }

    @Override
    public List<PostTag> getPostTags(String pid) {
        QueryWrapper<PostTag> tagPostQueryWrapper = new QueryWrapper<>();
        tagPostQueryWrapper.eq("pid", pid);
        return  postTagMapper.selectList(tagPostQueryWrapper);
    }
}
