package com.utp.sedapal.models;

public enum EstadoOrden {
    PENDIENTE_ASIGNACION("Pendiente de asignación"),
    ASIGNADA("Asignada"),
    EN_ATENCION("En atención"),
    ATENDIDA("Atendida");

    private final String etiqueta;

    EstadoOrden(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
