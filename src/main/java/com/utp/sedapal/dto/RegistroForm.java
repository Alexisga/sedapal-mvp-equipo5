package com.utp.sedapal.dto;
import jakarta.validation.constraints.*;
public class RegistroForm {
 @NotBlank @Size(max=80) private String nombre;
 @NotBlank @Pattern(regexp="[a-zA-Z0-9]{4,30}", message="Use entre 4 y 30 letras o números.") private String documento;
 @NotBlank @Size(min=8,max=72,message="La contraseña debe tener entre 8 y 72 caracteres.") private String password;
 public String getNombre(){return nombre;} public void setNombre(String v){nombre=v;}
 public String getDocumento(){return documento;} public void setDocumento(String v){documento=v;}
 public String getPassword(){return password;} public void setPassword(String v){password=v;}
 @AssertTrue(message="La contraseña no puede exceder 72 bytes UTF-8.")
 public boolean isPasswordBytesValid(){return password==null || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length<=72;}
}
