import { Injectable, inject } from '@angular/core';
// Injectable: marca la clase como servicio inyectable

import { HttpClient } from '@angular/common/http';
// Cliente para hacer peticiones HTTP (GET, POST, etc.)

import { Observable, tap } from 'rxjs';
// Observable: tipo de dato para respuestas asíncronas
// tap: ejecuta una acción sin modificar la respuesta

import { Usuario } from '../models/usuario';
// Interfaz que describe la forma del usuario que devuelve la API

@Injectable({
  providedIn: 'root',
  // registra el servicio globalmente, una sola instancia para toda la app
})
export class UsuarioAuthService {

  private http = inject(HttpClient);
  // instancia para hacer las peticiones

  private readonly urlBase = 'http://localhost:8081/api/login';
  // dirección base de tu API de login (readonly = no se puede reasignar después)

  login(correo: string, clave: string): Observable<Usuario> {
    return this.http.post<Usuario>(`${this.urlBase}/login`, { correo, clave })
    // hace el POST mandando correo y clave, espera un Usuario de vuelta

      .pipe(
        tap(usuario => this.guardarUsuario(usuario))
        // de paso, sin alterar la respuesta, guarda el usuario en localStorage
      );
  }

  
  private guardarUsuario(usuario: Usuario): void {
    localStorage.setItem('usuario', JSON.stringify(usuario));
    // guarda el usuario como texto (JSON) en el navegador
  }
  registrarUsuario(datos: any): Observable<Usuario> {
  return this.http.post<Usuario>(`${this.urlBase}/registrar`, datos);
}

  obtenerUsuario(): Usuario | null {
    const data = localStorage.getItem('usuario');
    // lee lo guardado (o null si no hay nada)

    return data ? JSON.parse(data) : null;
    // si hay dato, lo convierte de texto a objeto; si no, devuelve null
  }

  cerrarSesion(): void {
    localStorage.removeItem('usuario');
    // borra el usuario guardado → simula "logout"
  }

}