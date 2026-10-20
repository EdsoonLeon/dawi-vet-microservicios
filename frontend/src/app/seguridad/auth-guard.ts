import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map, catchError, of } from 'rxjs';
import { UsuarioAuthService } from '../services/usuario-auth-service';
import { UsuarioService } from '../services/usuario-service';

export const authGuard: CanActivateFn = (route) => {
  const authService = inject(UsuarioAuthService);
  const usuarioService = inject(UsuarioService);
  const router = inject(Router);

  const usuario = authService.obtenerUsuario();

  if (!usuario || !authService.obtenerToken()) {
    router.navigateByUrl('/login');
    return false;
  }

  const rolesPermitidos = route.data['roles'] as string[] | undefined;
  if (rolesPermitidos && !rolesPermitidos.includes(usuario.rol.nombre)) {
    router.navigateByUrl('/login');
    return false;
  }

  if (!usuario.activo) {
    authService.cerrarSesion();
    router.navigateByUrl('/login');
    return false;
  }

  // /api/usuarios/{id} está restringido a ADMIN en el backend.
  if (usuario.rol.nombre !== 'ADMIN') return true;

  return usuarioService.verificarActivo(usuario.id).pipe(
    map(activo => {
      if (!activo) {
        authService.cerrarSesion();
        router.navigateByUrl('/login');
        return false;
      }
      return true;
    }),
    catchError(() => {
      authService.cerrarSesion();
      router.navigateByUrl('/login');
      return of(false);
    })
  );
};
