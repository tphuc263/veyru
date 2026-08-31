package com.veyru.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "cloudinary")
public record CloudinaryProperties(
    @NotBlank String cloudName,
    @NotBlank String apiKey,
    @NotBlank String apiSecret,
    boolean secure) {}
