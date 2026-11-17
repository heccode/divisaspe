package pe.edu.utp.divisas.service;

import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.divisas.exception.DatosUsuarioInvalidosException;
import pe.edu.utp.divisas.exception.EmailDuplicadoException;
import pe.edu.utp.divisas.exception.UsuarioNoEncontradoException;
import pe.edu.utp.divisas.model.RegistroRequest;
import pe.edu.utp.divisas.model.TipoDocumento;
import pe.edu.utp.divisas.model.UsuarioRequest;
import pe.edu.utp.divisas.model.UsuarioResponse;
import pe.edu.utp.divisas.persistence.Usuario;
import pe.edu.utp.divisas.persistence.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final ValidacionService validacionService;

    public UsuarioService(UsuarioRepository repository, ValidacionService validacionService) {
        this.repository = repository;
        this.validacionService = validacionService;
    }

    @Transactional
    public UsuarioResponse registrarUsuario(RegistroRequest request, String passwordHash) {
        String email = normalizarEmail(request.email());
        String ruc = normalizarRuc(request.ruc());
        if (repository.existsByEmail(email)) {
            throw new EmailDuplicadoException(email);
        }
        try {
            return aRespuesta(repository.saveAndFlush(new Usuario(request.nombre().trim(), email, ruc, passwordHash)));
        } catch (DataIntegrityViolationException e) {
            throw new EmailDuplicadoException(email);
        }
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerUsuario(Long id) {
        return aRespuesta(buscar(id));
    }

    @Transactional
    public UsuarioResponse actualizarUsuario(Long id, UsuarioRequest request) {
        Usuario usuario = buscar(id);
        String email = normalizarEmail(request.email());
        String ruc = normalizarRuc(request.ruc());
        String apellidos = opcional(request.apellidos());
        String numeroDocumento = opcional(request.numeroDocumento());
        numeroDocumento = numeroDocumento == null ? null : numeroDocumento.toUpperCase(Locale.ROOT);
        validarDocumento(request.tipoDocumento(), numeroDocumento);
        if (repository.existsByEmailAndIdNot(email, id)) {
            throw new EmailDuplicadoException(email);
        }
        usuario.actualizar(request.nombre().trim(), apellidos, numeroDocumento == null ? null : request.tipoDocumento(),
                numeroDocumento, email, ruc);
        try {
            return aRespuesta(repository.saveAndFlush(usuario));
        } catch (DataIntegrityViolationException e) {
            throw new EmailDuplicadoException(email);
        }
    }

    private Usuario buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new UsuarioNoEncontradoException(id));
    }

    public static String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static String opcional(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim().replaceAll("\\s+", " ");
    }

    private static void validarDocumento(TipoDocumento tipo, String numero) {
        if (tipo == null && numero == null) {
            return;
        }
        if (tipo == null || numero == null) {
            throw new DatosUsuarioInvalidosException("Indica el tipo y el número de documento.");
        }
        if (!tipo.admite(numero)) {
            throw new DatosUsuarioInvalidosException(tipo.mensajeFormato());
        }
    }

    private String normalizarRuc(String ruc) {
        if (ruc == null || ruc.isBlank()) {
            return null;
        }
        String limpio = ruc.trim();
        validacionService.validarRuc(limpio);
        return limpio;
    }

    public static UsuarioResponse aRespuesta(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNombre(), u.getApellidos(), u.getTipoDocumento(),
                u.getNumeroDocumento(), u.getEmail(), u.getRuc(), u.getFechaRegistro(), u.getFechaActualizacion());
    }
}
