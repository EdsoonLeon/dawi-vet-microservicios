import { Component, inject } from '@angular/core';
import { Router, RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { UsuarioAuthService } from '../../services/usuario-auth-service';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './admin.html',
  styleUrl: './admin.css'
})
export class Admin {
  private authService = inject(UsuarioAuthService);
  private router = inject(Router);

  usuario = this.authService.obtenerUsuario();

  get iniciales(): string {
    if (!this.usuario) return '';
    return (this.usuario.nombre[0] ?? '') + (this.usuario.apellido[0] ?? '');
  }

  cerrarSesion() {
    this.authService.cerrarSesion();
    this.router.navigate(['/login']);
  }
}