package com.utp.sedapal.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "evidencias")
public class Evidencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombreOriginal;
    private String nombreAlmacenado;
    private String tipoContenido;
    private String ruta;
    private LocalDateTime fechaCarga;
    private String descripcion;
    private boolean visibleCiudadano;
    public boolean isVisibleCiudadano(){return visibleCiudadano;}
    public void setVisibleCiudadano(boolean v){visibleCiudadano=v;}

    @ManyToOne
    private AtencionTecnica atencion;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombreOriginal() { return nombreOriginal; }
    public void setNombreOriginal(String nombreOriginal) { this.nombreOriginal = nombreOriginal; }
    public String getNombreAlmacenado() { return nombreAlmacenado; }
    public void setNombreAlmacenado(String nombreAlmacenado) { this.nombreAlmacenado = nombreAlmacenado; }
    public String getTipoContenido() { return tipoContenido; }
    public void setTipoContenido(String tipoContenido) { this.tipoContenido = tipoContenido; }
    public String getRuta() { return ruta; }
    public void setRuta(String ruta) { this.ruta = ruta; }
    public LocalDateTime getFechaCarga() { return fechaCarga; }
    public void setFechaCarga(LocalDateTime fechaCarga) { this.fechaCarga = fechaCarga; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public AtencionTecnica getAtencion() { return atencion; }
    public void setAtencion(AtencionTecnica atencion) { this.atencion = atencion; }
}
