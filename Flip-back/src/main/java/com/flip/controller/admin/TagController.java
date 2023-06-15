package com.flip.controller.admin;

import com.flip.domain.Response;
import com.flip.entity.Tag;
import com.flip.entity.TagOption;
import com.flip.service.TagOptionService;
import com.flip.service.TagService;
import com.flip.validation.VG;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TagController {

    @Resource
    private TagService tagService;

    @Resource
    private TagOptionService tagOptionService;

    @GetMapping("/tags")
    public Response<List<Tag>> getAllTag() {
        return tagService.getAllTag();
    }

    @GetMapping("/tagsAndOptions")
    public Response<Object> getTagsAndOptions() {
        return tagOptionService.getTagsAndOptions();
    }

    @GetMapping("/tag")
    public Response<Object> getTag(@RequestParam("label") String tagLabel, @RequestParam("page") Integer currentPage) {
        return tagService.getTagAndPosts(tagLabel, currentPage);
    }

    @PostMapping("/sys-ctrl/tag")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public Response<Tag> addTag(@Validated(VG.class) @RequestBody Tag tag) {
        return tagService.addTag(tag);
    }

    @PutMapping("/sys-ctrl/tag")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public Response<List<Tag>> updateTag(@Validated(VG.class) @RequestBody Tag tag) {
        return tagService.updateTag(tag);
    }

    @DeleteMapping("/sys-ctrl/tag")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public Response<List<Tag>> deleteTag(@RequestBody Tag tag) {
        if ("unclassified".equals(tag.getLabel())) {
            return Response.error(400, "该标签无法删除");
        }
        return tagService.deleteTag(tag);
    }

    @DeleteMapping("/sys-ctrl/tagForce")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public Response<List<Tag>> forceDeleteTag(@RequestBody Tag tag) {
        if ("unclassified".equals(tag.getLabel())) {
            return Response.error(400, "该标签无法删除");
        }
        return tagService.forceDeleteTag(tag);
    }

    @GetMapping("/sys-ctrl/tagOption")
    public Response<List<TagOption>> getAllTagOptions() {
        return tagOptionService.getAllTagOptions();
    }

    @PostMapping("/sys-ctrl/tagOption")
    public Response<TagOption> addTagOption(@RequestBody TagOption tagOption) {
        return tagOptionService.addTagOption(tagOption);
    }

    @PutMapping("/sys-ctrl/tagOption")
    public Response<TagOption> updateTagOption(@Validated(VG.class) @RequestBody TagOption tagOption) {
        return tagOptionService.updateTagOption(tagOption);
    }

    @DeleteMapping("/sys-ctrl/tagOption")
    public Response<List<TagOption>> deleteTagOption(@RequestBody TagOption tagOption) {
        if ("other".equals(tagOption.getLabel())) {
            return Response.error(400, "该标签类型无法删除");
        }
        return tagOptionService.deleteTagOption(tagOption);
    }

    @DeleteMapping("/sys-ctrl/tagOptionForce")
    public Response<Object> forceDeleteTagOption(@RequestBody TagOption tagOption) {
        if ("other".equals(tagOption.getLabel())) {
            return Response.error(400, "该标签类型无法删除");
        }
        return tagOptionService.forceDeleteTagOption(tagOption);
    }
}
