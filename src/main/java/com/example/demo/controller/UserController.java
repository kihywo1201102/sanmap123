package com.example.demo.controller;

import com.example.demo.entity.SiteUser;
import com.example.demo.entity.Post;
import com.example.demo.entity.Review;
import com.example.demo.repository.SiteUserRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.ReviewRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // 👈 비밀번호 암호화 인코더 임포트!
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
    private PasswordEncoder passwordEncoder; // 👈 본인 확인 비밀번호 대조를 위한 인코더 주입

    // 🛡️ [방탄 장치 1] 어떤 형식의 로그인 정보가 세션에 박혀있든 에러 없이 유연하게 회원을 조회해오는 무적 가드 메서드
    private SiteUser getLoginUser(HttpSession session) {
        Object loginUserObj = session.getAttribute("loginUser");
        if (loginUserObj == null) return null;

        // 1. 만약 SiteUser 객체 통째로 캐스팅이 가능하다면 바로 변환
        if (loginUserObj instanceof SiteUser) {
            return siteUserRepository.findByUsername(((SiteUser) loginUserObj).getUsername()).orElse(null);
        }

        // 2. 클래스 로더 차이 등으로 캐스팅이 안 될 경우 리플렉션으로 안전하게 username 획득
        String username = null;
        try {
            java.lang.reflect.Method getUsername = loginUserObj.getClass().getMethod("getUsername");
            username = (String) getUsername.invoke(loginUserObj);
        } catch (Exception e) {
            try {
                java.lang.reflect.Method getEmail = loginUserObj.getClass().getMethod("getEmail");
                username = (String) getEmail.invoke(loginUserObj);
            } catch (Exception ex) {
                if (loginUserObj instanceof java.util.Map) {
                    username = (String) ((java.util.Map<?, ?>) loginUserObj).get("username");
                    if (username == null) {
                        username = (String) ((java.util.Map<?, ?>) loginUserObj).get("email");
                    }
                }
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
            return "redirect:/auth/login"; // 로그인 안 되어 있으면 로그인 페이지로 튕김
        }
        model.addAttribute("user", user);
        return "user/edit";
    }

    // 🛠️ 2. 내 정보 진짜로 수정하기 (구버전 폼 전송 파라미터 유연 대응)
    @PostMapping("/edit")
    public String editSubmit(
            @RequestParam(value = "nickname", required = false) String newNickname,
            @RequestParam(value = "currentPassword", required = false) String currentPassword, // 👈 required=false로 시스템 폭발 방어!
            @RequestParam(value = "newPassword", required = false) String newPassword,
            @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
            HttpSession session,
            Model model) {

        SiteUser user = getLoginUser(session);
        if (user == null) return "redirect:/auth/login";

        // 혹시 공백 닉네임이 넘어오면 기존 닉네임 유지 처리
        if (newNickname == null || newNickname.trim().isEmpty()) {
            newNickname = user.getNickname();
        }

        // 🛡️ [에러 차단] 본인 확인 비밀번호를 안 적었거나 구버전 HTML 폼일 때 시스템 에러 대신 친절한 경고창 전송
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            user.setNickname(newNickname);
            model.addAttribute("user", user);
            model.addAttribute("error", "본인 확인을 위해 기존 비밀번호를 반드시 입력해 주세요.");
            return "user/edit";
        }

        // 기존 비밀번호 대조
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            user.setNickname(newNickname);
            model.addAttribute("user", user);
            model.addAttribute("error", "기존 비밀번호가 일치하지 않습니다.");
            return "user/edit";
        }

        // 새 비밀번호 변경 희망 시 더블 검증
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
                user.setNickname(newNickname);
                model.addAttribute("user", user);
                model.addAttribute("error", "새 비밀번호 확인 칸을 마저 입력해 주세요.");
                return "user/edit";
            }
            if (!newPassword.equals(confirmPassword)) {
                user.setNickname(newNickname);
                model.addAttribute("user", user);
                model.addAttribute("error", "새 비밀번호와 확인용 비밀번호가 일치하지 않습니다.");
                return "user/edit";
            }
            // 암호화 인코딩 후 저장
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        // 닉네임 동기화 및 DB 저장
        String oldNickname = user.getNickname();
        user.setNickname(newNickname);
        siteUserRepository.save(user);

        if (oldNickname != null && !oldNickname.equals(newNickname)) {
            try {
                List<Post> myPosts = postRepository.findByAuthor(oldNickname);
                for (Post p : myPosts) {
                    p.setAuthor(newNickname);
                    postRepository.save(p);
                }
            } catch (Exception e) { e.printStackTrace(); }

            try {
                List<Review> myReviews = reviewRepository.findByAuthor(oldNickname);
                for (Review r : myReviews) {
                    r.setAuthor(newNickname);
                    reviewRepository.save(r);
                }
            } catch (Exception e) { e.printStackTrace(); }
        }

        // 세션 정보 신선하게 업데이트
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