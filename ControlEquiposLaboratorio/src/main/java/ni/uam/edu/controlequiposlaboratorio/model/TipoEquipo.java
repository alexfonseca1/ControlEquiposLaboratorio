package ni.uam.edu.controlequiposlaboratorio.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tipo_equipo", schema = "laboratorio")
public class TipoEquipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo")
    private Long idTipo;

    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 250)
    private String descripcion;

    @OneToMany(mappedBy = "tipoEquipo", fetch = FetchType.LAZY)
    private List<Equipo> equipos = new ArrayList<>();

    public TipoEquipo() {
    }

    public TipoEquipo(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public Long getIdTipo() {
        return idTipo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<Equipo> getEquipos() {
        return equipos;
    }

    @Override
    public String toString() {
        return nombre;
    }
}