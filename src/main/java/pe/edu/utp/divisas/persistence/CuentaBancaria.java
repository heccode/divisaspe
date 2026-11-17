package pe.edu.utp.divisas.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import pe.edu.utp.divisas.model.FlujoCuenta;
import pe.edu.utp.divisas.model.Moneda;

@Entity
@Table(name = "cuentas_bancarias",
        indexes = @Index(name = "idx_cuentas_usuario", columnList = "usuarioId"),
        uniqueConstraints = @UniqueConstraint(name = "uk_cuenta_usuario_flujo_cci", columnNames = {"usuarioId", "flujo", "cci"}))
public class CuentaBancaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private FlujoCuenta flujo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Moneda moneda;

    @ManyToOne(optional = false)
    @JoinColumn(name = "banco_id", nullable = false)
    private Banco banco;

    @Column(nullable = false, length = 20)
    private String numeroCuenta;

    @Column(nullable = false, length = 20)
    private String cci;

    @Column(nullable = false, length = 120)
    private String titular;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    protected CuentaBancaria() {
    }

    public CuentaBancaria(Long usuarioId, FlujoCuenta flujo, Moneda moneda, Banco banco,
                          String numeroCuenta, String cci, String titular) {
        this.usuarioId = usuarioId;
        this.flujo = flujo;
        this.moneda = moneda;
        this.banco = banco;
        this.numeroCuenta = numeroCuenta;
        this.cci = cci;
        this.titular = titular;
    }

    @PrePersist
    void alCrear() {
        fechaRegistro = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getUsuarioId() { return usuarioId; }
    public FlujoCuenta getFlujo() { return flujo; }
    public Moneda getMoneda() { return moneda; }
    public Banco getBanco() { return banco; }
    public String getNumeroCuenta() { return numeroCuenta; }
    public String getCci() { return cci; }
    public String getTitular() { return titular; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
}
