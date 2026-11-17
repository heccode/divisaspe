package pe.edu.utp.divisas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import pe.edu.utp.divisas.exception.InvalidConversionException;
import pe.edu.utp.divisas.model.Moneda;
import pe.edu.utp.divisas.model.ResultadoConversion;
import pe.edu.utp.divisas.model.TipoCambio;

class ConversionServiceTest {

    private final ConversionService service = new ConversionService();
    private final TipoCambio tc = new TipoCambio(LocalDate.of(2026, 8, 12),
            new BigDecimal("3.350"), new BigDecimal("3.360"), "BCRPData");

    @Test
    void usdAPenUsaCompra() {
        ResultadoConversion r = service.convertir(new BigDecimal("100"), Moneda.USD, Moneda.PEN, tc);
        assertThat(r.resultado()).isEqualByComparingTo("335.00");
        assertThat(r.resultado().scale()).isEqualTo(2);
        assertThat(r.tipoCambioAplicado()).isEqualByComparingTo("3.350");
    }

    @Test
    void penAUsdUsaVentaYRedondeaHalfUp() {
        ResultadoConversion r = service.convertir(new BigDecimal("336"), Moneda.PEN, Moneda.USD, tc);
        assertThat(r.resultado()).isEqualByComparingTo("100.00");
        assertThat(r.tipoCambioAplicado()).isEqualByComparingTo("3.360");

        ResultadoConversion r2 = service.convertir(new BigDecimal("100"), Moneda.PEN, Moneda.USD, tc);
        assertThat(r2.resultado()).isEqualByComparingTo("29.76");
    }

    @Test
    void montoCeroEsInvalido() {
        assertThatThrownBy(() -> service.convertir(BigDecimal.ZERO, Moneda.USD, Moneda.PEN, tc))
                .isInstanceOf(InvalidConversionException.class);
    }

    @Test
    void montoNegativoEsInvalido() {
        assertThatThrownBy(() -> service.convertir(new BigDecimal("-5"), Moneda.USD, Moneda.PEN, tc))
                .isInstanceOf(InvalidConversionException.class);
    }

    @Test
    void monedaNoSoportadaSeRechazaAlConvertirTextoAMoneda() {
        assertThatThrownBy(() -> Moneda.valueOf("EUR")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void monedaNulaEsInvalida() {
        assertThatThrownBy(() -> service.convertir(BigDecimal.ONE, null, Moneda.PEN, tc))
                .isInstanceOf(InvalidConversionException.class);
    }

    @Test
    void origenIgualADestinoEsInvalido() {
        assertThatThrownBy(() -> service.convertir(BigDecimal.TEN, Moneda.USD, Moneda.USD, tc))
                .isInstanceOf(InvalidConversionException.class);
    }
}
