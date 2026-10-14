package com.utp.sedapal.controllers;

import com.utp.sedapal.dto.ClasificacionForm;
import com.utp.sedapal.services.ReclamoService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class EvaluadorController {
    private final ReclamoService reclamoService;
    public EvaluadorController(ReclamoService reclamoService) { this.reclamoService = reclamoService; }

    @GetMapping("/evaluador")
    public String panel(Model model) {
        model.addAttribute("reclamos", reclamoService.listarParaClasificar());
        model.addAttribute("clasificacionForm", new ClasificacionForm());
        return "evaluador/clasificar";
    }

    @PostMapping("/evaluador/reclamos/{id}/clasificar")
    public String clasificar(@PathVariable Long id, @Valid @ModelAttribute ClasificacionForm form,
                             BindingResult result, Authentication auth, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            redirect.addFlashAttribute("error", result.getAllErrors().getFirst().getDefaultMessage());
            return "redirect:/evaluador";
        }
        try {
            reclamoService.clasificar(id, form.getTipoIncidencia(), form.getPrioridad(),
                    form.isRequiereAtencionCampo(), form.getObservacion(), auth.getName());
            redirect.addFlashAttribute("mensaje", "Reclamo clasificado y usuario notificado.");
        } catch (IllegalArgumentException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/evaluador";
    }
}
