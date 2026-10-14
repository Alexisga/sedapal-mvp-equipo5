package com.utp.sedapal.services;

import com.utp.sedapal.models.AuditoriaOperacion;
import com.utp.sedapal.repository.AuditoriaOperacionRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AuditoriaService {
    private final AuditoriaOperacionRepository repository;

    public AuditoriaService(AuditoriaOperacionRepository repository) {
        this.repository = repository;
    }

    public void registrar(String usuario, String accion, String entidad, Object entidadId, String detalle) {
        AuditoriaOperacion auditoria = new AuditoriaOperacion();
        auditoria.setFecha(LocalDateTime.now());
        auditoria.setUsuarioDocumento(usuario == null ? "SISTEMA" : usuario);
        auditoria.setAccion(accion);
        auditoria.setEntidad(entidad);
        auditoria.setEntidadId(entidadId == null ? null : entidadId.toString());
        auditoria.setDetalle(detalle);
        repository.save(auditoria);
    }

    public List<AuditoriaOperacion> listarRecientes() {
        return repository.findTop100ByOrderByFechaDesc();
    }
}
