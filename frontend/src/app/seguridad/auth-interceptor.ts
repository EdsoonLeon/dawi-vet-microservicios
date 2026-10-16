import { inject } from '@angular/core';
import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { UsuarioAuthService } from '../services/usuario-auth-service';

// Interceptor funcional: agrega el token a cada petición (excepto /auth/)
// y, si el backend responde 401/403, limpia la sesión y redirige al login.
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(UsuarioAuthService);
  const router = inject(Router);

  const esRutaAuth = req.url.includes('/auth/');
  const token = authService.obtenerToken();

  const peticion = (!esRutaAuth && token)
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(peticion).pipe(
    catchError((error: HttpErrorResponse) => {
      if (!esRutaAuth && (error.status === 401 || error.status === 403)) {
        authService.cerrarSesion();
        router.navigateByUrl('/login');
      }
      return throwError(() => error);
    })
  );
};
