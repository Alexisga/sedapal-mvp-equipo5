package com.utp.sedapal.repository;

import com.utp.sedapal.models.AsignacionTecnica;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AsignacionTecnicaRepository extends JpaRepository<AsignacionTecnica, Long> {
    Optional<AsignacionTecnica> findByOrdenId(Long ordenId);
}
