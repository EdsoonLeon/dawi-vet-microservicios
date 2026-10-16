export interface Consulta {
  id: number;
  fecha: string;
  diagnostico: string;
  tratamiento: string;
  observacion: string;
  mascotaId: number;
  mascotaNombre: string;
  veterinarioId: number;
  veterinarioNombre: string;
}
