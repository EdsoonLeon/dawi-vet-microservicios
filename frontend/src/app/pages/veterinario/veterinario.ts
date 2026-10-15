import { Component, inject } from '@angular/core';
import { Router, RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { UsuarioAuthService } from '../../services/usuario-auth-service';

@Component({
  selector: 'app-veterinario',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './veterinario.html',
  styleUrl: './veterinario.css'
})
export class Veterinario {
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