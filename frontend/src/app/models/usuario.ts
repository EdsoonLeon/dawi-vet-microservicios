import { Rol } from './rol';

export interface Usuario {
  id: number;
  nombre: string | null;
  apellido: string | null;
  correo: string;
  rol: Rol;
  activo: boolean;
}

export interface UsuarioRegistroRequest {
  nombre?: string | null;
  apellido?: string | null;
  correo: string;
  clave: string;
  rol: { id: number };
}

export interface UsuarioActualizacionRequest {
  nombre?: string | null;
  apellido?: string | null;
  correo?: string | null;
  clave?: string | null;
  rol?: { id: number } | null;
}
