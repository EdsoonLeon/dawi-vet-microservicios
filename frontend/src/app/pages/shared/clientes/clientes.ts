import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ClienteService } from '../../../services/cliente-service';
import { Cliente } from '../../../models/cliente';
import { ConfirmModal } from '../../../components/confirm-modal/confirm-modal';

@Component({
  selector: 'app-clientes',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule, ConfirmModal],
  templateUrl: './clientes.html',
  styleUrl: './clientes.css'
})
export class Clientes implements OnInit {
  private clienteService = inject(ClienteService);
  private fb = inject(FormBuilder);
  private cdr = inject(ChangeDetectorRef);

  clientes: Cliente[] = [];
  mostrarFormulario = false;
  clienteEditandoId: number | null = null;
  mensajeError = '';

  mostrarConfirmacion = false;
  idAEliminar: number | null = null;

  terminoBusqueda = '';

  clienteForm = this.fb.group({
    nombre: ['', Validators.required],
    apellido: ['', Validators.required],
    telefono: ['', Validators.required],
    direccion: ['', Validators.required],
    correo: ['', [Validators.required, Validators.email]],
  });

  get clientesFiltrados(): Cliente[] {
    const termino = this.terminoBusqueda.toLowerCase().trim();
    if (!termino) return this.clientes;

    return this.clientes.filter(c =>
      c.nombre.toLowerCase().includes(termino) ||
      c.apellido.toLowerCase().includes(termino) ||
      c.correo.toLowerCase().includes(termino) ||
      c.telefono.includes(termino)
    );
  }

  ngOnInit() {
    this.cargarClientes();
  }

  cargarClientes() {
    this.clienteService.listarClientes().subscribe({
      next: (data) => {
        this.clientes = data;
        this.cdr.markForCheck();
      },
      error: () => {
        this.mensajeError = 'No se pudo cargar la lista de clientes';
        this.cdr.markForCheck();
      }
    });
  }

  abrirNuevo() {
    this.clienteEditandoId = null;
    this.clienteForm.reset();
    this.mostrarFormulario = true;
  }

  abrirEditar(cliente: Cliente) {
    this.clienteEditandoId = cliente.id;
    this.clienteForm.setValue({
      nombre: cliente.nombre,
      apellido: cliente.apellido,
      telefono: cliente.telefono,
      direccion: cliente.direccion,
      correo: cliente.correo,
    });
    this.mostrarFormulario = true;
  }

  cerrarFormulario() {
    this.mostrarFormulario = false;
    this.clienteEditandoId = null;
    this.clienteForm.reset();
  }

  guardar() {
    if (this.clienteForm.invalid) {
      this.clienteForm.markAllAsTouched();
      return;
    }

    const datos = this.clienteForm.value as Cliente;

    if (this.clienteEditandoId) {
      this.clienteService.editarCliente(this.clienteEditandoId, datos).subscribe({
        next: () => { this.cargarClientes(); this.cerrarFormulario(); this.cdr.markForCheck(); },
        error: () => { this.mensajeError = 'No se pudo editar el cliente'; this.cdr.markForCheck(); }
      });
    } else {
      this.clienteService.registrarCliente(datos).subscribe({
        next: () => { this.cargarClientes(); this.cerrarFormulario(); this.cdr.markForCheck(); },
        error: () => { this.mensajeError = 'No se pudo registrar el cliente'; this.cdr.markForCheck(); }
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

    this.clienteService.eliminarCliente(this.idAEliminar).subscribe({
      next: () => this.cargarClientes(),
      error: () => { this.mensajeError = 'No se pudo desactivar el cliente'; this.cdr.markForCheck(); }
    });
    this.idAEliminar = null;
  }

  cancelarEliminar() {
    this.mostrarConfirmacion = false;
    this.idAEliminar = null;
  }

  activar(id: number) {
    this.clienteService.activarCliente(id).subscribe({
      next: () => this.cargarClientes(),
      error: () => { this.mensajeError = 'No se pudo reactivar el cliente'; this.cdr.markForCheck(); }
    });
  }
}