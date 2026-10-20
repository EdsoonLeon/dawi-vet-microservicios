export interface Cliente {
  id: number;
  nombre: string | null;
  apellido: string | null;
  telefono: string | null;
  direccion: string | null;
  correo: string | null;
  activo: boolean;
}

export type ClienteRequest = Partial<Pick<Cliente, 'nombre' | 'apellido' | 'telefono' | 'direccion' | 'correo'>>;
