import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Consulta } from '../models/consulta';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ConsultaService {
  private http = inject(HttpClient);
  private readonly urlBase = `${environment.apiUrl}/consultas`;

  registrarConsulta(consulta: any): Observable<Consulta> {
    return this.http.post<Consulta>(`${this.urlBase}/registrar`, consulta);
  }

  listarConsultas(): Observable<Consulta[]> {
    return this.http.get<Consulta[]>(this.urlBase);
  }

  listarPorMascota(mascotaId: number): Observable<Consulta[]> {
    return this.http.get<Consulta[]>(`${this.urlBase}/mascota/${mascotaId}`);
  }

  listarPorVeterinario(veterinarioId: number): Observable<Consulta[]> {
    return this.http.get<Consulta[]>(`${this.urlBase}/veterinario/${veterinarioId}`);
  }

  obtenerPorId(id: number): Observable<Consulta> {
    return this.http.get<Consulta>(`${this.urlBase}/${id}`);
  }

  editarConsulta(id: number, consulta: any): Observable<Consulta> {
    return this.http.put<Consulta>(`${this.urlBase}/${id}`, consulta);
  }

  eliminarConsulta(id: number): Observable<any> {
    return this.http.delete(`${this.urlBase}/${id}`, { responseType: 'text' });
  }
}