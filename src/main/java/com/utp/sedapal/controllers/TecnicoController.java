package com.utp.sedapal.controllers;

import com.utp.sedapal.dto.AtencionForm;
import com.utp.sedapal.models.Tecnico;
import com.utp.sedapal.models.UsuarioServicio;
import com.utp.sedapal.services.AuthService;
import com.utp.sedapal.services.ReclamoService;
import com.utp.sedapal.services.TecnicoService;
import jakarta.validation.Valid;
import java.util.List;
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
public class TecnicoController {
    private final TecnicoService tecnicoService;
    private final ReclamoService reclamoService;
    private final AuthService authService;
    public TecnicoController(TecnicoService tecnicoService, ReclamoService reclamoService, AuthService authService) {
        this.tecnicoService = tecnicoService; this.reclamoService = reclamoService; this.authService = authService;
    }

    @GetMapping("/tecnico")
    public String panel(Authentication auth, Model model) {
        UsuarioServicio usuario = authService.requerirPorDocumento(auth.getName());
        Tecnico tecnico = tecnicoService.buscarPorUsuario(usuario.getId()).orElse(null);
        model.addAttribute("tecnico", tecnico);
        model.addAttribute("ordenes", tecnico == null ? List.of() : tecnicoService.listarOrdenesDeTecnico(tecnico.getId()));
        model.addAttribute("atencionForm", new AtencionForm());
        if (tecnico == null) model.addAttribute("error", "No existe un técnico asociado al usuario autenticado.");
        return "tecnico/atencion";
    }

    @PostMapping("/tecnico/ordenes/{id}/iniciar")
    public String iniciar(@PathVariable Long id, Authentication auth, RedirectAttributes redirect) {
        UsuarioServicio usuario = authService.requerirPorDocumento(auth.getName());
        try {
            reclamoService.iniciarAtencion(id, usuario.getId(), auth.getName());
            redirect.addFlashAttribute("mensaje", "Atención iniciada y estado actualizado.");
        } catch (IllegalArgumentException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/tecnico";
    }

    @PostMapping("/tecnico/ordenes/{id}/resolver")
    public String resolver(@PathVariable Long id, @Valid @ModelAttribute AtencionForm form, BindingResult result,
                           Authentication auth, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            redirect.addFlashAttribute("error", result.getAllErrors().getFirst().getDefaultMessage());
            return "redirect:/tecnico";
        }
        UsuarioServicio usuario = authService.requerirPorDocumento(auth.getName());
        try {
            reclamoService.registrarAtencion(id, usuario.getId(), form, auth.getName());
            redirect.addFlashAttribute("mensaje", "Atención y evidencia registradas correctamente.");
        } catch (RuntimeException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/tecnico";
    }
}
