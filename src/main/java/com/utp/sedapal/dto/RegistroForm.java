package com.utp.sedapal.dto;
import jakarta.validation.constraints.*;
public class RegistroForm {
 @NotBlank(message="Ingrese su nombre.") @Size(max=80) private String nombre;
 @NotBlank(message="Ingrese su nombre de usuario.") @Size(max=255,message="El nombre de usuario admite hasta 255 caracteres.") private String documento;
 @NotBlank(message="Ingrese su contraseña.") @Size(min=8,max=72,message="La contraseña debe tener entre 8 y 72 caracteres.") private String password;
 public String getNombre(){return nombre;} public void setNombre(String v){nombre=v;}
 public String getDocumento(){return documento;} public void setDocumento(String v){documento=v==null?null:v.trim();}
 public String getPassword(){return password;} public void setPassword(String v){password=v;}
 @AssertTrue(message="La contraseña no puede exceder 72 bytes UTF-8.")
 public boolean isPasswordBytesValid(){return password==null || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length<=72;}
}
