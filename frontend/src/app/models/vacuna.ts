export interface VacunaRequest {
  nombre: string;
  descripcion?: string | null;
  enfermedadPreviene: string;
}

export interface Vacuna {
  id: number;
  nombre: string;
  descripcion: string | null;
  enfermedadPreviene: string;
  activo: boolean;
}

export type VacunaSortBy = 'id' | 'nombre' | 'enfermedadPreviene' | 'activo';
export type SortDirection = 'asc' | 'desc';

export interface VacunaFiltros {
  page?: number;
  size?: number;
  nombre?: string;
  activo?: boolean;
  sortBy?: VacunaSortBy;
  direction?: SortDirection;
}
