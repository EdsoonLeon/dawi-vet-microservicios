import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Veterinario } from '../models/veterinario';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class VeterinarioService {
  private http = inject(HttpClient);
  private readonly urlBase = `${environment.apiUrl}/veterinarios`;

  listarVeterinarios(): Observable<Veterinario[]> {
    return this.http.get<Veterinario[]>(this.urlBase);
  }

  obtenerPorId(id: number): Observable<Veterinario> {
    return this.http.get<Veterinario>(`${this.urlBase}/${id}`);
  }

  registrarVeterinario(datos: any): Observable<Veterinario> {
    return this.http.post<Veterinario>(`${this.urlBase}/registrar`, datos);
  }

  editarVeterinario(id: number, datos: any): Observable<Veterinario> {
    return this.http.put<Veterinario>(`${this.urlBase}/${id}`, datos);
  }

  eliminarVeterinario(id: number): Observable<any> {
    return this.http.delete(`${this.urlBase}/${id}`, { responseType: 'text' });
  }

  activarVeterinario(id: number): Observable<Veterinario> {
    return this.http.patch<Veterinario>(`${this.urlBase}/${id}/activar`, {});
  }

  obtenerPorUsuario(usuarioId: number): Observable<Veterinario | undefined> {
    return this.listarVeterinarios().pipe(
      map(vets => vets.find(v => v.usuarioId === usuarioId))
    );
  }
}
