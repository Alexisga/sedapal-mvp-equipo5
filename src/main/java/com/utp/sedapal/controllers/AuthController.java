package com.utp.sedapal.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/")
    public String inicio() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(@org.springframework.web.bind.annotation.RequestParam(required = false) Boolean error,
                        @org.springframework.web.bind.annotation.RequestParam(required = false) Boolean logout,
                        org.springframework.ui.Model model) {
        if (Boolean.TRUE.equals(error)) {
            model.addAttribute("error", "Documento o contraseña incorrectos.");
        }
        if (Boolean.TRUE.equals(logout)) {
            model.addAttribute("mensaje", "La sesión se cerró correctamente.");
        }
        return "login";
    }

    @GetMapping("/acceso-denegado")
    public String accesoDenegado() {
        return "error/403";
    }

    @GetMapping("/panel")
    public String panel(org.springframework.security.core.Authentication authentication) {
        String rol = authentication.getAuthorities().iterator().next().getAuthority();
        return switch (rol) {
            case "ROLE_USUARIO" -> "redirect:/usuario";
            case "ROLE_OPERADOR" -> "redirect:/operador";
            case "ROLE_EVALUADOR" -> "redirect:/evaluador";
            case "ROLE_SUPERVISOR" -> "redirect:/supervisor";
            case "ROLE_TECNICO" -> "redirect:/tecnico";
            case "ROLE_CALIDAD" -> "redirect:/calidad/reportes";
            case "ROLE_ADMINISTRADOR" -> "redirect:/admin";
            default -> "redirect:/login";
        };
    }
}
