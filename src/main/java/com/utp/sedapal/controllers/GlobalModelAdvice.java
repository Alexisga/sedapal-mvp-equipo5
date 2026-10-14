package com.utp.sedapal.controllers;

import com.utp.sedapal.models.UsuarioServicio;
import com.utp.sedapal.services.AuthService;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {
    private final AuthService authService;

    public GlobalModelAdvice(AuthService authService) {
        this.authService = authService;
    }

    @ModelAttribute
    public void agregarUsuario(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return;
        }
        UsuarioServicio usuario = authService.buscarPorDocumento(authentication.getName()).orElse(null);
        if (usuario != null) {
            model.addAttribute("usuarioActual", usuario);
            model.addAttribute("nombreUsuario", usuario.getNombre() + " " + usuario.getApellido());
            model.addAttribute("rolUsuario", usuario.getRol().getEtiqueta());
        }
    }
}
