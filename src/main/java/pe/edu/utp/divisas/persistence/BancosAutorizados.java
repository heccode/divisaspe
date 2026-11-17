package pe.edu.utp.divisas.persistence;

import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BancosAutorizados implements ApplicationRunner {

    private static final List<Banco> CATALOGO = List.of(
            new Banco("BCP", "BCP", "002"),
            new Banco("INTERBANK", "Interbank", "003"),
            new Banco("SCOTIABANK", "Scotiabank", "009"),
            new Banco("BBVA", "BBVA", "011"),
            new Banco("BANBIF", "BanBif", "038"));

    private final BancoRepository repository;

    public BancosAutorizados(BancoRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        CATALOGO.stream()
                .filter(b -> !repository.existsByCodigo(b.getCodigo()))
                .forEach(b -> repository.save(new Banco(b.getCodigo(), b.getNombre(), b.getPrefijoCci())));
    }
}
