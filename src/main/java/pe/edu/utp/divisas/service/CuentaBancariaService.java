package pe.edu.utp.divisas.service;

import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.divisas.exception.CuentaBancariaDuplicadaException;
import pe.edu.utp.divisas.exception.CuentaBancariaInvalidaException;
import pe.edu.utp.divisas.exception.UsuarioNoEncontradoException;
import pe.edu.utp.divisas.model.BancoResponse;
import pe.edu.utp.divisas.model.CuentaBancariaRequest;
import pe.edu.utp.divisas.model.CuentaBancariaResponse;
import pe.edu.utp.divisas.persistence.Banco;
import pe.edu.utp.divisas.persistence.BancoRepository;
import pe.edu.utp.divisas.persistence.CuentaBancaria;
import pe.edu.utp.divisas.persistence.CuentaBancariaRepository;
import pe.edu.utp.divisas.persistence.UsuarioRepository;

@Service
public class CuentaBancariaService {

    private final CuentaBancariaRepository cuentas;
    private final BancoRepository bancos;
    private final UsuarioRepository usuarios;
    private final AuditoriaService auditoria;

    public CuentaBancariaService(CuentaBancariaRepository cuentas, BancoRepository bancos,
                                 UsuarioRepository usuarios, AuditoriaService auditoria) {
        this.cuentas = cuentas;
        this.bancos = bancos;
        this.usuarios = usuarios;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public List<BancoResponse> listarBancos() {
        return bancos.findAllByOrderByNombreAsc().stream().map(CuentaBancariaService::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public List<CuentaBancariaResponse> listar(Long usuarioId) {
        verificarUsuario(usuarioId);
        return cuentas.findByUsuarioIdOrderByFechaRegistroDescIdDesc(usuarioId).stream()
                .map(CuentaBancariaService::aRespuesta)
                .toList();
    }

    @Transactional
    public CuentaBancariaResponse registrar(Long usuarioId, CuentaBancariaRequest request) {
        verificarUsuario(usuarioId);
        Banco banco = bancos.findById(request.bancoId())
                .orElseThrow(() -> new CuentaBancariaInvalidaException("El banco seleccionado no esta disponible."));
        // Los tres primeros digitos del CCI identifican a la entidad emisora.
        if (!request.cci().startsWith(banco.getPrefijoCci())) {
            throw new CuentaBancariaInvalidaException("El CCI no corresponde al banco " + banco.getNombre() + ".");
        }
        if (cuentas.existsByUsuarioIdAndFlujoAndCci(usuarioId, request.flujo(), request.cci())) {
            throw new CuentaBancariaDuplicadaException();
        }
        String titular = request.titular().trim().replaceAll("\\s+", " ");
        try {
            CuentaBancaria cuenta = cuentas.saveAndFlush(new CuentaBancaria(usuarioId, request.flujo(),
                    request.moneda(), banco, request.numeroCuenta(), request.cci(), titular));
            auditoria.registrarEvento("CUENTA_BANCARIA_REGISTRADA", usuarioId,
                    request.flujo() + " " + request.moneda() + " " + banco.getCodigo());
            return aRespuesta(cuenta);
        } catch (DataIntegrityViolationException e) {
            throw new CuentaBancariaDuplicadaException();
        }
    }

    private void verificarUsuario(Long usuarioId) {
        if (usuarioId == null || !usuarios.existsById(usuarioId)) {
            throw new UsuarioNoEncontradoException(usuarioId);
        }
    }

    private static BancoResponse aRespuesta(Banco b) {
        return new BancoResponse(b.getId(), b.getCodigo(), b.getNombre(), b.getPrefijoCci());
    }

    private static CuentaBancariaResponse aRespuesta(CuentaBancaria c) {
        return new CuentaBancariaResponse(c.getId(), c.getFlujo(), c.getMoneda(), aRespuesta(c.getBanco()),
                c.getNumeroCuenta(), c.getCci(), c.getTitular(), c.getFechaRegistro());
    }
}
