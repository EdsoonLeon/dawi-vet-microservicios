import { Cliente } from './cliente';

export interface Mascota {
  id: number;
  nombre: string;
  especie: string;
  raza: string;
  edad: number;
  activo: boolean;
  cliente: Cliente;
}