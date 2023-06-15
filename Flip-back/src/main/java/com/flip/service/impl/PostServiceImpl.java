package com.flip.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HtmlUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flip.domain.Response;
import com.flip.entity.*;
import com.flip.entity.dto.LoggedUser;
import com.flip.mapper.PostMapper;
import com.flip.mapper.PostTagMapper;
import com.flip.mapper.TagMapper;
import com.flip.mapper.UserMapper;
import com.flip.service.PostService;
import com.flip.service.SensitiveWordService;
import com.flip.service.UserService;
import com.flip.utils.SensitiveWordUtils;
import com.flip.utils.elastic.ElasticPostUtils;
import jakarta.annotation.Resource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    @Resource
    private PostMapper postMapper;

    @Resource
    private PostTagMapper postTagMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserService userService;

    @Resource
    private TagMapper tagMapper;

    @Resource
    private SensitiveWordService sensitiveWordService;

    @Resource
    private ElasticsearchClient elasticsearchClient;

    /**
     * 发布帖子
     * @param post 帖子实体
     * @return 帖子ID
     */
    @Override
    public Response<String> doPost(Post post) {
        LoggedUser loggedUser = (LoggedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        post.setUid(loggedUser.getUser().getUid());

        List<String> sensitiveWords = sensitiveWordService.getSensitiveStringWords();
        String postTitle = SensitiveWordUtils.stringSearchEx2Filter(post.getTitle(), sensitiveWords);
        String postContent = SensitiveWordUtils.stringSearchEx2Filter(post.getContent(), sensitiveWords);

        post.setTitle(postTitle);
        post.setContent(postContent);

        // 保存到数据库
        int insertResult = postMapper.insert(post);

        if (insertResult > 0) {
            post.getTags().forEach(tag -> {
                PostTag postTag = new PostTag(null, post.getId(), tag.getLabel(), loggedUser.getUser().getUid(), null);
                postTagMapper.insert(postTag);
            });

            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.select("username, nickname, avatar").eq("uid", post.getUid());
            User user = userMapper.selectOne(userQueryWrapper);
            post.setAuthor(user.getUsername());
            post.setNickname(user.getNickname());
            post.setAvatar(user.getAvatar());

            try {
                ElasticPostUtils.insertPostToEs(elasticsearchClient, post);
            } catch (IOException e) {
                return Response.success(200, "帖子发布成功", Map.of("postId", String.valueOf(post.getId())));
            }

            return Response.success(200, "帖子发布成功", Map.of("postId", String.valueOf(post.getId())));
        }

        return Response.error(400, "帖子发布失败");
    }

    /**
     * 获取最新主题列表
     * @param currentPage 当前页码
     * @return 最新主题列表
     */
    @Override
    public Response<Object> getLatestPostList(Integer currentPage) {
        QueryWrapper<Post> postQueryWrapper = new QueryWrapper<>();
        postQueryWrapper.select("id, uid, title, create_time as createTime, priority, status, type, reply_number as replyNumber, view_number as viewNumber, last_comment_time as lastCommentTime")
                .orderByDesc("createTime");

        Map<String, Object> mapTransfer = getPostList(currentPage, postQueryWrapper);
        return Response.success(200, mapTransfer);
    }

    @Override
    public Response<Object> getALlPostList(Integer currentPage) {
        QueryWrapper<Post> postQueryWrapper = new QueryWrapper<>();
        postQueryWrapper.select("id, uid, title, create_time as createTime, priority, status, type, reply_number as replyNumber, view_number as viewNumber, last_comment_time as lastCommentTime")
                .orderByDesc("lastCommentTime");

        Map<String, Object> mapTransfer = getPostList(currentPage, postQueryWrapper);
        return Response.success(200, mapTransfer);
    }

    @Override
    public Response<Object> getHotPostList(Integer currentPage) {
        QueryWrapper<Post> postQueryWrapper = new QueryWrapper<>();
        postQueryWrapper.select("id, uid, title, create_time as createTime, priority, status, type, reply_number as replyNumber, view_number as viewNumber, last_comment_time as lastCommentTime")
                .gt("reply_number", 0).orderByDesc("replyNumber").orderByDesc("lastCommentTime");

        Map<String, Object> mapTransfer = getPostList(currentPage, postQueryWrapper);
        return Response.success(200, mapTransfer);
    }

    @Override
    public Response<List<Tag>> changeTagOfPost(Post post) {
        LoggedUser loggedUser = (LoggedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long pid = post.getId();

        QueryWrapper<PostTag> tagPostQueryWrapper = new QueryWrapper<>();
        tagPostQueryWrapper.eq("pid", pid);
        boolean exists = postTagMapper.exists(tagPostQueryWrapper);
        if (exists) {
            postTagMapper.delete(tagPostQueryWrapper); //更新前，把原有的标签记录删掉
        }

        List<Tag> newTags = new ArrayList<>();
        post.getTags().forEach(tag -> {
            PostTag postTag = new PostTag(null, pid, tag.getLabel(), loggedUser.getUser().getUid(), null);
            postTagMapper.insert(postTag);

            QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
            tagQueryWrapper.eq("label", tag.getLabel()).select("label, name");
            newTags.add(tagMapper.selectOne(tagQueryWrapper));
        });

        try {
            post.setTags(newTags);
            ElasticPostUtils.updatePostTagsInEs(elasticsearchClient, post);
        } catch (IOException e) {
            return Response.success(200, "已更新标签", Map.of("newTags", newTags));
        }

        return Response.success(200, "已更新标签", Map.of("newTags", newTags));
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

    /**
     * 获取帖子详情
     * @param pid 帖子ID
     * @return 帖子详情
     */
    @Override
    public Response<Object> getPostInfo(String pid) {
        QueryWrapper<Post> PostQueryWrapper = new QueryWrapper<>();
        PostQueryWrapper.eq("id", pid);
        Post post = postMapper.selectOne(PostQueryWrapper);

        if (ObjectUtil.isNull(post)) {
            return Response.error(404, "请检查URL是否输入正确");
        }

        QueryWrapper<PostTag> tagPostQueryWrapper = new QueryWrapper<>();
        tagPostQueryWrapper.eq("pid", pid);
        List<PostTag> postTags = postTagMapper.selectList(tagPostQueryWrapper);
        List<Tag> tags = new ArrayList<>();
        postTags.forEach(tagPost -> {
            QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
            tagQueryWrapper.eq("label", tagPost.getTagLabel());
            Tag tag = tagMapper.selectOne(tagQueryWrapper);
            tags.add(tag);
        });
        post.setTags(tags);

        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.select("id, username, nickname, avatar").eq("uid", post.getUid());
        User user = userMapper.selectOne(userQueryWrapper);
        Role role = userService.loadUserRoleByUid(String.valueOf(post.getUid()));

        Map<String, Object> map = new HashMap<>();
        map.put("post", post);
        map.put("id", user.getId());
        map.put("uid", post.getUid());
        map.put("username", user.getUsername());
        map.put("nickname", user.getNickname());
        map.put("avatar", user.getAvatar());
        map.put("role", role);

        return Response.success(200, map);
    }

    /**
     * 修改帖子内容
     * @param post 帖子实体
     * @return 修改结果
     */
    @Override
    public Response<Object> doEditContent(Post post) {
        if (post.getContent().length() >= 65536) {
            return Response.error(400, "内容长度超限");
        }

        List<String> sensitiveWords = sensitiveWordService.getSensitiveStringWords();
        String postContent = SensitiveWordUtils.stringSearchEx2Filter(post.getContent(), sensitiveWords);

        UpdateWrapper<Post> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", post.getId()).set("content", postContent);
        int result = postMapper.update(null, updateWrapper);
        if (result > 0) {
            try {
                post.setContent(HtmlUtil.cleanHtmlTag(postContent));
                ElasticPostUtils.updatePostContentInEs(elasticsearchClient, post);
            } catch (IOException e) {
                return Response.success(200, "修改内容成功", Map.of("content", postContent));
            }
            return Response.success(200, "修改内容成功", Map.of("content", postContent));
        }

        return Response.error(400, "修改内容失败");
    }

    /**
     * 修改帖子标题
     * @param pid 帖子ID
     * @param title 帖子新标题
     * @return 修改结果
     */
    @Override
    public Response<Object> doEditTitle(String pid, String title) {
        if (title.length() > 100) {
            return Response.error(400, "标题长度超限");
        }

        List<String> sensitiveWords = sensitiveWordService.getSensitiveStringWords();
        String postTitle = SensitiveWordUtils.stringSearchEx2Filter(title, sensitiveWords);
        UpdateWrapper<Post> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", pid).set("title", HtmlUtil.escape(postTitle));

        int result = postMapper.update(null, updateWrapper);

        if (result > 0) {
            try {
                Post post = new Post();
                post.setId(Long.parseLong(pid));
                post.setTitle(postTitle);
                ElasticPostUtils.updatePostTitleInEs(elasticsearchClient, post);
            } catch (IOException e) {
                return Response.success(200, "修改标题成功", Map.of("title", postTitle));
            }
            return Response.success(200, "修改标题成功", Map.of("title", postTitle));
        }

        return Response.error(400, "修改标题失败");
    }
}
