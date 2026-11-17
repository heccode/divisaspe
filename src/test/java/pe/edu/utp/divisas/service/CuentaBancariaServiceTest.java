package pe.edu.utp.divisas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.edu.utp.divisas.exception.CuentaBancariaDuplicadaException;
import pe.edu.utp.divisas.exception.CuentaBancariaInvalidaException;
import pe.edu.utp.divisas.exception.UsuarioNoEncontradoException;
import pe.edu.utp.divisas.model.CuentaBancariaRequest;
import pe.edu.utp.divisas.model.CuentaBancariaResponse;
import pe.edu.utp.divisas.model.FlujoCuenta;
import pe.edu.utp.divisas.model.Moneda;
import pe.edu.utp.divisas.persistence.Banco;
import pe.edu.utp.divisas.persistence.BancoRepository;
import pe.edu.utp.divisas.persistence.CuentaBancaria;
import pe.edu.utp.divisas.persistence.CuentaBancariaRepository;
import pe.edu.utp.divisas.persistence.UsuarioRepository;

class CuentaBancariaServiceTest {

    private static final String CCI_BCP = "00219300123456789012";

    private CuentaBancariaRepository cuentas;
    private BancoRepository bancos;
    private UsuarioRepository usuarios;
    private CuentaBancariaService service;

    @BeforeEach
    void preparar() {
        cuentas = mock(CuentaBancariaRepository.class);
        bancos = mock(BancoRepository.class);
        usuarios = mock(UsuarioRepository.class);
        service = new CuentaBancariaService(cuentas, bancos, usuarios, mock(AuditoriaService.class));
        when(usuarios.existsById(5L)).thenReturn(true);
        when(bancos.findById(1L)).thenReturn(Optional.of(new Banco("BCP", "BCP", "002")));
        when(cuentas.saveAndFlush(any(CuentaBancaria.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private static CuentaBancariaRequest solicitud(String cci, String titular) {
        return new CuentaBancariaRequest(FlujoCuenta.ENVIO, Moneda.USD, 1L, "1930012345678", cci, titular);
    }

    @Test
    void registraLaCuentaYNormalizaElTitular() {
        CuentaBancariaResponse r = service.registrar(5L, solicitud(CCI_BCP, "  Lucia   Quispe Flores "));
        assertEquals("Lucia Quispe Flores", r.titular());
        assertEquals(FlujoCuenta.ENVIO, r.flujo());
        assertEquals(Moneda.USD, r.moneda());
        assertEquals("BCP", r.banco().codigo());
    }

    @Test
    void rechazaCciDeOtroBanco() {
        assertThrows(CuentaBancariaInvalidaException.class,
                () -> service.registrar(5L, solicitud("01121300123456789012", "Lucia Quispe Flores")));
        verify(cuentas, never()).saveAndFlush(any());
    }

    @Test
    void rechazaBancoInexistente() {
        CuentaBancariaRequest r = new CuentaBancariaRequest(FlujoCuenta.RECEPCION, Moneda.PEN, 99L,
                "1930012345678", CCI_BCP, "Lucia Quispe Flores");
        assertThrows(CuentaBancariaInvalidaException.class, () -> service.registrar(5L, r));
    }

    @Test
    void rechazaCuentaDuplicada() {
        when(cuentas.existsByUsuarioIdAndFlujoAndCci(5L, FlujoCuenta.ENVIO, CCI_BCP)).thenReturn(true);
        assertThrows(CuentaBancariaDuplicadaException.class,
                () -> service.registrar(5L, solicitud(CCI_BCP, "Lucia Quispe Flores")));
    }

    @Test
    void rechazaUsuarioInexistente() {
        assertThrows(UsuarioNoEncontradoException.class,
                () -> service.registrar(8L, solicitud(CCI_BCP, "Lucia Quispe Flores")));
        assertThrows(UsuarioNoEncontradoException.class, () -> service.listar(8L));
    }
}
