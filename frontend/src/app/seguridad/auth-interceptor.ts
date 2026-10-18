import { inject } from '@angular/core';
import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { UsuarioAuthService } from '../services/usuario-auth-service';

// El login es público; el registro y el resto de peticiones usan el JWT disponible.
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(UsuarioAuthService);
  const router = inject(Router);

  const esLogin = req.method === 'POST' && /\/api\/auth\/login(?:\?|$)/.test(req.urlWithParams);
  const token = authService.obtenerToken();

  const peticion = (!esLogin && token)
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(peticion).pipe(
    catchError((error: HttpErrorResponse) => {
      if (!esLogin && error.status === 401 && router.url !== '/login') {
        authService.cerrarSesion();
        router.navigateByUrl('/login');
      }
      return throwError(() => error);
    })
  );
};
