import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Cita } from '../models/cita';

@Injectable({
  providedIn: 'root'
})
export class CitaService {
  private http = inject(HttpClient);
  private readonly urlBase = 'http://localhost:8081/api/citas';

  registrarCita(cita: any): Observable<Cita> {
    return this.http.post<Cita>(`${this.urlBase}/registrar`, cita);
  }

  listarCitas(): Observable<Cita[]> {
    return this.http.get<Cita[]>(this.urlBase);
  }

  listarPorVeterinario(veterinarioId: number): Observable<Cita[]> {
    return this.http.get<Cita[]>(`${this.urlBase}/veterinario/${veterinarioId}`);
  }

  obtenerPorId(id: number): Observable<Cita> {
    return this.http.get<Cita>(`${this.urlBase}/${id}`);
  }

  editarCita(id: number, cita: any): Observable<Cita> {
    return this.http.put<Cita>(`${this.urlBase}/${id}`, cita);
  }

  cambiarEstado(id: number, estado: string): Observable<Cita> {
    return this.http.patch<Cita>(`${this.urlBase}/${id}/estado`, { estado });
  }

  cancelarCita(id: number): Observable<any> {
    return this.http.delete(`${this.urlBase}/${id}`, { responseType: 'text' });
  }
}