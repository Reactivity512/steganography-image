package com.github.reactivity512.steganography_image.dto.request;

import com.github.reactivity512.steganography_image.validation.ValidKeyLength;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record DecodeRequest(
        @NotNull(message = "Файл изображения не передан")
        MultipartFile image,

        @NotBlank(message = "Ключ не передан")
        @ValidKeyLength
        String key
) {}
