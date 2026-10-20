import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response';
import {
  AnulacionRequest,
  AplicacionVacuna,
  AplicacionVacunaFiltros,
  AplicacionVacunaRequest,
  AplicacionVacunaUpdateRequest,
} from '../models/aplicacion-vacuna';

@Injectable({ providedIn: 'root' })
export class AplicacionVacunaService {
  private readonly http = inject(HttpClient);
  private readonly urlBase = `${environment.apiUrl}/vacunas/aplicaciones`;

  registrarAplicacion(datos: AplicacionVacunaRequest): Observable<ApiResponse<AplicacionVacuna>> {
    return this.http.post<ApiResponse<AplicacionVacuna>>(`${this.urlBase}/registrar`, datos);
  }

  listarAplicaciones(filtros: AplicacionVacunaFiltros = {}): Observable<ApiResponse<PageResponse<AplicacionVacuna>>> {
    let params = new HttpParams();
    for (const [nombre, valor] of Object.entries(filtros)) {
      if (valor !== undefined && valor !== null) params = params.set(nombre, String(valor));
    }
    return this.http.get<ApiResponse<PageResponse<AplicacionVacuna>>>(this.urlBase, { params });
  }

  obtenerPorId(id: number): Observable<ApiResponse<AplicacionVacuna>> {
    return this.http.get<ApiResponse<AplicacionVacuna>>(`${this.urlBase}/${id}`);
  }

  editarAplicacion(id: number, datos: AplicacionVacunaUpdateRequest): Observable<ApiResponse<AplicacionVacuna>> {
    const body: AplicacionVacunaUpdateRequest = {
      fechaAplicacion: datos.fechaAplicacion,
      proximaDosis: datos.proximaDosis ?? null,
      lote: datos.lote ?? null,
      observacion: datos.observacion ?? null,
    };
    return this.http.put<ApiResponse<AplicacionVacuna>>(`${this.urlBase}/${id}`, body);
  }

  anularAplicacion(id: number, datos: AnulacionRequest): Observable<ApiResponse<AplicacionVacuna>> {
    return this.http.patch<ApiResponse<AplicacionVacuna>>(`${this.urlBase}/${id}/anular`, datos);
  }
}
