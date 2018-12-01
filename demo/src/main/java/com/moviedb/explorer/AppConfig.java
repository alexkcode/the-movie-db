package com.moviedb.explorer;

import com.nurkiewicz.asyncretry.AsyncRetryExecutor;
import com.nurkiewicz.asyncretry.RetryExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class AppConfig {

    @Value("${api.key}")
    private String apiKey;

    @Value("${url}")
    private String server;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getServer() {
        return server;
    }

    public RetryExecutor getRetryExecutor() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        return new AsyncRetryExecutor(scheduler).
                retryOn(CompletionException.class).
                retryOn(HttpClientErrorException.class).
                withFixedBackoff(10000).     //10s times 2 after each retry
                withMaxRetries(100);
    }

    public RetryExecutor getLongRetryExecutor() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        return new AsyncRetryExecutor(scheduler).
                retryOn(CompletionException.class).
                retryOn(HttpClientErrorException.class).
//                withExponentialBackoff(10000, 2).     //1m times 2 after each retry
                withFixedBackoff(11000).
                withMaxRetries(1000);
//                retryInfinitely();
    }
}
