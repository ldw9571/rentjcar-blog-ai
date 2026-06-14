package com.rentjcar.blogai.controller;

import com.rentjcar.blogai.service.BlogSeoService;
import com.rentjcar.blogai.service.UsageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BlogSeoController {

    @Autowired
    private BlogSeoService blogSeoService;

    @Autowired
    private UsageService usageService;

    @GetMapping("/seo")
    public String seoForm(Model model) {
        addUsageInfo(model);
        return "seo";
    }

    // ─────────────────────────────────────────
    // SEO 분석
    // ─────────────────────────────────────────
    @PostMapping("/seo/analyze")
    public String analyze(
            @RequestParam("keyword") String keyword,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            Model model) {

        if (!usageService.canUse()) {
            model.addAttribute("errorMsg", "❌ 오늘의 무료 횟수(" + usageService.getDailyLimit() + "회)를 모두 사용했습니다.");
            model.addAttribute("keyword", keyword);
            model.addAttribute("title", title);
            model.addAttribute("content", content);
            model.addAttribute("activeTab", "analyze");
            addUsageInfo(model);
            return "seo";
        }
        try {
            String result = blogSeoService.analyze(keyword, title, content);
            usageService.increaseCount();
            model.addAttribute("analyzeResult", result);
            model.addAttribute("keyword", keyword);
            model.addAttribute("title", title);
            model.addAttribute("content", content);
            model.addAttribute("activeTab", "analyze");
            addUsageInfo(model);
            return "seo";
        } catch (RuntimeException e) {
            model.addAttribute("errorMsg", resolveErrorMsg(e.getMessage()));
            model.addAttribute("keyword", keyword);
            model.addAttribute("title", title);
            model.addAttribute("content", content);
            model.addAttribute("activeTab", "analyze");
            addUsageInfo(model);
            return "seo";
        }
    }

    // ─────────────────────────────────────────
    // 포스팅 가이드라인 + 완성본 생성
    // ─────────────────────────────────────────
    @PostMapping("/seo/guide")
    public String guide(
            @RequestParam("guideKeyword")   String keyword,
            @RequestParam("restaurantName") String restaurantName,
            @RequestParam("address")        String address,
            @RequestParam("phone")          String phone,
            @RequestParam("category")       String category,
            @RequestParam("hours")          String hours,
            @RequestParam("menu")           String menu,
            @RequestParam("myReview")       String myReview,
            Model model) {

        if (!usageService.canUse()) {
            model.addAttribute("errorMsg", "❌ 오늘의 무료 횟수(" + usageService.getDailyLimit() + "회)를 모두 사용했습니다.");
            model.addAttribute("myReview",       myReview);
            addUsageInfo(model);
            return "seo";
        }
        try {
            String raw = blogSeoService.generateGuideAndPost(
                    keyword, restaurantName, address, phone, category, hours, menu, myReview);
            usageService.increaseCount();

            // 가이드 / 포스팅 파싱
            String guide = "";
            String post  = "";
            int gs = raw.indexOf("===GUIDE_START===");
            int ge = raw.indexOf("===GUIDE_END===");
            int ps = raw.indexOf("===POST_START===");
            int pe = raw.indexOf("===POST_END===");
            if (gs >= 0 && ge > gs) guide = raw.substring(gs + "===GUIDE_START===".length(), ge).trim();
            if (ps >= 0 && pe > ps) post  = raw.substring(ps + "===POST_START===".length(), pe).trim();

            model.addAttribute("guideResult",    guide);
            model.addAttribute("postResult",     post);
            model.addAttribute("guideKeyword",   keyword);
            model.addAttribute("restaurantName", restaurantName);
            model.addAttribute("address",        address);
            model.addAttribute("phone",          phone);
            model.addAttribute("category",       category);
            model.addAttribute("hours",          hours);
            model.addAttribute("menu",           menu);
            model.addAttribute("activeTab", "guide");
            addUsageInfo(model);
            return "seo";
        } catch (RuntimeException e) {
            model.addAttribute("errorMsg", resolveErrorMsg(e.getMessage()));
            model.addAttribute("activeTab", "guide");
            addUsageInfo(model);
            return "seo";
        }
    }

    private void addUsageInfo(Model model) {
        model.addAttribute("remainingCount", usageService.getRemainingCount());
        model.addAttribute("usedCount",      usageService.getUsedCount());
        model.addAttribute("dailyLimit",     usageService.getDailyLimit());
    }

    private String resolveErrorMsg(String msg) {
        if ("QUOTA_EXCEEDED".equals(msg))   return "❌ Gemini API 사용 한도 초과! 잠시 후 다시 시도해주세요.";
        if ("PAYMENT_REQUIRED".equals(msg)) return "❌ API 결제 확인이 필요합니다.";
        if ("FORBIDDEN".equals(msg))        return "❌ API 키를 확인해주세요.";
        return "❌ 오류 발생: " + msg;
    }
}