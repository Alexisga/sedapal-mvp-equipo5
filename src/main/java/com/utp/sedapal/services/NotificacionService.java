package com.utp.sedapal.services;

import com.utp.sedapal.models.Notificacion;
import com.utp.sedapal.models.Reclamo;
import com.utp.sedapal.models.UsuarioServicio;
import com.utp.sedapal.repository.NotificacionRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificacionService {
    private final NotificacionRepository repository;

    public NotificacionService(NotificacionRepository repository) {
        this.repository = repository;
    }

    public void crear(UsuarioServicio usuario, Reclamo reclamo, String tipo, String mensaje) {
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(usuario);
        notificacion.setReclamo(reclamo);
        notificacion.setTipo(tipo);
        notificacion.setMensaje(mensaje);
        notificacion.setFecha(LocalDateTime.now());
        notificacion.setLeida(false);
        repository.save(notificacion);
    }

    public List<Notificacion> listar(Long usuarioId) {
        return repository.findTop10ByUsuarioIdOrderByFechaDesc(usuarioId);
    }

    public long contarNoLeidas(Long usuarioId) {
        return repository.countByUsuarioIdAndLeidaFalse(usuarioId);
    }

    @Transactional
    public void marcarLeida(Long id, Long usuarioId) {
        repository.findById(id).filter(n -> n.getUsuario().getId().equals(usuarioId)).ifPresent(n -> {
            n.setLeida(true);
            repository.save(n);
        });
    }
}
