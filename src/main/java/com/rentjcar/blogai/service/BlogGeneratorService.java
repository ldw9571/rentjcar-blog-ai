package com.rentjcar.blogai.service;

import com.rentjcar.blogai.dto.BlogRequestDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class BlogGeneratorService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    public String generateBlogPost(BlogRequestDto dto) {

        RestTemplate restTemplate = new RestTemplate();
        String url = API_URL + "?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", buildPrompt(dto));

        Map<String, Object> parts = new HashMap<>();
        parts.put("parts", Collections.singletonList(textPart));

        Map<String, Object> body = new HashMap<>();
        body.put("contents", Collections.singletonList(parts));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> resParts = (List<Map<String, Object>>) content.get("parts");
            return (String) resParts.get(0).get("text");

        } catch (HttpClientErrorException e) {
            int code = e.getStatusCode().value();
            if (code == 429) throw new RuntimeException("QUOTA_EXCEEDED");
            if (code == 402) throw new RuntimeException("PAYMENT_REQUIRED");
            if (code == 403) throw new RuntimeException("FORBIDDEN");
            throw new RuntimeException("API_ERROR:" + code);
        }
    }

    private String buildPrompt(BlogRequestDto dto) {
        String fullModel = dto.getYear() + "년 " + dto.getBrand() + " "
                + dto.getCarModel() + " " + dto.getTrim();

        return "당신은 자동차 장기렌트 전문 블로그 작가입니다.\n"
                + "아래 중고 장기렌트 차량 정보를 바탕으로 네이버 블로그 포스팅 글을 작성해주세요.\n\n"

                + "【차량 정보】\n"
                + "- 관리번호: " + dto.getCarNo() + "\n"
                + "- 차량: " + fullModel + "\n"
                + "- 등록: " + dto.getRegDate() + "\n"
                + "- 주행거리: " + dto.getMileage() + "KM\n"
                + "- 사고이력: " + dto.getAccident() + "\n"
                + "- 색상: " + dto.getColor() + "\n"
                + "- 추가옵션: " + dto.getExtraOptions() + "\n"
                + "- 신차 출고가: " + dto.getNewPrice() + "원\n\n"

                + "【렌탈 조건】\n"
                + "- 담보(보증금): " + dto.getDeposit() + "\n"
                + "- 계약기간: " + dto.getContractPeriod() + "\n"
                + "- 인수 잔존가치(인수가): " + dto.getResidualValue() + "원\n"
                + "- 법인 (" + dto.getKmCorp() + "): 월 " + dto.getPriceCorpRate() + "원\n"
                + "- 개인사업자 (" + dto.getKmBiz() + "): 월 " + dto.getPriceBizRate() + "원\n"
                + "- 개인 (" + dto.getKmPersonal() + "): 월 " + dto.getPricePersonal() + "원\n\n"

                + "【포스팅 방향】\n"
                + "- 톤앤매너: " + dto.getTone() + "\n"
                + "- 강조 포인트: " + dto.getHighlight() + "\n"
                + "- 추가 메모: " + (dto.getMemo() != null ? dto.getMemo() : "없음") + "\n\n"

                + "【작성 규칙】\n"
                + "1. 전체 분량 약 1800~2200자 (한글 기준)\n"
                + "2. 구성: 도입부 → 차량 상세 → 렌탈 조건 → 가성비 분석 → 상담 유도\n"
                + "3. SEO 키워드 자연스럽게 포함 (예: " + dto.getBrand() + " " + dto.getCarModel() + " 장기렌트, 중고 장기렌트)\n"
                + "4. 이모지 적절히 활용해 가독성 높이기\n"
                + "5. 마지막 줄에 '📞 렌트제이카 상담: blog.naver.com/rentjcar' 포함\n"
                + "6. 글만 출력하고 다른 설명은 하지 마세요.\n";
    }
}