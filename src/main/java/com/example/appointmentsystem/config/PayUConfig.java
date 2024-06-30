package com.example.appointmentsystem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PayUConfig {

    @Value("${payu.client.id}")
    private String clientId;

    @Value("${payu.client.secret}")
    private String clientSecret;

    @Value("${payu.api.url}")
    private String apiUrl;

    @Value("${payu.notify.url}")
    private String notifyUrl;

    @Value("${payu.continue.url}")
    private String continueUrl;

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public String getContinueUrl() {
        return continueUrl;
    }
}
