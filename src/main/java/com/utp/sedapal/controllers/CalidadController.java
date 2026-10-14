package com.utp.sedapal.controllers;

import com.utp.sedapal.repository.EstadoReclamoRepository;
import com.utp.sedapal.repository.ZonaRepository;
import com.utp.sedapal.services.ReporteService;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CalidadController {
    private final ReporteService reporteService;
    private final ZonaRepository zonaRepository;
    private final EstadoReclamoRepository estadoRepository;
    public CalidadController(ReporteService reporteService, ZonaRepository zonaRepository,
                             EstadoReclamoRepository estadoRepository) {
        this.reporteService = reporteService; this.zonaRepository = zonaRepository; this.estadoRepository = estadoRepository;
    }

    @GetMapping("/calidad/reportes")
    public String reportes(@RequestParam(required = false) String zona,
                           @RequestParam(required = false) String estado,
                           @RequestParam(required = false) String prioridad,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                           Model model) {
        var reclamos = reporteService.filtrar(zona, estado, prioridad, desde, hasta);
        model.addAttribute("reclamos", reclamos);
        model.addAttribute("resumen", reporteService.resumir(reclamos));
        model.addAttribute("zonas", zonaRepository.findAll());
        model.addAttribute("estados", estadoRepository.findAllByOrderByOrdenAsc());
        model.addAttribute("zonaSeleccionada", zona); model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("prioridadSeleccionada", prioridad); model.addAttribute("desde", desde); model.addAttribute("hasta", hasta);
        return "calidad/reportes";
    }

    @GetMapping("/calidad/reportes.csv")
    public ResponseEntity<byte[]> csv(@RequestParam(required = false) String zona,
                                      @RequestParam(required = false) String estado,
                                      @RequestParam(required = false) String prioridad) {
        byte[] contenido = reporteService.generarCsv(reporteService.filtrar(zona, estado, prioridad, null, null))
                .getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte-reclamos.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(contenido);
    }
}
