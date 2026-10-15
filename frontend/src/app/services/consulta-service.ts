import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Consulta } from '../models/consulta';

@Injectable({
  providedIn: 'root'
})
export class ConsultaService {
  private http = inject(HttpClient);
  private readonly urlBase = 'http://localhost:8081/api/consultas';

  registrarConsulta(consulta: any): Observable<Consulta> {
    return this.http.post<Consulta>(`${this.urlBase}/registrar`, consulta);
  }

  listarPorVeterinario(veterinarioId: number): Observable<Consulta[]> {
    return this.http.get<Consulta[]>(`${this.urlBase}/veterinario/${veterinarioId}`);
  }

  editarConsulta(id: number, consulta: any): Observable<Consulta> {
    return this.http.put<Consulta>(`${this.urlBase}/${id}`, consulta);
  }

  eliminarConsulta(id: number): Observable<any> {
    return this.http.delete(`${this.urlBase}/${id}`, { responseType: 'text' });
  }
}