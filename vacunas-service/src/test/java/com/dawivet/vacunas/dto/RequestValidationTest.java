package com.dawivet.vacunas.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

class RequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void vacunaConCamposInvalidosProduceErroresDeValidacion() {
        var violations = validator.validate(new VacunaRequest("   ", "x".repeat(501), " "));

        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .contains("nombre", "descripcion", "enfermedadPreviene");
    }
}
