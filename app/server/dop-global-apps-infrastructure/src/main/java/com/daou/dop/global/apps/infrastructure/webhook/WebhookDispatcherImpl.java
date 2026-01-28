package com.daou.dop.global.apps.infrastructure.webhook;

import com.daou.dop.global.apps.core.repository.WebhookSubscriptionRepository;
import com.daou.dop.global.apps.core.webhook.WebhookDispatcher;
import com.daou.dop.global.apps.domain.enums.WebhookTargetType;
import com.daou.dop.global.apps.domain.webhook.WebhookSubscription;
import com.daou.dop.global.apps.plugin.sdk.WebhookEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 웹훅 이벤트 디스패처 구현체
 * 구독 설정에 따라 HTTP 호출 또는 내부 메서드 호출
 */
@Component
public class WebhookDispatcherImpl implements WebhookDispatcher {

    private static final Logger log = LoggerFactory.getLogger(WebhookDispatcherImpl.class);

    private final WebhookSubscriptionRepository subscriptionRepository;
    private final RestTemplate restTemplate;
    private final ApplicationContext applicationContext;
    private final ObjectMapper objectMapper;

    public WebhookDispatcherImpl(
            WebhookSubscriptionRepository subscriptionRepository,
            RestTemplate restTemplate,
            ApplicationContext applicationContext,
            ObjectMapper objectMapper) {
        this.subscriptionRepository = subscriptionRepository;
        this.restTemplate = restTemplate;
        this.applicationContext = applicationContext;
        this.objectMapper = objectMapper;
    }

    @Override
    public void dispatch(WebhookEvent event) {
        // 매칭되는 구독 조회
        List<WebhookSubscription> subscriptions = subscriptionRepository
                .findMatchingSubscriptions(event.pluginId(), event.eventType(), event.connectionId());

        log.debug("Found {} subscriptions for plugin={}, event={}, connection={}",
                subscriptions.size(), event.pluginId(), event.eventType(), event.connectionId());

        // 각 구독에 대해 디스패치
        for (WebhookSubscription subscription : subscriptions) {
            try {
                if (matchesFilter(subscription, event)) {
                    dispatchToTarget(subscription, event);
                }
            } catch (Exception e) {
                log.error("Dispatch failed: subscriptionId={}, error={}",
                        subscription.getId(), e.getMessage(), e);
                // 실패해도 다른 구독 처리 계속
            }
        }
    }

    private boolean matchesFilter(WebhookSubscription subscription, WebhookEvent event) {
        String filterExpr = subscription.getFilterExpr();
        if (filterExpr == null || filterExpr.isBlank()) {
            return true;
        }

        // TODO: JSONPath 필터 구현
        return true;
    }

    private void dispatchToTarget(WebhookSubscription subscription, WebhookEvent event) {
        WebhookTargetType targetType = subscription.getTargetType();

        switch (targetType) {
            case HTTP -> dispatchHttp(subscription, event);
            case INTERNAL -> dispatchInternal(subscription, event);
        }
    }

    private void dispatchHttp(WebhookSubscription subscription, WebhookEvent event) {
        String url = subscription.getTargetUrl();
        log.info("Dispatching to HTTP: url={}, event={}", url, event.eventType());

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String body = objectMapper.writeValueAsString(event);
            HttpEntity<String> request = new HttpEntity<>(body, headers);

            restTemplate.postForEntity(url, request, Void.class);
            log.info("HTTP dispatch success: url={}", url);
        } catch (Exception e) {
            log.error("HTTP dispatch failed: url={}, error={}", url, e.getMessage());
            throw new RuntimeException("HTTP dispatch failed: " + e.getMessage(), e);
        }
    }

    private void dispatchInternal(WebhookSubscription subscription, WebhookEvent event) {
        String targetMethod = subscription.getTargetMethod();
        log.info("Dispatching to internal: method={}, event={}", targetMethod, event.eventType());

        try {
            // "ServiceName.methodName" 파싱
            String[] parts = targetMethod.split("\\.");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid target method format: " + targetMethod);
            }

            String beanName = uncapitalize(parts[0]);
            String methodName = parts[1];

            // Bean 조회
            Object bean = applicationContext.getBean(beanName);

            // 메서드 조회 및 호출
            Method method = findMethod(bean.getClass(), methodName, WebhookEvent.class);
            if (method == null) {
                throw new NoSuchMethodException("Method not found: " + targetMethod);
            }

            method.invoke(bean, event);
            log.info("Internal dispatch success: method={}", targetMethod);
        } catch (Exception e) {
            log.error("Internal dispatch failed: method={}, error={}", targetMethod, e.getMessage());
            throw new RuntimeException("Internal dispatch failed: " + e.getMessage(), e);
        }
    }

    private Method findMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        try {
            return clazz.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException e) {
            for (Class<?> iface : clazz.getInterfaces()) {
                try {
                    return iface.getMethod(methodName, paramTypes);
                } catch (NoSuchMethodException ignored) {
                }
            }
            return null;
        }
    }

    private String uncapitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }
}
