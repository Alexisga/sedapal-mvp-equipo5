package com.utp.sedapal.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "historial_estados")
public class HistorialEstado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime fecha;
    private String responsable;
    @Column(length = 1000)
    private String observacion;

    @ManyToOne
    private Reclamo reclamo;

    @ManyToOne
    private EstadoReclamo estado;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public Reclamo getReclamo() { return reclamo; }
    public void setReclamo(Reclamo reclamo) { this.reclamo = reclamo; }
    public EstadoReclamo getEstado() { return estado; }
    public void setEstado(EstadoReclamo estado) { this.estado = estado; }
}
