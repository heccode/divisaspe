package pe.edu.utp.divisas.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditoriaService {

    private static final Logger AUDITORIA = LoggerFactory.getLogger("auditoria");

    public void registrarEvento(String evento, Long usuarioId, String detalle) {
        AUDITORIA.info("evento={} usuarioId={} detalle={}", evento, usuarioId, detalle);
    }
}
