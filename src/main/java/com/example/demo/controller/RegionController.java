package com.example.demo.controller;

import com.example.demo.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/region")
public class RegionController {

    @Autowired
    private PostRepository postRepository;

    @GetMapping("/gyeonggi")
    public String gyeonggiRegionPage(Model model) {

        // 1. 경기 지역 메달 랭킹용 가짜 데이터 (Map 형식)
        List<Map<String, Object>> topMountains = new ArrayList<>();

        Map<String, Object> m1 = new HashMap<>();
        m1.put("id", "1"); m1.put("rank", 1); m1.put("name", "청계산 매봉");
        m1.put("visits", 1850); m1.put("height", "582m");
        m1.put("img", "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=400&q=80");
        topMountains.add(m1);

        Map<String, Object> m2 = new HashMap<>();
        m2.put("id", "3"); m2.put("rank", 2); m2.put("name", "수리산 태을봉");
        m2.put("visits", 1210); m2.put("height", "489m");
        m2.put("img", "https://images.unsplash.com/photo-1501555088652-021faa106b9b?auto=format&fit=crop&w=400&q=80");
        topMountains.add(m2);

        model.addAttribute("topMountains", topMountains);

        // 2. 자랑 게시판에 들어갈 실시간 진짜 DB 데이터 연동
        model.addAttribute("localHills", postRepository.findAllByOrderByCreatedAtDesc());

        return "region/gyeonggi";
    }
}