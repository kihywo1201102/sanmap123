package com.example.demo.controller;

import com.example.demo.entity.SiteUser;
import com.example.demo.entity.Post;
import com.example.demo.entity.Review;
import com.example.demo.repository.SiteUserRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.ReviewRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // 👈 비밀번호 검증용 임포트!
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private SiteUserRepository siteUserRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // 👈 기존 비밀번호 암호화 검증기 주입!

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

    // 🛠️ 1. 내 정보 수정 화면 보기
    @GetMapping("/edit")
    public String editForm(HttpSession session, Model model) {
        SiteUser user = getLoginUser(session);
        if (user == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("user", user);
        return "user/edit";
    }

    // 🛠️ 2. 내 정보 수정 완료 처리 (비밀번호 더블 체크 로직 탑재)
    @PostMapping("/edit")
    public String editSubmit(
            @RequestParam("nickname") String newNickname,
            @RequestParam("currentPassword") String currentPassword, // 기존 비밀번호
            @RequestParam(value = "newPassword", required = false) String newPassword, // 새 비밀번호
            @RequestParam(value = "confirmPassword", required = false) String confirmPassword, // 새 비밀번호 확인
            HttpSession session,
            Model model) {

        SiteUser user = getLoginUser(session);
        if (user == null) return "redirect:/auth/login";

        // [검증 1] 기존 비밀번호 일치 검사
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            user.setNickname(newNickname); // 사용자가 새로 입력하려던 닉네임은 유지
            model.addAttribute("user", user);
            model.addAttribute("error", "기존 비밀번호가 일치하지 않습니다.");
            return "user/edit";
        }

        // [검증 2] 새 비밀번호를 변경하고자 값을 입력한 경우
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            // 두 번 입력한 새 비밀번호가 일치하는지 비교
            if (!newPassword.equals(confirmPassword)) {
                user.setNickname(newNickname);
                model.addAttribute("user", user);
                model.addAttribute("error", "새 비밀번호와 확인용 비밀번호가 일치하지 않습니다.");
                return "user/edit";
            }
            // 비밀번호를 안전하게 암호화하여 세팅
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        // 3. 닉네임 수정 및 동기화
        String oldNickname = user.getNickname();
        user.setNickname(newNickname);
        siteUserRepository.save(user);

        if (!oldNickname.equals(newNickname)) {
            List<Post> myPosts = postRepository.findByAuthor(oldNickname);
            for (Post p : myPosts) {
                p.setAuthor(newNickname);
                postRepository.save(p);
            }
            List<Review> myReviews = reviewRepository.findByAuthor(oldNickname);
            for (Review r : myReviews) {
                r.setAuthor(newNickname);
                reviewRepository.save(r);
            }
        }

        session.setAttribute("loginUser", user);

        return "redirect:/";
    }

    // 📝 3. 내가 쓴 자랑글 피드 모아보기
    @GetMapping("/my-posts")
    public String myPosts(HttpSession session, Model model) {
        SiteUser user = getLoginUser(session);
        if (user == null) {
            return "redirect:/auth/login";
        }

        List<Post> myPosts = postRepository.findByAuthorOrderByCreatedAtDesc(user.getNickname());
        model.addAttribute("myPosts", myPosts);
        model.addAttribute("user", user);

        return "user/my-posts";
    }
}