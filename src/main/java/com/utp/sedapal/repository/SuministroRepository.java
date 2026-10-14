package com.utp.sedapal.repository;

import com.utp.sedapal.models.Suministro;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuministroRepository extends JpaRepository<Suministro, Long> {

    Optional<Suministro> findByCodigoSuministro(String codigoSuministro);

    Optional<Suministro> findByCodigoSuministroAndUsuarioId(String codigoSuministro, Long usuarioId);

    List<Suministro> findByUsuarioId(Long usuarioId);
}
