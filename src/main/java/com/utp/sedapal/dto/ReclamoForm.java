package com.utp.sedapal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ReclamoForm {
    private Long usuarioId;
    @Size(max=200) private String direccion;
    @Size(max=80) private String distrito;
    public String getDireccion(){return direccion;} public void setDireccion(String v){direccion=v;}
    public String getDistrito(){return distrito;} public void setDistrito(String v){distrito=v;}


    private String codigoSuministro;

    @NotBlank(message = "Seleccione el tipo de incidencia.")
    private String tipoIncidencia;

    @NotBlank(message = "Seleccione la prioridad.")
    @Pattern(regexp = "Alta|Media|Baja", message = "La prioridad no es válida.")
    private String prioridad;

    @NotBlank(message = "Ingrese una descripción.")
    @Size(min = 15, max = 1000, message = "La descripción debe tener entre 15 y 1000 caracteres.")
    private String descripcion;

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getCodigoSuministro() { return codigoSuministro; }
    public void setCodigoSuministro(String codigoSuministro) { this.codigoSuministro = codigoSuministro; }
    public String getTipoIncidencia() { return tipoIncidencia; }
    public void setTipoIncidencia(String tipoIncidencia) { this.tipoIncidencia = tipoIncidencia; }
    public String getPrioridad() { return prioridad; }
    public void setPrioridad(String prioridad) { this.prioridad = prioridad; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
