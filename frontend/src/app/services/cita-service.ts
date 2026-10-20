import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Cita, CitaActualizacionRequest, CitaRegistroRequest } from '../models/cita';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CitaService {
  private http = inject(HttpClient);
  private readonly urlBase = `${environment.apiUrl}/citas`;

  registrarCita(cita: CitaRegistroRequest): Observable<Cita> {
    return this.http.post<Cita>(`${this.urlBase}/registrar`, cita);
  }

  listarCitas(): Observable<Cita[]> {
    return this.http.get<Cita[]>(this.urlBase);
  }

  listarPorVeterinario(veterinarioId: number): Observable<Cita[]> {
    return this.http.get<Cita[]>(`${this.urlBase}/veterinario/${veterinarioId}`);
  }

  listarPorMascota(mascotaId: number): Observable<Cita[]> {
    return this.http.get<Cita[]>(`${this.urlBase}/mascota/${mascotaId}`);
  }

  obtenerPorId(id: number): Observable<Cita> {
    return this.http.get<Cita>(`${this.urlBase}/${id}`);
  }

  editarCita(id: number, cita: CitaActualizacionRequest): Observable<Cita> {
    return this.http.put<Cita>(`${this.urlBase}/${id}`, cita);
  }

  cambiarEstado(id: number, estado: string): Observable<Cita> {
    return this.http.patch<Cita>(`${this.urlBase}/${id}/estado`, { estado });
  }

  cancelarCita(id: number): Observable<string> {
    return this.http.delete(`${this.urlBase}/${id}`, { responseType: 'text' });
  }
}
