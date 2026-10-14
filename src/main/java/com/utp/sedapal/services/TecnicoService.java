package com.utp.sedapal.services;

import com.utp.sedapal.models.OrdenTrabajo;
import com.utp.sedapal.models.Tecnico;
import com.utp.sedapal.repository.OrdenTrabajoRepository;
import com.utp.sedapal.repository.TecnicoRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class TecnicoService {

    private final TecnicoRepository tecnicoRepository;
    private final OrdenTrabajoRepository ordenTrabajoRepository;

    public TecnicoService(TecnicoRepository tecnicoRepository, OrdenTrabajoRepository ordenTrabajoRepository) {
        this.tecnicoRepository = tecnicoRepository;
        this.ordenTrabajoRepository = ordenTrabajoRepository;
    }

    public List<Tecnico> listarTecnicosDisponibles() {
        return tecnicoRepository.findByDisponibleTrue();
    }

    public Optional<Tecnico> buscarPorUsuario(Long usuarioId) {
        return tecnicoRepository.findByUsuarioId(usuarioId);
    }

    public List<OrdenTrabajo> listarOrdenesDeTecnico(Long tecnicoId) {
        return ordenTrabajoRepository.findByAsignacion_Tecnico_IdOrderByFechaCreacionDesc(tecnicoId);
    }
}
