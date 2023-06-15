package com.flip.controller.common;

import com.flip.domain.Response;
import com.flip.entity.Comment;
import com.flip.service.CommentService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/getComments")
    public Response<Object> getComments(@RequestParam String pid) {
        return commentService.getComments(pid);
    }

    @GetMapping("/getReplies")
    public Response<Object> getReplies(@RequestParam String pid, @RequestParam Integer parentId) {
        return commentService.getReplies(pid, parentId);
    }

    @PostMapping("/doComment")
    public Response<Object> doComment(@RequestBody Comment comment) {
        return commentService.doComment(comment);
    }

    @PostMapping("/doReply")
    public Response<Object> doReply(@RequestBody Comment comment) {
        return commentService.doReply(comment);
    }
}
