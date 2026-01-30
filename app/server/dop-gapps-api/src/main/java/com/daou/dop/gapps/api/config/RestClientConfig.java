package com.daou.dop.gapps.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

/**
 * RestClient 설정
 *
 * Virtual Threads와 함께 사용되는 동기식 HTTP 클라이언트
 * - 게이트웨이 프록시용
 * - 외부 API 호출용
 */
@Configuration
public class RestClientConfig {

    /**
     * RestClient.Builder Bean
     *
     * Spring Boot autoconfiguration이 제공하지 않는 경우 직접 생성
     */
    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    /**
     * 공통 RestClient Bean
     *
     * Virtual Threads 환경에서 블로킹 호출이지만 고성능 유지
     */
    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        return builder
                .requestInterceptor(loggingInterceptor())
                .build();
    }

    /**
     * 로깅 인터셉터
     */
    private ClientHttpRequestInterceptor loggingInterceptor() {
        return (request, body, execution) -> {
            // 요청 로깅
            String method = request.getMethod().name();
            String uri = request.getURI().toString();

            // Virtual Thread 확인
            Thread currentThread = Thread.currentThread();
            boolean isVirtual = currentThread.isVirtual();

            System.out.printf("[%s] %s %s (Virtual: %b)%n",
                currentThread.getName(), method, uri, isVirtual);

            // 요청 실행
            return execution.execute(request, body);
        };
    }
}
