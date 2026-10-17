package com.dawivet.auth.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> manejarValidacion(MethodArgumentNotValidException exception) {
        Map<String, String> errores = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> errores.putIfAbsent(error.getField(), error.getDefaultMessage()));

        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "Datos de entrada inválidos");
        problema.setProperty("errores", errores);
        return ResponseEntity.badRequest().body(problema);
    }

    @ExceptionHandler({ConstraintViolationException.class, HandlerMethodValidationException.class})
    ResponseEntity<ProblemDetail> manejarParametrosInvalidos(Exception exception) {
        return respuesta(HttpStatus.BAD_REQUEST, "Parámetro inválido");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ProblemDetail> manejarJsonInvalido(HttpMessageNotReadableException exception) {
        return respuesta(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud no es válido");
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ProblemDetail> manejarCredencialesInvalidas(BadCredentialsException exception) {
        return respuesta(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ProblemDetail> manejarEstado(ResponseStatusException exception) {
        String mensaje = exception.getReason() == null ? "No se pudo procesar la solicitud" : exception.getReason();
        return respuesta(exception.getStatusCode(), mensaje);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ProblemDetail> manejarIntegridad(DataIntegrityViolationException exception) {
        return respuesta(HttpStatus.CONFLICT, "Conflicto con los datos existentes");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ProblemDetail> manejarRutaInexistente(NoResourceFoundException exception) {
        return respuesta(HttpStatus.NOT_FOUND, "Recurso no encontrado");
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> manejarErrorInesperado(Exception exception) {
        log.error("Error inesperado en auth-service: {}", exception.getClass().getSimpleName());
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
    }

    private ResponseEntity<ProblemDetail> respuesta(HttpStatusCode estado, String mensaje) {
        return ResponseEntity.status(estado).body(problema(estado, mensaje));
    }

    private ProblemDetail problema(HttpStatusCode estado, String mensaje) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(estado, mensaje);
        HttpStatus httpStatus = HttpStatus.resolve(estado.value());
        problema.setTitle(httpStatus == null ? "Error" : httpStatus.getReasonPhrase());
        return problema;
    }
}
