package com.springboot.asa.learning.presentation.controller;

import com.springboot.asa.learning.infrastructure.persistence.entity.*;
import com.springboot.asa.learning.infrastructure.persistence.repository.*;
import com.springboot.asa.learning.infrastructure.security.AuthenticatedUser;
import com.springboot.asa.learning.presentation.dto.request.*;
import com.springboot.asa.learning.presentation.dto.response.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommunityController {

    private final JpaForumRepository forumRepo;
    private final JpaForumPostRepository postRepo;
    private final JpaNewsRepository newsRepo;
    private final JpaUserRepository userRepo;

    // ─── NEWS ─────────────────────────────────────────────────────────────────

    @GetMapping("/programs/{programId}/news")
    public ResponseEntity<Page<NewsResponse>> listNews(
            @PathVariable UUID programId,
            Pageable pageable) {

        Page<NewsResponse> page = newsRepo
                .findByProgramIdAndDeletedAtIsNullOrderByPublishedAtDesc(programId, pageable)
                .map(this::toNewsResponse);

        return ResponseEntity.ok(page);
    }

    @PostMapping("/programs/{programId}/news")
    @ResponseStatus(HttpStatus.CREATED)
    public NewsResponse createNews(
            @PathVariable UUID programId,
            @Valid @RequestBody CreateNewsRequest req,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        requireLeaderOrAdmin(principal);

        NewsEntity news = new NewsEntity();
        news.setProgramId(programId);
        news.setAuthorId(principal.id());
        news.setTitle(req.title());
        news.setContent(req.content());
        news.setPublishedAt(OffsetDateTime.now());

        return toNewsResponse(newsRepo.save(news));
    }

    @DeleteMapping("/news/{newsId}")
    public ResponseEntity<Void> deleteNews(
            @PathVariable UUID newsId,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        requireLeaderOrAdmin(principal);

        NewsEntity news = newsRepo.findById(newsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Noticia no encontrada"));
        news.setDeletedAt(OffsetDateTime.now());
        newsRepo.save(news);

        return ResponseEntity.noContent().build();
    }

    // ─── FORUM & POSTS ────────────────────────────────────────────────────────

    @GetMapping("/programs/{programId}/forum")
    @Transactional
    public ResponseEntity<List<ForumPostResponse>> getForum(@PathVariable UUID programId) {
        ForumEntity forum = getOrCreateForum(programId);
        List<ForumPostResponse> posts = buildPostTree(forum.getId());
        return ResponseEntity.ok(posts);
    }

    @PostMapping("/programs/{programId}/forum/posts")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ForumPostResponse createPost(
            @PathVariable UUID programId,
            @Valid @RequestBody CreatePostRequest req,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        ForumEntity forum = getOrCreateForum(programId);

        ForumPostEntity post = new ForumPostEntity();
        post.setForumId(forum.getId());
        post.setParentPostId(req.parentPostId());
        post.setAuthorId(principal.id());
        post.setContent(req.content());
        post.setDeleted(false);

        ForumPostEntity saved = postRepo.save(post);
        String email = emailOf(saved.getAuthorId());

        return new ForumPostResponse(
                saved.getId(), saved.getForumId(), saved.getParentPostId(),
                saved.getAuthorId(), email, saved.getContent(),
                saved.isDeleted(), saved.getCreatedAt(), List.of());
    }

    @DeleteMapping("/forums/posts/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable UUID postId,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        requireLeaderOrAdmin(principal);

        ForumPostEntity post = postRepo.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post no encontrado"));
        post.setDeleted(true);
        postRepo.save(post);

        return ResponseEntity.noContent().build();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private ForumEntity getOrCreateForum(UUID programId) {
        return forumRepo.findByProgramId(programId).orElseGet(() -> {
            ForumEntity f = new ForumEntity();
            f.setProgramId(programId);
            f.setTitle("Foro General");
            return forumRepo.save(f);
        });
    }

    private List<ForumPostResponse> buildPostTree(UUID forumId) {
        return postRepo
                .findByForumIdAndParentPostIdIsNullOrderByCreatedAtAsc(forumId)
                .stream()
                .map(post -> toPostResponse(post, true))
                .toList();
    }

    private ForumPostResponse toPostResponse(ForumPostEntity post, boolean includeReplies) {
        String email = emailOf(post.getAuthorId());
        String content = post.isDeleted() ? "[Mensaje eliminado]" : post.getContent();

        List<ForumPostResponse> replies = includeReplies
                ? postRepo.findByParentPostIdOrderByCreatedAtAsc(post.getId())
                        .stream()
                        .map(r -> toPostResponse(r, false))
                        .toList()
                : List.of();

        return new ForumPostResponse(
                post.getId(), post.getForumId(), post.getParentPostId(),
                post.getAuthorId(), email, content,
                post.isDeleted(), post.getCreatedAt(), replies);
    }

    private NewsResponse toNewsResponse(NewsEntity news) {
        String email = emailOf(news.getAuthorId());
        return new NewsResponse(
                news.getId(), news.getProgramId(), news.getAuthorId(),
                email, news.getTitle(), news.getContent(), news.getPublishedAt());
    }

    private String emailOf(UUID userId) {
        return userRepo.findById(userId).map(u -> u.getEmail()).orElse("desconocido");
    }

    private void requireLeaderOrAdmin(AuthenticatedUser p) {
        var r = p.roles();
        if (r.contains("PROGRAM_LEADER") || r.contains("ADMIN") || r.contains("SUPER_ADMIN")) return;
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para esta acción");
    }
}
