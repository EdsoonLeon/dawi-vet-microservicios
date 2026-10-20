export interface Veterinario {
  id: number;
  especialidad: string | null;
  colegiatura: string | null;
  edad: number | null;
  activo: boolean;
  usuarioId: number;
  nombre: string | null;
  apellido: string | null;
  correo: string | null;
}

export interface VeterinarioRegistroRequest {
  especialidad: string | null;
  colegiatura: string | null;
  edad: number | null;
  usuario: {
    nombre: string | null;
    apellido: string | null;
    correo: string;
    clave: string;
  };
}

export type VeterinarioActualizacionRequest = Pick<VeterinarioRegistroRequest, 'especialidad' | 'colegiatura' | 'edad'>;
