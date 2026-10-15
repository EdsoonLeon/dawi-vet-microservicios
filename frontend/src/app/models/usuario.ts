import { Rol } from './rol';

export interface Usuario {
  id: number;
  nombre: string;
  apellido: string;
  correo: string;
  clave?: string;
  rol: Rol;
  activo: boolean;
}