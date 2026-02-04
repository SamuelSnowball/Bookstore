package com.example.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

/**
 * Jackson configuration to support immutable POJOs (like jOOQ-generated classes).
 * Enables constructor-based deserialization using parameter names.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        
        // Register ParameterNamesModule to enable constructor-based deserialization
        objectMapper.registerModule(new ParameterNamesModule());
        
        // Don't fail on unknown properties
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // Don't include null values in serialization
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        
        return objectMapper;
    }
}
