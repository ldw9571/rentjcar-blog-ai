package com.rentjcar.blogai.service;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;

@Service
public class UsageService {

    private static final int DAILY_LIMIT = 250;
    private static final String FILE_PATH = "usage_count.json";

    // ─────────────────────────────────────────
    // 오늘 남은 횟수 조회
    // ─────────────────────────────────────────
    public int getRemainingCount() {
        String today = LocalDate.now().toString();
        if (!today.equals(readField("date"))) return DAILY_LIMIT;
        int used = parseIntSafe(readField("used"));
        return Math.max(0, DAILY_LIMIT - used);
    }

    // ─────────────────────────────────────────
    // 오늘 사용한 횟수 조회
    // ─────────────────────────────────────────
    public int getUsedCount() {
        String today = LocalDate.now().toString();
        if (!today.equals(readField("date"))) return 0;
        return parseIntSafe(readField("used"));
    }

    // ─────────────────────────────────────────
    // 사용 가능 여부 확인
    // ─────────────────────────────────────────
    public boolean canUse() {
        return getRemainingCount() > 0;
    }

    // ─────────────────────────────────────────
    // 사용 횟수 1 증가
    // ─────────────────────────────────────────
    public void increaseCount() {
        String today = LocalDate.now().toString();
        int used;
        if (!today.equals(readField("date"))) {
            used = 1;
        } else {
            used = parseIntSafe(readField("used")) + 1;
        }
        writeFile(today, used);
    }

    // ─────────────────────────────────────────
    // 전체 한도 반환
    // ─────────────────────────────────────────
    public int getDailyLimit() {
        return DAILY_LIMIT;
    }

    // ─────────────────────────────────────────
    // JSON에서 특정 키 값 읽기
    // {"date":"2026-05-31","used":3} 형태만 처리
    // ─────────────────────────────────────────
    private String readField(String key) {
        try {
            Path path = Paths.get(FILE_PATH);
            if (!Files.exists(path)) return null;
            String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8).trim();
            String pattern = "\"" + key + "\":";
            int idx = content.indexOf(pattern);
            if (idx < 0) return null;
            int start = idx + pattern.length();
            char first = content.charAt(start);
            if (first == '"') {
                int end = content.indexOf('"', start + 1);
                return content.substring(start + 1, end);
            } else {
                int end = start;
                while (end < content.length() && Character.isDigit(content.charAt(end))) end++;
                return content.substring(start, end);
            }
        } catch (Exception e) {
            return null;
        }
    }

    // ─────────────────────────────────────────
    // JSON 파일 쓰기
    // ─────────────────────────────────────────
    private void writeFile(String date, int used) {
        try {
            String json = "{\"date\":\"" + date + "\",\"used\":" + used + "}";
            Files.write(
                    Paths.get(FILE_PATH),
                    json.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (Exception e) {
            // 저장 실패 시 서비스 중단 방지
        }
    }

    private int parseIntSafe(String val) {
        try { return Integer.parseInt(val); }
        catch (Exception e) { return 0; }
    }
}