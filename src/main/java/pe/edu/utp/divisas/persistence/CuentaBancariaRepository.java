package pe.edu.utp.divisas.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utp.divisas.model.FlujoCuenta;

public interface CuentaBancariaRepository extends JpaRepository<CuentaBancaria, Long> {

    List<CuentaBancaria> findByUsuarioIdOrderByFechaRegistroDescIdDesc(Long usuarioId);

    boolean existsByUsuarioIdAndFlujoAndCci(Long usuarioId, FlujoCuenta flujo, String cci);
}
