package com.utp.sedapal.repository;

import com.utp.sedapal.models.AuditoriaOperacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaOperacionRepository extends JpaRepository<AuditoriaOperacion, Long> {
    List<AuditoriaOperacion> findTop100ByOrderByFechaDesc();
}
