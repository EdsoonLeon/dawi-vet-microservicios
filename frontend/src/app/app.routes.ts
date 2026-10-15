import { Routes } from '@angular/router';
import { Login } from './pages/login/login';
import { Admin } from './pages/admin/admin';
import { Usuarios } from './pages/admin/usuarios/usuarios';
import { Veterinario } from './pages/veterinario/veterinario';
import { Agenda } from './pages/veterinario/agenda/agenda';
import { Consultas } from './pages/veterinario/consultas/consultas';
import { Recepcionista } from './pages/recepcionista/recepcionista';
import { Clientes } from './pages/shared/clientes/clientes';
import { Mascotas } from './pages/shared/mascotas/mascotas';
import { Citas } from './pages/shared/citas/citas';
import { authGuard } from './seguridad/auth-guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: Login },
  {
    path: 'admin',
    component: Admin,
    canActivate: [authGuard],
    data: { roles: ['ADMINISTRADOR'] },
    children: [
      { path: '', redirectTo: 'usuarios', pathMatch: 'full' },
      { path: 'usuarios', component: Usuarios },
      { path: 'clientes', component: Clientes },
      { path: 'mascotas', component: Mascotas },
      { path: 'citas', component: Citas },
    ]
  },
  {
    path: 'veterinario',
    component: Veterinario,
    canActivate: [authGuard],
    data: { roles: ['VETERINARIO'] },
    children: [
      { path: '', redirectTo: 'agenda', pathMatch: 'full' },
      { path: 'agenda', component: Agenda },
      { path: 'consultas', component: Consultas },
    ]
  },
  {
    path: 'recepcionista',
    component: Recepcionista,
    canActivate: [authGuard],
    data: { roles: ['RECEPCIONISTA'] },
    children: [
      { path: '', redirectTo: 'clientes', pathMatch: 'full' },
      { path: 'clientes', component: Clientes },
      { path: 'mascotas', component: Mascotas },
      { path: 'citas', component: Citas },
    ]
  },
];