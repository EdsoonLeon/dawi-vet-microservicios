import { TestBed } from '@angular/core/testing';

import { UsuarioAuthService } from './usuario-auth-service';

describe('UsuarioAuthService', () => {
  let service: UsuarioAuthService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(UsuarioAuthService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
