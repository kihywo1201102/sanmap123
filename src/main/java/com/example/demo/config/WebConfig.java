package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 프로젝트 루트 하위의 "uploads" 절대 디렉토리 경로 획득
        String uploadPath = new File("uploads").getAbsolutePath();

        // 브라우저에서 /uploads/이름.jpg 로 접근 시 로컬 uploads 폴더와 직접 다이렉트 연결
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}