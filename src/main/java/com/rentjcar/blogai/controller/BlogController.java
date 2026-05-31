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
            model.addAttribute("dto", dto);
            return "result";

        } catch (RuntimeException e) {
            String msg = e.getMessage();
            String errorMsg;
            if ("QUOTA_EXCEEDED".equals(msg)) {
                errorMsg = "❌ 사용 한도 초과! 잠시 후 다시 시도해주세요.";
            } else if ("PAYMENT_REQUIRED".equals(msg)) {
                errorMsg = "❌ API 결제 확인이 필요합니다.";
            } else if ("FORBIDDEN".equals(msg)) {
                errorMsg = "❌ API 키를 확인해주세요.";
            } else {
                errorMsg = "❌ 오류 발생: " + msg;
            }
            model.addAttribute("errorMsg", errorMsg);
            model.addAttribute("blogRequest", dto);
            return "index";
        }
    }
}