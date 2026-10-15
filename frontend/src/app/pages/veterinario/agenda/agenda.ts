import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CitaService } from '../../../services/cita-service';
import { VeterinarioService } from '../../../services/veterinario-service';
import { UsuarioAuthService } from '../../../services/usuario-auth-service';
import { Cita } from '../../../models/cita';

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
  private authService = inject(UsuarioAuthService);

  citas = signal<Cita[]>([]);
  mensajeError = signal('');
  veterinarioId: number | null = null;

  estados = ['PENDIENTE', 'COMPLETADA', 'CANCELADA', 'NO_ASISTIO'];

  terminoBusqueda = signal('');

  citasFiltradas = computed(() => {
    const termino = this.terminoBusqueda().toLowerCase().trim();
    const lista = this.citas();
    if (!termino) return lista;

    return lista.filter(c =>
      c.mascota.nombre.toLowerCase().includes(termino) ||
      c.mascota.cliente.nombre.toLowerCase().includes(termino) ||
      c.mascota.cliente.apellido.toLowerCase().includes(termino) ||
      c.estado.toLowerCase().includes(termino)
    );
  });

  ngOnInit() {
    const usuario = this.authService.obtenerUsuario();
    if (!usuario) {
      this.mensajeError.set('No se pudo identificar al usuario');
      return;
    }

    this.veterinarioService.obtenerPorUsuario(usuario.id).subscribe({
      next: (vet) => {
        if (!vet) {
          this.mensajeError.set('No se encontró un perfil de veterinario para este usuario');
          return;
        }
        this.veterinarioId = vet.id;
        this.cargarAgenda();
      },
      error: () => this.mensajeError.set('No se pudo cargar tu perfil de veterinario')
    });
  }

  cargarAgenda() {
    if (!this.veterinarioId) return;
    this.citaService.listarPorVeterinario(this.veterinarioId).subscribe({
      next: (data) => this.citas.set(data),
      error: () => this.mensajeError.set('No se pudo cargar tu agenda')
    });
  }

  cambiarEstado(id: number, estado: string) {
    this.citaService.cambiarEstado(id, estado).subscribe({
      next: () => this.cargarAgenda(),
      error: () => this.mensajeError.set('No se pudo cambiar el estado')
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