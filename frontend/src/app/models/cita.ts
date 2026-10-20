export interface Cita {
  id: number;
  fecha: string | null;
  hora: string | null;
  estado: string;
  mascotaId: number;
  mascotaNombre: string | null;
  veterinarioId: number;
  veterinarioNombre: string | null;
}

export interface CitaRegistroRequest {
  fecha: string | null;
  hora: string | null;
  mascotaId: number;
  veterinarioId: number;
}

export type CitaActualizacionRequest = Pick<CitaRegistroRequest, 'fecha' | 'hora'>;
