package com.example.demo.controller;

import com.example.demo.entity.Post;
import com.example.demo.entity.Mountain;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.MountainRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequestMapping("/post")
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MountainRepository mountainRepository;

    @GetMapping("/write")
    public String writeForm(@RequestParam(value = "mountainId", required = false) Long mountainId, HttpSession session, Model model) {
        if (session.getAttribute("loginUser") == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("mountains", mountainRepository.findAll());

        // ⭐️ [추가] 상세 페이지에서 넘어온 산 ID를 폼에 찔러 넣어 줍니다.
        model.addAttribute("preSelectedId", mountainId);
        return "post/write";
    }

    @PostMapping("/write")
    public String savePost(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("mountainId") Long mountainId,
            @RequestParam("photo") MultipartFile photo,
            HttpSession session) throws IOException {

        if (session.getAttribute("loginUser") == null) {
            return "redirect:/auth/login";
        }

        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setCreatedAt(LocalDateTime.now());

        Mountain mountain = mountainRepository.findById(mountainId).orElse(null);
        post.setMountain(mountain);

        if (!photo.isEmpty()) {
            File uploadDir = new File("uploads");
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            String originalFilename = photo.getOriginalFilename();
            String uuidFilename = UUID.randomUUID().toString() + "_" + originalFilename;
            File targetFile = new File(uploadDir, uuidFilename);
            photo.transferTo(targetFile);

            post.setImageUrl("/uploads/" + uuidFilename);
        } else {
            post.setImageUrl("https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=800&q=80");
        }

        // 유저 닉네임 세션 처리
        Object loginUserObj = session.getAttribute("loginUser");
        String nickname = "익명등산객";
        try {
            java.lang.reflect.Method getNickname = loginUserObj.getClass().getMethod("getNickname");
            nickname = (String) getNickname.invoke(loginUserObj);
        } catch (Exception e) {
            if (loginUserObj instanceof java.util.Map) {
                nickname = (String) ((java.util.Map<?, ?>) loginUserObj).get("nickname");
            }
        }
        post.setAuthor(nickname);

        postRepository.save(post);

        // ⭐️ 저장 후 내가 글을 썼던 그 산의 상세 페이지로 기분 좋게 리다이렉트!
        return "redirect:/mountain/detail?id=" + mountainId;
    }
}