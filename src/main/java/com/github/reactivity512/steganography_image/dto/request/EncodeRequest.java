package com.github.reactivity512.steganography_image.dto.request;

import com.github.reactivity512.steganography_image.validation.ValidKeyLength;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EncodeRequest (
        @NotNull(message = "Файл изображения не передан")
        org.springframework.web.multipart.MultipartFile image,

        @NotBlank(message = "Текст для сокрытия пуст")
        String text,

        @NotBlank(message = "Ключ не передан")
        @ValidKeyLength
        String key
) {}
