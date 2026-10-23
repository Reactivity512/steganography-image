package com.github.reactivity512.steganography_image.validation;

import com.github.reactivity512.steganography_image.config.SteganographyProperties;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.nio.charset.StandardCharsets;

public class KeyLengthValidator implements ConstraintValidator<ValidKeyLength, String> {
    private final SteganographyProperties props;

    public KeyLengthValidator(SteganographyProperties props) { // Spring внедрит
        this.props = props;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        if (value == null) return true;
        int len = value.getBytes(StandardCharsets.UTF_8).length;
        if (len < props.minKeyBytes()) {
            return fail(ctx, "Ключ слишком короткий (минимум " + props.minKeyBytes() + " байт)");
        }
        if (len > props.maxKeyBytes()) {
            return fail(ctx, "Ключ слишком велик (макс. " + props.maxKeyBytes() + " байт)");
        }
        return true;
    }

    private boolean fail(ConstraintValidatorContext ctx, String msg) {
        ctx.disableDefaultConstraintViolation();
        ctx.buildConstraintViolationWithTemplate(msg).addConstraintViolation();
        return false;
    }
}
