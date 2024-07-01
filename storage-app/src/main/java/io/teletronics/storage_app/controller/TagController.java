package io.teletronics.storage_app.controller;

import io.teletronics.storage_app.dto.response.TagResponse;
import io.teletronics.storage_app.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RequestMapping("/api/tags")
@RestController
public class TagController {
    private final TagService tagService;

    @Autowired
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public Mono<TagResponse> listTags() {
        return tagService.loadTags();
    }

}
