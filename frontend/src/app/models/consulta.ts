export interface Consulta {
  id: number;
  fecha: string | null;
  diagnostico: string | null;
  tratamiento: string | null;
  observacion: string | null;
  mascotaId: number;
  mascotaNombre: string | null;
  veterinarioId: number;
  veterinarioNombre: string | null;
}

export interface ConsultaRegistroRequest {
  fecha: string | null;
  diagnostico: string | null;
  tratamiento: string | null;
  observacion: string | null;
  mascotaId: number;
  veterinarioId: number;
}

export type ConsultaActualizacionRequest = Pick<ConsultaRegistroRequest,
  'fecha' | 'diagnostico' | 'tratamiento' | 'observacion'>;
