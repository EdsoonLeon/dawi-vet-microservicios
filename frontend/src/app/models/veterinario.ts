import { Usuario } from './usuario';

export interface Veterinario {
  id: number;
  especialidad: string;
  colegiatura: string;
  edad: number;
  usuario: Usuario;
}