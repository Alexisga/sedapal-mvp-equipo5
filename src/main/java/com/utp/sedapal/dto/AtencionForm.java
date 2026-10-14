package com.utp.sedapal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public class AtencionForm {
    @NotBlank(message = "Describa las acciones realizadas.")
    @Size(min = 10, max = 1500, message = "Las acciones deben tener entre 10 y 1500 caracteres.")
    private String accionesRealizadas;

    @NotBlank(message = "Ingrese el resultado de la atención.")
    @Size(min = 10, max = 1500, message = "El resultado debe tener entre 10 y 1500 caracteres.")
    private String resultado;

    @Size(max = 1000, message = "Las observaciones no deben superar 1000 caracteres.")
    private String observaciones;

    @Size(max = 250, message = "La descripción de la evidencia es demasiado extensa.")
    private String descripcionEvidencia;

    private MultipartFile evidencia;

    public String getAccionesRealizadas() { return accionesRealizadas; }
    public void setAccionesRealizadas(String accionesRealizadas) { this.accionesRealizadas = accionesRealizadas; }
    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public String getDescripcionEvidencia() { return descripcionEvidencia; }
    public void setDescripcionEvidencia(String descripcionEvidencia) { this.descripcionEvidencia = descripcionEvidencia; }
    public MultipartFile getEvidencia() { return evidencia; }
    public void setEvidencia(MultipartFile evidencia) { this.evidencia = evidencia; }
}
