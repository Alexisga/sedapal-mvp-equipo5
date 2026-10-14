package com.utp.sedapal.repository;

import com.utp.sedapal.models.Evidencia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvidenciaRepository extends JpaRepository<Evidencia, Long> {
    java.util.List<Evidencia> findByAtencion_Orden_Reclamo_Id(Long id);
    long countByAtencion_Orden_Reclamo_Id(Long id);
}
