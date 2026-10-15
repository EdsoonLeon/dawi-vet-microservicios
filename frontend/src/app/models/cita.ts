import { Mascota } from './mascota';
import { Veterinario } from './veterinario';

export interface Cita {
  id: number;
  fecha: string;
  hora: string;
  estado: string;
  mascota: Mascota;
  veterinario: Veterinario;
}