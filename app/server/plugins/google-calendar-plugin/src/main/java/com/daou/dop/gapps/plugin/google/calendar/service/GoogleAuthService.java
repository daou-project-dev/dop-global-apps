package com.daou.dop.gapps.plugin.google.calendar.service;

import com.daou.dop.gapps.plugin.sdk.CredentialContext;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;

/**
 * Google 인증 서비스
 * - Service Account JSON 키 파일 기반 인증
 * - Domain-Wide Delegation 지원
 * - Application Default Credentials (ADC) 지원
 */
public class GoogleAuthService {

    private static final Logger log = LoggerFactory.getLogger(GoogleAuthService.class);
    private static final String APPLICATION_NAME = "DaouOffice-GlobalApps";

    // TODO: 운영 시 제거 - 로컬 테스트용 하드코딩 경로
    private static final String LOCAL_JSON_KEY_PATH =
            System.getProperty("user.home") + "/.config/gcloud/application_default_credentials.json";

    /**
     * CredentialContext에서 Calendar 서비스 생성
     * TODO: 운영 시 하드코딩 경로 제거 필요
     */
    public Calendar createCalendarService(CredentialContext credential) throws IOException, GeneralSecurityException {
        // 로컬 테스트: 하드코딩 경로 사용
        GoogleCredentials credentials = loadCredentialsFromFile(LOCAL_JSON_KEY_PATH);
        credentials = credentials.createScoped(Collections.singletonList(CalendarScopes.CALENDAR));
        credentials.refreshIfExpired();
        log.debug("Using local JSON key: {}", LOCAL_JSON_KEY_PATH);
        return buildCalendarService(credentials);
    }

    /**
     * 파일 경로에서 Credentials 로드
     */
    private GoogleCredentials loadCredentialsFromFile(String filePath) throws IOException {
        try (InputStream inputStream = new FileInputStream(filePath)) {
            return GoogleCredentials.fromStream(inputStream);
        }
    }

    /**
     * Calendar 서비스 빌드
     */
    private Calendar buildCalendarService(GoogleCredentials credentials) throws GeneralSecurityException, IOException {
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                requestInitializer)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
