package com.utp.sedapal.services;

import com.utp.sedapal.dto.UsuarioForm;
import com.utp.sedapal.models.RolUsuario;
import com.utp.sedapal.models.UsuarioServicio;
import com.utp.sedapal.repository.UsuarioServicioRepository;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {
    private final UsuarioServicioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    public AdminService(UsuarioServicioRepository repository, PasswordEncoder passwordEncoder,
                        AuditoriaService auditoriaService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaService = auditoriaService;
    }

    public List<UsuarioServicio> listarUsuarios() {
        return repository.findAll();
    }

    @Transactional
    public UsuarioServicio crear(UsuarioForm form, String responsable) {
        if (repository.findByDocumento(form.getDocumento()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con ese documento.");
        }
        UsuarioServicio usuario = new UsuarioServicio();
        usuario.setNombre(form.getNombre().trim());
        usuario.setApellido(form.getApellido().trim());
        usuario.setDocumento(form.getDocumento().trim());
        usuario.setPassword(passwordEncoder.encode(form.getPassword()));
        usuario.setRol(form.getRol());
        usuario.setCorreo(form.getCorreo());
        usuario.setTelefono(form.getTelefono());
        usuario.setActivo(true);
        usuario = repository.save(usuario);
        auditoriaService.registrar(responsable, "CREAR_USUARIO", "UsuarioServicio", usuario.getId(),
                "Rol asignado: " + usuario.getRol());
        return usuario;
    }

    @Transactional
    public void actualizarRolYEstado(Long id, RolUsuario rol, boolean activo, String responsable) {
        UsuarioServicio usuario = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        usuario.setRol(rol);
        usuario.setActivo(activo);
        repository.save(usuario);
        auditoriaService.registrar(responsable, "ACTUALIZAR_USUARIO", "UsuarioServicio", id,
                "Rol=" + rol + ", activo=" + activo);
    }
}
