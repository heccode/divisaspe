package pe.edu.utp.divisas.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import pe.edu.utp.divisas.model.TipoDocumento;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 120)
    private String apellidos;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private TipoDocumento tipoDocumento;

    @Column(length = 20)
    private String numeroDocumento;

    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @Column(length = 11)
    private String ruc;

    @Column(length = 100)
    private String passwordHash;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;

    protected Usuario() {
    }

    public Usuario(String nombre, String email, String ruc, String passwordHash) {
        this.nombre = nombre;
        this.email = email;
        this.ruc = ruc;
        this.passwordHash = passwordHash;
    }

    @PrePersist
    void alCrear() {
        fechaRegistro = LocalDateTime.now();
        fechaActualizacion = fechaRegistro;
    }

    @PreUpdate
    void alActualizar() {
        fechaActualizacion = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellidos() { return apellidos; }
    public TipoDocumento getTipoDocumento() { return tipoDocumento; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public String getEmail() { return email; }
    public String getRuc() { return ruc; }
    public String getPasswordHash() { return passwordHash; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }

    public void actualizar(String nombre, String apellidos, TipoDocumento tipoDocumento, String numeroDocumento,
                           String email, String ruc) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.email = email;
        this.ruc = ruc;
    }

    public void cambiarPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
