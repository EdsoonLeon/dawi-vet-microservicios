package com.dawivet.vacunas.exception;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.dawivet.vacunas.dto.ErrorResponse;
import com.dawivet.vacunas.dto.FieldErrorDetail;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DomainException.class)
    ResponseEntity<ErrorResponse> handleDomain(DomainException ex, HttpServletRequest request) {
        return response(ex.getStatus(), ex.getMessage(), ex.getCode(), List.of(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorDetail(error.getField(), error.getDefaultMessage()))
                .toList();
        return response(HttpStatus.BAD_REQUEST, "Existen campos inválidos",
                "VALIDACION_CAMPOS", details, request);
    }

    @ExceptionHandler(BindException.class)
    ResponseEntity<ErrorResponse> handleBind(BindException ex, HttpServletRequest request) {
        List<FieldErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorDetail(error.getField(), error.getDefaultMessage()))
                .toList();
        return response(HttpStatus.BAD_REQUEST, "Existen parámetros inválidos",
                "PARAMETROS_INVALIDOS", details, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ErrorResponse> handleConstraint(
            ConstraintViolationException ex, HttpServletRequest request) {
        List<FieldErrorDetail> details = ex.getConstraintViolations().stream()
                .map(violation -> new FieldErrorDetail(
                        lastSegment(violation.getPropertyPath().toString()), violation.getMessage()))
                .toList();
        return response(HttpStatus.BAD_REQUEST, "Existen parámetros inválidos",
                "PARAMETROS_INVALIDOS", details, request);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ResponseEntity<ErrorResponse> handleMethodValidation(
            HandlerMethodValidationException ex, HttpServletRequest request) {
        List<FieldErrorDetail> details = ex.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> new FieldErrorDetail(
                                result.getMethodParameter().getParameterName(),
                                error.getDefaultMessage())))
                .toList();
        return response(HttpStatus.BAD_REQUEST, "Existen parámetros inválidos",
                "PARAMETROS_INVALIDOS", details, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErrorResponse> handleUnreadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST,
                "El cuerpo JSON es inválido o contiene tipos o campos no permitidos",
                "JSON_INVALIDO", List.of(), request);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    ResponseEntity<ErrorResponse> handleParameter(Exception ex, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "Uno de los parámetros tiene un formato inválido",
                "PARAMETROS_INVALIDOS", List.of(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Restricción de datos al procesar {}", request.getRequestURI());
        return response(HttpStatus.CONFLICT, "La operación incumple una restricción de datos",
                "RESTRICCION_DATOS", List.of(), request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ErrorResponse> handleNoResource(
            NoResourceFoundException ex, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, "No se encontró la ruta solicitada",
                "RUTA_NO_ENCONTRADA", List.of(), request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ErrorResponse> handleMethod(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        return response(HttpStatus.METHOD_NOT_ALLOWED, "El método HTTP no está permitido para esta ruta",
                "METODO_NO_PERMITIDO", List.of(), request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Error interno no controlado en {}", request.getRequestURI(), ex);
        return response(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno inesperado", "ERROR_INTERNO", List.of(), request);
    }

    private ResponseEntity<ErrorResponse> response(HttpStatus status, String message,
            String code, List<FieldErrorDetail> details, HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(ErrorResponse.of(message, code, details, request.getRequestURI()));
    }

    private String lastSegment(String path) {
        int separator = path.lastIndexOf('.');
        return separator >= 0 ? path.substring(separator + 1) : path;
    }
}
