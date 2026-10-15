import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Veterinario } from './veterinario';

describe('Veterinario', () => {
  let component: Veterinario;
  let fixture: ComponentFixture<Veterinario>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Veterinario],
    }).compileComponents();

    fixture = TestBed.createComponent(Veterinario);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
