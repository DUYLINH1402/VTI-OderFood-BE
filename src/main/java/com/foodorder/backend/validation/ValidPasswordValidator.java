package com.foodorder.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidPasswordValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isBlank()) {
            return buildViolation(context, "PASSWORD_INVALID_EMPTY");
        }
        if (password.length() < 6) {
            return buildViolation(context, "PASSWORD_INVALID_LENGTH");
        }
        if (!password.matches(".*[A-Z].*")) {
            return buildViolation(context, "PASSWORD_INVALID_UPPERCASE");
        }
        if (!password.matches(".*\\d.*")) {
            return buildViolation(context, "PASSWORD_INVALID_NUMBER");
        }

        return true;
    }

    private boolean buildViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        return false;
    }
}
