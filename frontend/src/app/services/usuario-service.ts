import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable ,map} from 'rxjs';
import { Usuario } from '../models/usuario';


@Injectable({
  providedIn: 'root'
})
export class UsuarioService {
  private http = inject(HttpClient);
  private readonly urlBase = 'http://localhost:8081/api/usuarios';

  listarUsuarios(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(this.urlBase);
  }

  obtenerPorId(id: number): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.urlBase}/${id}`);
  }

  editarUsuario(id: number, usuario: any): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.urlBase}/${id}`, usuario);
  }

  eliminarUsuario(id: number): Observable<any> {
    return this.http.delete(`${this.urlBase}/${id}`, { responseType: 'text' });
  }

  activarUsuario(id: number): Observable<Usuario> {
    return this.http.patch<Usuario>(`${this.urlBase}/${id}/activar`, {});
  }

  verificarActivo(id: number): Observable<boolean> {
  return this.obtenerPorId(id).pipe(
    map(usuario => usuario.activo)
  );
}
}