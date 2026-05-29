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

        String prompt = buildPrompt(dto);
        RestTemplate restTemplate = new RestTemplate();
        String url = API_URL + "?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);

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
            int statusCode = e.getStatusCode().value();

            if (statusCode == 429) {
                throw new RuntimeException("QUOTA_EXCEEDED");
            } else if (statusCode == 402) {
                throw new RuntimeException("PAYMENT_REQUIRED");
            } else if (statusCode == 403) {
                throw new RuntimeException("FORBIDDEN");
            } else {
                throw new RuntimeException("API_ERROR:" + statusCode);
            }
        }
    }

    private String buildPrompt(BlogRequestDto dto) {
        return "당신은 자동차 장기렌트 전문 블로그 작가입니다.\n" +
                "아래 차량 정보를 바탕으로 네이버 블로그 포스팅 글을 작성해주세요.\n\n" +
                "【작성 조건】\n" +
                "- 총 글자수: 2000자 내외 (한글 기준)\n" +
                "- 말투: 친근하고 정보성 있게\n" +
                "- 구성: 차량 소개 → 렌탈 조건 안내 → 장기렌트 장점 → 상담 유도\n" +
                "- 업체명: 렌트제이카\n" +
                "- 마지막에 상담 문의 유도 문구 포함\n" +
                "- 이모지 적절히 활용\n\n" +
                "【차량 정보】\n" +
                "- 차량 모델: " + dto.getCarModel() + "\n" +
                "- 월 렌탈료: " + dto.getMonthlyFee() + "원\n" +
                "- 계약 기간: " + dto.getContractPeriod() + "\n" +
                "- 보증금: " + dto.getDeposit() + "원\n" +
                "- 약정 주행거리: " + dto.getMileage() + "\n" +
                "- 보험 종류: " + dto.getInsuranceType() + "\n" +
                "- 추가 옵션: " + (dto.getExtraOptions() != null ? dto.getExtraOptions() : "없음") + "\n\n" +
                "위 정보를 바탕으로 블로그 포스팅 글을 작성해주세요.";
    }
}