package com.rentjcar.blogai.controller;

import com.rentjcar.blogai.dto.BlogRequestDto;
import com.rentjcar.blogai.service.BlogGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class BlogController {

    @Autowired
    private BlogGeneratorService blogGeneratorService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("blogRequest", new BlogRequestDto());
        return "index";
    }

    @PostMapping("/generate")
    public String generate(@ModelAttribute BlogRequestDto dto, Model model) {
        try {
            String result = blogGeneratorService.generateBlogPost(dto);
            model.addAttribute("result", result);
            model.addAttribute("blogRequest", dto);
            return "result";

        } catch (RuntimeException e) {
            String msg = e.getMessage();
            String errorMsg;

            if ("QUOTA_EXCEEDED".equals(msg)) {
                errorMsg = "❌ 오늘 무료 사용 한도를 초과했어요! 내일 자정 이후 다시 시도해주세요.";
            } else if ("PAYMENT_REQUIRED".equals(msg)) {
                errorMsg = "❌ 유료 전환이 필요한 요청이에요. 무료 한도 내에서만 사용 가능합니다.";
            } else if ("FORBIDDEN".equals(msg)) {
                errorMsg = "❌ API 키 권한이 없어요. API 키를 확인해주세요.";
            } else {
                errorMsg = "❌ 오류가 발생했어요: " + msg;
            }

            model.addAttribute("errorMsg", errorMsg);
            model.addAttribute("blogRequest", dto);
            return "index";
        }
    }
}