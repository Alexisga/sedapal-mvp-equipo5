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
@Table(name = "notificaciones")
public class Notificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 1000, nullable = false)
    private String mensaje;
    private String tipo;
    private LocalDateTime fecha;
    private boolean leida;

    @ManyToOne
    private UsuarioServicio usuario;

    @ManyToOne
    private Reclamo reclamo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public boolean isLeida() { return leida; }
    public void setLeida(boolean leida) { this.leida = leida; }
    public UsuarioServicio getUsuario() { return usuario; }
    public void setUsuario(UsuarioServicio usuario) { this.usuario = usuario; }
    public Reclamo getReclamo() { return reclamo; }
    public void setReclamo(Reclamo reclamo) { this.reclamo = reclamo; }
}
