package com.github.reactivity512.steganography_image;

import com.github.reactivity512.steganography_image.config.SteganographyProperties;
import com.github.reactivity512.steganography_image.dto.request.DecodeRequest;
import com.github.reactivity512.steganography_image.validation.KeyLengthValidator;
import jakarta.validation.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;

public class DecodeRequestValidationTest {

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
    @DisplayName("null image → нарушение")
    void nullImage_violation() {
        DecodeRequest req = new DecodeRequest(null, "1234567890123456");
        assertThat(hasViolation(req, "изображения")).isTrue();
    }

    @Test
    @DisplayName("пустой ключ → нарушение")
    void blankKey_violation() {
        DecodeRequest req = new DecodeRequest(image(), "");
        assertThat(hasViolation(req, "Ключ")).isTrue();
    }

    @Test
    @DisplayName("валидный запрос → нарушений нет")
    void valid_noViolations() {
        DecodeRequest req = new DecodeRequest(image(), "1234567890123456");
        assertThat(validator.validate(req)).isEmpty();
    }
}
