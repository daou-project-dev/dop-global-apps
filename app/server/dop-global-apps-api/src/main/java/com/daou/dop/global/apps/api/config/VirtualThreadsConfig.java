package com.daou.dop.global.apps.api.config;

import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.task.SimpleAsyncTaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Virtual Threads 설정
 *
 * Spring Boot 4.0 + Virtual Threads 통합
 * - 비동기 작업에 Virtual Threads 사용
 * - @Async 메서드에 적용
 */
@Configuration
@EnableAsync
public class VirtualThreadsConfig {

    /**
     * Virtual Threads 기반 AsyncTaskExecutor
     *
     * Spring Boot 4.0에서는 spring.threads.virtual.enabled=true 설정 시
     * 자동으로 Virtual Threads를 사용하지만, 명시적 설정 가능
     */
    @Bean(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    public AsyncTaskExecutor asyncTaskExecutor(SimpleAsyncTaskExecutorBuilder builder) {
        return builder
                .virtualThreads(true)
                .threadNamePrefix("vt-")
                .build();
    }
}
