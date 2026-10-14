package com.utp.sedapal.repository;

import com.utp.sedapal.models.Zona;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZonaRepository extends JpaRepository<Zona, Long> {
    Optional<Zona> findByNombre(String nombre);
}
