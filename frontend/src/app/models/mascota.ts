export interface Mascota {
  id: number;
  nombre: string | null;
  especie: string | null;
  raza: string | null;
  edad: number | null;
  activo: boolean;
  clienteId: number;
  clienteNombre: string | null;
}

export interface MascotaRegistroRequest {
  nombre: string | null;
  especie: string | null;
  raza: string | null;
  edad: number | null;
  clienteId: number;
}

export type MascotaActualizacionRequest = Omit<MascotaRegistroRequest, 'clienteId'>;
