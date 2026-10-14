package com.utp.sedapal.repository;

import com.utp.sedapal.models.Incidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IncidenciaRepository extends JpaRepository<Incidencia, Long> {
}
