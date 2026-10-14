package com.utp.sedapal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ClasificacionForm {
    @NotBlank(message = "Seleccione el tipo de incidencia.")
    private String tipoIncidencia;

    @NotBlank(message = "Seleccione la prioridad.")
    @Pattern(regexp = "Alta|Media|Baja", message = "La prioridad no es válida.")
    private String prioridad;

    private boolean requiereAtencionCampo = true;

    @NotBlank(message = "Ingrese la evaluación realizada.")
    @Size(min = 10, max = 1000, message = "La evaluación debe tener entre 10 y 1000 caracteres.")
    private String observacion;

    public String getTipoIncidencia() { return tipoIncidencia; }
    public void setTipoIncidencia(String tipoIncidencia) { this.tipoIncidencia = tipoIncidencia; }
    public String getPrioridad() { return prioridad; }
    public void setPrioridad(String prioridad) { this.prioridad = prioridad; }
    public boolean isRequiereAtencionCampo() { return requiereAtencionCampo; }
    public void setRequiereAtencionCampo(boolean requiereAtencionCampo) { this.requiereAtencionCampo = requiereAtencionCampo; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
}
