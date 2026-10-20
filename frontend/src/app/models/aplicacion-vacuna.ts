import { SortDirection } from './vacuna';

export type EstadoAplicacion = 'REGISTRADA' | 'ANULADA';

export interface AplicacionVacunaRequest {
  vacunaId: number;
  mascotaId: number;
  veterinarioId: number;
  fechaAplicacion: string;
  proximaDosis?: string | null;
  lote?: string | null;
  observacion?: string | null;
}

export interface AplicacionVacunaUpdateRequest {
  fechaAplicacion: string;
  proximaDosis?: string | null;
  lote?: string | null;
  observacion?: string | null;
}

export interface AnulacionRequest {
  motivo: string;
}

export interface AplicacionVacuna {
  id: number;
  vacunaId: number;
  mascotaId: number;
  veterinarioId: number;
  fechaAplicacion: string;
  proximaDosis: string | null;
  lote: string | null;
  observacion: string | null;
  mascotaNombre: string;
  veterinarioNombre: string;
  vacunaNombre: string;
  estado: EstadoAplicacion;
  motivoAnulacion: string | null;
  fechaAnulacion: string | null;
}

export type AplicacionVacunaSortBy =
  | 'id'
  | 'fechaAplicacion'
  | 'proximaDosis'
  | 'mascotaNombre'
  | 'vacunaNombre'
  | 'veterinarioNombre'
  | 'estado';

export interface AplicacionVacunaFiltros {
  page?: number;
  size?: number;
  nombreMascota?: string;
  mascotaId?: number;
  veterinarioId?: number;
  vacunaId?: number;
  estado?: EstadoAplicacion;
  fechaDesde?: string;
  fechaHasta?: string;
  sortBy?: AplicacionVacunaSortBy;
  direction?: SortDirection;
}
