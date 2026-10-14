package com.utp.sedapal.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "atenciones_tecnicas")
public class AtencionTecnica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime fechaRegistro;
    @Column(length = 1500, nullable = false)
    private String accionesRealizadas;
    @Column(length = 1500, nullable = false)
    private String resultado;
    @Column(length = 1000)
    private String observaciones;

    @OneToOne
    private OrdenTrabajo orden;

    @OneToMany(mappedBy = "atencion", cascade = CascadeType.ALL)
    private List<Evidencia> evidencias = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public String getAccionesRealizadas() { return accionesRealizadas; }
    public void setAccionesRealizadas(String accionesRealizadas) { this.accionesRealizadas = accionesRealizadas; }
    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public OrdenTrabajo getOrden() { return orden; }
    public void setOrden(OrdenTrabajo orden) { this.orden = orden; }
    public List<Evidencia> getEvidencias() { return evidencias; }
    public void setEvidencias(List<Evidencia> evidencias) { this.evidencias = evidencias; }
}
