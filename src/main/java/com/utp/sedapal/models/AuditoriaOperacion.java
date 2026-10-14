package com.utp.sedapal.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_operaciones")
public class AuditoriaOperacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime fecha;
    private String usuarioDocumento;
    private String accion;
    private String entidad;
    private String entidadId;
    @Column(length = 1500)
    private String detalle;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public String getUsuarioDocumento() { return usuarioDocumento; }
    public void setUsuarioDocumento(String usuarioDocumento) { this.usuarioDocumento = usuarioDocumento; }
    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }
    public String getEntidad() { return entidad; }
    public void setEntidad(String entidad) { this.entidad = entidad; }
    public String getEntidadId() { return entidadId; }
    public void setEntidadId(String entidadId) { this.entidadId = entidadId; }
    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
}
