import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Veterinario } from '../models/veterinario';

@Injectable({
  providedIn: 'root'
})
export class VeterinarioService {
  private http = inject(HttpClient);
  private readonly urlBase = 'http://localhost:8081/api/veterinarios';

  listarVeterinarios(): Observable<Veterinario[]> {
    return this.http.get<Veterinario[]>(this.urlBase);
  }

  registrarVeterinario(datos: any): Observable<Veterinario> {
    return this.http.post<Veterinario>(`${this.urlBase}/registrar`, datos);
  }

  obtenerPorUsuario(usuarioId: number): Observable<Veterinario | undefined> {
    return this.listarVeterinarios().pipe(
      map(vets => vets.find(v => v.usuario.id === usuarioId))
    );
  }
}