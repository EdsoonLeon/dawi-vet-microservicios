import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ConfirmModal } from '../../../components/confirm-modal/confirm-modal';
import { Vacuna, VacunaRequest } from '../../../models/vacuna';
import { UsuarioAuthService } from '../../../services/usuario-auth-service';
import { VacunaService } from '../../../services/vacuna-service';
import { mensajeDeError } from '../../../shared/http-error.util';

@Component({
  selector: 'app-vacunas',
  standalone: true,
  imports: [FormsModule, ReactiveFormsModule, ConfirmModal],
  templateUrl: './vacunas.html',
})
export class Vacunas implements OnInit {
  private readonly vacunaService = inject(VacunaService);
  private readonly authService = inject(UsuarioAuthService);
  private readonly fb = inject(FormBuilder);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly esAdmin = this.authService.obtenerUsuario()?.rol.nombre === 'ADMIN';
  readonly vacunaForm = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(120)]],
    descripcion: ['', Validators.maxLength(500)],
    enfermedadPreviene: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(200)]],
  });

  vacunas: Vacuna[] = [];
  busquedaNombre = '';
  private nombreAplicado = '';
  estadoFiltro: 'todas' | 'true' | 'false' = 'todas';
  pagina = 0;
  size = 10;
  totalPaginas = 0;
  totalElementos = 0;
  cargando = false;
  guardando = false;
  procesandoId: number | null = null;
  mostrarFormulario = false;
  vacunaEditandoId: number | null = null;
  vacunaADesactivar: Vacuna | null = null;
  mensajeError = '';
  mensajeExito = '';

  ngOnInit(): void {
    this.cargarVacunas();
  }

  cargarVacunas(): void {
    if (this.cargando) return;
    this.cargando = true;
    this.mensajeError = '';
    this.vacunaService.listarVacunas({
      page: this.pagina,
      size: this.size,
      nombre: this.nombreAplicado || undefined,
      activo: this.estadoFiltro === 'todas' ? undefined : this.estadoFiltro === 'true',
    }).subscribe({
      next: (response) => {
        const datos = response.data;
        this.cargando = false;
        if (this.pagina > 0 && datos.content.length === 0) {
          this.pagina = Math.max(0, datos.totalPages - 1);
          this.cargarVacunas();
          return;
        }
        this.vacunas = datos.content;
        this.pagina = datos.page;
        this.totalPaginas = datos.totalPages;
        this.totalElementos = datos.totalElements;
        this.cdr.markForCheck();
      },
      error: (error) => {
        this.cargando = false;
        this.vacunas = [];
        this.totalElementos = 0;
        this.totalPaginas = 0;
        this.mensajeError = mensajeDeError(error, 'No se pudo cargar el catálogo de vacunas.');
        this.cdr.markForCheck();
      },
    });
  }

  aplicarFiltros(): void {
    if (this.cargando) return;
    this.nombreAplicado = this.busquedaNombre.trim();
    this.pagina = 0;
    this.cargarVacunas();
  }

  cambiarTamano(size: number): void {
    if (this.cargando) return;
    this.size = size;
    this.pagina = 0;
    this.cargarVacunas();
  }

  cambiarPagina(pagina: number): void {
    if (this.cargando || pagina < 0 || pagina >= this.totalPaginas) return;
    this.pagina = pagina;
    this.cargarVacunas();
  }

  abrirNuevo(): void {
    if (!this.esAdmin || this.cargando) return;
    this.vacunaEditandoId = null;
    this.vacunaForm.reset();
    this.mensajeError = '';
    this.mostrarFormulario = true;
  }

  abrirEditar(vacuna: Vacuna): void {
    if (!this.esAdmin) return;
    this.vacunaEditandoId = vacuna.id;
    this.vacunaForm.setValue({
      nombre: vacuna.nombre,
      descripcion: vacuna.descripcion ?? '',
      enfermedadPreviene: vacuna.enfermedadPreviene,
    });
    this.mensajeError = '';
    this.mostrarFormulario = true;
  }

  cerrarFormulario(): void {
    if (this.guardando) return;
    this.mostrarFormulario = false;
    this.vacunaEditandoId = null;
    this.vacunaForm.reset();
  }

  guardar(): void {
    if (!this.esAdmin || this.guardando) return;
    if (this.vacunaForm.invalid) {
      this.vacunaForm.markAllAsTouched();
      return;
    }

    const valores = this.vacunaForm.getRawValue();
    const datos: VacunaRequest = {
      nombre: valores.nombre.trim(),
      descripcion: valores.descripcion.trim() || null,
      enfermedadPreviene: valores.enfermedadPreviene.trim(),
    };
    const operacion = this.vacunaEditandoId === null
      ? this.vacunaService.registrarVacuna(datos)
      : this.vacunaService.editarVacuna(this.vacunaEditandoId, datos);

    this.guardando = true;
    this.mensajeError = '';
    operacion.subscribe({
      next: (response) => {
        this.guardando = false;
        this.mensajeExito = response.message;
        this.cerrarFormulario();
        this.cargarVacunas();
        this.cdr.markForCheck();
      },
      error: (error) => {
        this.guardando = false;
        this.mensajeError = mensajeDeError(error, 'No se pudo guardar la vacuna.');
        this.cdr.markForCheck();
      },
    });
  }

  pedirDesactivar(vacuna: Vacuna): void {
    if (!this.esAdmin || this.procesandoId !== null) return;
    this.vacunaADesactivar = vacuna;
  }

  cancelarDesactivacion(): void {
    this.vacunaADesactivar = null;
  }

  confirmarDesactivacion(): void {
    const vacuna = this.vacunaADesactivar;
    this.vacunaADesactivar = null;
    if (!this.esAdmin || !vacuna || this.procesandoId !== null) return;
    this.procesandoId = vacuna.id;
    this.mensajeError = '';
    this.vacunaService.desactivarVacuna(vacuna.id).subscribe({
      next: (response) => {
        this.procesandoId = null;
        this.mensajeExito = response.message;
        this.cargarVacunas();
        this.cdr.markForCheck();
      },
      error: (error) => {
        this.procesandoId = null;
        this.mensajeError = mensajeDeError(error, 'No se pudo desactivar la vacuna.');
        this.cdr.markForCheck();
      },
    });
  }

  activar(vacuna: Vacuna): void {
    if (!this.esAdmin || this.procesandoId !== null) return;
    this.procesandoId = vacuna.id;
    this.mensajeError = '';
    this.vacunaService.activarVacuna(vacuna.id).subscribe({
      next: (response) => {
        this.procesandoId = null;
        this.mensajeExito = response.message;
        this.cargarVacunas();
        this.cdr.markForCheck();
      },
      error: (error) => {
        this.procesandoId = null;
        this.mensajeError = mensajeDeError(error, 'No se pudo activar la vacuna.');
        this.cdr.markForCheck();
      },
    });
  }
}
