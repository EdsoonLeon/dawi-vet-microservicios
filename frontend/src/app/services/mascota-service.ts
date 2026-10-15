import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Mascota } from '../models/mascota';

@Injectable({
  providedIn: 'root'
})
export class MascotaService {
  private http = inject(HttpClient);
  private readonly urlBase = 'http://localhost:8081/api/mascotas';

  registrarMascota(mascota: any): Observable<Mascota> {
    return this.http.post<Mascota>(`${this.urlBase}/registrar`, mascota);
  }

  listarMascotas(): Observable<Mascota[]> {
    return this.http.get<Mascota[]>(this.urlBase);
  }

  listarPorCliente(clienteId: number): Observable<Mascota[]> {
    return this.http.get<Mascota[]>(`${this.urlBase}/cliente/${clienteId}`);
  }

  obtenerPorId(id: number): Observable<Mascota> {
    return this.http.get<Mascota>(`${this.urlBase}/${id}`);
  }

  editarMascota(id: number, mascota: any): Observable<Mascota> {
    return this.http.put<Mascota>(`${this.urlBase}/${id}`, mascota);
  }

  eliminarMascota(id: number): Observable<any> {
    return this.http.delete(`${this.urlBase}/${id}`, { responseType: 'text' });
  }

  activarMascota(id: number): Observable<Mascota> {
    return this.http.patch<Mascota>(`${this.urlBase}/${id}/activar`, {});
  }
}