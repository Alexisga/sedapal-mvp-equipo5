package com.utp.sedapal.services;

import com.utp.sedapal.models.Reclamo;
import com.utp.sedapal.repository.OrdenTrabajoRepository;
import com.utp.sedapal.repository.ReclamoRepository;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReporteService {
    private final ReclamoRepository reclamoRepository;
    private final OrdenTrabajoRepository ordenTrabajoRepository;

    public ReporteService(ReclamoRepository reclamoRepository,
                          OrdenTrabajoRepository ordenTrabajoRepository) {
        this.reclamoRepository = reclamoRepository;
        this.ordenTrabajoRepository = ordenTrabajoRepository;
    }

    @Transactional(readOnly = true)
    public List<Reclamo> filtrar(String zona, String estado, String prioridad, LocalDate desde, LocalDate hasta) {
        return reclamoRepository.findAllByOrderByFechaRegistroDesc().stream()
                .filter(r -> zona == null || zona.isBlank() ||
                        (r.getSuministro().getZona() != null && zona.equals(r.getSuministro().getZona().getNombre())))
                .filter(r -> estado == null || estado.isBlank() || estado.equals(r.getEstado().getNombre()))
                .filter(r -> prioridad == null || prioridad.isBlank() || prioridad.equals(r.getPrioridad()))
                .filter(r -> desde == null || !r.getFechaRegistro().toLocalDate().isBefore(desde))
                .filter(r -> hasta == null || !r.getFechaRegistro().toLocalDate().isAfter(hasta))
                .toList();
    }

    @Transactional(readOnly = true)
    public ResumenReporte resumir(List<Reclamo> reclamos) {
        long cerrados = reclamos.stream().filter(r -> "Cerrado".equals(r.getEstado().getNombre())).count();
        long atendidos = reclamos.stream().filter(r -> "Atendido".equals(r.getEstado().getNombre())).count();
        long enProceso = reclamos.size() - cerrados - atendidos;
        var reclamoIds = reclamos.stream().map(Reclamo::getId).toList();
        double promedioHoras = (reclamoIds.isEmpty() ? java.util.stream.Stream
                .<com.utp.sedapal.models.OrdenTrabajo>empty() : ordenTrabajoRepository.findByReclamoIdIn(reclamoIds).stream())
                .filter(o -> o.getFechaCierre() != null && o.getFechaCreacion() != null)
                .mapToLong(o -> Math.max(0, Duration.between(o.getFechaCreacion(), o.getFechaCierre()).toMinutes()))
                .mapToDouble(minutos -> minutos / 60.0)
                .average().orElse(0);
        return new ResumenReporte(reclamos.size(), enProceso, atendidos, cerrados, promedioHoras);
    }

    public String generarCsv(List<Reclamo> reclamos) {
        StringBuilder csv = new StringBuilder("Código,Fecha,Usuario,Suministro,Zona,Tipo,Prioridad,Estado\n");
        for (Reclamo r : reclamos) {
            csv.append(esc(r.getCodigo())).append(',')
                    .append(r.getFechaRegistro()).append(',')
                    .append(esc(r.getUsuario().getNombre() + " " + r.getUsuario().getApellido())).append(',')
                    .append(esc(r.getSuministro().getCodigoSuministro())).append(',')
                    .append(esc(r.getSuministro().getZona() == null ? "" : r.getSuministro().getZona().getNombre())).append(',')
                    .append(esc(r.getTipoIncidencia())).append(',')
                    .append(esc(r.getPrioridad())).append(',')
                    .append(esc(r.getEstado().getNombre())).append('\n');
        }
        return csv.toString();
    }

    private String esc(String valor) {
        return '"' + (valor == null ? "" : valor.replace("\"", "\"\"")) + '"';
    }

    public record ResumenReporte(long total, long enProceso, long atendidos, long cerrados,
                                 double promedioHorasAtencion) { }
}
