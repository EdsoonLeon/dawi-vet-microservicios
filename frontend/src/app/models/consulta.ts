import { Mascota } from './mascota';
import { Veterinario } from './veterinario';

export interface Consulta {
  id: number;
  fecha: string;
  diagnostico: string;
  tratamiento: string;
  observacion: string;
  mascota: Mascota;
  veterinario: Veterinario;
}