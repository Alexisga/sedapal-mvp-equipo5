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
@Table(name = "suministros")
public class Suministro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @jakarta.persistence.Column(unique = true, nullable = false)
    private String codigoSuministro;
    private String direccion;
    private String distrito;

    @ManyToOne
    private UsuarioServicio usuario;

    @ManyToOne
    private Zona zona;

    @OneToMany(mappedBy = "suministro")
    private List<Reclamo> reclamos = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoSuministro() {
        return codigoSuministro;
    }

    public void setCodigoSuministro(String codigoSuministro) {
        this.codigoSuministro = codigoSuministro;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }

    public UsuarioServicio getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioServicio usuario) {
        this.usuario = usuario;
    }

    public Zona getZona() {
        return zona;
    }

    public void setZona(Zona zona) {
        this.zona = zona;
    }

    public List<Reclamo> getReclamos() {
        return reclamos;
    }

    public void setReclamos(List<Reclamo> reclamos) {
        this.reclamos = reclamos;
    }
}
