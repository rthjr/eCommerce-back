package com.ecommerce.user.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

/**
 * Configuration for asynchronous email processing.
 * Enables @Async support and configures a dedicated thread pool for email operations.
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig implements AsyncConfigurer {

    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);  // Minimum threads
        executor.setMaxPoolSize(5);   // Maximum threads for email sending
        executor.setQueueCapacity(100); // Queue size for pending emails
        executor.setThreadNamePrefix("email-async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        // Rejection policy: Log and run in caller thread as fallback
        executor.setRejectedExecutionHandler((r, executor1) -> {
            log.warn("Email task queue is full! Running in caller thread as fallback.");
            r.run();
        });

        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new CustomAsyncExceptionHandler();
    }

    /**
     * Custom exception handler for async email operations
     */
    public static class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
        @Override
        public void handleUncaughtException(Throwable throwable, Method method, Object... params) {
            log.error("Async email operation failed in method: {}", method.getName(), throwable);
            log.error("Method parameters: {}", (Object[]) params);

            // Could add alerting here (e.g., send to monitoring system)
            // For now, just ensure it's logged
        }
    }
}
