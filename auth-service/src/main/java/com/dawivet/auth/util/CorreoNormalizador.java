package com.dawivet.auth.util;

import java.util.Locale;

public final class CorreoNormalizador {

    private CorreoNormalizador() {
    }

    public static String normalizar(String correo) {
        return correo == null ? null : correo.trim().toLowerCase(Locale.ROOT);
    }
}
