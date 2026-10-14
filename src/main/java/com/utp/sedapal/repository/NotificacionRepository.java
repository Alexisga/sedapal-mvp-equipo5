package com.utp.sedapal.repository;

import com.utp.sedapal.models.Notificacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findTop10ByUsuarioIdOrderByFechaDesc(Long usuarioId);
    long countByUsuarioIdAndLeidaFalse(Long usuarioId);
}
