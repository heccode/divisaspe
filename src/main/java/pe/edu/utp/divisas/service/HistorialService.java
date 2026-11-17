package pe.edu.utp.divisas.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.divisas.exception.UsuarioNoEncontradoException;
import pe.edu.utp.divisas.model.ConversionResponse;
import pe.edu.utp.divisas.model.OperacionResponse;
import pe.edu.utp.divisas.persistence.Operacion;
import pe.edu.utp.divisas.persistence.OperacionRepository;
import pe.edu.utp.divisas.persistence.UsuarioRepository;

@Service
public class HistorialService {

    private final OperacionRepository operaciones;
    private final UsuarioRepository usuarios;

    public HistorialService(OperacionRepository operaciones, UsuarioRepository usuarios) {
        this.operaciones = operaciones;
        this.usuarios = usuarios;
    }

    @Transactional
    public OperacionResponse registrarOperacion(Long usuarioId, ConversionResponse c) {
        verificarUsuario(usuarioId);
        Operacion op = operaciones.save(new Operacion(usuarioId, c.montoOriginal(), c.monedaOrigen(),
                c.monedaDestino(), c.tipoCambioAplicado(), c.resultado(), c.fechaReferencia(), c.fuente()));
        return aRespuesta(op);
    }

    @Transactional(readOnly = true)
    public List<OperacionResponse> consultarHistorial(Long usuarioId) {
        verificarUsuario(usuarioId);
        return operaciones.findByUsuarioIdOrderByFechaOperacionDescIdDesc(usuarioId).stream()
                .map(HistorialService::aRespuesta)
                .toList();
    }

    private void verificarUsuario(Long usuarioId) {
        if (usuarioId == null || !usuarios.existsById(usuarioId)) {
            throw new UsuarioNoEncontradoException(usuarioId);
        }
    }

    private static OperacionResponse aRespuesta(Operacion o) {
        return new OperacionResponse(o.getId(), o.getUsuarioId(), o.getMonto(), o.getMonedaOrigen(),
                o.getMonedaDestino(), o.getTipoCambioAplicado(), o.getResultado(), o.getFechaReferencia(),
                o.getFuente(), o.getFechaOperacion());
    }
}
