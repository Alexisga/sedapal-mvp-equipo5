package com.utp.sedapal.repository;

import com.utp.sedapal.models.OrdenTrabajo;
import com.utp.sedapal.models.EstadoOrden;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdenTrabajoRepository extends JpaRepository<OrdenTrabajo, Long> {

    List<OrdenTrabajo> findByReclamoIdOrderByFechaAsignacionDesc(Long reclamoId);

    List<OrdenTrabajo> findByEstadoOrderByFechaCreacionAsc(EstadoOrden estado);

    List<OrdenTrabajo> findByAsignacion_Tecnico_IdOrderByFechaCreacionDesc(Long tecnicoId);

    List<OrdenTrabajo> findByReclamoIdIn(Collection<Long> reclamoIds);

    boolean existsByReclamoIdAndEstadoIn(Long reclamoId, List<EstadoOrden> estados);
}
