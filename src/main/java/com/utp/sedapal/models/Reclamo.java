package com.utp.sedapal.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reclamos")
public class Reclamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String codigo;

    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaConfirmacion;
    private String confirmadoPor;
    @Column(length=500) private String motivoReapertura;
    private LocalDateTime fechaSolicitudReapertura;
    public LocalDateTime getFechaConfirmacion(){return fechaConfirmacion;}
    public void setFechaConfirmacion(LocalDateTime v){fechaConfirmacion=v;}
    public String getConfirmadoPor(){return confirmadoPor;}
    public void setConfirmadoPor(String v){confirmadoPor=v;}
    public String getMotivoReapertura(){return motivoReapertura;}
    public void setMotivoReapertura(String v){motivoReapertura=v;}
    public LocalDateTime getFechaSolicitudReapertura(){return fechaSolicitudReapertura;}
    public void setFechaSolicitudReapertura(LocalDateTime v){fechaSolicitudReapertura=v;}


    @Column(length = 1000)
    private String descripcion;

    private String tipoIncidencia;
    private String prioridad;
    private boolean requiereAtencionCampo = true;

    @ManyToOne
    private UsuarioServicio usuario;

    @ManyToOne
    private Suministro suministro;

    @ManyToOne
    private EstadoReclamo estado;

    @OneToMany(mappedBy = "reclamo", cascade = CascadeType.ALL)
    private List<Incidencia> incidencias = new ArrayList<>();

    @OneToMany(mappedBy = "reclamo", cascade = CascadeType.ALL)
    private List<OrdenTrabajo> ordenesTrabajo = new ArrayList<>();

    @OneToMany(mappedBy = "reclamo", cascade = CascadeType.ALL)
    private List<HistorialEstado> historial = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipoIncidencia() {
        return tipoIncidencia;
    }

    public void setTipoIncidencia(String tipoIncidencia) {
        this.tipoIncidencia = tipoIncidencia;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }

    public boolean isRequiereAtencionCampo() {
        return requiereAtencionCampo;
    }

    public void setRequiereAtencionCampo(boolean requiereAtencionCampo) {
        this.requiereAtencionCampo = requiereAtencionCampo;
    }

    public UsuarioServicio getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioServicio usuario) {
        this.usuario = usuario;
    }

    public Suministro getSuministro() {
        return suministro;
    }

    public void setSuministro(Suministro suministro) {
        this.suministro = suministro;
    }

    public EstadoReclamo getEstado() {
        return estado;
    }

    public void setEstado(EstadoReclamo estado) {
        this.estado = estado;
    }

    public List<Incidencia> getIncidencias() {
        return incidencias;
    }

    public void setIncidencias(List<Incidencia> incidencias) {
        this.incidencias = incidencias;
    }

    public List<OrdenTrabajo> getOrdenesTrabajo() {
        return ordenesTrabajo;
    }

    public void setOrdenesTrabajo(List<OrdenTrabajo> ordenesTrabajo) {
        this.ordenesTrabajo = ordenesTrabajo;
    }

    public List<HistorialEstado> getHistorial() {
        return historial;
    }

    public void setHistorial(List<HistorialEstado> historial) {
        this.historial = historial;
    }
}
