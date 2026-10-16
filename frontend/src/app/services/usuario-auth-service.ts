import { Injectable, inject } from '@angular/core';
// Injectable: marca la clase como servicio inyectable

import { HttpClient } from '@angular/common/http';
// Cliente para hacer peticiones HTTP (GET, POST, etc.)

import { Observable, map, tap } from 'rxjs';
// Observable: tipo de dato para respuestas asíncronas
// tap: ejecuta una acción sin modificar la respuesta

import { Usuario } from '../models/usuario';
// Interfaz que describe la forma del usuario que devuelve la API
import { environment } from '../../environments/environment';

// ---------------------------------------------------------------------------
// SUPUESTO sobre la forma de la respuesta de POST /api/auth/login.
// El contrato solo dice "devuelve token (y datos del usuario)", sin detallar
// la forma exacta. Se asume que el backend responde:
//   { token: string, usuario: { id, nombre, apellido, correo, rol: {id, nombre}, activo } }
// Si el backend devuelve los datos del usuario "planos" junto al token
// (sin el wrapper "usuario"), este es el ÚNICO lugar que hay que ajustar:
// la interfaz LoginResponse y la función mapearRespuestaLogin() de abajo.
// ---------------------------------------------------------------------------
interface LoginResponse {
  token: string;
  usuario: Usuario;
}

function mapearRespuestaLogin(resp: LoginResponse): { token: string; usuario: Usuario } {
  return { token: resp.token, usuario: resp.usuario };
}

@Injectable({
  providedIn: 'root',
  // registra el servicio globalmente, una sola instancia para toda la app
})
export class UsuarioAuthService {

  private http = inject(HttpClient);
  // instancia para hacer las peticiones

  private readonly urlBase = `${environment.apiUrl}/auth`;
  // dirección base de la API de autenticación, a través del API Gateway

  login(correo: string, clave: string): Observable<Usuario> {
    return this.http.post<LoginResponse>(`${this.urlBase}/login`, { correo, clave })
    // hace el POST mandando correo y clave, espera token + usuario de vuelta

      .pipe(
        map(mapearRespuestaLogin),
        tap(({ token, usuario }) => this.guardarSesion(token, usuario)),
        map(({ usuario }) => usuario)
      );
  }

  private guardarSesion(token: string, usuario: Usuario): void {
    localStorage.setItem('token', token);
    localStorage.setItem('usuario', JSON.stringify(usuario));
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

  obtenerToken(): string | null {
    return localStorage.getItem('token');
  }

  cerrarSesion(): void {
    localStorage.removeItem('usuario');
    localStorage.removeItem('token');
    // borra el usuario y el token guardados → simula "logout"
  }

}
