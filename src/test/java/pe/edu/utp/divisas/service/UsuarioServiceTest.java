package pe.edu.utp.divisas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.edu.utp.divisas.exception.DatosUsuarioInvalidosException;
import pe.edu.utp.divisas.model.TipoDocumento;
import pe.edu.utp.divisas.model.UsuarioRequest;
import pe.edu.utp.divisas.model.UsuarioResponse;
import pe.edu.utp.divisas.persistence.Usuario;
import pe.edu.utp.divisas.persistence.UsuarioRepository;

class UsuarioServiceTest {

    private UsuarioRepository repository;
    private UsuarioService service;

    @BeforeEach
    void preparar() throws ReflectiveOperationException {
        repository = mock(UsuarioRepository.class);
        service = new UsuarioService(repository, mock(ValidacionService.class));
        Usuario u = new Usuario("Lucia", "lucia.quispe@gmail.com", null, "hash");
        for (String campo : new String[] {"fechaRegistro", "fechaActualizacion"}) {
            var f = Usuario.class.getDeclaredField(campo);
            f.setAccessible(true);
            f.set(u, LocalDateTime.now());
        }
        when(repository.findById(9L)).thenReturn(Optional.of(u));
        when(repository.saveAndFlush(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private static UsuarioRequest datos(TipoDocumento tipo, String numero) {
        return new UsuarioRequest("Lucia", "  Quispe   Flores ", tipo, numero, "Lucia.Quispe@gmail.com", null);
    }

    @Test
    void guardaApellidosYDocumento() {
        UsuarioResponse r = service.actualizarUsuario(9L, datos(TipoDocumento.DNI, "45871236"));
        assertEquals("Quispe Flores", r.apellidos());
        assertEquals(TipoDocumento.DNI, r.tipoDocumento());
        assertEquals("45871236", r.numeroDocumento());
    }

    @Test
    void documentoEsOpcionalSiNoSeIndicaNada() {
        UsuarioResponse r = service.actualizarUsuario(9L, datos(null, " "));
        assertNull(r.tipoDocumento());
        assertNull(r.numeroDocumento());
    }

    @Test
    void pasaporteSeGuardaEnMayusculas() {
        assertEquals("AB123456", service.actualizarUsuario(9L, datos(TipoDocumento.PASAPORTE, "ab123456")).numeroDocumento());
    }

    @Test
    void rechazaDocumentoIncompletoOConFormatoInvalido() {
        assertThrows(DatosUsuarioInvalidosException.class, () -> service.actualizarUsuario(9L, datos(TipoDocumento.DNI, null)));
        assertThrows(DatosUsuarioInvalidosException.class, () -> service.actualizarUsuario(9L, datos(null, "45871236")));
        assertThrows(DatosUsuarioInvalidosException.class, () -> service.actualizarUsuario(9L, datos(TipoDocumento.DNI, "4587123")));
        assertThrows(DatosUsuarioInvalidosException.class, () -> service.actualizarUsuario(9L, datos(TipoDocumento.CE, "ABC123456")));
    }
}
