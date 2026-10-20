import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response';
import { Vacuna, VacunaFiltros, VacunaRequest } from '../models/vacuna';

@Injectable({ providedIn: 'root' })
export class VacunaService {
  private readonly http = inject(HttpClient);
  private readonly urlBase = `${environment.apiUrl}/vacunas`;

  registrarVacuna(datos: VacunaRequest): Observable<ApiResponse<Vacuna>> {
    return this.http.post<ApiResponse<Vacuna>>(`${this.urlBase}/registrar`, datos);
  }

  listarVacunas(filtros: VacunaFiltros = {}): Observable<ApiResponse<PageResponse<Vacuna>>> {
    let params = new HttpParams();
    for (const [nombre, valor] of Object.entries(filtros)) {
      if (valor !== undefined && valor !== null) params = params.set(nombre, String(valor));
    }
    return this.http.get<ApiResponse<PageResponse<Vacuna>>>(this.urlBase, { params });
  }

  obtenerPorId(id: number): Observable<ApiResponse<Vacuna>> {
    return this.http.get<ApiResponse<Vacuna>>(`${this.urlBase}/${id}`);
  }

  editarVacuna(id: number, datos: VacunaRequest): Observable<ApiResponse<Vacuna>> {
    return this.http.put<ApiResponse<Vacuna>>(`${this.urlBase}/${id}`, datos);
  }

  desactivarVacuna(id: number): Observable<ApiResponse<Vacuna>> {
    return this.http.delete<ApiResponse<Vacuna>>(`${this.urlBase}/${id}`);
  }

  activarVacuna(id: number): Observable<ApiResponse<Vacuna>> {
    return this.http.patch<ApiResponse<Vacuna>>(`${this.urlBase}/${id}/activar`, null);
  }
}
