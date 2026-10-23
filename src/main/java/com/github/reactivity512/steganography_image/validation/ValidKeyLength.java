package com.github.reactivity512.steganography_image.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = KeyLengthValidator.class)
@Component
public @interface ValidKeyLength {
    String message() default "Недопустимая длина ключа";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
