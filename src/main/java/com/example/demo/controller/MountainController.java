package com.example.demo.controller;

import com.example.demo.entity.Mountain;
import com.example.demo.entity.Post;
import com.example.demo.entity.Review;
import com.example.demo.repository.MountainRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.ReviewRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/mountain")
public class MountainController {

    @Autowired
    private MountainRepository mountainRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    // 🎯 1. [404 해결 부활!] 진짜 MySQL DB와 연동된 지능형 맞춤 매칭 검색 엔진
    @GetMapping("/search")
    public String searchMountain(
            @RequestParam(value = "region", required = false, defaultValue = "") String region,
            @RequestParam(value = "difficulty", required = false, defaultValue = "0") int difficulty,
            @RequestParam(value = "view", required = false, defaultValue = "0") int view,
            @RequestParam(value = "traffic", required = false, defaultValue = "0") int traffic,
            Model model) {

        // DB에서 등록된 모든 진짜 산 목록 조회
        List<Mountain> mountains = mountainRepository.findAll();
        List<Map<String, Object>> results = new ArrayList<>();

        for (Mountain m : mountains) {
            // 지역 필터링 (경기도, 강원도 등 선택지역과 맞지 않으면 스킵)
            if (!region.isEmpty() && !m.getRegion().equals(region)) {
                continue;
            }

            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId().toString());
            map.put("name", m.getName());
            map.put("region", m.getRegion());
            map.put("location", m.getRegion() + " 권역 / 최고고도 " + m.getHeight());
            map.put("height", m.getHeight());
            map.put("difficultyLevel", m.getDifficultyLevel());
            map.put("viewScore", m.getViewScore());
            map.put("trafficScore", m.getTrafficScore());
            map.put("img", m.getImg());

            // 조건 선택 기반 일치율/평점 매칭도 산출
            double sum = 0;
            int count = 0;

            if (difficulty > 0) {
                int mDiff = m.getDifficultyLevel();
                sum += (5 - Math.abs(mDiff - difficulty)); // 난이도는 조건 차이가 적을수록 만점에 수렴
                count++;
            }
            if (view > 0) {
                sum += m.getViewScore();
                count++;
            }
            if (traffic > 0) {
                sum += m.getTrafficScore();
                count++;
            }

            double matchScore = (count == 0) ? 4.5 : (sum / count);
            map.put("matchScore", Math.round(matchScore * 10) / 10.0);

            results.add(map);
        }

        // 매칭 점수 기준 내림차순(정렬 순위) 정렬 가동
        results.sort((m1, m2) -> Double.compare((double) m2.get("matchScore"), (double) m1.get("matchScore")));

        model.addAttribute("searchResults", results);
        return "mountain/search-results";
    }

    // 🏔️ 2. 특정 산 상세 정보 조회 및 매핑된 자랑글/리뷰 데이터 배달
    @GetMapping("/detail")
    public String mountainDetail(@RequestParam("id") String id, Model model) {
        Long mountainId = Long.parseLong(id);

        Mountain mountain = mountainRepository.findById(mountainId).orElse(null);
        model.addAttribute("mountain", mountain);

        List<Post> mountainPosts = postRepository.findByMountainIdOrderByCreatedAtDesc(mountainId);
        model.addAttribute("mountainPosts", mountainPosts);

        List<Review> mountainReviews = reviewRepository.findByMountainIdOrderByIdDesc(mountainId);
        model.addAttribute("mountainReviews", mountainReviews);

        return "mountain/detail";
    }

    // ✍️ 3. 리얼 리뷰 데이터베이스 영구 저장 API
    @PostMapping("/review/add")
    public String addReview(
            @RequestParam("mountainId") Long mountainId,
            @RequestParam("difficulty") String difficulty,
            @RequestParam("viewRating") int viewRating,
            @RequestParam("transport") String transport,
            @RequestParam("content") String content,
            HttpSession session) {

        if (session.getAttribute("loginUser") == null) {
            return "redirect:/auth/login";
        }

        Review review = new Review();
        review.setDifficulty(difficulty);
        review.setViewRating(viewRating);
        review.setTransport(transport);
        review.setContent(content);
        review.setCreatedAt(LocalDateTime.now());

        Mountain mountain = mountainRepository.findById(mountainId).orElse(null);
        review.setMountain(mountain);

        Object loginUserObj = session.getAttribute("loginUser");
        String nickname = "클린등산객";
        try {
            java.lang.reflect.Method getNickname = loginUserObj.getClass().getMethod("getNickname");
            nickname = (String) getNickname.invoke(loginUserObj);
        } catch (Exception e) {
            if (loginUserObj instanceof java.util.Map) {
                nickname = (String) ((java.util.Map<?, ?>) loginUserObj).get("nickname");
            }
        }
        review.setAuthor(nickname);

        reviewRepository.save(review);

        return "redirect:/mountain/detail?id=" + mountainId;
    }
}