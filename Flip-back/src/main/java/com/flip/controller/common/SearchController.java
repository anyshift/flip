package com.flip.controller.common;

import com.flip.domain.Response;
import com.flip.entity.Post;
import com.flip.entity.User;
import com.flip.service.SearchService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
public class SearchController {
    @Resource
    private SearchService searchService;

    @GetMapping("/post")
    public Response<List<Post>> searchPostsByKey(@RequestParam String keyword) {
        return searchService.searchPostsByKey(keyword);
    }

    @GetMapping("/user")
    public Response<List<User>> searchUsersByKey(@RequestParam String keyword) {
        return searchService.searchUsersByKey(keyword);
    }

    @PostMapping("/addPosts")
    public Response<String> insertAllPostToEs() {
        return searchService.insertAllPostToEs();
    }

    @PostMapping("/addUsers")
    public Response<String> insertAllUserToEs() {
        return searchService.insertAllUserToEs();
    }
}
