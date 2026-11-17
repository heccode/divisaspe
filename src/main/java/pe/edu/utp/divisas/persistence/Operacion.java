package pe.edu.utp.divisas.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import pe.edu.utp.divisas.model.Moneda;

@Entity
@Table(name = "operaciones", indexes = @Index(name = "idx_operaciones_usuario", columnList = "usuarioId"))
public class Operacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long usuarioId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Moneda monedaOrigen;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Moneda monedaDestino;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal tipoCambioAplicado;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal resultado;

    @Column(nullable = false)
    private LocalDate fechaReferencia;

    @Column(nullable = false, length = 40)
    private String fuente;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaOperacion;

    protected Operacion() {
    }

    public Operacion(Long usuarioId, BigDecimal monto, Moneda monedaOrigen, Moneda monedaDestino,
                     BigDecimal tipoCambioAplicado, BigDecimal resultado, LocalDate fechaReferencia, String fuente) {
        this.usuarioId = usuarioId;
        this.monto = monto;
        this.monedaOrigen = monedaOrigen;
        this.monedaDestino = monedaDestino;
        this.tipoCambioAplicado = tipoCambioAplicado;
        this.resultado = resultado;
        this.fechaReferencia = fechaReferencia;
        this.fuente = fuente;
    }

    @PrePersist
    void alCrear() {
        fechaOperacion = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getUsuarioId() { return usuarioId; }
    public BigDecimal getMonto() { return monto; }
    public Moneda getMonedaOrigen() { return monedaOrigen; }
    public Moneda getMonedaDestino() { return monedaDestino; }
    public BigDecimal getTipoCambioAplicado() { return tipoCambioAplicado; }
    public BigDecimal getResultado() { return resultado; }
    public LocalDate getFechaReferencia() { return fechaReferencia; }
    public String getFuente() { return fuente; }
    public LocalDateTime getFechaOperacion() { return fechaOperacion; }
}
