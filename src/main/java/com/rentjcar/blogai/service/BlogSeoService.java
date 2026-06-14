package com.rentjcar.blogai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class BlogSeoService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    // ─────────────────────────────────────────
    // SEO 분석
    // ─────────────────────────────────────────
    public String analyze(String keyword, String title, String content) {
        return callGemini(buildAnalyzePrompt(keyword, title, content));
    }

    // ─────────────────────────────────────────
    // 포스팅 가이드라인 + 완성본 생성
    // ─────────────────────────────────────────
    public String generateGuideAndPost(String keyword, String restaurantName,
                                       String address, String phone,
                                       String category, String hours,
                                       String menu, String myReview) {
        return callGemini(buildGuideAndPostPrompt(keyword, restaurantName, address, phone, category, hours, menu, myReview));
    }

    // ─────────────────────────────────────────
    // SEO 분석 프롬프트
    // ─────────────────────────────────────────
    private String buildAnalyzePrompt(String keyword, String title, String content) {
        return "너는 네이버 블로그 SEO 컨설턴트이자 맛집 전문 에디터다.\n"
                + "아래 블로그 글을 분석해서 항목별로 평가해줘.\n\n"
                + "목표:\n"
                + "- 네이버 검색 상위 노출\n"
                + "- 자연스러운 후기\n"
                + "- 광고 느낌 최소화\n"
                + "- 실제 방문 후기처럼 작성\n\n"
                + "【입력 정보】\n"
                + "메인 키워드: " + keyword + "\n"
                + "제목: " + title + "\n"
                + "본문:\n" + content + "\n\n"
                + "【출력 형식 - 반드시 아래 형식 그대로】\n"
                + "[SEO 점수] 00점 / 100점\n\n"
                + "[잘된 점]\n* 내용\n\n"
                + "[수정이 필요한 점]\n* 내용\n\n"
                + "[수정 추천 문장]\n기존: \n수정: \n\n"
                + "[삭제하면 좋은 문장]\n* 내용\n\n"
                + "[추가하면 좋은 문장]\n* 내용\n\n"
                + "[예상 노출 키워드 TOP 5]\n1. 2. 3. 4. 5.\n\n"
                + "[상위노출 가능성 평가]\n내용\n\n"
                + "[최종 업로드 버전]\n(SEO 최적화된 본문 전체 다시 작성)\n\n"
                + "[추천 제목 TOP 10]\n1. 2. 3. 4. 5. 6. 7. 8. 9. 10.\n\n"
                + "[최종 의견]\n"
                + "이 글이 어떤 키워드로 노출될 가능성이 높은지 설명.\n"
                + "상위 노출을 위한 가장 중요한 수정사항 3가지 설명.\n";
    }

    // ─────────────────────────────────────────
    // 가이드라인 + 포스팅 완성본 프롬프트
    // ─────────────────────────────────────────
    private String buildGuideAndPostPrompt(String keyword, String restaurantName,
                                           String address, String phone,
                                           String category, String hours,
                                           String menu, String myReview) {
        return "너는 네이버 블로그 SEO 전문가이자 10년 경력의 맛집 블로거야.\n"
                + "아래 식당 정보와 실제 방문 후기를 바탕으로 ① 포스팅 가이드라인과 ② 실제 블로그 포스팅 완성본을 작성해줘.\n\n"
                + "【식당 정보】\n"
                + "식당명: " + restaurantName + "\n"
                + "주소: " + address + "\n"
                + "전화: " + (phone.isEmpty() ? "없음" : phone) + "\n"
                + "카테고리: " + (category.isEmpty() ? "없음" : category) + "\n"
                + "영업시간: " + (hours.isEmpty() ? "없음" : hours) + "\n"
                + "대표메뉴: " + (menu.isEmpty() ? "없음" : menu) + "\n"
                + "메인 키워드: " + keyword + "\n\n"
                + "【실제 방문 후기 (이 내용을 자연스럽게 녹여서 써줘)】\n"
                + (myReview.isEmpty() ? "없음" : myReview) + "\n\n"

                + "【포스팅 완성본 작성 규칙】\n"
                + "1. 전체 분량 1500~2000자\n"
                + "2. 구어체, 친근한 말투 (예: ~했어요, ~더라고요, ~이에요)\n"
                + "3. '~습니다' 체 절대 금지, 구어체로만\n"
                + "4. 문장이 너무 길지 않게 (2~3줄마다 줄바꿈)\n"
                + "5. 이모지 자연스럽게 활용 (과하지 않게)\n"
                + "6. 도입부 첫 문장에 키워드 자연스럽게 포함\n"
                + "7. 위치, 영업시간, 메뉴, 가격 정보 자연스럽게 녹이기\n"
                + "8. 마무리에 재방문 의사 자연스럽게 표현\n"
                + "9. 문장마다 같은 패턴 반복 금지\n"
                + "10. 광고성 문구 절대 금지 (협찬, 내돈내산 강조 금지)\n"
                + "11. 실제로 방문한 사람처럼 생생하게\n\n"

                + "【출력 형식 - 반드시 아래 구분자 그대로】\n"
                + "===GUIDE_START===\n"
                + "[추천 제목 5개]\n"
                + "1. (제목)\n2. (제목)\n3. (제목)\n4. (제목)\n5. (제목)\n\n"
                + "[예상 노출 키워드 TOP 5]\n"
                + "1. 2. 3. 4. 5.\n\n"
                + "[포스팅 구성 가이드]\n"
                + "▶ 도입부: (가이드)\n"
                + "▶ 식당 기본 정보: (가이드)\n"
                + "▶ 메뉴 소개: (가이드)\n"
                + "▶ 맛 & 분위기: (가이드)\n"
                + "▶ 사진 배치: (가이드)\n"
                + "▶ 마무리: (가이드)\n\n"
                + "[꼭 넣어야 할 정보]\n"
                + "□ 항목\n\n"
                + "[피해야 할 표현]\n"
                + "* 내용\n"
                + "===GUIDE_END===\n"
                + "===POST_START===\n"
                + "(블로그 포스팅 완성본 전체)\n"
                + "===POST_END===\n";
    }

    // ─────────────────────────────────────────
    // Gemini API 공통 호출
    // ─────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private String callGemini(String prompt) {
        RestTemplate restTemplate = new RestTemplate();
        String url = GEMINI_URL + "?key=" + geminiApiKey;

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
            List<Map<String, Object>> candidates =
                    (List<Map<String, Object>>) response.getBody().get("candidates");
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
}