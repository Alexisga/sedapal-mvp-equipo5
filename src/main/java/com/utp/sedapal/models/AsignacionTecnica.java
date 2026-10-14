package com.utp.sedapal.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "asignaciones_tecnicas")
public class AsignacionTecnica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime fechaAsignacion;
    private boolean activa = true;

    @OneToOne
    private OrdenTrabajo orden;

    @ManyToOne
    private Tecnico tecnico;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getFechaAsignacion() { return fechaAsignacion; }
    public void setFechaAsignacion(LocalDateTime fechaAsignacion) { this.fechaAsignacion = fechaAsignacion; }
    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
    public OrdenTrabajo getOrden() { return orden; }
    public void setOrden(OrdenTrabajo orden) { this.orden = orden; }
    public Tecnico getTecnico() { return tecnico; }
    public void setTecnico(Tecnico tecnico) { this.tecnico = tecnico; }
}
