package com.utp.sedapal.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tecnicos")
public class Tecnico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombres;
    private String especialidad;
    private boolean disponible;

    @ManyToOne
    private UsuarioServicio usuario;

    @OneToMany(mappedBy = "tecnico")
    private List<AsignacionTecnica> asignaciones = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public UsuarioServicio getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioServicio usuario) {
        this.usuario = usuario;
    }

    public List<AsignacionTecnica> getAsignaciones() {
        return asignaciones;
    }

    public void setAsignaciones(List<AsignacionTecnica> asignaciones) {
        this.asignaciones = asignaciones;
    }
}
