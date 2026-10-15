import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { CitaService } from '../../../services/cita-service';
import { MascotaService } from '../../../services/mascota-service';
import { VeterinarioService } from '../../../services/veterinario-service';
import { Cita } from '../../../models/cita';
import { Mascota } from '../../../models/mascota';
import { Veterinario } from '../../../models/veterinario';
import { ConfirmModal } from '../../../components/confirm-modal/confirm-modal';

@Component({
  selector: 'app-citas',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule, ConfirmModal],
  templateUrl: './citas.html',
  styleUrl: './citas.css'
})
export class Citas implements OnInit {
  private citaService = inject(CitaService);
  private mascotaService = inject(MascotaService);
  private veterinarioService = inject(VeterinarioService);
  private fb = inject(FormBuilder);

  citas = signal<Cita[]>([]);
  mascotas = signal<Mascota[]>([]);
  mascotasActivas = computed(() => this.mascotas().filter(m => m.activo));
  veterinarios = signal<Veterinario[]>([]);
  mostrarFormulario = false;
  citaEditandoId: number | null = null;
  mensajeError = signal('');

  mostrarConfirmacion = false;
  idACancelar: number | null = null;

  terminoBusqueda = signal('');

  citasFiltradas = computed(() => {
    const termino = this.terminoBusqueda().toLowerCase().trim();
    const lista = this.citas();
    if (!termino) return lista;

    return lista.filter(c =>
      c.mascota.nombre.toLowerCase().includes(termino) ||
      c.veterinario.usuario.nombre.toLowerCase().includes(termino) ||
      c.veterinario.usuario.apellido.toLowerCase().includes(termino) ||
      c.estado.toLowerCase().includes(termino)
    );
  });

  estados = ['PENDIENTE', 'COMPLETADA', 'CANCELADA', 'NO_ASISTIO'];

  citaForm = this.fb.group({
    fecha: ['', Validators.required],
    hora: ['', Validators.required],
    mascotaId: ['', Validators.required],
    veterinarioId: ['', Validators.required],
  });

  ngOnInit() {
    this.cargarCitas();
    this.cargarMascotas();
    this.cargarVeterinarios();
  }

  cargarCitas() {
    this.citaService.listarCitas().subscribe({
      next: (data) => this.citas.set(data),
      error: () => this.mensajeError.set('No se pudo cargar la lista de citas')
    });
  }

  cargarMascotas() {
    this.mascotaService.listarMascotas().subscribe({
      next: (data) => this.mascotas.set(data)
    });
  }

  cargarVeterinarios() {
    this.veterinarioService.listarVeterinarios().subscribe({
      next: (data) => this.veterinarios.set(data)
    });
  }

  abrirNuevo() {
    this.citaEditandoId = null;
    this.citaForm.reset();
    this.mostrarFormulario = true;
  }

  abrirEditar(cita: Cita) {
    this.citaEditandoId = cita.id;
    this.citaForm.setValue({
      fecha: cita.fecha,
      hora: cita.hora.slice(0, 5),
      mascotaId: String(cita.mascota.id),
      veterinarioId: String(cita.veterinario.id),
    });
    this.mostrarFormulario = true;
  }

  cerrarFormulario() {
    this.mostrarFormulario = false;
    this.citaEditandoId = null;
    this.citaForm.reset();
  }

  guardar() {
    if (this.citaForm.invalid) {
      this.citaForm.markAllAsTouched();
      return;
    }

    const valores = this.citaForm.value;

    if (this.citaEditandoId) {
      const datosEdicion = { fecha: valores.fecha, hora: valores.hora };
      this.citaService.editarCita(this.citaEditandoId, datosEdicion).subscribe({
        next: () => { this.cargarCitas(); this.cerrarFormulario(); },
        error: () => this.mensajeError.set('No se pudo editar la cita')
      });
    } else {
      const datosRegistro = {
        fecha: valores.fecha,
        hora: valores.hora,
        mascota: { id: Number(valores.mascotaId) },
        veterinario: { id: Number(valores.veterinarioId) },
      };
      this.citaService.registrarCita(datosRegistro).subscribe({
        next: () => { this.cargarCitas(); this.cerrarFormulario(); },
        error: () => this.mensajeError.set('No se pudo registrar la cita')
      });
    }
  }

  cambiarEstado(id: number, estado: string) {
    this.citaService.cambiarEstado(id, estado).subscribe({
      next: () => this.cargarCitas(),
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

  pedirCancelar(id: number) {
    this.idACancelar = id;
    this.mostrarConfirmacion = true;
  }

  confirmarCancelacion() {
    this.mostrarConfirmacion = false;
    if (this.idACancelar === null) return;

    this.citaService.cancelarCita(this.idACancelar).subscribe({
      next: () => this.cargarCitas(),
      error: () => this.mensajeError.set('No se pudo cancelar la cita')
    });
    this.idACancelar = null;
  }

  cerrarConfirmacion() {
    this.mostrarConfirmacion = false;
    this.idACancelar = null;
  }
}