import { HttpErrorResponse } from '@angular/common/http';

const MENSAJES_HTTP: Record<number, string> = {
  0: 'No se pudo conectar con el servidor. Inténtalo nuevamente.',
  401: 'La sesión no es válida o las credenciales son incorrectas.',
  403: 'No tienes permisos para realizar esta operación.',
  404: 'No se encontró el recurso solicitado.',
  409: 'El registro entra en conflicto con datos existentes.',
  500: 'Ocurrió un error interno. Inténtalo nuevamente.'
};

const DETALLE_TECNICO = /(?:\bstack\s*trace\b|\bexception\b|\bSQLException\b|\bSQLState\b|\bSQL\b|\bselect\s+.+\s+from\b|\binsert\s+into\b|\bupdate\s+.+\s+set\b|\bdelete\s+from\b|\btoken\s+(?:jwt\s+)?(?:inv[aá]lido|expired)|at\s+[\w.$]+\([^)]*:\d+\))/i;

function mensajeSeguro(valor: unknown): string | null {
  if (typeof valor !== 'string') return null;
  const texto = valor.trim();
  return texto && texto.length <= 240 && !DETALLE_TECNICO.test(texto) ? texto : null;
}

function mensajeValidacion(valor: unknown): string | null {
  if (!valor || typeof valor !== 'object') return null;
  if (Array.isArray(valor)) {
    const mensajes = valor.map(item => {
      if (typeof item === 'string') return mensajeSeguro(item);
      if (item && typeof item === 'object') {
        const detalle = item as Record<string, unknown>;
        return mensajeSeguro(detalle['defaultMessage']) ?? mensajeSeguro(detalle['message']);
      }
      return null;
    }).filter((item): item is string => !!item);
    return mensajes.length ? [...new Set(mensajes)].join(' ') : null;
  }
  const mensajes = Object.values(valor).map(mensajeSeguro).filter((item): item is string => !!item);
  return mensajes.length ? [...new Set(mensajes)].join(' ') : null;
}

/** Extrae un mensaje seguro de los formatos de error devueltos por los servicios. */
export function mensajeDeError(err: unknown, mensajePorDefecto: string): string {
  if (err instanceof HttpErrorResponse) {
    const cuerpo = err.error;
    if (cuerpo && typeof cuerpo === 'object') {
      const data = cuerpo as Record<string, unknown>;
      const validacion = mensajeValidacion(data['errores'])
        ?? mensajeValidacion(data['errors'])
        ?? (data['error'] && typeof data['error'] === 'object'
          ? mensajeValidacion((data['error'] as Record<string, unknown>)['details'])
          : null);
      if (validacion) return validacion;

      const mensaje = mensajeSeguro(data['detail'])
        ?? mensajeSeguro(data['message'])
        ?? mensajeSeguro(data['mensaje'])
        ?? mensajeSeguro(data['error']);
      if (mensaje) return mensaje;
    }
    const texto = mensajeSeguro(cuerpo);
    if (texto) return texto;
    if (err.status === 401 && /iniciar sesión|credenciales/i.test(mensajePorDefecto)) {
      return 'Credenciales incorrectas. Verifica tu correo y contraseña.';
    }
    return MENSAJES_HTTP[err.status] ?? mensajePorDefecto;
  }
  return mensajePorDefecto;
}
