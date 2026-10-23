package com.github.reactivity512.steganography_image.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "steganography")
public record SteganographyProperties(
    @Min(1) @Max(2) int secureLevel,
    @Min(3) int minKeyBytes,
    @Min(16) int maxKeyBytes
) {
}
