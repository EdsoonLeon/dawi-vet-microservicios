import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-confirm-modal',
  standalone: true,
  imports: [],
  templateUrl: './confirm-modal.html',
  styleUrl: './confirm-modal.css'
})
export class ConfirmModal {
  visible = input(false);
  titulo = input('Confirmar acción');
  mensaje = input('¿Estás seguro?');
  textoConfirmar = input('Eliminar');

  confirmado = output<void>();
  cancelado = output<void>();

  onConfirmar() {
    this.confirmado.emit();
  }

  onCancelar() {
    this.cancelado.emit();
  }
}