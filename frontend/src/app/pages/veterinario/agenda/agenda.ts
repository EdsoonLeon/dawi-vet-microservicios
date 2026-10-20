import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CitaService } from '../../../services/cita-service';
import { VeterinarioService } from '../../../services/veterinario-service';
import { MascotaService } from '../../../services/mascota-service';
import { UsuarioAuthService } from '../../../services/usuario-auth-service';
import { Cita } from '../../../models/cita';
import { Mascota } from '../../../models/mascota';
import { mensajeDeError } from '../../../shared/http-error.util';

@Component({
  selector: 'app-agenda',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './agenda.html',
  styleUrl: './agenda.css'
})
export class Agenda implements OnInit {
  private citaService = inject(CitaService);
  private veterinarioService = inject(VeterinarioService);
  private mascotaService = inject(MascotaService);
  private authService = inject(UsuarioAuthService);

  citas = signal<Cita[]>([]);
  mensajeError = signal('');
  veterinarioId: number | null = null;

  // Mapa mascotaId -> Mascota, para poder mostrar el dueño de cada cita
  // (la respuesta de /api/citas ya no trae la mascota ni el cliente anidados).
  mascotasPorId = signal<Map<number, Mascota>>(new Map());

  estados = ['PENDIENTE', 'COMPLETADA', 'CANCELADA', 'NO_ASISTIO'];

  terminoBusqueda = signal('');

  citasFiltradas = computed(() => {
    const termino = this.terminoBusqueda().toLowerCase().trim();
    const lista = this.citas();
    if (!termino) return lista;

    return lista.filter(c =>
      (c.mascotaNombre ?? '').toLowerCase().includes(termino) ||
      this.duenoDe(c.mascotaId).toLowerCase().includes(termino) ||
      c.estado.toLowerCase().includes(termino)
    );
  });

  ngOnInit() {
    const usuario = this.authService.obtenerUsuario();
    if (!usuario) {
      this.mensajeError.set('No se pudo identificar al usuario');
      return;
    }

    this.cargarMascotas();

    this.veterinarioService.obtenerPorUsuario(usuario.id).subscribe({
      next: (vet) => {
        if (!vet) {
          this.mensajeError.set('No se encontró un perfil de veterinario para este usuario');
          return;
        }
        this.veterinarioId = vet.id;
        this.cargarAgenda();
      },
      error: (err) => this.mensajeError.set(mensajeDeError(err, 'No se pudo cargar tu perfil de veterinario'))
    });
  }

  cargarMascotas() {
    this.mascotaService.listarMascotas().subscribe({
      next: (data) => {
        const mapa = new Map<number, Mascota>();
        data.forEach(m => mapa.set(m.id, m));
        this.mascotasPorId.set(mapa);
      }
    });
  }

  duenoDe(mascotaId: number): string {
    return this.mascotasPorId().get(mascotaId)?.clienteNombre ?? '';
  }

  cargarAgenda() {
    if (!this.veterinarioId) return;
    this.citaService.listarPorVeterinario(this.veterinarioId).subscribe({
      next: (data) => this.citas.set(data),
      error: (err) => this.mensajeError.set(mensajeDeError(err, 'No se pudo cargar tu agenda'))
    });
  }

  cambiarEstado(id: number, estado: string) {
    this.citaService.cambiarEstado(id, estado).subscribe({
      next: () => this.cargarAgenda(),
      error: (err) => this.mensajeError.set(mensajeDeError(err, 'No se pudo cambiar el estado'))
    });
  }

  claseEstado(estado: string): string {
    const base = 'text-label-sm px-sm py-xs rounded-full border-none focus:outline-none focus:ring-2 focus:ring-primary';
    switch (estado) {
      case 'COMPLETADA':
        return base + ' bg-primary/15 text-primary';
      case 'CANCELADA':
        return base + ' bg-secondary/15 text-secondary';
      case 'NO_ASISTIO':
        return base + ' bg-error/15 text-error';
      default:
        return base + ' bg-on-surface-variant/10 text-on-surface-variant';
    }
  }
}
