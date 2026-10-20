import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ConsultaService } from '../../../services/consulta-service';
import { MascotaService } from '../../../services/mascota-service';
import { VeterinarioService } from '../../../services/veterinario-service';
import { UsuarioAuthService } from '../../../services/usuario-auth-service';
import { Consulta } from '../../../models/consulta';
import { Mascota } from '../../../models/mascota';
import { ConfirmModal } from '../../../components/confirm-modal/confirm-modal';
import { mensajeDeError } from '../../../shared/http-error.util';

@Component({
  selector: 'app-consultas',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule, ConfirmModal],
  templateUrl: './consultas.html',
  styleUrl: './consultas.css'
})
export class Consultas implements OnInit {
  private consultaService = inject(ConsultaService);
  private mascotaService = inject(MascotaService);
  private veterinarioService = inject(VeterinarioService);
  private authService = inject(UsuarioAuthService);
  private fb = inject(FormBuilder);

  consultas = signal<Consulta[]>([]);
  mascotas = signal<Mascota[]>([]);
  mascotasActivas = signal<Mascota[]>([]);
  mostrarFormulario = false;
  consultaEditandoId: number | null = null;
  mensajeError = signal('');
  mensajeExito = signal('');
  veterinarioId: number | null = null;

  mostrarConfirmacion = false;
  idAEliminar: number | null = null;

  terminoBusqueda = signal('');

  consultasFiltradas = computed(() => {
    const termino = this.terminoBusqueda().toLowerCase().trim();
    const lista = this.consultas();
    if (!termino) return lista;

    return lista.filter(c =>
      (c.mascotaNombre ?? '').toLowerCase().includes(termino) ||
      (c.diagnostico ?? '').toLowerCase().includes(termino) ||
      (c.tratamiento ?? '').toLowerCase().includes(termino)
    );
  });

  consultaForm = this.fb.group({
    fecha: ['', Validators.required],
    diagnostico: ['', Validators.required],
    tratamiento: ['', Validators.required],
    observacion: [''],
    mascotaId: ['', Validators.required],
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
        this.cargarConsultas();
      },
      error: (err) => this.mensajeError.set(mensajeDeError(err, 'No se pudo cargar tu perfil de veterinario'))
    });

    this.cargarMascotas();
  }

  cargarConsultas() {
    if (!this.veterinarioId) return;
    this.consultaService.listarPorVeterinario(this.veterinarioId).subscribe({
      next: (data) => this.consultas.set(data),
      error: (err) => this.mensajeError.set(mensajeDeError(err, 'No se pudo cargar tus consultas'))
    });
  }

  cargarMascotas() {
    this.mascotaService.listarMascotas().subscribe({
      next: (data) => {
        this.mascotas.set(data);
        this.mascotasActivas.set(data.filter(m => m.activo));
      }
    });
  }

  abrirNuevo() {
    this.consultaEditandoId = null;
    this.consultaForm.reset();
    this.consultaForm.get('mascotaId')?.enable();
    this.mensajeError.set('');
    this.mensajeExito.set('');
    this.mostrarFormulario = true;
  }

  abrirEditar(consulta: Consulta) {
    this.consultaEditandoId = consulta.id;
    this.consultaForm.setValue({
      fecha: consulta.fecha,
      diagnostico: consulta.diagnostico,
      tratamiento: consulta.tratamiento,
      observacion: consulta.observacion ?? '',
      mascotaId: String(consulta.mascotaId),
    });
    // El PUT no acepta cambiar la mascota: se deshabilita para no enviarla.
    this.consultaForm.get('mascotaId')?.disable();
    this.mensajeError.set('');
    this.mensajeExito.set('');
    this.mostrarFormulario = true;
  }

  cerrarFormulario() {
    this.mostrarFormulario = false;
    this.consultaEditandoId = null;
    this.consultaForm.reset();
    this.consultaForm.get('mascotaId')?.enable();
  }

  guardar() {
    if (this.consultaForm.invalid || !this.veterinarioId) {
      this.consultaForm.markAllAsTouched();
      return;
    }

    const valores = this.consultaForm.getRawValue();

    if (this.consultaEditandoId) {
      const datosEdicion = {
        fecha: valores.fecha,
        diagnostico: valores.diagnostico,
        tratamiento: valores.tratamiento,
        observacion: valores.observacion,
      };
      this.consultaService.editarConsulta(this.consultaEditandoId, datosEdicion).subscribe({
        next: () => {
          this.mensajeExito.set('Consulta actualizada correctamente');
          this.cargarConsultas();
          this.cerrarFormulario();
        },
        error: (err) => this.mensajeError.set(mensajeDeError(err, 'No se pudo editar la consulta'))
      });
    } else {
      const datosRegistro = {
        fecha: valores.fecha,
        diagnostico: valores.diagnostico,
        tratamiento: valores.tratamiento,
        observacion: valores.observacion,
        mascotaId: Number(valores.mascotaId),
        veterinarioId: this.veterinarioId,
      };
      this.consultaService.registrarConsulta(datosRegistro).subscribe({
        next: () => {
          this.mensajeExito.set('Consulta registrada correctamente');
          this.cargarConsultas();
          this.cerrarFormulario();
        },
        error: (err) => this.mensajeError.set(mensajeDeError(err, 'No se pudo registrar la consulta'))
      });
    }
  }

  pedirEliminar(id: number) {
    this.idAEliminar = id;
    this.mostrarConfirmacion = true;
  }

  confirmarEliminar() {
    this.mostrarConfirmacion = false;
    if (this.idAEliminar === null) return;

    this.consultaService.eliminarConsulta(this.idAEliminar).subscribe({
      next: () => { this.mensajeExito.set('Consulta eliminada correctamente'); this.cargarConsultas(); },
      error: (err) => this.mensajeError.set(mensajeDeError(err, 'No se pudo eliminar la consulta'))
    });
    this.idAEliminar = null;
  }

  cancelarEliminar() {
    this.mostrarConfirmacion = false;
    this.idAEliminar = null;
  }
}
