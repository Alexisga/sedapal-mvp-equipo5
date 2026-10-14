package com.utp.sedapal.services;
import com.utp.sedapal.dto.RegistroForm;
import com.utp.sedapal.models.*;
import com.utp.sedapal.repository.UsuarioServicioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
@Service
public class RegistroService {
 private final UsuarioServicioRepository usuarios; private final PasswordEncoder encoder;
 public RegistroService(UsuarioServicioRepository u, PasswordEncoder e){usuarios=u;encoder=e;}
 @Transactional public void registrar(RegistroForm f){
  if(usuarios.findByDocumento(f.getDocumento()).isPresent()) throw new IllegalArgumentException("El nombre de usuario ya está registrado.");
  UsuarioServicio u=new UsuarioServicio(); u.setNombre(f.getNombre().trim());u.setApellido("");
  u.setDocumento(f.getDocumento());u.setPassword(encoder.encode(f.getPassword()));u.setRol(RolUsuario.USUARIO);
  usuarios.saveAndFlush(u);
 }
}
