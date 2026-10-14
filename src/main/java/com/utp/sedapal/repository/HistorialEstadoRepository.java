package com.utp.sedapal.repository;

import com.utp.sedapal.models.HistorialEstado;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Long> {
    List<HistorialEstado> findByReclamoIdOrderByFechaAsc(Long reclamoId);
}
