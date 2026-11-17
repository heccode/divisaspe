package pe.edu.utp.divisas.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "bancos")
public class Banco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false, length = 3)
    private String prefijoCci;

    protected Banco() {
    }

    public Banco(String codigo, String nombre, String prefijoCci) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.prefijoCci = prefijoCci;
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getPrefijoCci() { return prefijoCci; }
}
