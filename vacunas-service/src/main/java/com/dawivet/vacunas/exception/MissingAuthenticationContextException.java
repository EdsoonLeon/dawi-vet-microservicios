package com.dawivet.vacunas.exception;

public class MissingAuthenticationContextException extends RuntimeException {
    public MissingAuthenticationContextException() {
        super("No existe un encabezado Authorization disponible para la llamada interna");
    }
}
