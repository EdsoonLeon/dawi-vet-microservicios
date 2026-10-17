package com.dawivet.vacunas.dto;

import java.util.List;

public record ErrorData(String code, List<FieldErrorDetail> details) {
}
