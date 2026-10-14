package com.utp.sedapal.repository;

import com.utp.sedapal.models.EstadoReclamo;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstadoReclamoRepository extends JpaRepository<EstadoReclamo, Long> {

    Optional<EstadoReclamo> findByNombre(String nombre);

    List<EstadoReclamo> findAllByOrderByOrdenAsc();
}
