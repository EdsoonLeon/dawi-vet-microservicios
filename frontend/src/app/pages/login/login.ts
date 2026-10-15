import { Component, inject } from '@angular/core';
// inject: forma moderna de traer un servicio
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
// Herramientas para formularios reactivos y validaciones
import { Router } from '@angular/router';
// Sirve para navegar entre rutas desde código
import { UsuarioAuthService } from '../../services/usuario-auth-service';
// Servicio que llama a la API de login

@Component({
  selector: 'app-login',
  standalone: true,          // no depende de NgModule
  imports: [ReactiveFormsModule], // necesario por formGroup/formControlName
  templateUrl: './login.html',
  styleUrl: './login.css',
})

export class Login {
  private fb = inject(FormBuilder);          // constructor de formularios
  private authService = inject(UsuarioAuthService); // login contra la API
  private router = inject(Router);           // para redirigir

  mensajeError = '';
  // guarda el error a mostrar en pantalla (pública, el HTML la lee)

  loginForm = this.fb.group({
    correo: ['', [Validators.required, Validators.email]], // obligatorio + formato email
    clave: ['', [Validators.required]]                     // obligatorio
  });

  onSubmit() {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched(); // fuerza mostrar errores de validación
      return; // corta si el form no es válido
    }

    this.mensajeError = ''; // limpia error previo

    const { correo, clave } = this.loginForm.value;
    // saca los valores del formulario

    this.authService.login(correo!, clave!).subscribe({
      // llama a la API (el ! = confío que no es null)

      next: (usuario) => {
        // éxito → llegó el usuario con su rol
        this.redirigirSegunRol(usuario.rol.nombre);
      },

      error: (err) => {
        // falló el login (401, etc.)
        this.mensajeError = 'Error al iniciar sesión. Verifique sus credenciales.';
      }
    });
  }

  private redirigirSegunRol(rol: string) {
  switch (rol) {
    case 'ADMINISTRADOR':
      this.router.navigate(['/admin']);
      break;
    case 'VETERINARIO':
      this.router.navigate(['/veterinario']);
      break;
    case 'RECEPCIONISTA':
      this.router.navigate(['/recepcionista']);
      break;
    default:
      this.mensajeError = 'Rol no reconocido';
    }
  }
}