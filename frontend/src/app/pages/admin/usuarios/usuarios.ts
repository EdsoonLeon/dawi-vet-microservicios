import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { UsuarioService } from '../../../services/usuario-service';
import { UsuarioAuthService } from '../../../services/usuario-auth-service';
import { RolService } from '../../../services/rol-service';
import { VeterinarioService } from '../../../services/veterinario-service';
import { Usuario, UsuarioActualizacionRequest } from '../../../models/usuario';
import { Rol } from '../../../models/rol';
import { ConfirmModal } from '../../../components/confirm-modal/confirm-modal';
import { mensajeDeError } from '../../../shared/http-error.util';

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule, ConfirmModal],
  templateUrl: './usuarios.html',
  styleUrl: './usuarios.css'
})
export class Usuarios implements OnInit {
  private usuarioService = inject(UsuarioService);
  private usuarioAuthService = inject(UsuarioAuthService);
  private rolService = inject(RolService);
  private veterinarioService = inject(VeterinarioService);
  private fb = inject(FormBuilder);

  usuarios = signal<Usuario[]>([]);
  roles = signal<Rol[]>([]);
  mostrarFormulario = false;
  usuarioEditandoId: number | null = null;
  // id propio del veterinario (distinto del id de usuario) cuando se edita uno
  vetEditandoId: number | null = null;
  mensajeError = signal('');
  mensajeExito = signal('');

  mostrarConfirmacion = false;
  idAEliminar: number | null = null;

  terminoBusqueda = signal('');

  usuariosFiltrados = computed(() => {
    const termino = this.terminoBusqueda().toLowerCase().trim();
    const lista = this.usuarios();
    if (!termino) return lista;

    return lista.filter(u =>
      (u.nombre ?? '').toLowerCase().includes(termino) ||
      (u.apellido ?? '').toLowerCase().includes(termino) ||
      u.correo.toLowerCase().includes(termino) ||
      u.rol.nombre.toLowerCase().includes(termino)
    );
  });

  usuarioForm = this.fb.group({
    nombre: ['', Validators.required],
    apellido: ['', Validators.required],
    correo: ['', [Validators.required, Validators.email]],
    clave: [''],
    rolId: ['', Validators.required],
    especialidad: [''],
    colegiatura: [''],
    edadVeterinario: [''],
  });

  ngOnInit() {
    this.cargarUsuarios();
    this.cargarRoles();

    // Activa/desactiva las validaciones de especialidad/colegiatura/edad
    // en vivo, según el rol seleccionado (tanto al crear como al editar).
    this.usuarioForm.get('rolId')?.valueChanges.subscribe(() => this.actualizarValidadoresVeterinario());
  }

  cargarUsuarios() {
    this.usuarioService.listarUsuarios().subscribe({
      next: (data) => this.usuarios.set(data),
      error: (err) => this.mensajeError.set(mensajeDeError(err, 'No se pudo cargar la lista de usuarios'))
    });
  }

  cargarRoles() {
    this.rolService.listarRoles().subscribe({
      next: (data) => this.roles.set(data)
    });
  }

  get rolVeterinarioSeleccionado(): boolean {
    const rolId = this.usuarioForm.get('rolId')?.value;
    const rol = this.roles().find(r => String(r.id) === String(rolId));
    return rol?.nombre === 'VETERINARIO';
  }

  private actualizarValidadoresVeterinario() {
    const especialidad = this.usuarioForm.get('especialidad');
    const colegiatura = this.usuarioForm.get('colegiatura');
    const edad = this.usuarioForm.get('edadVeterinario');

    if (this.rolVeterinarioSeleccionado) {
      especialidad?.setValidators([Validators.required]);
      colegiatura?.setValidators([Validators.required]);
      edad?.setValidators([Validators.required, Validators.min(18)]);
    } else {
      especialidad?.clearValidators();
      colegiatura?.clearValidators();
      edad?.clearValidators();
    }
    especialidad?.updateValueAndValidity();
    colegiatura?.updateValueAndValidity();
    edad?.updateValueAndValidity();
  }

  abrirNuevo() {
    this.usuarioEditandoId = null;
    this.vetEditandoId = null;
    this.usuarioForm.reset();
    this.usuarioForm.get('clave')?.setValidators([Validators.required]);
    this.usuarioForm.get('clave')?.updateValueAndValidity();
    this.actualizarValidadoresVeterinario();
    this.mensajeError.set('');
    this.mensajeExito.set('');
    this.mostrarFormulario = true;
  }

  abrirEditar(usuario: Usuario) {
    this.usuarioEditandoId = usuario.id;
    this.vetEditandoId = null;
    this.usuarioForm.get('clave')?.clearValidators();
    this.usuarioForm.get('clave')?.updateValueAndValidity();
    this.usuarioForm.setValue({
      nombre: usuario.nombre,
      apellido: usuario.apellido,
      correo: usuario.correo,
      clave: '',
      rolId: String(usuario.rol.id),
      especialidad: '',
      colegiatura: '',
      edadVeterinario: '',
    });
    this.actualizarValidadoresVeterinario();
    this.mensajeError.set('');
    this.mensajeExito.set('');
    this.mostrarFormulario = true;

    if (usuario.rol.nombre === 'VETERINARIO') {
      this.veterinarioService.obtenerPorUsuario(usuario.id).subscribe({
        next: (vet) => {
          if (vet) {
            this.vetEditandoId = vet.id;
            this.usuarioForm.patchValue({
              especialidad: vet.especialidad,
              colegiatura: vet.colegiatura,
              edadVeterinario: String(vet.edad),
            });
          }
        },
        error: (err) => this.mensajeError.set(mensajeDeError(err, 'No se pudo cargar los datos de veterinario'))
      });
    }
  }

  cerrarFormulario() {
    this.mostrarFormulario = false;
    this.usuarioEditandoId = null;
    this.vetEditandoId = null;
    this.usuarioForm.reset();
  }

  guardar() {
    if (this.usuarioForm.invalid) {
      this.usuarioForm.markAllAsTouched();
      return;
    }

    const valores = this.usuarioForm.getRawValue();
    const esVeterinario = this.rolVeterinarioSeleccionado;

    if (this.usuarioEditandoId) {
      if (esVeterinario && !this.vetEditandoId) {
        this.mensajeError.set('No se puede cambiar el rol a Veterinario desde la edición de usuarios.');
        return;
      }

      const datosEdicion: UsuarioActualizacionRequest = {
        nombre: valores.nombre,
        apellido: valores.apellido,
        correo: valores.correo,
        rol: { id: Number(valores.rolId) },
      };
      if (valores.clave && valores.clave.trim() !== '') {
        datosEdicion.clave = valores.clave;
      }

      this.usuarioService.editarUsuario(this.usuarioEditandoId, datosEdicion).subscribe({
        next: () => {
          if (esVeterinario && this.vetEditandoId) {
            const datosVet = {
              especialidad: valores.especialidad,
              colegiatura: valores.colegiatura,
              edad: Number(valores.edadVeterinario),
            };
            this.veterinarioService.editarVeterinario(this.vetEditandoId, datosVet).subscribe({
              next: () => {
                this.mensajeExito.set('Usuario actualizado correctamente');
                this.cargarUsuarios();
                this.cerrarFormulario();
              },
              error: (err) => this.mensajeError.set(
                mensajeDeError(err, 'El usuario se actualizó, pero no se pudieron guardar los datos de veterinario')
              )
            });
          } else {
            this.mensajeExito.set('Usuario actualizado correctamente');
            this.cargarUsuarios();
            this.cerrarFormulario();
          }
        },
        error: (err) => this.mensajeError.set(mensajeDeError(err, 'No se pudo editar el usuario'))
      });
      return;
    }

    if (esVeterinario) {
      const datosRegistro = {
        especialidad: valores.especialidad,
        colegiatura: valores.colegiatura,
        edad: Number(valores.edadVeterinario),
        usuario: {
          nombre: valores.nombre,
          apellido: valores.apellido,
          correo: valores.correo ?? '',
          clave: valores.clave ?? '',
        }
      };

      this.veterinarioService.registrarVeterinario(datosRegistro).subscribe({
        next: () => {
          this.mensajeExito.set('Veterinario registrado correctamente');
          this.cargarUsuarios();
          this.cerrarFormulario();
        },
        error: (err) => this.mensajeError.set(mensajeDeError(err, 'No se pudo registrar el veterinario'))
      });
      return;
    }

    const datosRegistro = {
      nombre: valores.nombre,
      apellido: valores.apellido,
      correo: valores.correo ?? '',
      clave: valores.clave ?? '',
      rol: { id: Number(valores.rolId) },
    };
    this.usuarioAuthService.registrarUsuario(datosRegistro).subscribe({
      next: () => {
        this.mensajeExito.set('Usuario registrado correctamente');
        this.cargarUsuarios();
        this.cerrarFormulario();
      },
      error: (err) => this.mensajeError.set(mensajeDeError(err, 'No se pudo registrar el usuario'))
    });
  }

  pedirEliminar(id: number) {
    this.idAEliminar = id;
    this.mostrarConfirmacion = true;
  }

  confirmarEliminar() {
    this.mostrarConfirmacion = false;
    if (this.idAEliminar === null) return;

    this.usuarioService.eliminarUsuario(this.idAEliminar).subscribe({
      next: () => { this.mensajeExito.set('Usuario desactivado correctamente'); this.cargarUsuarios(); },
      error: (err) => this.mensajeError.set(mensajeDeError(err, 'No se pudo desactivar el usuario'))
    });
    this.idAEliminar = null;
  }

  cancelarEliminar() {
    this.mostrarConfirmacion = false;
    this.idAEliminar = null;
  }

  activar(id: number) {
    this.usuarioService.activarUsuario(id).subscribe({
      next: () => { this.mensajeExito.set('Usuario reactivado correctamente'); this.cargarUsuarios(); },
      error: (err) => this.mensajeError.set(mensajeDeError(err, 'No se pudo reactivar el usuario'))
    });
  }
}
