package com.dawivet.veterinarios.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dawivet.veterinarios.client.AuthClient;
import com.dawivet.veterinarios.dto.RolDto;
import com.dawivet.veterinarios.dto.UsuarioDatos;
import com.dawivet.veterinarios.dto.UsuarioDto;
import com.dawivet.veterinarios.dto.UsuarioRegistroDto;
import com.dawivet.veterinarios.dto.VeterinarioRequest;
import com.dawivet.veterinarios.model.Veterinario;
import com.dawivet.veterinarios.repository.IVeterinarioRepository;

@ExtendWith(MockitoExtension.class)
class VeterinarioServiceTest {

    @Mock
    private IVeterinarioRepository repository;

    @Mock
    private AuthClient authClient;

    @InjectMocks
    private VeterinarioService service;

    @Test
    void creaLaCuentaMedianteAuthYConservaElContratoFeign() {
        RolDto rol = new RolDto(3, "VETERINARIO");
        UsuarioDto usuarioCreado = new UsuarioDto();
        usuarioCreado.setId(20);
        when(authClient.listarRoles()).thenReturn(List.of(rol));
        when(authClient.registrar(any())).thenReturn(usuarioCreado);
        when(repository.save(any())).thenAnswer(invocation -> {
            Veterinario veterinario = invocation.getArgument(0);
            veterinario.setId(7);
            return veterinario;
        });

        UsuarioDatos usuario = new UsuarioDatos();
        usuario.setNombre("Ana");
        usuario.setApellido("López");
        usuario.setCorreo("ana@vet.com");
        usuario.setClave("ClaveSegura123");
        VeterinarioRequest request = new VeterinarioRequest();
        request.setEspecialidad("Cirugía");
        request.setColegiatura("CMVP-1234");
        request.setEdad(35);
        request.setUsuario(usuario);

        Veterinario resultado = service.registrarVeterinario(request);

        ArgumentCaptor<UsuarioRegistroDto> captor = ArgumentCaptor.forClass(UsuarioRegistroDto.class);
        verify(authClient).registrar(captor.capture());
        assertEquals(3, captor.getValue().getRol().getId());
        assertEquals("VETERINARIO", captor.getValue().getRol().getNombre());
        assertEquals(20, resultado.getUsuarioId());
        assertEquals(7, resultado.getId());
    }
}
