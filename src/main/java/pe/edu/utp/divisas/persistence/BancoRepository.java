package pe.edu.utp.divisas.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BancoRepository extends JpaRepository<Banco, Long> {

    boolean existsByCodigo(String codigo);

    List<Banco> findAllByOrderByNombreAsc();
}
