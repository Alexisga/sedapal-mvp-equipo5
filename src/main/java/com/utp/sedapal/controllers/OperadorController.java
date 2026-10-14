package com.utp.sedapal.controllers;

import com.utp.sedapal.dto.ReclamoForm;
import com.utp.sedapal.models.Reclamo;
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
public class OperadorController {
    private final ReclamoService reclamoService;

    public OperadorController(ReclamoService reclamoService) {
        this.reclamoService = reclamoService;
    }

    @GetMapping("/operador")
    public String panel(Model model) {
        model.addAttribute("reclamos", reclamoService.listarTodos());
        return "operador/dashboard";
    }

    @GetMapping("/operador/reclamos/nuevo")
    public String nuevo(Model model) {
        if (!model.containsAttribute("reclamoForm")) model.addAttribute("reclamoForm", new ReclamoForm());
        cargarCatalogos(model);
        return "operador/registrar_reclamo";
    }

    @PostMapping("/operador/reclamos")
    public String registrar(@Valid @ModelAttribute ReclamoForm reclamoForm, BindingResult result,
                            Authentication authentication, Model model, RedirectAttributes redirect) {
        if (reclamoForm.getUsuarioId() == null) result.rejectValue("usuarioId", "required", "Seleccione un usuario.");
        if (result.hasErrors()) {
            cargarCatalogos(model);
            return "operador/registrar_reclamo";
        }
        try {
            Reclamo reclamo = reclamoService.registrarReclamo(reclamoForm.getUsuarioId(),
                    reclamoForm.getCodigoSuministro(), reclamoForm.getTipoIncidencia(), reclamoForm.getPrioridad(),
                    reclamoForm.getDescripcion(), authentication.getName());
            redirect.addFlashAttribute("mensaje", "Reclamo " + reclamo.getCodigo() + " registrado correctamente.");
            return "redirect:/operador";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            cargarCatalogos(model);
            return "operador/registrar_reclamo";
        }
    }

    @PostMapping("/operador/reclamos/{id}/cerrar")
    public String cerrar(@PathVariable Long id, Authentication authentication, RedirectAttributes redirect) {
        try {
            reclamoService.cerrar(id, authentication.getName());
            redirect.addFlashAttribute("mensaje", "Reclamo cerrado y resultado comunicado.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/operador";
    }

    private void cargarCatalogos(Model model) {
        model.addAttribute("usuarios", reclamoService.listarUsuariosServicio());
        model.addAttribute("suministros", reclamoService.listarSuministros());
    }
}
