package com.utp.sedapal.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "estados_reclamo")
public class EstadoReclamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @jakarta.persistence.Column(unique = true, nullable = false)
    private String nombre;

    @jakarta.persistence.Column(nullable = false)
    private int orden;

    @OneToMany(mappedBy = "estado")
    private List<Reclamo> reclamos = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public List<Reclamo> getReclamos() {
        return reclamos;
    }

    public void setReclamos(List<Reclamo> reclamos) {
        this.reclamos = reclamos;
    }
}
