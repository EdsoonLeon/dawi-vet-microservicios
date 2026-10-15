import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MascotaService } from '../../../services/mascota-service';
import { ClienteService } from '../../../services/cliente-service';
import { Mascota } from '../../../models/mascota';
import { Cliente } from '../../../models/cliente';
import { ConfirmModal } from '../../../components/confirm-modal/confirm-modal';

@Component({
  selector: 'app-mascotas',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule, ConfirmModal],
  templateUrl: './mascotas.html',
  styleUrl: './mascotas.css'
})
export class Mascotas implements OnInit {
  private mascotaService = inject(MascotaService);
  private clienteService = inject(ClienteService);
  private fb = inject(FormBuilder);

  mascotas = signal<Mascota[]>([]);
  clientes = signal<Cliente[]>([]);
  clientesActivos = computed(() => this.clientes().filter(c => c.activo));
  mostrarFormulario = false;
  mascotaEditandoId: number | null = null;
  mensajeError = signal('');

  mostrarConfirmacion = false;
  idAEliminar: number | null = null;

  terminoBusqueda = signal('');

  mascotasFiltradas = computed(() => {
    const termino = this.terminoBusqueda().toLowerCase().trim();
    const lista = this.mascotas();
    if (!termino) return lista;

    return lista.filter(m =>
      m.nombre.toLowerCase().includes(termino) ||
      m.especie.toLowerCase().includes(termino) ||
      m.raza.toLowerCase().includes(termino) ||
      m.cliente.nombre.toLowerCase().includes(termino) ||
      m.cliente.apellido.toLowerCase().includes(termino)
    );
  });

  mascotaForm = this.fb.group({
    nombre: ['', Validators.required],
    especie: ['', Validators.required],
    raza: ['', Validators.required],
    edad: [0, [Validators.required, Validators.min(0)]],
    clienteId: ['', Validators.required],
  });

  ngOnInit() {
    this.cargarMascotas();
    this.cargarClientes();
  }

  cargarMascotas() {
    this.mascotaService.listarMascotas().subscribe({
      next: (data) => this.mascotas.set(data),
      error: () => this.mensajeError.set('No se pudo cargar la lista de mascotas')
    });
  }

  cargarClientes() {
    this.clienteService.listarClientes().subscribe({
      next: (data) => this.clientes.set(data)
    });
  }

  abrirNuevo() {
    this.mascotaEditandoId = null;
    this.mascotaForm.reset({ edad: 0 });
    this.mostrarFormulario = true;
  }

  abrirEditar(mascota: Mascota) {
    this.mascotaEditandoId = mascota.id;
    this.mascotaForm.setValue({
      nombre: mascota.nombre,
      especie: mascota.especie,
      raza: mascota.raza,
      edad: mascota.edad,
      clienteId: String(mascota.cliente.id),
    });
    this.mostrarFormulario = true;
  }

  cerrarFormulario() {
    this.mostrarFormulario = false;
    this.mascotaEditandoId = null;
    this.mascotaForm.reset();
  }

  guardar() {
    if (this.mascotaForm.invalid) {
      this.mascotaForm.markAllAsTouched();
      return;
    }

    const valores = this.mascotaForm.value;

    if (this.mascotaEditandoId) {
      const datosEdicion = {
        nombre: valores.nombre,
        especie: valores.especie,
        raza: valores.raza,
        edad: valores.edad,
      };
      this.mascotaService.editarMascota(this.mascotaEditandoId, datosEdicion).subscribe({
        next: () => { this.cargarMascotas(); this.cerrarFormulario(); },
        error: () => this.mensajeError.set('No se pudo editar la mascota')
      });
    } else {
      const datosRegistro = {
        nombre: valores.nombre,
        especie: valores.especie,
        raza: valores.raza,
        edad: valores.edad,
        cliente: { id: Number(valores.clienteId) },
      };
      this.mascotaService.registrarMascota(datosRegistro).subscribe({
        next: () => { this.cargarMascotas(); this.cerrarFormulario(); },
        error: () => this.mensajeError.set('No se pudo registrar la mascota')
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

    this.mascotaService.eliminarMascota(this.idAEliminar).subscribe({
      next: () => this.cargarMascotas(),
      error: () => this.mensajeError.set('No se pudo desactivar la mascota')
    });
    this.idAEliminar = null;
  }

  cancelarEliminar() {
    this.mostrarConfirmacion = false;
    this.idAEliminar = null;
  }

  activar(id: number) {
    this.mascotaService.activarMascota(id).subscribe({
      next: () => this.cargarMascotas(),
      error: () => this.mensajeError.set('No se pudo reactivar la mascota')
    });
  }
}