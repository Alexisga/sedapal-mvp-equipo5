package com.utp.sedapal.repository;

import com.utp.sedapal.models.UsuarioServicio;
import com.utp.sedapal.models.RolUsuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioServicioRepository extends JpaRepository<UsuarioServicio, Long> {

    Optional<UsuarioServicio> findByDocumento(String documento);

    List<UsuarioServicio> findByRol(RolUsuario rol);
}
