package com.utp.sedapal.controllers;

import com.utp.sedapal.services.ReclamoService;
import com.utp.sedapal.services.TecnicoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SupervisorController {
    private final ReclamoService reclamoService;
    private final TecnicoService tecnicoService;
    public SupervisorController(ReclamoService reclamoService, TecnicoService tecnicoService) {
        this.reclamoService = reclamoService; this.tecnicoService = tecnicoService;
    }

    @GetMapping("/supervisor")
    public String panel(Model model) {
        model.addAttribute("reclamosClasificados", reclamoService.listarClasificados());
        model.addAttribute("ordenesPendientes", reclamoService.listarOrdenesPendientes());
        model.addAttribute("tecnicos", tecnicoService.listarTecnicosDisponibles());
        return "supervisor/ordenes";
    }

    @PostMapping("/supervisor/reclamos/{id}/ordenes")
    public String generar(@PathVariable Long id, Authentication auth, RedirectAttributes redirect) {
        try {
            var orden = reclamoService.generarOrden(id, auth.getName());
            redirect.addFlashAttribute("mensaje", "Orden " + orden.getCodigo() + " generada.");
        } catch (IllegalArgumentException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/supervisor";
    }

    @PostMapping("/supervisor/ordenes/{id}/asignar")
    public String asignar(@PathVariable Long id, @RequestParam Long tecnicoId, Authentication auth,
                          RedirectAttributes redirect) {
        try {
            reclamoService.asignarTecnico(id, tecnicoId, auth.getName());
            redirect.addFlashAttribute("mensaje", "Técnico asignado y usuario notificado.");
        } catch (IllegalArgumentException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/supervisor";
    }
}
