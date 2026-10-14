package com.utp.sedapal.repository;

import com.utp.sedapal.models.Reclamo;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReclamoRepository extends JpaRepository<Reclamo, Long> {

    Optional<Reclamo> findByCodigo(String codigo);

    List<Reclamo> findByUsuarioIdOrderByFechaRegistroDesc(Long usuarioId);

    List<Reclamo> findByEstado_NombreInOrderByFechaRegistroAsc(Collection<String> nombres);

    List<Reclamo> findByEstado_NombreOrderByFechaRegistroAsc(String nombre);

    List<Reclamo> findAllByOrderByFechaRegistroDesc();

    long countByEstado_Nombre(String nombre);
}
