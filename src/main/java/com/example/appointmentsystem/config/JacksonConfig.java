package com.example.appointmentsystem.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Hibernate5Module());
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setVisibility(objectMapper.getVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY));
        return objectMapper;
    }
}
