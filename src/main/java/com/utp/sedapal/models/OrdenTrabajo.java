package com.utp.sedapal.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "ordenes_trabajo")
public class OrdenTrabajo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String codigo;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaAsignacion;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaCierre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoOrden estado;

    @Column(length = 1000)
    private String resultado;

    @ManyToOne
    private Reclamo reclamo;

    @OneToOne(mappedBy = "orden", cascade = CascadeType.ALL)
    private AsignacionTecnica asignacion;

    @OneToOne(mappedBy = "orden", cascade = CascadeType.ALL)
    private AtencionTecnica atencion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaAsignacion() {
        return fechaAsignacion;
    }

    public void setFechaAsignacion(LocalDateTime fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }

    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDateTime fechaCierre) { this.fechaCierre = fechaCierre; }
    public EstadoOrden getEstado() { return estado; }
    public void setEstado(EstadoOrden estado) { this.estado = estado; }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public Reclamo getReclamo() {
        return reclamo;
    }

    public void setReclamo(Reclamo reclamo) {
        this.reclamo = reclamo;
    }

    public AsignacionTecnica getAsignacion() { return asignacion; }
    public void setAsignacion(AsignacionTecnica asignacion) { this.asignacion = asignacion; }
    public AtencionTecnica getAtencion() { return atencion; }
    public void setAtencion(AtencionTecnica atencion) { this.atencion = atencion; }
}
