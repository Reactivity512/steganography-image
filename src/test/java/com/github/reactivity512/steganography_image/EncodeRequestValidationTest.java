package com.github.reactivity512.steganography_image;

import com.github.reactivity512.steganography_image.config.SteganographyProperties;
import com.github.reactivity512.steganography_image.dto.request.EncodeRequest;
import com.github.reactivity512.steganography_image.validation.KeyLengthValidator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class EncodeRequestValidationTest {
    private static final int MIN_KEY = 16;
    private static final int MAX_KEY = 256;

    private Validator validator;

    @BeforeEach
    void setUp() {
        SteganographyProperties props = new SteganographyProperties(2, MIN_KEY, MAX_KEY);
        KeyLengthValidator keyValidator = new KeyLengthValidator(props);

        ValidatorFactory factory = Validation.byDefaultProvider()
                .configure()
                .constraintValidatorFactory(new ConstraintValidatorFactory() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
                        if (key == KeyLengthValidator.class) {
                            return (T) keyValidator;
                        }
                        try {
                            return key.getDeclaredConstructor().newInstance();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    @Override
                    public void releaseInstance(ConstraintValidator<?, ?> instance) {}
                })
                .buildValidatorFactory();
        validator = factory.getValidator();
    }

    private MockMultipartFile image() {
        return new MockMultipartFile("image", "test.png", "image/png", new byte[]{1, 2, 3});
    }

    private boolean hasViolation(Object req, String fragment) {
        return validator.validate(req).stream()
                .anyMatch(v -> v.getMessage().contains(fragment));
    }

    @Test
    @DisplayName("null image → нарушение @NotNull")
    void nullImage_violation() {
        EncodeRequest req = new EncodeRequest(null, "hi", "1234567890123456");
        assertThat(hasViolation(req, "изображения")).isTrue();
    }

    @Test
    @DisplayName("пустой текст → нарушение @NotBlank")
    void blankText_violation() {
        EncodeRequest req = new EncodeRequest(image(), "   ", "1234567890123456");
        assertThat(hasViolation(req, "Текст")).isTrue();
    }

    @Test
    @DisplayName("пустой ключ → нарушение @NotBlank")
    void blankKey_violation() {
        EncodeRequest req = new EncodeRequest(image(), "hi", "");
        assertThat(hasViolation(req, "Ключ")).isTrue();
    }

    @Test
    @DisplayName("короткий ключ → нарушение @ValidKeyLength")
    void shortKey_violation() {
        EncodeRequest req = new EncodeRequest(image(), "hi", "short");
        assertThat(hasViolation(req, "короткий")).isTrue();
    }

    @Test
    @DisplayName("длинный ключ → нарушение @ValidKeyLength")
    void longKey_violation() {
        String longKey = "x".repeat(MAX_KEY + 1);
        EncodeRequest req = new EncodeRequest(image(), "hi", longKey);
        assertThat(hasViolation(req, "велик")).isTrue();
    }

    @Test
    @DisplayName("валидные данные → нарушений нет")
    void valid_noViolations() {
        EncodeRequest req = new EncodeRequest(image(), "hi", "1234567890123456");
        assertThat(validator.validate(req)).isEmpty();
    }

    @Test
    @DisplayName("ключ считается в байтах UTF-8, а не в символах")
    void keyLengthInBytes_utf8() {
        // 8 кириллических символов = 16 байт в UTF-8 → проходит
        String key8 = "ключключ";
        assertThat(key8.getBytes(StandardCharsets.UTF_8).length).isEqualTo(16);

        EncodeRequest req = new EncodeRequest(image(), "hi", key8);
        assertThat(validator.validate(req)).isEmpty();
    }
}
