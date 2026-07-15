package com.example.demo.controller;

import com.example.demo.entity.Post;
import com.example.demo.entity.Comment;
import com.example.demo.entity.SiteUser;
import com.example.demo.entity.Mountain;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.SiteUserRepository;
import com.example.demo.repository.MountainRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/post")
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private SiteUserRepository siteUserRepository;

    @Autowired
    private MountainRepository mountainRepository;

    // 세션 로그인 회원 안전 검증용 메서드
    private SiteUser getLoginUser(HttpSession session) {
        Object loginUserObj = session.getAttribute("loginUser");
        if (loginUserObj == null) return null;

        String username = null;
        try {
            java.lang.reflect.Method getUsername = loginUserObj.getClass().getMethod("getUsername");
            username = (String) getUsername.invoke(loginUserObj);
        } catch (Exception e) {
            if (loginUserObj instanceof java.util.Map) {
                username = (String) ((java.util.Map<?, ?>) loginUserObj).get("username");
            }
        }

        if (username != null) {
            return siteUserRepository.findByUsername(username).orElse(null);
        }
        return null;
    }

    // 📝 1. 내 등산기 작성 화면 열기
    @GetMapping("/write")
    public String writeForm(
            @RequestParam(value = "mountainId", required = false) Long mountainId,
            Model model,
            HttpSession session) {

        SiteUser loginUser = getLoginUser(session);
        if (loginUser == null) {
            return "redirect:/auth/login";
        }

        List<Mountain> mountains = mountainRepository.findAll();
        model.addAttribute("mountains", mountains);

        if (mountainId != null) {
            model.addAttribute("mountainId", mountainId);
            Mountain currentMountain = mountainRepository.findById(mountainId).orElse(null);
            model.addAttribute("mountain", currentMountain);
        }

        return "post/write";
    }

    // 📝 2. 작성한 글과 이미지 저장
    @PostMapping("/write")
    public String writeSubmit(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "mountainId", required = false) Long mountainId,
            @RequestParam(value = "mountain", required = false) Long mountain,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            @RequestParam(value = "file", required = false) MultipartFile fileFile,
            HttpSession session) {

        SiteUser loginUser = getLoginUser(session);
        if (loginUser == null) {
            return "redirect:/auth/login";
        }

        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setAuthor(loginUser.getNickname());

        Long finalMountainId = (mountainId != null) ? mountainId : mountain;
        if (finalMountainId != null) {
            Mountain m = mountainRepository.findById(finalMountainId).orElse(null);
            post.setMountain(m);
        }

        try {
            java.lang.reflect.Method setCreatedAt = post.getClass().getMethod("setCreatedAt", java.time.LocalDateTime.class);
            setCreatedAt.invoke(post, java.time.LocalDateTime.now());
        } catch (Exception e) {
        }

        MultipartFile file = (imageFile != null && !imageFile.isEmpty()) ? imageFile : fileFile;
        if (file != null && !file.isEmpty()) {
            try {
                String originalFilename = file.getOriginalFilename();
                String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
                String uuidFilename = UUID.randomUUID().toString() + ext;

                String uploadDir = System.getProperty("user.dir") + "/uploads/";
                File dir = new File(uploadDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                File targetFile = new File(uploadDir + uuidFilename);
                file.transferTo(targetFile);

                post.setImageUrl("/uploads/" + uuidFilename);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 새 글을 쓸 때는 추천수 등을 명시적으로 0으로 초기화해서 저장합니다.
        post.setViews(0);
        post.setRecommends(0);
        post.setDecommends(0);
        post.setReports(0);

        postRepository.save(post);

        return "redirect:/mountain/detail?id=" + finalMountainId;
    }

    // 🔍 3. 자랑글 상세 보기 (⭐️ 기존 데이터의 NULL 값을 완벽히 가공하여 500 에러를 격파하는 핵심 구역!)
    @GetMapping("/detail")
    public String postDetail(@RequestParam("id") Long id, Model model, HttpSession session) {
        Post post = postRepository.findById(id).orElse(null);
        if (post == null) {
            return "redirect:/";
        }

        // 🛡️ [방탄 필터] DB에 저장된 값이 null인 경우 안전하게 0으로 치환 후 연산합니다.
        Integer currentViews = post.getViews();
        post.setViews((currentViews != null ? currentViews : 0) + 1);

        if (post.getRecommends() == null) post.setRecommends(0);
        if (post.getDecommends() == null) post.setDecommends(0);
        if (post.getReports() == null) post.setReports(0);

        postRepository.save(post);

        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(id);

        model.addAttribute("post", post);
        model.addAttribute("comments", comments);
        model.addAttribute("loginUser", getLoginUser(session));
        return "post/detail";
    }

    // 👍 4. 추천하기 (개추)
    @PostMapping("/recommend")
    @ResponseBody
    public String recommendPost(@RequestParam("id") Long id) {
        Post post = postRepository.findById(id).orElse(null);
        if (post == null) return "fail";

        // 🛡️ [NPE 방지] null 체크 후 1 증가
        Integer currentRec = post.getRecommends();
        post.setRecommends((currentRec != null ? currentRec : 0) + 1);

        postRepository.save(post);
        return String.valueOf(post.getRecommends());
    }

    // 👎 5. 비추천하기 (비추)
    @PostMapping("/decommend")
    @ResponseBody
    public String decommendPost(@RequestParam("id") Long id) {
        Post post = postRepository.findById(id).orElse(null);
        if (post == null) return "fail";

        // 🛡️ [NPE 방지] null 체크 후 1 증가
        Integer currentDec = post.getDecommends();
        post.setDecommends((currentDec != null ? currentDec : 0) + 1);

        postRepository.save(post);
        return String.valueOf(post.getDecommends());
    }

    // 🚨 6. 신고하기
    @PostMapping("/report")
    @ResponseBody
    public String reportPost(@RequestParam("id") Long id) {
        Post post = postRepository.findById(id).orElse(null);
        if (post == null) return "fail";

        // 🛡️ [NPE 방지] null 체크 후 1 증가
        Integer currentRep = post.getReports();
        post.setReports((currentRep != null ? currentRep : 0) + 1);

        postRepository.save(post);
        return String.valueOf(post.getReports());
    }

    // 💬 7. 댓글 달기
    @PostMapping("/comment")
    public String addComment(@RequestParam("postId") Long postId, @RequestParam("content") String content, HttpSession session) {
        SiteUser loginUser = getLoginUser(session);
        if (loginUser == null) {
            return "redirect:/auth/login";
        }

        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("글이 없습니다."));

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setAuthor(loginUser.getNickname());
        comment.setPost(post);
        commentRepository.save(comment);

        return "redirect:/post/detail?id=" + postId;
    }

    // 🗑️ 8. 댓글 삭제
    @PostMapping("/comment/delete")
    public String deleteComment(@RequestParam("commentId") Long commentId, @RequestParam("postId") Long postId, HttpSession session) {
        SiteUser loginUser = getLoginUser(session);
        Comment comment = commentRepository.findById(commentId).orElse(null);

        if (comment != null && loginUser != null && comment.getAuthor().equals(loginUser.getNickname())) {
            commentRepository.delete(comment);
        }
        return "redirect:/post/detail?id=" + postId;
    }

    // 🛠️ 9. 게시글 수정 화면
    @GetMapping("/edit")
    public String editForm(@RequestParam("id") Long id, Model model, HttpSession session) {
        SiteUser loginUser = getLoginUser(session);
        Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("글이 없습니다."));

        if (loginUser == null || !post.getAuthor().equals(loginUser.getNickname())) {
            return "redirect:/";
        }

        model.addAttribute("post", post);
        return "post/edit";
    }

    // 🛠️ 10. 게시글 수정 완료 처리
    @PostMapping("/edit")
    public String editSubmit(@RequestParam("id") Long id, @RequestParam("title") String title, @RequestParam("content") String content, HttpSession session) {
        SiteUser loginUser = getLoginUser(session);
        Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("글이 없습니다."));

        if (loginUser != null && post.getAuthor().equals(loginUser.getNickname())) {
            post.setTitle(title);
            post.setContent(content);
            postRepository.save(post);
        }
        return "redirect:/post/detail?id=" + id;
    }

    // 🗑️ 11. 게시글 삭제 처리
    @PostMapping("/delete")
    public String deletePost(@RequestParam("id") Long id, HttpSession session) {
        SiteUser loginUser = getLoginUser(session);
        Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("글이 없습니다."));

        if (loginUser != null && post.getAuthor().equals(loginUser.getNickname())) {
            postRepository.delete(post);
        }
        return "redirect:/";
    }
}