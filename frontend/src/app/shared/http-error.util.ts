import { HttpErrorResponse } from '@angular/common/http';

/**
 * Extrae un mensaje legible de un error HTTP para mostrarlo en la UI.
 * El backend (vía API Gateway) puede responder con { mensaje }, { message }
 * o un texto plano; si no se reconoce la forma, se usa el mensaje por defecto.
 */
export function mensajeDeError(err: unknown, mensajePorDefecto: string): string {
  if (err instanceof HttpErrorResponse) {
    const cuerpo = err.error;
    if (typeof cuerpo === 'string' && cuerpo.trim()) return cuerpo;
    if (cuerpo && typeof cuerpo === 'object') {
      if (typeof cuerpo.mensaje === 'string' && cuerpo.mensaje) return cuerpo.mensaje;
      if (typeof cuerpo.message === 'string' && cuerpo.message) return cuerpo.message;
      if (typeof cuerpo.error === 'string' && cuerpo.error) return cuerpo.error;
    }
    if (err.status === 0) return 'No se pudo conectar con el servidor.';
  }
  return mensajePorDefecto;
}
