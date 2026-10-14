package com.utp.sedapal.repository;

import com.utp.sedapal.models.Tecnico;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TecnicoRepository extends JpaRepository<Tecnico, Long> {

    List<Tecnico> findByDisponibleTrue();

    Optional<Tecnico> findByUsuarioId(Long usuarioId);
}
