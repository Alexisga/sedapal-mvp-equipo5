package com.utp.sedapal.dto;

import com.utp.sedapal.models.RolUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UsuarioForm {
    @NotBlank @Size(max = 80)
    private String nombre;
    @NotBlank @Size(max = 100)
    private String apellido;
    @NotBlank @Pattern(regexp = "\\d{8,12}", message = "El documento debe tener entre 8 y 12 dígitos.")
    private String documento;
    @NotBlank @Size(min = 6, max = 72, message = "La contraseña debe tener al menos 6 caracteres.")
    private String password;
    @NotNull(message = "Seleccione un rol.")
    private RolUsuario rol;
    @Email(message = "Ingrese un correo válido.")
    private String correo;
    private String telefono;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public RolUsuario getRol() { return rol; }
    public void setRol(RolUsuario rol) { this.rol = rol; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}
