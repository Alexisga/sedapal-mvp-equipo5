package com.utp.sedapal.services;

import com.utp.sedapal.models.UsuarioServicio;
import com.utp.sedapal.repository.UsuarioServicioRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements UserDetailsService {

    private final UsuarioServicioRepository usuarioRepository;

    public AuthService(UsuarioServicioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String documento) throws UsernameNotFoundException {
        UsuarioServicio usuario = usuarioRepository.findByDocumento(documento)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado."));
        return User.withUsername(usuario.getDocumento())
                .password(usuario.getPassword())
                .roles(usuario.getRol().name())
                .disabled(!usuario.isActivo())
                .build();
    }

    public Optional<UsuarioServicio> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Optional<UsuarioServicio> buscarPorDocumento(String documento) {
        return usuarioRepository.findByDocumento(documento);
    }

    public UsuarioServicio requerirPorDocumento(String documento) {
        return buscarPorDocumento(documento)
                .orElseThrow(() -> new IllegalArgumentException("Usuario autenticado no encontrado."));
    }

    public List<UsuarioServicio> listarUsuarios() {
        return usuarioRepository.findAll();
    }
}
