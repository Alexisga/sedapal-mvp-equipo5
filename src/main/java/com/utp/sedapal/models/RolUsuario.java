package com.utp.sedapal.models;

public enum RolUsuario {
    USUARIO("Usuario del servicio"),
    OPERADOR("Operador de atención"),
    EVALUADOR("Evaluador"),
    SUPERVISOR("Supervisor"),
    TECNICO("Técnico"),
    CALIDAD("Responsable de calidad"),
    ADMINISTRADOR("Administrador");

    private final String etiqueta;

    RolUsuario(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
