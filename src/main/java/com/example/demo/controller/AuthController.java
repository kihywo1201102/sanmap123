package com.example.demo.controller;

import com.example.demo.dto.UserSignupDto;
import com.example.demo.entity.SiteUser;
import com.example.demo.repository.SiteUserRepository;
import jakarta.servlet.http.HttpSession; // 세션 관리를 위해 추가
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam; // 로그인 파라미터 수집용

import java.util.Optional;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private SiteUserRepository siteUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // [회원가입 페이지]
    @GetMapping("/signup")
    public String signupPage(Model model) {
        model.addAttribute("userSignupDto", new UserSignupDto());
        return "auth/signup";
    }

    // [회원가입 처리]
    @PostMapping("/signup")
    public String signupProcess(@Valid @ModelAttribute("userSignupDto") UserSignupDto dto,
                                BindingResult bindingResult,
                                Model model) {
        if (bindingResult.hasErrors()) {
            String defaultMessage = bindingResult.getFieldError().getDefaultMessage();
            model.addAttribute("error", defaultMessage);
            return "auth/signup";
        }
        if (!dto.getPassword().equals(dto.getPasswordConfirm())) {
            model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
            return "auth/signup";
        }
        if (siteUserRepository.findByUsername(dto.getUsername()).isPresent()) {
            model.addAttribute("error", "이미 사용 중인 아이디입니다.");
            return "auth/signup";
        }

        SiteUser user = new SiteUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());

        siteUserRepository.save(user);
        return "redirect:/?success=true";
    }

    // [로그인 페이지]
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    // [★ 신규: 로그인 데이터 검증 및 처리]
    @PostMapping("/login")
    public String loginProcess(@RequestParam("username") String username,
                               @RequestParam("password") String password,
                               HttpSession session,
                               Model model) {

        // 1. 입력한 아이디로 MySQL DB 뒤지기
        Optional<SiteUser> userOpt = siteUserRepository.findByUsername(username);

        // 2. 가입된 아이디가 없거나 혹은 암호화된 비밀번호 대조 결과가 다를 때
        if (userOpt.isEmpty() || !passwordEncoder.matches(password, userOpt.get().getPassword())) {
            model.addAttribute("error", "아이디 또는 비밀번호가 일치하지 않습니다.");
            return "auth/login"; // 실패 시 붉은 경고창을 띄우며 로그인 창에 잔류
        }

        // 3. [로그인 대성공] 유저의 중요한 정보 객체를 컴퓨터 세션 보관함에 쏙 저장!
        session.setAttribute("loginUser", userOpt.get());

        return "redirect:/"; // 메인 대문 페이지로 당당히 복귀!
    }

    // [로그아웃 처리]
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // 세션 보관함을 폭파시켜 안전하게 로그아웃
        return "redirect:/";
    }
}